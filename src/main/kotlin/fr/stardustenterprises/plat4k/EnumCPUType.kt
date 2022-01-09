package fr.stardustenterprises.plat4k

/**
 * Represents a processor type.
 *
 * @author lambdagg
 */
enum class EnumCPUType(
    /**
     * The number of bits of the current type.
     * -1 means unknown.
     */
    val bits: Short = -1
) {
    /**
     * The 32-bits processor type.
     */
    X32(32),

    /**
     * The 64-bits processor type.
     */
    X64(64),

    /**
     * The unknown processor type.
     */
    UNKNOWN,
}
