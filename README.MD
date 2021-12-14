# plat4k
a platform identifier library for the 
[JVM](https://adoptium.net), written in [Kotlin](https://kotlinlang.org).

# how use
this is going to go over how to setup 
[plat4k](https://github.com/stardust-enterprises/plat4k) on a 
[gradle](https://gradle.org) project using a Kotlin DSL buildscript,
tho this should be pretty straightforward using the Groovy DSL.

first you need to import the library, you can either import it from
the [maven central](https://repo1.maven.org/maven2/) repository or 
from [jitpack](https://jitpack.io) automatic builds.

for [maven central](https://repo1.maven.org/maven2/), just add
[plat4k](https://github.com/stardust-enterprises/plat4k) to your dependencies block:
```kotlin
// change to the latest version
val PLAT4K_VERSION = "1.0.0" 

dependencies {
    implementation("fr.stardustenterprises:plat4k:$PLAT4K_VERSION")
}
```

for [jitpack](https://jitpack.io), add the repository as well as the dependency,
this time using the github domain:

```kotlin
// change to the latest github release, commit, or branch name
val PLAT4K_RELEASE = "1.0.0" 

repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.stardust-enterprises:plat4k:$PLAT4K_RELEASE")
}
```

# contributing
contributions are open, feel free to open a [pull request](https://github.com/stardust-enterprises/plat4k/pull/new) (especially for new OS/architecture support).

# licensing
this project is under the [MIT license](https://github.com/stardust-enterprises/plat4k/blob/trunk/LICENSE).
