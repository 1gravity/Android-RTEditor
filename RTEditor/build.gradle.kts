plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("maven-publish")
    id("signing")
}

android {
    compileSdk = Build.compileSdkVersion
    buildToolsVersion = Build.buildToolsVersion

    defaultConfig {
        minSdk = Build.minSdkVersion
        targetSdk = Build.targetSdkVersion
    }

    configurations {
        all {
            // to prevent two lint errors:
            // 1) commons-logging defines classes that conflict with classes now provided by Android
            // 2) httpclient defines classes that conflict with classes now provided by Android
            exclude("org.apache.httpcomponents", "httpclient")
        }
    }

    lint {
        isAbortOnError = true
        disable("UnusedResources")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("com.1gravity:android-colorpicker:2.2.2")
    implementation("org.greenrobot:eventbus:3.1.1")
    implementation("androidx.appcompat:appcompat:1.3.1")
}

tasks {
    val sourceFiles = android.sourceSets.getByName("main").java.srcDirs

    register<Javadoc>("withJavadoc") {
        isFailOnError = false

        // the code needs to be compiled before we can create the Javadoc
        dependsOn(android.libraryVariants.toList().last().javaCompileProvider)

        if (! project.plugins.hasPlugin("org.jetbrains.kotlin.android")) {
            setSource(sourceFiles)
        }

        // add Android runtime classpath
        android.bootClasspath.forEach { classpath += project.fileTree(it) }

        // add classpath for all dependencies
        android.libraryVariants.forEach { variant ->
            variant.javaCompileProvider.get().classpath.files.forEach { file ->
                classpath += project.fileTree(file)
            }
        }

        // We don't need javadoc for internals.
        exclude("**/internal/*")

        // Append Java 8 and Android references
        val options = options as StandardJavadocDocletOptions
        options.links("https://developer.android.com/reference")
        options.links("https://docs.oracle.com/javase/8/docs/api/")

        // Workaround for the following error when running on on JDK 9+
        // "The code being documented uses modules but the packages defined in ... are in the unnamed module."
        if (JavaVersion.current() >= JavaVersion.VERSION_1_9) {
            options.addStringOption("-release", "8")
        }
    }

    register<Jar>("withJavadocJar") {
        archiveClassifier.set("javadoc")
        dependsOn(named("withJavadoc"))
        val destination = named<Javadoc>("withJavadoc").get().destinationDir
        from(destination)
    }

    register<Jar>("withSourcesJar") {
        archiveClassifier.set("sources")
        from(sourceFiles)
    }
}

afterEvaluate {
    fun Project.getRepositoryUrl(): java.net.URI {
        val isReleaseBuild = properties["POM_VERSION_NAME"]?.toString()?.contains("SNAPSHOT") == false
        val releaseRepoUrl = properties["RELEASE_REPOSITORY_URL"]?.toString() ?: "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
        val snapshotRepoUrl = properties["SNAPSHOT_REPOSITORY_URL"]?.toString() ?: "https://oss.sonatype.org/content/repositories/snapshots/"
        return uri(if (isReleaseBuild) releaseRepoUrl else snapshotRepoUrl)
    }

    publishing {
        publications {
            val props = project.properties

            // 1. configure repositories
            repositories {
                maven {
                    url = getRepositoryUrl()
                    // credentials are stored in ~/.gradle/gradle.properties with ~ being the path of the home directory
                    credentials {
                        username = props["ossUsername"]?.toString() ?: throw IllegalStateException("ossUsername not found")
                        password = props["ossPassword"]?.toString() ?: throw IllegalStateException("ossPassword not found")
                    }
                }
            }

            // 2. configure publication
            val publicationName = props["POM_NAME"]?.toString() ?: "publication"
            create<MavenPublication>(publicationName) {
                from(project.components["release"])
                artifact(tasks.named<Jar>("withJavadocJar"))
                artifact(tasks.named<Jar>("withSourcesJar"))

                pom {
                    groupId = props["POM_GROUP_ID"].toString()
                    artifactId = props["POM_ARTIFACT_ID"].toString()
                    version = props["POM_VERSION_NAME"].toString()

                    name.set(props["POM_NAME"].toString())
                    description.set(props["POM_DESCRIPTION"].toString())
                    url.set(props["POM_URL"].toString())
                    packaging = props["POM_PACKAGING"].toString()

                    scm {
                        url.set(props["POM_SCM_URL"].toString())
                        connection.set(props["POM_SCM_CONNECTION"].toString())
                        developerConnection.set(props["POM_SCM_DEV_CONNECTION"].toString())
                    }

                    organization {
                        name.set(props["POM_COMPANY_NAME"].toString())
                        url.set(props["POM_COMPANY_URL"].toString())
                    }

                    developers {
                        developer {
                            id.set(props["POM_DEVELOPER_ID"].toString())
                            name.set(props["POM_DEVELOPER_NAME"].toString())
                            email.set(props["POM_DEVELOPER_EMAIL"].toString())
                        }
                    }

                    licenses {
                        license {
                            name.set(props["POM_LICENCE_NAME"].toString())
                            url.set(props["POM_LICENCE_URL"].toString())
                            distribution.set(props["POM_LICENCE_DIST"].toString())
                        }
                    }
                }
            }

            // 3. sign the artifacts
            signing {
                val signingKeyId = props["signingKeyId"]?.toString() ?: throw IllegalStateException("signingKeyId not found")
                val signingKeyPassword = props["signingKeyPassword"]?.toString() ?: throw IllegalStateException("signingKeyPassword not found")
                val signingKey = props["signingKey"]?.toString() ?: throw IllegalStateException("signingKey not found")
                useInMemoryPgpKeys(signingKeyId, signingKey, signingKeyPassword)
                sign(publishing.publications.getByName(publicationName))
            }
        }
    }
}
