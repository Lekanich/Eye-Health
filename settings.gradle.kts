rootProject.name = "Eye-Health"

pluginManagement {
	repositories {
		maven("https://oss.sonatype.org/content/repfixositories/snapshots/")
		gradlePluginPortal()
		mavenCentral()
		maven {
			name = "GitHubPackages"
			url = uri("https://maven.pkg.github.com/lekanich/GradlePlugins")
			credentials {
				username =
					providers.environmentVariable("GITHUB_ACTOR").orNull
						?: providers.environmentVariable("GH_ACTOR").orNull
				password =
					providers.environmentVariable("GITHUB_TOKEN").orNull
						?: providers.environmentVariable("GH_TOKEN").orNull
			}
		}
	}
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
