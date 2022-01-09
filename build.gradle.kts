@file:Suppress("UNUSED_VARIABLE")

plugins {
    `java-library`
    kotlin("jvm")
    id("org.jetbrains.dokka")
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
}

group = "fr.stardustenterprises"
version = "1.4.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("net.java.dev.jna:jna:5.10.0")
}

tasks {
    dokkaHtml {
        dokkaSourceSets {
            configureEach {
                skipDeprecated.set(true)
                reportUndocumented.set(true)
            }
        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    /* Artifacts */

    // Source artifact, including everything the 'main' does but not compiled.
    val sourcesJar by registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    val javadocJar by registering(Jar::class) {
        archiveClassifier.set("javadoc")
        dependsOn(dokkaHtml)
        from(dokkaHtml)
    }
}

val artifactTasks = arrayOf(
    tasks["sourcesJar"],
    tasks["javadocJar"]
)

artifacts {
    artifactTasks.forEach(::archives)
}

val projectName = project.name
val desc = "Platform identifier library for the JVM."
val authors = arrayOf("xtrm", "lambdagg")
val repo = "stardust-enterprises/$projectName"

publishing.publications {
    create<MavenPublication>("mavenJava") {
        from(components["java"])
        artifactTasks.forEach(::artifact)

        pom {
            name.set(projectName)
            description.set(desc)
            url.set("https://github.com/$repo")

            licenses {
                license {
                    name.set("ISC License")
                    url.set("https://opensource.org/licenses/ISC")
                    distribution.set("repo")
                }
            }

            developers {
                authors.forEach {
                    developer {
                        id.set(it)
                        name.set(it)
                    }
                }
            }

            scm {
                connection.set("scm:git:git://github.com/$repo.git")
                developerConnection.set("scm:git:ssh://github.com/$repo.git")
                url.set("https://github.com/$repo")
            }
        }

        // Configure the signing extension to sign this Maven artifact.
        signing.sign(this)
    }
}

// Set up the Sonatype artifact publishing.
nexusPublishing.repositories.sonatype {
    nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
    snapshotRepositoryUrl.set(
        uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    )

    // Skip this step if environment variables NEXUS_USERNAME or NEXUS_PASSWORD aren't set.
    username.set(properties["NEXUS_USERNAME"] as? String ?: return@sonatype)
    password.set(properties["NEXUS_PASSWORD"] as? String ?: return@sonatype)
}
