package fr.stardustenterprises.plat4k

/**
 * A data class containing an [EnumOperatingSystem] and its paired
 * [EnumArchitecture].
 *
 * @author xtrm, lambdagg
 */
data class Platform(
    /**
     * This platform's operating system.
     */
    val operatingSystem: EnumOperatingSystem = EnumOperatingSystem.UNKNOWN,

    /**
     * This platform's architecture.
     */
    val architecture: EnumArchitecture = EnumArchitecture.UNKNOWN
) {
    companion object {
        /**
         * The current detected [Platform] instance.
         */
        @JvmStatic
        val currentPlatform: Platform = Platform(
            EnumOperatingSystem.currentOS,
            EnumArchitecture.currentArch
        )

        /**
         * @see currentPlatform
         */
        @JvmStatic
        @Deprecated(
            "'current' has been renamed to 'currentPlatform' as of 1.4.0, and deprecated stuff is soon to be removed.",
            ReplaceWith("currentPlatform")
        )
        val current: Platform = currentPlatform
    }
}
