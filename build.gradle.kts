import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost

buildscript {
    dependencies {
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.18.0")
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

plugins {
    kotlin("jvm") version "1.6.0"
}

apply(plugin = "com.vanniktech.maven.publish.base")

group = "fr.stardustenterprises"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.0")
}

object Publishing {
    const val url = "https://github.com/stardust-enterprises/plat4k"
    const val connection = "scm:git:https://github.com/stardust-enterprises/plat4k.git"
    const val devConnection = "scm:git:git@github.com:stardust-enterprises/plat4k.git"
}

plugins.withId("com.vanniktech.maven.publish.base") {
    configure<MavenPublishBaseExtension> {
        publishToMavenCentral(SonatypeHost.DEFAULT)
        signAllPublications()
        pom {
            name.set(project.name)
            description.set("Square’s meticulous HTTP client for Java and Kotlin.")
            url.set("https://square.github.io/okhttp/")
            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }
            scm {
                connection.set("scm:git:https://github.com/square/okhttp.git")
                developerConnection.set("scm:git:ssh://git@github.com/square/okhttp.git")
                url.set("https://github.com/square/okhttp")
            }
            developers {
                developer {
                    name.set("Square, Inc.")
                }
            }
        }
    }
}
