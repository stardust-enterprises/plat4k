package fr.stardustenterprises.plat4k

enum class EnumArchitecture(
    vararg val identifiers: String
) {
    ARM64("aarch64", "arm64"),
    ARM("arm", "armv7"),
    X86("x86", "i386", "i486", "i586", "i686"),
    X86_64("x86_64", "amd64", "x64")
}