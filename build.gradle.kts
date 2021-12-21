@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm") version "1.6.0"
    id("org.jetbrains.dokka") version "1.6.0"
    `maven-publish`
    signing
}

val NEXUS_USERNAME: String by project
val NEXUS_PASSWORD: String by project

group = "fr.stardustenterprises"
version = "1.1.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    val dokkaHtml by getting(DokkaTask::class) {
        dokkaSourceSets {
            configureEach {
                skipDeprecated.set(true)
                reportUndocumented.set(true)
                perPackageOption {
                    matchingRegex.set(""".*\.jna.*""")
                    suppress.set(true)
                }
            }
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    val javadoc = tasks.named("dokkaHtml")
    dependsOn(javadoc)
    from(javadoc.get().outputs)
}

artifacts {
    archives(sourcesJar)
    archives(javadocJar)
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())

            pom {
                name.set("plat4k")
                description.set("Platform identifier library for the JVM.")
                url.set("https://github.com/stardust-enterprises/plat4k")
                licenses {
                    license {
                        name.set("ISC License")
                        url.set("https://opensource.org/licenses/ISC")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("xtrm")
                        name.set("xtrm")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/stardust-enterprises/plat4k.git")
                    developerConnection.set("scm:git:ssh://github.com/stardust-enterprises/plat4k.git")
                    url.set("https://github.com/stardust-enterprises/plat4k")
                }
            }
        }
    }
    repositories {
        maven {
            credentials {
                username = NEXUS_USERNAME
                password = NEXUS_PASSWORD
            }

            name = "Sonatype"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
