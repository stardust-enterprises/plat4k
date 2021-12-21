package fr.stardustenterprises.plat4k

/**
 * A data class containing an [EnumOperatingSystem]
 * and its paired [EnumArchitecture]
 */
data class Platform(
    val operatingSystem: EnumOperatingSystem,
    val architecture: EnumArchitecture
) {
    companion object {
        /**
         * The current detected [Platform] instance.
         */
        @JvmStatic
        val current: Platform by lazy {
            Platform(
                EnumOperatingSystem.currentOS,
                EnumArchitecture.currentArch
            )
        }
    }
}
