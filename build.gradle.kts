plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "net.thenextlvl.arkitektonika"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.0")
    compileOnly("org.projectlombok:lombok:1.18.30")

    implementation("org.xerial:sqlite-jdbc:3.7.2")
    implementation("com.sparkjava:spark-core:2.9.4")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("ch.qos.logback:logback-classic:1.2.6")


    implementation("net.thenextlvl.core:annotations:2.0.1")
    implementation("net.thenextlvl.core:files:1.0.3")
    implementation("net.thenextlvl.core:utils:1.0.8")
    implementation("net.thenextlvl.core:nbt:1.3.9")

    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "net.thenextlvl.arkitektonika.Arkitektonika"
        attributes["Version"] = version
    }
}