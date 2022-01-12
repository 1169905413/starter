import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
  import org.jetbrains.kotlin.gradle.tasks.KotlinCompile



//import com.github.jengelman.gradle.plugins.shadow.tasks.

plugins {
  application
  kotlin ("jvm") version "1.3.72"
  id("com.github.johnrengelman.shadow") version "5.2.0"
}


val kotlinVersion = "1.3.72"
val vertxVersion = "3.9.4"
val junitJupiterVersion = "5.6.0"
group = "com.lj"
version = "1.0.0-SNAPSHOT"

repositories {
//  maven (url= "https://maven.aliyun.com/repository/public" )
//  maven (url= "http://mirrors.cloud.tencent.com/nexus/repository/maven-public/" )

//  maven {
//    url = uri("https://maven.aliyun.com/repository/public")
//  }
  mavenCentral()
  jcenter()
}



var mainVerticleName = "com.lj.starter.MainVerticle"
val watchForChange = "src/**/*"
val doOnChange = ".\\gradlew classes"
val launcherClassName = "io.vertx.core.Launcher"

application {
  mainClassName = launcherClassName
}

dependencies {
  implementation("io.vertx:vertx-web:$vertxVersion")
  implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
  implementation("org.influxdb:influxdb-java:2.21")
  implementation("com.influxdb:influxdb-client-java:1.13.0")


  //  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.1")

  implementation("net.java.dev.jna","jna","5.6.0")
  implementation("net.java.dev.jna:jna-platform:5.6.0")
  implementation("io.github.java-native","jssc","2.9.2")

  implementation(kotlin("stdlib-jdk8"))
  implementation( files("lib/Automation.BDaq.jar"))



  testImplementation("io.vertx:vertx-junit5:$vertxVersion")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"

tasks.withType<ShadowJar> {

  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles {
    include("META-INF/services/io.vertx.core.spi.VerticleFactory")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}

//for (name in arrayListOf("stdt", "lvdt")) {
//  tasks.register(name){
//
//  }
//}

