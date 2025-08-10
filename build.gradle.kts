import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import java.io.ByteArrayOutputStream
import java.time.LocalDate

fun properties(key: String) = providers.gradleProperty(key)

fun environment(key: String) = providers.environmentVariable(key)

fun canCreateTag(): Boolean = properties("createTag").map { it.toBoolean() }.getOrElse(true)

plugins {
	// Java support
	id("java")
	alias(libs.plugins.kotlin)
	alias(libs.plugins.intelliJPlatform)
	alias(libs.plugins.changelog)
	alias(libs.plugins.detekt)
	checkstyle
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()
val jdkVersion = 21

println("ArtifactVersion is : ${properties("pluginVersion").get()}")
println("Java version is : ${System.getProperty("java.version")}")
println("Java runtime version is : ${System.getProperty("java.runtime.version")}")

// Set the JVM language level used to build the project.
kotlin {
	jvmToolchain(jdkVersion)
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(jdkVersion))
	}
}

// Configure project's dependencies
repositories {
	mavenCentral()

	// IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
	intellijPlatform {
		defaultRepositories()
	}
}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {
	compileOnly(libs.lombok)
	annotationProcessor(libs.lombok)

	detektPlugins(libs.detektFormatting)

	// IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
	intellijPlatform {
		create(properties("platformType").get(), properties("platformVersion").get())

		// Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
		bundledPlugins(properties("platformBundledPlugins").map { it.split(',') })

		// Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
		plugins(properties("platformPlugins").map { it.split(',') })

		pluginVerifier()
		zipSigner()
		testFramework(TestFrameworkType.Platform)
	}
}

// Configure IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
	pluginConfiguration {
		name = properties("pluginName").get()
		version = properties("pluginVersion").get()

		// Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
		description =
			providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
				val start = "<!-- Plugin description -->"
				val end = "<!-- Plugin description end -->"

				with(it.lines()) {
					if (!containsAll(listOf(start, end))) {
						throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
					}
					subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
				}
			}


		val changelog = project.changelog // local variable for configuration cache compatibility
		// Get the latest available change notes from the changelog file
		changeNotes =
			properties("pluginVersion").map { pluginVersion ->
				with(changelog) {
					renderItem(
						(getOrNull(pluginVersion) ?: getUnreleased()).withHeader(false),
						Changelog.OutputType.HTML,
					)
				}
			}

		ideaVersion {
			sinceBuild = properties("pluginSinceBuild")
			untilBuild = provider { null }
		}
	}

	publishing {
		if (File("token.txt").exists()) {
			token = File("token.txt").readText(Charsets.UTF_8)
		}

		// pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
		// Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
		// https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
		channels = properties("pluginVersion").map {
			listOf(
				it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" })
		}
	}

	// https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html#intellijPlatform-pluginVerification-ides
	pluginVerification {
		ides {
			recommended()
		}
	}
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
	version = properties("pluginVersion").get()
	header = provider { "${version.get()} - ${LocalDate.now()}" }
	itemPrefix = "-"
	keepUnreleasedSection = true
	groups = listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security")
}

// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
	config.setFrom("./detekt-config.yml")
	buildUponDefaultConfig = true
}

checkstyle {
	toolVersion = "10.3.3"
}

tasks {
	wrapper {
		gradleVersion = properties("gradleVersion").get()
	}

	withType<Detekt> {
		jvmTarget = "$jdkVersion"
		reports {
			// Enable/Disable XML report (default: true)
			xml.required = false
			xml.outputLocation = file("build/reports/detekt.xml")
			// Enable/Disable HTML report (default: true)
			html.required = true
			html.outputLocation = file("build/reports/detekt.html")
			// Enable/Disable TXT report (default: true)
			txt.required = false
			txt.outputLocation = file("build/reports/detekt.txt")
		}
	}

	withType<Checkstyle>().configureEach {
		configFile = file("config/checkstyle/checkstyle.xml")
	}

	runIde {
//        jvmArgs.add("-Didea.ProcessCanceledException=disabled")
//        systemProperty 'idea.auto.reload.plugins', false
		systemProperty("eye.debug.run", true)
	}

	val gitCheckStatus by register<Exec>("gitCheckStatus") {
		description = "Check git working directory status"
		group = PublishingPlugin.PUBLISH_TASK_GROUP

		outputs.upToDateWhen { false }

		commandLine("git", "status", "--porcelain")
		doFirst {
			standardOutput = ByteArrayOutputStream()
		}

		doLast {
			val output = standardOutput.toString().trim()
			check(output.isEmpty()) {
				"Workspace is dirty. Please commit or stash changes:\n$output"
			}
			println("Git workspace is clean")
		}
	}

	val gitCheckTag by register<Exec>("gitCheckTag") {
		description = "Check git tag if exists"
		group = PublishingPlugin.PUBLISH_TASK_GROUP
		dependsOn(gitCheckStatus)
		outputs.upToDateWhen { false }

		val canCreateTag = canCreateTag()
		onlyIf { canCreateTag }

		val tagName = "v${properties("pluginVersion").get()}"
		commandLine("git", "tag", "-l", tagName)
		isIgnoreExitValue = true

		doFirst {
			standardOutput = ByteArrayOutputStream()
		}
		doLast {
			val output = standardOutput.toString().trim()
			check(output.isEmpty()) {
				"Git tag $tagName already exists\n$output"
			}
		}
	}

	val gitCreateTag by register<Exec>("gitCreateTag") {
		description = "Create git tag"
		group = PublishingPlugin.PUBLISH_TASK_GROUP
		dependsOn(gitCheckTag)

		outputs.upToDateWhen { false }

		val tagName = "v${properties("pluginVersion").get()}"
		commandLine("git", "tag", "-a", tagName, "-m", "Gradle created tag for $tagName")

		doLast {
			println("Created git tag: $tagName")
		}
	}

	val gitPushTag by register<Exec>("gitPushTag") {
		description = "Push git tag to origin"
		group = PublishingPlugin.PUBLISH_TASK_GROUP
		dependsOn(gitCreateTag)

		val tagName = "v${properties("pluginVersion").get()}"
		commandLine("git", "push", "origin", "refs/tags/$tagName")

		doLast {
			println("Pushed tag $tagName to origin")
		}
	}

	publishPlugin {
		dependsOn(patchChangelog)
		finalizedBy(gitPushTag)
	}
}

intellijPlatformTesting {
	runIde {
		register("runIdeForUiTests") {
			task {
				jvmArgumentProviders +=
					CommandLineArgumentProvider {
						listOf(
							"-Drobot-server.port=8082",
							"-Dide.mac.message.dialogs.as.sheets=false",
							"-Djb.privacy.policy.text=<!--999.999-->",
							"-Djb.consents.confirmation.enabled=false",
						)
					}
			}

			plugins {
				robotServerPlugin()
			}
		}
	}
}
