plugins {
    application
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.diffplug.spotless") version "7.0.2"
}

group = "app.ciserver"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.15.2")
    implementation("io.javalin:javalin:6.4.0")
    implementation("io.javalin:javalin-rendering:6.4.0")
    implementation("org.thymeleaf:thymeleaf:3.1.3.RELEASE")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}


application {
    mainClass = "app.ciserver.Main"
}

tasks.named<JavaExec>("run") {
    val env = System.getenv("JAVA_ENV") ?: "dev"
    environment("JAVA_ENV", env)
}

tasks.test {
    useJUnitPlatform()
}

spotless {

    java {
        importOrder()
        removeUnusedImports()

        eclipse()
            .sortMembersEnabled(true)

        formatAnnotations()
    }
}
