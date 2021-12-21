# plat4k [![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.stardustenterprises/plat4k/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.stardustenterprises/plat4k)

a platform identifier library for the 
[JVM](https://adoptium.net), written in [Kotlin](https://kotlinlang.org).

# how use
to import from [maven central](https://repo1.maven.org/maven2/), just add
[plat4k](https://github.com/stardust-enterprises/plat4k) to your dependencies block:
```kotlin
// change to the latest version
val PLAT4K_VERSION = "1.1.2" 

dependencies {
    implementation("fr.stardustenterprises:plat4k:$PLAT4K_VERSION")
}
```

# contributing
contributions are open, feel free to open a [pull request](https://github.com/stardust-enterprises/plat4k/pull/new) (especially for new OS/architecture support).

# licensing
this project is under the [ISC license](https://github.com/stardust-enterprises/plat4k/blob/trunk/LICENSE),
except for the [ELFAnalyser](https://github.com/stardust-enterprises/plat4k/tree/trunk/src/main/java/com/sun/jna/ELFAnalyser.java)
class taken from the [JNA Project](https://github.com/java-native-access/jna/), which is itself licensed under the 
[Apache 2.0 license](http://www.apache.org/licenses/).
