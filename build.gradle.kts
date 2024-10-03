plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.3"
}

group = "net.thenextlvl.arkitektonika"
version = "2.1.2"

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
    compileOnly("org.jetbrains:annotations:25.0.0")
    compileOnly("org.projectlombok:lombok:1.18.34")

    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
    implementation("io.javalin:javalin:6.3.0")

    implementation("com.google.code.gson:gson:2.11.0")
    implementation("ch.qos.logback:logback-classic:1.5.8")

    implementation("net.thenextlvl.core:version-checker:1.2.1")
    implementation("net.thenextlvl.core:annotations:2.0.1")
    implementation("net.thenextlvl.core:files:1.0.5")
    implementation("net.thenextlvl.core:utils:1.0.9")
    implementation("net.thenextlvl.core:nbt:1.4.2")

    annotationProcessor("org.projectlombok:lombok:1.18.34")
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