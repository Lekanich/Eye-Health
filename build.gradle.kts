import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.internal.provider.sources.GradlePropertyValueSource
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    // Java support
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intellij)
    alias(libs.plugins.changelog)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    checkstyle
}

group = properties("pluginGroup")
version = properties("pluginVersion")

println("ArtifactVersion is : ${properties("pluginVersion").get()}")

// Configure project's dependencies
repositories {
    mavenCentral()
}
dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    detektPlugins(libs.detektFormatting)
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName = properties("pluginName")
    version = properties("platformVersion")
    type = properties("platformType")
    downloadSources = properties("platformDownloadSources").map { it.toBoolean() }
    updateSinceUntilBuild = true

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins = properties("platformPlugins")
        .map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}

// Configure gradle-changelog-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version = properties("pluginVersion")
    header = provider(version::get)
    itemPrefix = "-"
    keepUnreleasedSection = false
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
    // Set the compatibility versions to 1.8
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    withType<Detekt> {
        jvmTarget = "17"
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
        reports {
            configFile = file("config/checkstyle/checkstyle.xml")
        }
    }

    patchPluginXml {
        dependsOn("patchChangelog")
        doLast {
            println("I'm Gradle")
        }

        version = properties("pluginVersion")
        // like to put a major version here, instead of the specific
        sinceBuild = properties("pluginSinceBuild")
        // remove until build
        // Maybe it's not very safe but the plugin is simple, and it shouldn't require an every platform release update.
        untilBuild = ""

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription = File(projectDir, "README.MD").readText().lines().run {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            if (!containsAll(listOf(start, end))) {
                throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
            }
            subList(indexOf(start) + 1, indexOf(end))
        }.joinToString("\n").run { markdownToHTML(this) }

        // Get the latest available change notes from the changelog file
        changeNotes = provider {
            changelog.getAll()
                .filterKeys { it != "[Unreleased]" }
                .values.joinToString("") {
                    changelog.renderItem(it.withHeader(true), Changelog.OutputType.HTML)
                }
        }
    }

    runIde {
//        jvmArgs.add("-Didea.ProcessCanceledException=disabled")
//        systemProperty 'idea.auto.reload.plugins', false
        systemProperty("eye.debug.run", true)
    }

    // TODO: signPlugin

//    //https://data.services.jetbrains.com/products?fields=code,name,releases.downloads,releases.version,releases.build,releases.type&code=IIC,IIU
    runPluginVerifier {
        ideVersions = properties("pluginVerifierIdeVersions").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
    }

    publishPlugin {
        dependsOn("patchChangelog")
        if (File("token.txt").exists()) {
            token = File("token.txt").readText(Charsets.UTF_8)
        }
//        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = properties("pluginVersion").map { listOf(it.split('-').getOrElse(1) { "default" }.split('.').first()) }
    }
}
