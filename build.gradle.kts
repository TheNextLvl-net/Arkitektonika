plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.1"
}

group = "net.thenextlvl.arkitektonika"
version = "2.1.4"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.compileJava {
    options.release.set(25)
}

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.thenextlvl.net/snapshots")
}

dependencies {
    compileOnly("org.jspecify:jspecify:1.0.0")

    implementation("org.xerial:sqlite-jdbc:3.51.1.0")
    implementation("io.javalin:javalin:6.7.0")

    implementation("com.google.code.gson:gson:2.13.2")
    implementation("ch.qos.logback:logback-classic:1.5.29")

    implementation("net.thenextlvl.version-checker:github:1.0.1")
    implementation("net.thenextlvl.core:files:4.0.0-pre1")
    implementation("net.thenextlvl:nbt:4.3.4")
}

tasks.shadowJar {
    archiveFileName.set("arkitektonika.jar")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "net.thenextlvl.arkitektonika.Arkitektonika"
        attributes["Implementation-Version"] = version
    }
}