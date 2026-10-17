plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "pe.fullstack"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}
//implementando codigo variable para mapstruc y lombok
val mapstructVersion = "1.7.0.Beta1"
val lombokVersion = "1.18.30"
val lombokMapstructBindingVersion = "0.2.0"
//cierre de codigo para libreria mapstruck y lombok

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")//1era libreria para el spring y el rest
	implementation("org.springframework.boot:spring-boot-starter-validation")//2da libreria para validar
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("org.mapstruct:mapstruct:${mapstructVersion}")//3era libreria implementando mapstruck
	implementation("org.projectlombok:lombok:${lombokVersion}")//4ta libreria implementando lombok
	annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
	annotationProcessor("org.projectlombok:lombok:${lombokVersion}")
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:${lombokMapstructBindingVersion}")

}

tasks.withType<Test> {
	useJUnitPlatform()
}
