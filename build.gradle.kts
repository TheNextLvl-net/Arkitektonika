plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "net.thenextlvl.arkitektonika"
version = "1.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("org.projectlombok:lombok:1.18.32")

    implementation("org.xerial:sqlite-jdbc:3.41.2.2")
    implementation("com.sparkjava:spark-core:2.9.4")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("ch.qos.logback:logback-classic:1.5.6")

    implementation("net.thenextlvl.core:annotations:2.0.1")
    implementation("net.thenextlvl.core:files:1.0.4")
    implementation("net.thenextlvl.core:utils:1.0.9")
    implementation("net.thenextlvl.core:nbt:1.4.1")

    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

tasks.shadowJar {
    archiveFileName.set("arkitektonika.jar")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "net.thenextlvl.arkitektonika.Arkitektonika"
        attributes["Version"] = version
    }
}