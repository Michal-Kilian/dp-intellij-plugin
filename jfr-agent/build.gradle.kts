plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "sk.fiit.dp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.google.code.gson:gson:2.13.2")
}

tasks.jar {
    manifest {
        attributes(
            "Premain-Class" to "sk.fiit.dp.JFRAgent",
            "Agent-Class" to "sk.fiit.dp.JFRAgent",
            "Can-Redefine-Classes" to "false",
            "Can-Retransform-Classes" to "false",
        )
    }
}

tasks.test {
    useJUnitPlatform()
}