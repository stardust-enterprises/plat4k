package fr.stardustenterprises.plat4k

import com.sun.jna.ELFAnalyser
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Enum of known Processor Architectures.
 */
enum class EnumArchitecture(
    val identifier: String,
    val aliases: Array<String> = arrayOf(),
    val bits: Short,
    private val postCheck: () -> Boolean = { true }
) {
    X86_64("x86_64", arrayOf("x86_64", "amd64", "x64"), 64),
    X86("x86", arrayOf("i386", "i486", "i586", "i686", "x86"), 32),

    ARM("arm", arrayOf("armv7", "armel", "armle", "armv", "arm"), 32),
    AARCH("aarch32", arrayOf("aarch32", "arm32"), 32),
    AARCH_64("aarch64", arrayOf("aarch64", "arm64"), 64),

    MIPS("mips", arrayOf("mipsle", "mipsel", "mips"), 32),
    MIPS_64("mips64", arrayOf("mips64"), 64),

    PPC("powerpc", arrayOf("ppcel", "ppcle", "powerpc", "ppc"), 32),
    PPC_64("ppc64", arrayOf("ppc64", "powerpc64", "ppc64el", "ppc64le"), 64),

    S390X("s390x", arrayOf("s390"), 64),
    SPARCV9("sparcv9", arrayOf("sparcv9"), 64),

    UNKNOWN("unknown", arrayOf(), -1);

    companion object {
        /**
         * The parsed [EnumArchitecture] from the [rawArchitecture] value.
         */
        @JvmStatic
        val currentArch: EnumArchitecture by lazy {
            var architecture = UNKNOWN
            val iter = values().maxOf { it.aliases.size }

            for (i in 0 until iter) {
                values().filter { it.aliases.size > i }.forEach {
                    val id = it.aliases[i]
                    if (rawArchitecture.contains(id)) {
                        architecture = it
                    }
                }
            }

            architecture
        }

        /**
         * A [String] containing the raw architecture name.
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
                ?: throw RuntimeException("Couldn't detect runtime architecture.")
            }.lowercase()

            if (arch == "zarch_64") {
                arch = "s390x"
            }
            // https://bugs.openjdk.java.net/browse/JDK-8073139
            if (arch == "ppc64" && System.getProperty("sun.cpu.endian") == "little") {
                arch = "ppc64le"
            }
            if(arch == "arm" && EnumOperatingSystem.currentOS == EnumOperatingSystem.LINUX && isSoftFloat) {
                arch = "armel"
            }

            arch
        }

        private val isSoftFloat: Boolean by lazy {
            try {
                val self = File("/proc/self/exe")
                if (self.exists()) {
                    val ahfd = ELFAnalyser.analyse(self.canonicalPath)
                    !ahfd.isArmHardFloat
                }
            } catch (ignored: Exception) {
            }
            false
        }

        private val unameArch: String? by lazy {
            try {
                val p = ProcessBuilder("uname", "-m").start()
                val line = BufferedReader(InputStreamReader(p.inputStream)).readLine()
                line?.lowercase()
            } catch (fail: Exception) {
                null
            }
        }
    }
}