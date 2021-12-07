package fr.stardustenterprises.plat4k

enum class EnumArchitecture(
    vararg val identifiers: String
) {
    ARM64("aarch64", "arm64"),
    ARM("arm", "armv7"),
    X86("x86", "i386", "i486", "i586", "i686"),
    X86_64("x86_64", "amd64", "x64"),
    UNKNOWN("unknown");

    companion object {
        val currentArch: EnumArchitecture by lazy {
            val arch = run {
                try {
                    System.getenv("PROCESSOR_ARCHITECTURE")
                } catch (exception: Throwable) {
                    null
                } ?: try {
                    System.getenv("PROCESSOR_ARCHITEW6432")
                } catch (exception: Throwable) {
                    null
                } ?: System.getProperty("os.arch") ?: throw RuntimeException("Couldn't detect runtime architecture.")
            }.lowercase()

            var architecture = UNKNOWN
            val iter = values().maxOf { it.identifiers.size }
            for (i in 0 until iter) {
                values().filter { it.identifiers.size > i }.forEach {
                    val id = it.identifiers[i]
                    if (arch.contains(id)) {
                        architecture = it
                    }
                }
            }
            architecture
        }
    }
}