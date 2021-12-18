import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.18.0")
    }
}

plugins {
    kotlin("jvm") version "1.6.0"
}

apply(plugin = "com.vanniktech.maven.publish.base")

group = "fr.stardustenterprises"
version = "1.1.0-rc1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.0")
}

plugins.withId("com.vanniktech.maven.publish.base") {
    configure<MavenPublishBaseExtension> {
        publishToMavenCentral(SonatypeHost.S01)
        signAllPublications()
        pom {
            name.set(project.name)
            description.set("Platform identifier library for the JVM.")
            url.set("https://github.com/stardust-enterprises/plat4k")
            licenses {
                license {
                    name.set("ISC License")
                    url.set("https://opensource.org/licenses/ISC")
                    distribution.set("repo")
                }
            }
            scm {
                connection.set("scm:git:https://github.com/stardust-enterprises/plat4k.git")
                developerConnection.set("scm:git:ssh://git@github.com/stardust-enterprises/plat4k.git")
                url.set("https://github.com/stardust-enterprises/plat4k")
            }
            developers {
                developer {
                    id.set("xtrm")
                    name.set("xtrm")
                    url.set("https://github.com/xtrm-en")
                }
            }
        }
    }
}