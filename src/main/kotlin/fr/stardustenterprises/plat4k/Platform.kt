package fr.stardustenterprises.plat4k

data class Platform(
    val operatingSystem: EnumOperatingSystem,
    val architecture: EnumArchitecture
) {
    companion object {
        @JvmStatic
        fun getCurrent() =
            Platform(
                EnumOperatingSystem.parseCurrent(),
                EnumArchitecture.currentArch()
            )
    }
}
