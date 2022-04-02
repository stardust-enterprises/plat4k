package fr.stardustenterprises.plat4k

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Enum of known Operating System types, also includes `glibc` or `musl` stdlib
 * detection.
 *
 * @author xtrm, lambdagg
 */
enum class EnumOperatingSystem(
    /**
     * The OperatingSystem's legible name.
     */
    val osName: String,
    /**
     * Aliases from which we can identify this OperatingSystem.
     */
    val aliases: Array<String>,
    /**
     * The native library prefix.
     */
    val nativePrefix: String = "lib",
    /**
     * The native library "suffix"/file extension.
     */
    val nativeSuffix: String = ".so",
    /**
     * Check to be run for extra detection accuracy.
     */
    private val postCheck: () -> Boolean = { true }
) {
    /**
     * The Windows OS. (windows, win)
     */
    WINDOWS("Windows", arrayOf("windows", "win"), "", ".dll"),

    /**
     * The Linux OS. (linux, nix, nux)
     */
    LINUX("Linux", arrayOf("linux", "nix", "nux"), postCheck = { !muslPresent && !isAndroid }),

    /**
     * The Linux MUSL OS. (linux, nix, nux)
     */
    LINUX_MUSL("Linux-musl", arrayOf("linux", "nix", "nux"), postCheck = { muslPresent && !isAndroid }),

    /**
     * The Android OS. (android, linux, nix, nux)
     */
    ANDROID("Android", arrayOf("android", "linux", "nix", "nux"), postCheck = { isAndroid }),

    /**
     * The Darwin OS. (darwin, macos, osx)
     */
    MACOS("macOS", arrayOf("darwin", "macos", "osx"), nativeSuffix = ".dylib"),

    /**
     * The Solaris OS. (solaris, sunos)
     */
    SOLARIS("Solaris", arrayOf("solaris", "sunos")),

    /**
     * The FreeBSD OS. (freebsd)
     */
    FREE_BSD("FreeBSD", "freebsd"),

    /**
     * The NetBSD OS. (netbsd)
     */
    NET_BSD("NetBSD", "netbsd"),

    /**
     * The OpenBSD OS. (openbsd)
     */
    OPEN_BSD("OpenBSD", "openbsd"),

    /**
     * The DragonflyBSD OS. (dragonfly)
     */
    DRAGONFLY_BSD("DragonflyBSD", "dragonfly"),

    /**
     * Unknown BSD OS.
     */
    UNKNOWN_BSD("Unknown BSD", arrayOf("_DO_NOT_DETECT", "bsd")),

    /**
     * The AIX OS. (aix)
     */
    AIX("AIX", "aix"),

    /**
     * The Haiku OS. (haiku, hrev*****)
     */
    HAIKU("Haiku", arrayOf("haiku", "hrev")),

    /**
     * The Illumos OS. (illumos, omnios, openindiana)
     */
    ILLUMOS("Illumos", arrayOf("illumos", "omnios", "openindiana")),

    /**
     * An unknown OS.
     */
    UNKNOWN("Unknown", "unknown");

    constructor(osName: String, identifier: String) : this(osName, arrayOf(identifier))

    /**
     * Companion object for [EnumOperatingSystem]
     */
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
                        if(it.postCheck.invoke()) {
                            operatingSystem = it
                        }
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
