import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "com.alttd"
version = "1.0.0-SNAPSHOT"
description = "ProxyPlaytime plugin."

apply<JavaLibraryPlugin>()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
    }

    withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }

    shadowJar {
        dependsOn(getByName("relocateJars") as ConfigureShadowRelocation)
        archiveFileName.set("${project.name}-${project.version}.jar")
//        minimize()
        configurations = listOf(project.configurations.shadow.get())
    }

    build {
        dependsOn(shadowJar)
    }

    create<ConfigureShadowRelocation>("relocateJars") {
        target = shadowJar.get()
        prefix = "${project.name}.lib"
    }
}

dependencies {
    // Velocity
    compileOnly("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT") // Velocity
    annotationProcessor("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")
    shadow("mysql:mysql-connector-java:8.0.27") // mysql
    compileOnly("net.luckperms:api:5.4") // luckperms
}