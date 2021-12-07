package fr.stardustenterprises.plat4k

/**
 * A data class containing an [EnumOperatingSystem] and its paired [EnumArchitecture]
 */
data class Platform(
    val operatingSystem: EnumOperatingSystem,
    val architecture: EnumArchitecture
) {
    companion object {
        val current = Platform(
            EnumOperatingSystem.currentOS,
            EnumArchitecture.currentArch
        )
    }
}
