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
    val nativePrefix: String = "lib",
    val nativeSuffix: String = ".so",
    private val postCheck: () -> Boolean = { true }
) {
    WINDOWS("Windows", arrayOf("windows", "win"), "", ".dll"),

    LINUX("Linux", arrayOf("linux", "nix", "nux"), postCheck = { !muslPresent && !isAndroid }),
    LINUX_MUSL("Linux-musl", arrayOf("linux", "nix", "nux"), postCheck = { muslPresent && !isAndroid }),
    ANDROID("Android", arrayOf("android", "linux", "nix", "nux"), postCheck = { isAndroid }),

    MACOS("macOS", arrayOf("darwin", "macos", "osx"), nativeSuffix = ".dylib"),

    SOLARIS("Solaris", arrayOf("solaris", "sunos")),

    FREE_BSD("FreeBSD", "freebsd"),
    NET_BSD("NetBSD", "netbsd"),
    OPEN_BSD("OpenBSD", "openbsd"),
    DRAGONFLY_BSD("DragonflyBSD", "dragonfly")

    AIX("AIX", "aix"),

    UNKNOWN("Unknown", "unknown");

    constructor(osName: String, identifier: String) : this(osName, arrayOf(identifier))

    companion object {
        /**
         * The parsed [EnumOperatingSystem] reference.
         */
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

        /**
         * Is the current Operating System using `musl` as the C stdlib.
         *
         * For more info, see [musl libc](https://musl.libc.org/)
         */
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

        /**
         * Is the current Operating System [ANDROID].
         */
        @JvmStatic
        val isAndroid: Boolean =
            System.getProperty("java.vm.vendor")?.lowercase()?.contains("android") ?: false
    }
}
