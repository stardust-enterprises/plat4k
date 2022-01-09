/* Copyright (c) 2017 Matthias Bläsing, All Rights Reserved
 *
 * The contents of this file is dual-licensed under 2
 * alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to
 * the project.
 *
 * You may obtain a copy of the LGPL License at:
 *
 * http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */
package fr.stardustenterprises.plat4k.jna

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

/**
 * Analyse an ELF file for platform specific attributes.
 * JNA source transcribed to Kotlin (@lambdagg).
 * Primary use-case: Detect whether the java binary is arm hardfloat or softfloat.
 */
class ELFAnalyser private constructor(
    /**
     * The name of the parsed file.
     */
    val filename: String
) {
    /**
     * Whether the parsed file was detected to be an ELF file.
     */
    var isELF = false
        private set

    /**
     * Whether the parsed file was detected to be for a 64bit architecture
     * and pointers are expected to be 8-byte wide.
     */
    var is64 = false
        private set

    /**
     * Whether the parsed file is detected to be big endian or little
     * endian.
     */
    var isBigEndian = false
        private set

    /**
     * Whether the parsed file was detected to conform to the hardware
     * floating-point procedure-call standard via ELF flags.
     */
    var isArmHardFloatFlag = false
        private set

    /**
     * Whether the parsed file was detected to conform to the software
     * floating-point procedure-call standard via ELF flags,
     */
    var isArmSoftFloatFlag = false
        private set

    /**
     * Whether the parsed file was detected to specify, that FP
     * parameters/result passing conforms to AAPCS, VFP variant (hardfloat).
     */
    var isArmEabiAapcsVfp = false
        private set

    /**
     * Whether the parsed file was detected to be built for the ARM arch.
     */
    var isArm = false
        private set

    /**
     * Whether the parsed file was detected to not be soft float.
     *
     * @see isArmEabiAapcsVfp
     * @see isArmHardFloatFlag
     */
    val isArmHardFloat: Boolean
        get() = isArmEabiAapcsVfp || isArmHardFloatFlag

    @Throws(IOException::class)
    private fun runDetection() {
        RandomAccessFile(filename, "r").use { random ->
            // run precheck - only of if the file at least hold an ELF header
            // parsing runs further.
            if (random.length() > 4) {
                val magic = ByteArray(4)

                random.seek(0)
                random.read(magic)

                if (magic.contentEquals(ELF_MAGIC)) {
                    isELF = true
                }
            }

            if (!isELF) {
                return
            }

            random.seek(4)

            // The total header size depends on the pointer size of the
            // platform so before the header is loaded the pointer size has to
            // be determined.
            val sizeIndicator = random.readByte()
            val endianessIndicator = random.readByte()

            is64 = sizeIndicator.toInt() == EI_CLASS_64BIT
            isBigEndian = endianessIndicator.toInt() == EI_DATA_BIG_ENDIAN

            random.seek(0)

            // header length
            val headerData = ByteBuffer.allocate(if (is64) 64 else 52)
            random.channel.read(headerData, 0)
            headerData.order(
                if (isBigEndian) ByteOrder.BIG_ENDIAN
                else ByteOrder.LITTLE_ENDIAN
            )

            // e_machine
            isArm = headerData[0x12].toInt() == E_MACHINE_ARM
            if (isArm) {
                // e_flags
                val flags = headerData.getInt(if (is64) 0x30 else 0x24)

                isArmHardFloatFlag =
                    (flags and EF_ARM_ABI_FLOAT_HARD) == EF_ARM_ABI_FLOAT_HARD

                isArmSoftFloatFlag =
                    (flags and EF_ARM_ABI_FLOAT_SOFT) == EF_ARM_ABI_FLOAT_SOFT

                parseEabiAapcsVfp(headerData, random)
            }
        }
    }

    @Throws(IOException::class)
    private fun parseEabiAapcsVfp(
        headerData: ByteBuffer,
        raf: RandomAccessFile
    ) {
        val sectionHeaders = ELFSectionHeaders(is64, isBigEndian, headerData, raf)

        sectionHeaders.entries
            .filter { it.name == ".ARM.attributes" }
            .forEach {

                val armAttributesBuffer = ByteBuffer.allocate(it.size)

                armAttributesBuffer.order(
                    if (isBigEndian) ByteOrder.BIG_ENDIAN
                    else ByteOrder.LITTLE_ENDIAN
                )

                raf.channel.read(armAttributesBuffer, it.offset.toLong())
                armAttributesBuffer.rewind()

                val armAttributes = parseArmAttributes(armAttributesBuffer)
                val fileAttributes = armAttributes[1] ?: return

                isArmEabiAapcsVfp = let {
                    /*
                      Tag_ABI_VFP_args, (=28), uleb128
                       0 The user intended FP parameter/result passing to conform to AAPCS, base variant
                       1 The user intended FP parameter/result passing to conform to AAPCS, VFP variant
                       2 The user intended FP parameter/result passing to conform to tool chain-specific conventions
                       3 Code is compatible with both the base and VFP variants; the non-variadic functions to pass FP
                         parameters/results
                     */
                    var abiVfpTag = fileAttributes[ArmAeabiAttributesTag.ABI_VFP_args]

                    if (abiVfpTag !is Int) {
                        if (abiVfpTag is BigInteger) {
                            abiVfpTag = abiVfpTag.toInt()
                        } else {
                            return@let false
                        }
                    }

                    abiVfpTag == 1
                }
            }
    }

    internal class ELFSectionHeaders(
        is64: Boolean,
        bigEndian: Boolean,
        headerData: ByteBuffer,
        raf: RandomAccessFile
    ) {
        val entries: MutableList<ELFSectionHeaderEntry> =
            mutableListOf()

        init {
            val shoff: Long
            val shentsize: Int
            val shnum: Int
            val shstrndx: Short

            if (is64) {
                shoff = headerData.getLong(0x28)
                shentsize = headerData.getShort(0x3A).toInt()
                shnum = headerData.getShort(0x3C).toInt()
                shstrndx = headerData.getShort(0x3E)
            } else {
                shoff = headerData.getInt(0x20).toLong()
                shentsize = headerData.getShort(0x2E).toInt()
                shnum = headerData.getShort(0x30).toInt()
                shstrndx = headerData.getShort(0x32)
            }

            val tableLength = shnum * shentsize
            val data = ByteBuffer.allocate(tableLength)

            data.order(if (bigEndian) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN)
            raf.channel.read(data, shoff)

            for (i in 0 until shnum) {
                data.position(i * shentsize)
                val header = data.slice()
                header.order(data.order())
                header.limit(shentsize)
                entries.add(ELFSectionHeaderEntry(is64, header))
            }

            val stringTable = entries[shstrndx.toInt()]
            val stringBuffer = ByteBuffer.allocate(stringTable.size)

            stringBuffer.order(
                if (bigEndian)
                    ByteOrder.BIG_ENDIAN
                else
                    ByteOrder.LITTLE_ENDIAN
            )

            raf.channel.read(stringBuffer, stringTable.offset.toLong())
            stringBuffer.rewind()
            val stream = ByteArrayOutputStream(20)

            entries.forEach {
                stream.reset()
                stringBuffer.position(it.nameOffset)

                while (stringBuffer.position() < stringBuffer.limit()) {
                    val i = stringBuffer.get().toInt()

                    if (i == 0) {
                        break
                    } else {
                        stream.write(i)
                    }
                }

                it.name = stream.toString("ASCII")
            }
        }
    }

    internal class ELFSectionHeaderEntry(
        is64: Boolean,
        sectionHeaderData: ByteBuffer
    ) {
        val nameOffset: Int
        var name: String? = null
        val type: Int
        val flags: Int
        val offset: Int
        val size: Int

        override fun toString(): String {
            return "ELFSectionHeaderEntry{nameIdx=$nameOffset, name=$name," +
                "type=$type, flags=$flags, offset=$offset, size=$size}"
        }

        init {
            nameOffset = sectionHeaderData.getInt(0x0)
            type = sectionHeaderData.getInt(0x4)

            flags = (if (is64) sectionHeaderData.getLong(0x8) else sectionHeaderData.getInt(0x8))
                .toInt()

            offset = (if (is64) sectionHeaderData.getLong(0x18) else sectionHeaderData.getInt(0x10))
                .toInt()

            size = (if (is64) sectionHeaderData.getLong(0x20) else sectionHeaderData.getInt(0x14))
                .toInt()
        }
    }

    internal class ArmAeabiAttributesTag(
        val value: Int,
        val name: String,
        val parameterType: ParameterType
    ) {
        enum class ParameterType {
            UINT32, NTBS, ULEB128
        }

        override fun toString(): String = "$name ($value)"

        override fun hashCode(): Int = 67 * 7 + value

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }

            if (other == null || javaClass != other.javaClass) {
                return false
            }

            return value == (other as ArmAeabiAttributesTag).value
        }

        companion object {
            private val tags: MutableList<ArmAeabiAttributesTag> =
                mutableListOf()

            private val valueMap: MutableMap<Int, ArmAeabiAttributesTag> =
                mutableMapOf()

            private val nameMap: MutableMap<String, ArmAeabiAttributesTag> =
                mutableMapOf()

            // Enumerated from ARM IHI 0045E, 2.5 Attributes summary and history
            val File = addTag(1, "File", ParameterType.UINT32)
            val Section = addTag(2, "Section", ParameterType.UINT32)
            val Symbol = addTag(3, "Symbol", ParameterType.UINT32)
            val CPU_raw_name = addTag(4, "CPU_raw_name", ParameterType.NTBS)
            val CPU_name = addTag(5, "CPU_name", ParameterType.NTBS)
            val CPU_arch = addTag(6, "CPU_arch", ParameterType.ULEB128)
            val CPU_arch_profile = addTag(7, "CPU_arch_profile", ParameterType.ULEB128)
            val ARM_ISA_use = addTag(8, "ARM_ISA_use", ParameterType.ULEB128)
            val THUMB_ISA_use = addTag(9, "THUMB_ISA_use", ParameterType.ULEB128)
            val FP_arch = addTag(10, "FP_arch", ParameterType.ULEB128)
            val WMMX_arch = addTag(11, "WMMX_arch", ParameterType.ULEB128)
            val Advanced_SIMD_arch = addTag(12, "Advanced_SIMD_arch", ParameterType.ULEB128)
            val PCS_config = addTag(13, "PCS_config", ParameterType.ULEB128)
            val ABI_PCS_R9_use = addTag(14, "ABI_PCS_R9_use", ParameterType.ULEB128)
            val ABI_PCS_RW_data = addTag(15, "ABI_PCS_RW_data", ParameterType.ULEB128)
            val ABI_PCS_RO_data = addTag(16, "ABI_PCS_RO_data", ParameterType.ULEB128)
            val ABI_PCS_GOT_use = addTag(17, "ABI_PCS_GOT_use", ParameterType.ULEB128)
            val ABI_PCS_wchar_t = addTag(18, "ABI_PCS_wchar_t", ParameterType.ULEB128)
            val ABI_FP_rounding = addTag(19, "ABI_FP_rounding", ParameterType.ULEB128)
            val ABI_FP_denormal = addTag(20, "ABI_FP_denormal", ParameterType.ULEB128)
            val ABI_FP_exceptions = addTag(21, "ABI_FP_exceptions", ParameterType.ULEB128)
            val ABI_FP_user_exceptions = addTag(22, "ABI_FP_user_exceptions", ParameterType.ULEB128)
            val ABI_FP_number_model = addTag(23, "ABI_FP_number_model", ParameterType.ULEB128)
            val ABI_align_needed = addTag(24, "ABI_align_needed", ParameterType.ULEB128)
            val ABI_align8_preserved = addTag(25, "ABI_align8_preserved", ParameterType.ULEB128)
            val ABI_enum_size = addTag(26, "ABI_enum_size", ParameterType.ULEB128)
            val ABI_HardFP_use = addTag(27, "ABI_HardFP_use", ParameterType.ULEB128)
            val ABI_VFP_args = addTag(28, "ABI_VFP_args", ParameterType.ULEB128)
            val ABI_WMMX_args = addTag(29, "ABI_WMMX_args", ParameterType.ULEB128)
            val ABI_optimization_goals = addTag(30, "ABI_optimization_goals", ParameterType.ULEB128)
            val ABI_FP_optimization_goals = addTag(31, "ABI_FP_optimization_goals", ParameterType.ULEB128)
            val compatibility = addTag(32, "compatibility", ParameterType.NTBS)
            val CPU_unaligned_access = addTag(34, "CPU_unaligned_access", ParameterType.ULEB128)
            val FP_HP_extension = addTag(36, "FP_HP_extension", ParameterType.ULEB128)
            val ABI_FP_16bit_format = addTag(38, "ABI_FP_16bit_format", ParameterType.ULEB128)
            val MPextension_use = addTag(42, "MPextension_use", ParameterType.ULEB128)
            val DIV_use = addTag(44, "DIV_use", ParameterType.ULEB128)
            val nodefaults = addTag(64, "nodefaults", ParameterType.ULEB128)
            val also_compatible_with = addTag(65, "also_compatible_with", ParameterType.NTBS)
            val conformance = addTag(67, "conformance", ParameterType.NTBS)
            val T2EE_use = addTag(66, "T2EE_use", ParameterType.ULEB128)
            val Virtualization_use = addTag(68, "Virtualization_use", ParameterType.ULEB128)
            val MPextension_use2 = addTag(70, "MPextension_use", ParameterType.ULEB128)

            private fun addTag(
                value: Int,
                name: String,
                type: ParameterType
            ): ArmAeabiAttributesTag {
                val tag = ArmAeabiAttributesTag(value, name, type)

                if (!valueMap.containsKey(tag.value)) {
                    valueMap[tag.value] = tag
                }

                if (!nameMap.containsKey(tag.name)) {
                    nameMap[tag.name] = tag
                }

                tags.add(tag)

                return tag
            }

            fun getTags(): List<ArmAeabiAttributesTag> =
                listOf(*tags.toTypedArray())

            fun getByName(name: String): ArmAeabiAttributesTag? =
                nameMap[name]

            fun getByValue(value: Int): ArmAeabiAttributesTag? =
                if (valueMap.containsKey(value)) {
                    valueMap[value]
                } else {
                    ArmAeabiAttributesTag(
                        value,
                        "Unknown $value",
                        getParameterType(value)
                    )
                }

            private fun getParameterType(value: Int): ParameterType =
                getByValue(value)?.parameterType
                    ?: if (value % 2 == 0) {
                        ParameterType.ULEB128
                    } else {
                        ParameterType.NTBS
                    }
        }
    }

    companion object {
        /**
         * Generic ELF header
         */
        private val ELF_MAGIC = byteArrayOf(
            0x7F.toByte(),
            'E'.code.toByte(),
            'L'.code.toByte(),
            'F'.code.toByte()
        )

        /**
         * e_flags mask if executable file conforms to hardware floating-point
         * procedure-call standard (arm ABI version 5)
         */
        private const val EF_ARM_ABI_FLOAT_HARD = 0x00000400

        /**
         * e_flags mask if executable file conforms to software floating-point
         * procedure-call standard (arm ABI version 5)
         */
        private const val EF_ARM_ABI_FLOAT_SOFT = 0x00000200
        private const val EI_DATA_BIG_ENDIAN = 2
        private const val E_MACHINE_ARM = 0x28
        private const val EI_CLASS_64BIT = 2

        @Throws(IOException::class)
        fun analyse(filename: String): ELFAnalyser {
            val res = ELFAnalyser(filename)
            res.runDetection()
            return res
        }

        private fun parseArmAttributes(
            buffer: ByteBuffer
        ): Map<Int?, Map<ArmAeabiAttributesTag?, Any>?> {
            val format = buffer.get()

            if (format.toInt() != 0x41) {
                // Version A
                // Not supported
                return emptyMap()
            }

            while (buffer.position() < buffer.limit()) {
                val posSectionStart = buffer.position()
                val sectionLength = buffer.int

                if (sectionLength <= 0) {
                    // Fail!
                    break
                }

                val vendorName = readNTBS(buffer)

                if ("aeabi" == vendorName) {
                    return parseAEABI(buffer)
                }

                buffer.position(posSectionStart + sectionLength)
            }
            return emptyMap()
        }

        private fun parseAEABI(
            buffer: ByteBuffer
        ): Map<Int?, Map<ArmAeabiAttributesTag?, Any>?> {
            val data: MutableMap<Int?, Map<ArmAeabiAttributesTag?, Any>?> =
                mutableMapOf()

            while (buffer.position() < buffer.limit()) {
                val pos = buffer.position()
                val subsectionTag = readULEB128(buffer).toInt()
                val length = buffer.int

                if (subsectionTag == 1.toByte().toInt()) {
                    data[subsectionTag] = parseFileAttribute(buffer)
                }

                buffer.position(pos + length)
            }

            return data
        }

        private fun parseFileAttribute(
            buffer: ByteBuffer
        ): Map<ArmAeabiAttributesTag?, Any> {
            val result: MutableMap<ArmAeabiAttributesTag?, Any> =
                mutableMapOf()

            while (buffer.position() < buffer.limit()) {
                val tag = ArmAeabiAttributesTag.getByValue(
                    readULEB128(buffer).toInt()
                )

                when (tag!!.parameterType) {
                    ArmAeabiAttributesTag.ParameterType.UINT32 ->
                        result[tag] = buffer.int

                    ArmAeabiAttributesTag.ParameterType.NTBS ->
                        result[tag] = readNTBS(buffer)

                    ArmAeabiAttributesTag.ParameterType.ULEB128 ->
                        result[tag] = readULEB128(buffer)
                }
            }

            return result
        }

        private fun readNTBS(buffer: ByteBuffer): String {
            val startingPos = buffer.position()
            var currentByte: Byte

            do {
                currentByte = buffer.get()
            } while (
                currentByte != '\u0000'.code.toByte() &&
                buffer.position() <= buffer.limit()
            )

            val terminatingPosition = buffer.position()
            val data = ByteArray(terminatingPosition - startingPos - 1)

            buffer.position(startingPos)
            buffer[data]
            buffer.position(buffer.position() + 1)

            return String(data, StandardCharsets.US_ASCII)
        }

        private fun readULEB128(buffer: ByteBuffer): BigInteger {
            var result = BigInteger.ZERO
            var shift = 0

            while (true) {
                val b = buffer.get()

                result = result.or(
                    BigInteger.valueOf(
                        (b.toInt() and 127).toLong()
                    ).shiftLeft(shift)
                )

                if ((b.toInt() and 128) == 0) {
                    break
                }

                shift += 7
            }

            return result
        }
    }
}
