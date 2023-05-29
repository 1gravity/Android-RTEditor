plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("maven-publish")
    id("signing")
}

android {
    namespace = "com.onegravity.rteditor"

    compileSdk = Build.compileSdkVersion

    defaultConfig {
        minSdk = Build.minSdkVersion
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
        abortOnError = true
        disable += "UnusedResources"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    implementation("com.1gravity:android-colorpicker:_")
    implementation("org.greenrobot:eventbus:_")
    implementation(AndroidX.appCompat)
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
    fun Project.get(name: String, def: String = "$name not found") =
            properties[name]?.toString() ?: System.getenv(name) ?: def

    fun Project.getRepositoryUrl(): java.net.URI {
        val isReleaseBuild = !get("POM_VERSION_NAME").contains("SNAPSHOT")
        val releaseRepoUrl = get("RELEASE_REPOSITORY_URL", "https://oss.sonatype.org/service/local/staging/deploy/maven2/")
        val snapshotRepoUrl = get("SNAPSHOT_REPOSITORY_URL", "https://oss.sonatype.org/content/repositories/snapshots/")
        return uri(if (isReleaseBuild) releaseRepoUrl else snapshotRepoUrl)
    }

    publishing {
        publications {
            // 1. configure repositories
            repositories {
                maven {
                    url = getRepositoryUrl()
                    // credentials are stored in ~/.gradle/gradle.properties with ~ being the path of the home directory
                    credentials {
                        username = project.get("ossUsername")
                        password = project.get("ossPassword")
                    }
                }
            }

            // 2. configure publication
            val publicationName = project.get("POM_NAME", "publication")
            create<MavenPublication>(publicationName) {
                from(project.components["release"])
                artifact(tasks.named<Jar>("withJavadocJar"))
                artifact(tasks.named<Jar>("withSourcesJar"))

                pom {
                    groupId = project.get("POM_GROUP_ID")
                    artifactId = project.get("POM_ARTIFACT_ID")
                    version = project.get("POM_VERSION_NAME")

                    name.set(project.get("POM_NAME"))
                    description.set(project.get("POM_DESCRIPTION"))
                    url.set(project.get("POM_URL"))
                    packaging = project.get("POM_PACKAGING")

                    scm {
                        url.set(project.get("POM_SCM_URL"))
                        connection.set(project.get("POM_SCM_CONNECTION"))
                        developerConnection.set(project.get("POM_SCM_DEV_CONNECTION"))
                    }

                    organization {
                        name.set(project.get("POM_COMPANY_NAME"))
                        url.set(project.get("POM_COMPANY_URL"))
                    }

                    developers {
                        developer {
                            id.set(project.get("POM_DEVELOPER_ID"))
                            name.set(project.get("POM_DEVELOPER_NAME"))
                            email.set(project.get("POM_DEVELOPER_EMAIL"))
                        }
                    }

                    licenses {
                        license {
                            name.set(project.get("POM_LICENCE_NAME"))
                            url.set(project.get("POM_LICENCE_URL"))
                            distribution.set(project.get("POM_LICENCE_DIST"))
                        }
                    }
                }
            }

            // 3. sign the artifacts
            signing {
                val signingKeyId = project.get("signingKeyId")
                val signingKeyPassword = project.get("signingKeyPassword")
                val signingKey = project.get("signingKey")
                useInMemoryPgpKeys(signingKeyId, signingKey, signingKeyPassword)
                sign(publishing.publications.getByName(publicationName))
            }
        }
    }
}
