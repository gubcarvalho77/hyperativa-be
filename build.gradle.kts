plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "8.13.1"
    id("jacoco")
}

group = "com.hyperativa"
version = "1.0.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
}

extra.apply {
    set("mapStructVersion", "1.6.3")
    set("mapstructBindingVersion", "0.2.0")
    set("authJavaJwt", "4.5.0")
    set("apacheCommons", "1.10.0")
    set("openapi", "2.8.14")
}

val appName = rootProject.name

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("openapi")}")

    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.liquibase:liquibase-core")

    implementation("commons-validator:commons-validator:${property("apacheCommons")}")
    implementation("com.auth0:java-jwt:${property("authJavaJwt")}")

    implementation("org.mapstruct:mapstruct:${property("mapStructVersion")}")
    implementation("org.projectlombok:lombok-mapstruct-binding:${property("mapstructBindingVersion")}")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.mapstruct:mapstruct-processor:${property("mapStructVersion")}");

    runtimeOnly("org.postgresql:postgresql")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.3")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.annotationProcessorPath = configurations.annotationProcessor.get()
    options.compilerArgs.addAll(listOf(
        "-parameters",
        "-Amapstruct.defaultComponentModel=spring"
    ))
}


sourceSets {
    main {
        resources {
            srcDir("build/generated-resources")
            exclude("**/*.tpl")
        }
    }
}

jacoco {
    toolVersion = "0.8.10"
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }
}
