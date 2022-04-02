package fr.stardustenterprises.plat4k

import fr.stardustenterprises.plat4k.EnumOperatingSystem.*

/**
 * Represents an [OperatingSystem][EnumOperatingSystem] Family.
 *
 * @author xtrm
 */
enum class EnumFamily(
    /**
     * The [OperatingSystem][EnumOperatingSystem]s in this Family.
     */
    private vararg val operatingSystems: EnumOperatingSystem,
    /**
     * The parent Family, can be null.
     */
    private val parentFamily: EnumFamily? = null
) {
    /**
     * The Windows Family.
     */
    WINDOWS(EnumOperatingSystem.WINDOWS),

    /**
     * The BSD Family.
     */
    BSD(OPEN_BSD, FREE_BSD, NET_BSD, DRAGONFLY_BSD, UNKNOWN_BSD),

    /**
     * The UNIX Family.
     */
    UNIX(LINUX, LINUX_MUSL, ANDROID, MACOS, AIX, ILLUMOS, parentFamily = BSD),

    /**
     * The Unknown Family.
     */
    UNKNOWN(EnumOperatingSystem.UNKNOWN, UNKNOWN_BSD);

    /**
     * Wheather or not this Family contains
     * the provided [OperatingSystem][EnumOperatingSystem]s.
     */
    fun containsAll(vararg operatingSystems: EnumOperatingSystem): Boolean =
        operatingSystems.all { this.operatingSystems.contains(it) } &&
            (this.parentFamily?.containsAll(*operatingSystems) ?: true)

    /**
     * @see EnumFamily.containsAll
     */
    operator fun contains(operatingSystem: EnumOperatingSystem): Boolean =
        this.containsAll(operatingSystem)
}
