import java.net.URL

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
        val moduleFile = File(projectDir, "MODULE.temp.MD")

        run {
            // In order to have a description on the rendered docs, we have to have
            // a file with the # Module thingy in it. That's what we're
            // automagically creating here.

            doFirst {
                moduleFile.writeText("# Module $projectName\n$desc")
            }

            doLast {
                moduleFile.delete()
            }
        }

        moduleName.set(projectName)

        dokkaSourceSets.configureEach {
            displayName.set("$projectName github")
            includes.from(moduleFile.path)

            skipDeprecated.set(false)
            includeNonPublic.set(false)
            skipEmptyPackages.set(true)
            reportUndocumented.set(true)
            suppressObviousFunctions.set(true)

            // Link the source to the documentation
            sourceLink {
                localDirectory.set(file("src"))
                remoteUrl.set(URL("https://github.com/$repo/tree/trunk/src"))
            }

            // JNA external documentation links
            externalDocumentationLink {
                url.set(URL("https://javadoc.io/doc/net.java.dev.jna/jna/5.10.0/"))
            }
        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    /* Artifacts */

    // Source artifact, including everything the 'main' does but not compiled.
    create("sourcesJar", Jar::class) {
        group = "build"

        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    create("javadocJar", Jar::class) {
        group = "build"

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
