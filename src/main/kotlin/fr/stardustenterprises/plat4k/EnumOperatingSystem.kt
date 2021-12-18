package fr.stardustenterprises.plat4k

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Enum of known Operating System types,
 * also includes `glibc` or `musl` stdlib detection.
 */
enum class EnumOperatingSystem(
    val osName: String,
    val aliases: Array<String>,
    val nativeSuffix: String = ".so",
    val nativePrefix: String = "lib",
    private val postCheck: () -> Boolean = { true }
) {
    WINDOWS("Windows", arrayOf("windows", "win"), ".dll", ""),

    LINUX("Linux", arrayOf("linux", "nix"), postCheck = { !muslPresent && !isDalvikRuntime }),
    LINUX_MUSL("Linux-musl", arrayOf("linux", "nix"), postCheck = { muslPresent && !isDalvikRuntime }),
    ANDROID("Android", arrayOf("linux", "nix"), postCheck = { isDalvikRuntime }),

    MACOS("macOS", arrayOf("darwin", "macos", "osx"), ".dylib"),

    SOLARIS("Solaris", arrayOf("solaris", "sunos")),

    FREE_BSD("FreeBSD", "freebsd"),
    NET_BSD("NetBSD", "netbsd"),
    OPEN_BSD("OpenBSD", "openbsd"),

    AIX("AIX", "aix"),

    UNKNOWN("Unknown", "unknown");

    constructor(osName: String, identifier: String) : this(osName, arrayOf(identifier))

    companion object {
        @JvmStatic
        val currentOS: EnumOperatingSystem by lazy {
            val name = System.getProperty("os.name").lowercase()

            var operatingSystem = UNKNOWN
            val iter = values().maxOf { it.aliases.size }
            for (i in 0 until iter) {
                values().filter { it.aliases.size > i }.forEach {
                    val id = it.aliases[i]
                    if (name.contains(id)) {
                        operatingSystem = it
                    }
                }
            }

            operatingSystem
        }

        @JvmStatic
        val muslPresent: Boolean by lazy {
            try {
                val p = ProcessBuilder("ldd", "--version").start()
                var line = BufferedReader(InputStreamReader(p.inputStream)).readLine()
                if (line == null) {
                    line = BufferedReader(InputStreamReader(p.errorStream)).readLine()
                }
                line != null && line.lowercase().startsWith("musl")
            } catch (fail: Exception) {
                false
            }
        }

        @JvmStatic
        val isDalvikRuntime: Boolean =
            "dalvik" == System.getProperty("java.vm.name").lowercase()
    }
}
