package fr.stardustenterprises.plat4k

import com.sun.jna.ELFAnalyserWrapper
import com.sun.jna.Native
import fr.stardustenterprises.plat4k.EnumCPUType.X32
import fr.stardustenterprises.plat4k.EnumCPUType.X64
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Enum of known CPU architectures.
 *
 * @author xtrm, lambdagg
 */
enum class EnumArchitecture(
    /**
     * The identifier of the current architecture.
     *
     * Note that when formatting, this is not used as an alias, and we have to
     * therefore add the same identifier. See the [aliases] definition for more
     * insights as to why and how.
     */
    val identifier: String,

    /**
     * The other aliases of the current architecture.
     *
     * Note that those aliases are read from the top to the bottom when
     * formatting, meaning the index of the alias in the array is important.
     * Also, this step is done using a [String.contains] instruction.
     */
    val aliases: Array<String> = arrayOf(),

    /**
     * The type of CPU this architecture complains to.
     */
    val cpuType: EnumCPUType
) {
    /**
     * The standard (or at least most common) 64-bits architecture. (x86_64,
     * amd64, x64)
     */
    X86_64("x86_64", arrayOf("x86_64", "amd64", "x64"), X64),

    /**
     * The standard (or at least most common) 32-bits x86 architecture.
     * (i3, i4, i5, i6 & x86)
     */
    X86("x86", arrayOf("i386", "i486", "i586", "i686", "x86", "amd32"), X32),


    /**
     * The non-aarch ARM architecture, 32-bits.
     */
    ARM("arm", arrayOf("armv7l", "armv7", "armel", "armle", "armv", "arm"), X32),

    /**
     * The aarch ARM architecture, 32-bits. (aarch32, arm32).
     */
    AARCH("aarch32", arrayOf("aarch32", "arm32"), X32),

    /**
     * The aarch ARM architecture, 64-bits. (aarch64, arm64).
     */
    AARCH_64("aarch64", arrayOf("aarch64", "arm64"), X64),


    /**
     * The MIPS architecture, 32-bits. (mipsle, mipsel, mips)
     * https://github.com/golang/go/issues/18622#issuecomment-272060013
     */
    MIPS("mips", arrayOf("mipsle", "mipsel", "mips"), X32),

    /**
     * The MIPS architecture, 64-bits. (mips64)
     */
    MIPS_64("mips64", arrayOf("mips64", "mips64el", "mips"), X64),


    /**
     * The PowerPC architecture, 32-bits. (ppcel, pccle, powerpc, ppc)
     */
    PPC("powerpc", arrayOf("ppcel", "ppcle", "powerpc", "ppc"), X32),

    /**
     * The PowerPC architecture, 64-bits. (ppc64, powerpc64, ppc64el, ppc64le)
     */
    PPC_64("ppc64", arrayOf("ppc64", "powerpc64", "ppc64el", "ppc64le"), X64),


    /**
     * The S390X architecture, 64-bits. (s390)
     */
    S390X("s390x", arrayOf("s390"), X64),

    /**
     * The SPARCv9 architecture, 64-bits. (sparcv9, sparc)
     */
    SPARCV9("sparcv9", arrayOf("sparcv9", "sparc"), X64),


    /**
     * The unknown architecture type, if we ever need it.
     */
    UNKNOWN("unknown", arrayOf(), EnumCPUType.UNKNOWN);

    companion object {
        /**
         * The parsed [EnumArchitecture] from the [rawArchitecture] value.
         */
        @JvmStatic
        val currentArch: EnumArchitecture by lazy {
            var architecture = UNKNOWN
            val iter = values().maxOf { it.aliases.size }

            val platformByteCount = if(com.sun.jna.Platform.is64Bit()) 8
                                    else Native.POINTER_SIZE

            for (i in 0 until iter) {
                values().filter { it.aliases.size > i }.forEach {
                    val id = it.aliases[i]
                    if (rawArchitecture.contains(id)) {
                        val cpuByteCount = it.cpuType.bits / 8
                        if (cpuByteCount == platformByteCount) {
                            architecture = it
                        }
                    }
                }
            }

            architecture
        }

        /**
         * The raw name of the current architecture.
         */
        @JvmStatic
        val rawArchitecture: String by lazy {
            var arch = run {
                try {
                    System.getenv("PROCESSOR_ARCHITECTURE")
                } catch (exception: Throwable) {
                    null
                } ?: try {
                    System.getenv("PROCESSOR_ARCHITEW6432")
                } catch (exception: Throwable) {
                    null
                } ?: System.getProperty("os.arch") ?: unameArch
                ?: throw RuntimeException(
                    "Couldn't detect runtime architecture."
                )
            }.lowercase()

            if (arch == "zarch_64") {
                arch = "s390x"
            }

            // https://bugs.openjdk.java.net/browse/JDK-8073139
            if (
                arch == "ppc64" &&
                System.getProperty("sun.cpu.endian") == "little"
            ) {
                arch = "ppc64le"
            }

            if (
                arch == "arm" &&
                EnumOperatingSystem.currentOS == EnumOperatingSystem.LINUX &&
                isSoftFloat
            ) {
                arch = "armel"
            }

            arch
        }

        /**
         * Whether the architecture is soft float (emulated floating point
         * unit) or hard float (on-chip floating point unit).
         * Uses JNA's [ELFAnalyser] to do the work (via [ELFAnalyserWrapper]).
         */
        private val isSoftFloat: Boolean by lazy {
            try {
                val javaBin = File("/proc/self/exe")
                javaBin.exists() &&
                    !ELFAnalyserWrapper.isArmHardFloat(javaBin.canonicalPath)
            } catch (ignored: Exception) {
                false
            }
        }

        /**
         * The result of the *nix command `uname -m`.
         * https://stackoverflow.com/a/45125525
         */
        private val unameArch: String? by lazy {
            try {
                BufferedReader(
                    InputStreamReader(
                        ProcessBuilder("uname", "-m").start().inputStream
                    )
                ).readLine()?.lowercase()
            } catch (ignored: Exception) {
                null
            }
        }
    }
}
