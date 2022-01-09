# plat4k 
[![Build][badge-github-ci]][plat4k-gradle-ci] [![Maven Central][badge-mvnc]][plat4k-mvnc]

a platform identifier library for the [JVM][jvm], written in [Kotlin][kotlin].

# how use

you can import [plat4k][plat4k] from [maven central][mvnc] just by adding it to your dependencies:

## gradle

```kotlin
dependencies {
    implementation("fr.stardustenterprises:plat4k:{VERSION}")
}
```

## maven

```xml
<dependency>
    <groupId>fr.stardustenterprises</groupId>
    <artifactId>plat4k</artifactId>
    <version>{VERSION}</version>
</dependency>
```

# contributing

you can contribute by [forking the repository][fork], making your changes and [creating a new pull request][new-pr]
describing what you changed, why and how.

# licensing

this project is under the [ISC license][blob-license].


<!-- Links -->

[jvm]: https://adoptium.net "adoptium website"

[kotlin]: https://kotlinlang.org "kotlin website"

[plat4k]: https://github.com/stardust-enterprises/plat4k "plat4k github repository"

[fork]: https://github.com/stardust-enterprises/plat4k/fork "fork this repository"

[new-pr]: https://github.com/stardust-enterprises/plat4k/pulls/new "create a new pull request"

[new-issue]: https://github.com/stardust-enterprises/plat4k/issues/new "create a new issue"

[mvnc]: https://repo1.maven.org/maven2/ "maven central website"

[plat4k-mvnc]: https://maven-badges.herokuapp.com/maven-central/fr.stardustenterprises/plat4k "maven central repository"

[plat4k-gradle-ci]: https://github.com/stardust-enterprises/plat4k/actions/workflows/gradle-ci.yml "gradle ci workflow"

[blob-license]: https://github.com/stardust-enterprises/plat4k/blob/trunk/LICENSE "LICENSE source file"

<!-- Badges -->

[badge-mvnc]: https://maven-badges.herokuapp.com/maven-central/fr.stardustenterprises/plat4k/badge.svg "maven central badge"

[badge-github-ci]: https://github.com/stardust-enterprises/plat4k/actions/workflows/gradle-ci.yml/badge.svg?branch=trunk "github actions badge"
