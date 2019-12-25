plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("org.gradle.signing")
    id("org.jetbrains.dokka") version "0.10.0"
}

android {
    compileSdkVersion(29)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.61")
    implementation("androidx.appcompat:appcompat:1.1.0")
    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test.ext:junit:1.1.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.1")
}

publishing {

    repositories {
        maven {
            name = "local"
            url = uri("$buildDir/repository")
        }
        maven {
            name = "central"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
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

            group = "de.halfbit"
            artifactId = "edge-to-edge"
            version = "0.1"

            artifact(sourcesJar)
            artifact(javadocJar)
            artifact("$buildDir/outputs/aar/${project.getName()}-release.aar")

            pom {
                name.set("Edge-to-Edge")
                description.set("Android library for enabling edge-to-edge content and insetting views using simple Kotlin DSL")
                url.set("http://www.halfbit.de")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("beworker")
                        name.set("Sergej Shafarenka")
                        email.set("info@halfbit.de")
                    }
                }
                scm {
                    connection.set("https://github.com/beworker/knot")
                    developerConnection.set("scm:git:ssh://github.com:beworker/knot.git")
                    url.set("https://github.com/beworker/knot")
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