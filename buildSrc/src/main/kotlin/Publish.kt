import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.get
import java.net.URI

fun PublishingExtension.repositories(project: Project) {
    repositories {
        maven {
            url = URI(project.getRepositoryUrl())
            // credentials are stored in ~/.gradle/gradle.properties with ~ being the path of the home directory
            credentials {
                username = project.properties["oss.username"]?.toString()
                        ?: throw IllegalStateException("oss.username not found")
                password = project.properties["oss.password"]?.toString()
                        ?: throw IllegalStateException("oss.password not found")
            }
        }
    }
}

fun MavenPublication.configure(project: Project, vararg artifacts: TaskProvider<Jar>) {
    val props: MutableMap<String, *> = project.properties
    groupId = props["POM_GROUP_ID"].toString()
    artifactId = props["POM_ARTIFACT_ID"].toString()
    version = props["POM_VERSION_NAME"].toString()

    from(project.components["release"])
    artifacts.forEach { artifact(it) }

    pom {
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

private fun Project.isReleaseBuild() = properties["POM_VERSION_NAME"]?.toString()
        ?.contains("SNAPSHOT") == false

private fun Project.getReleaseRepositoryUrl() = properties["RELEASE_REPOSITORY_URL"]?.toString()
        ?: "https://oss.sonatype.org/service/local/staging/deploy/maven2/"

private fun Project.getSnapshotRepositoryUrl() = properties["SNAPSHOT_REPOSITORY_URL"]?.toString()
        ?: "https://oss.sonatype.org/content/repositories/snapshots/"

private fun Project.getRepositoryUrl() = if (isReleaseBuild()) getReleaseRepositoryUrl() else
    getSnapshotRepositoryUrl()
