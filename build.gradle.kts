plugins {
    java
    id("com.gradleup.shadow") version "9.4.1"
}

group = "com.monkey"
version = "3.0.0"
description = "KT"

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
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.16")
    implementation("org.bstats:bstats-bukkit:3.2.1")
    implementation("org.reflections:reflections:0.10.2")
    compileOnly("net.luckperms:api:5.5")
    compileOnly("org.json:json:20251224")
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("com.zaxxer:HikariCP:7.0.2")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
    exclude("com/monkey/kt/utils/discord/security/Main.java")
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand("version" to project.version)
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
