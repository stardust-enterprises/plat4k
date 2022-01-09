package com.sun.jna

/**
 * Dirty wrapping class to access the package-private [ELFAnalyser] class
 * to help with architecture recognition.
 */
object ELFAnalyserWrapper {
    /**
     * @param path to a system binary
     *
     * @return is the provided path to a hardfloat binary
     */
    fun isArmHardFloat(path: String) =
        ELFAnalyser.analyse(path).isArmHardFloat
}
