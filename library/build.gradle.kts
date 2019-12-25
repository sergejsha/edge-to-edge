plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("org.gradle.signing")
    id("org.jetbrains.dokka") version Version.dokka
}

android {
    compileSdkVersion(29)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = Pom.version
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(Libraries.stdlib)
    implementation(Libraries.appCompat)
}

publishing {

    repositories {
        maven {
            name = "local"
            url = uri("$buildDir/repository")
        }
        maven {
            name = Pom.MavenCentral.name
            url = uri(Pom.MavenCentral.url)
            credentials {
                username = project.getNexusUser()
                password = project.getNexusPassword()
            }
        }
    }

    val dokka by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class) {
        outputFormat = "javadoc"
        outputDirectory = "$buildDir/javadoc"
    }

    val sourcesJar by tasks.creating(Jar::class) {
        archiveClassifier.set("sources")
        from(android.sourceSets["main"].java.srcDirs)
    }

    val javadocJar by tasks.creating(Jar::class) {
        archiveClassifier.set("javadoc")
        from(dokka)
    }

    publications {
        create("Release", MavenPublication::class) {
            group = Pom.group
            artifactId = Pom.artifactId
            version = Pom.version

            artifact(sourcesJar)
            artifact(javadocJar)
            artifact("$buildDir/outputs/aar/${project.name}-release.aar")

            pom {
                url.set(Pom.url)
                name.set(Pom.name)
                description.set(Pom.description)
                licenses {
                    license {
                        name.set(Pom.License.name)
                        url.set(Pom.License.url)
                    }
                }
                developers {
                    developer {
                        id.set(Pom.Developer.id)
                        name.set(Pom.Developer.name)
                        email.set(Pom.Developer.email)
                    }
                }
                scm {
                    url.set(Pom.Github.url)
                    connection.set(Pom.Github.url)
                    developerConnection.set(Pom.Github.cloneUrl)
                }
            }
        }
    }
}

if (project.hasSigningKey()) {
    signing {
        sign(publishing.publications["Release"])
    }
}

fun Project.getNexusUser() = this.findProperty("NEXUS_USERNAME") as? String ?: ""
fun Project.getNexusPassword() = this.findProperty("NEXUS_PASSWORD") as? String ?: ""
fun Project.hasSigningKey() = this.hasProperty("signing.keyId")
