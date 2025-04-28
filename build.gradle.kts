plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta12"
}

group = "net.thenextlvl.arkitektonika"
version = "2.1.4"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.compileJava {
    options.release.set(21)
}

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")
    implementation("io.javalin:javalin:6.6.0")

    implementation("com.google.code.gson:gson:2.13.1")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    implementation("net.thenextlvl.core:version-checker:2.0.2")
    implementation("net.thenextlvl.core:files:2.0.3")
    implementation("net.thenextlvl.core:utils:1.1.0")
    implementation("net.thenextlvl.core:nbt:2.3.2")
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