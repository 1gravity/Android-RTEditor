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
    implementation("org.greenrobot:eventbus:3.1.1")
    implementation("androidx.appcompat:appcompat:1.3.1")
}

afterEvaluate {
    publishing {
        repositories(project)
        publications {
            val publicationName = project.properties["POM_NAME"]?.toString() ?: "publication"
            create<MavenPublication>(publicationName) {
                configure(project)
            }
            signing {
                sign(publishing.publications.getByName(publicationName))
            }
        }
    }
}
