plugins {
    kotlin("jvm") version "1.9.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
}

group = "de.tectoast"
version = "3.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(20)
}



dependencies {
    compileOnly("net.dv8tion:JDA:5.0.0-beta.18")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}


publishing {
    publications {
        create<MavenPublication>("emolga") {
            from(components["java"])
        }
    }
}
