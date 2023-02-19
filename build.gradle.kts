import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    kotlin("jvm") version Dependency.Kotlin.Version
    id("io.papermc.paperweight.userdev") version "1.4.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly("io.papermc.paper:paper-api:${Dependency.Paper.Version}-R0.1-SNAPSHOT")
    Dependency.Libraries.Lib.forEach { compileOnly(it) }
    paperDevBundle("${Dependency.Paper.Version}-R0.1-SNAPSHOT")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    }
    processResources {
        filesMatching("**/*.yml") {
            expand(project.properties)
        }
        filteringCharset = "UTF-8"
    }
    reobfJar {
        outputJar.set(File(outputJar.get().asFile.parent, "${rootProject.name}.jar"))
    }

    register<Task>("generatePluginYml") {
        val resourcesDir = rootProject.file("src/main/resources").also { if (!it.exists()) it.mkdirs() }
        val pluginYml = File(resourcesDir, "plugin.yml").also { if (!it.exists()) it.createNewFile() }

        pluginYml.writeText(
            """
name: Hardcore
version: ${rootProject.properties["version"]}
main: ${rootProject.properties["group"]}.${rootProject.properties["codeName"]}.plugin.${rootProject.properties["codeName"].toString().capitalize()}Plugin
api-version: ${Dependency.Paper.API}
libraries:
${Dependency.Libraries.LibCore.joinToString("\n") { " - $it" }}
        """.trimIndent()
        )
    }

    register<Copy>("paperJar") {
        dependsOn(rootProject.tasks.findByName("generatePluginYml"))

        val prefix = rootProject.name
        val plugins = rootProject.file(".server/plugins")
        val update = File(plugins, "update")
        val regex = Regex("($prefix).*(.jar)")

        from(reobfJar)
        into(if (plugins.listFiles { _, it -> it.matches(regex) }?.isNotEmpty() == true) update else plugins)

        doLast {
            update.mkdirs()
            File(update, "RELOAD").delete()
        }
    }
}

idea {
    module {
        excludeDirs.addAll(listOf(file(".server"), file("out")))
    }
}