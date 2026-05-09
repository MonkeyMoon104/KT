plugins {
    java
    alias(libs.plugins.shadow)
}

group = "com.monkey"
description = "KT"
version = providers.gradleProperty("version").get()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.worldguard.bukkit)
    implementation(libs.bstats.bukkit)
    implementation(libs.reflections)
    compileOnly(libs.luckperms.api)
    compileOnly(libs.json)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.vault.api)
    compileOnly(libs.hikari)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.processResources {
    filteringCharset = "UTF-8"
    val pluginProperties = mapOf("version" to version.toString())
    inputs.properties(pluginProperties)
    filesMatching("plugin.yml") {
        expand(pluginProperties)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("org.bstats", "com.monkey.kt.bstats")
    relocate("org.reflections", "com.monkey.kt.reflections")
}

tasks.jar {
    archiveClassifier.set("plain")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
