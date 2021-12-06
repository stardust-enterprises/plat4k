package fr.stardustenterprises.plat4k

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

enum class EnumOperatingSystem(
    val osName: String,
    val identifiers: Array<String>,
    val nativeSuffix: String = ".so",
    val nativePrefix: String = "lib",
    private val postCheck: () -> Boolean = { true }
) {
    WINDOWS("Windows", arrayOf("windows", "win"), ".dll", ""),
    LINUX("Linux", arrayOf("linux", "nix"), postCheck = { !checkMusl() }),
    LINUX_MUSL("Linux-MUSL", arrayOf("linux-musl", "linux", "nix"), postCheck = { checkMusl() }),
    MACOS("macOS", arrayOf("darwin", "macos", "osx"), ".dylib"),
    SOLARIS("Solaris", arrayOf("solaris", "sunos")),
    FREE_BSD("FreeBSD", "freebsd"),
    NET_BSD("NetBSD", "netbsd"),
    OPEN_BSD("OpenBSD", "openbsd");

    constructor(osName: String, identifier: String) : this(osName, arrayOf(identifier))

    companion object {
        private var isMusl: Boolean? = null
        private val lock = Object()

        private fun checkMusl(): Boolean {
            if (isMusl == null) {
                isMusl = synchronized(lock) {
                    var check: Boolean
                    try {
                        val p = ProcessBuilder("ldd", "--version").start()
                        var line = BufferedReader(InputStreamReader(p.inputStream)).readLine()
                        if (line == null) {
                            line = BufferedReader(InputStreamReader(p.errorStream)).readLine()
                        }
                        check = line != null && line.lowercase(Locale.getDefault()).startsWith("musl")
                    } catch (fail: Exception) {
                        check = false
                    }
                    check
                }
            }
            return isMusl!!
        }
    }
}