plugins {
    id("java")
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("core.CLI")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("info.picocli:picocli:4.7.7")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("benchmark") {
    group = "application"
    description = "Runs the PPL inference benchmarks."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("core.Benchmark")
}
tasks.jar {
    archiveFileName.set("java-ppl.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "core.CLI"
    }
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
}