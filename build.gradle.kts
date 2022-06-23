import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
}

group = "me.snwy"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("org.jline:jline-builtins:3.21.0")
    implementation("org.jline:jline-reader:3.21.0")
    implementation("org.jline:jline-terminal:3.21.0")
    implementation("org.knowm.xchart:xchart:3.8.1")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.14.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("oasis.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "me.snwy.oasis.MainKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile)) {
            exclude("META-INF/*.RSA", "META-INF/*.DSA", "META-INF/*.SF", "META-INF/*.EC")
        }
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
