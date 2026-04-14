plugins {
    java
    application
}

group = "com.r11s.spring.batch"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.batch:spring-batch-core:5.2.2")
    implementation("com.h2database:h2:2.3.232")

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.batch:spring-batch-test:5.2.2")
}

application {
    mainClass = "org.springframework.batch.core.launch.support.CommandLineJobRunner"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
