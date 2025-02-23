import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    kotlin("jvm") version "1.3.70" apply false
    kotlin("kapt") version "1.4.0" apply false
}

//apply(plugin = "kotlin-kapt")

allprojects {
    group = "io.pleo"
    version = "1.0"

    repositories {
        mavenCentral()
        jcenter()
        maven(url = "https://dl.bintray.com/arrow-kt/arrow-kt/")
        maven(url = "https://oss.jfrog.org/artifactory/oss-snapshot-local/")
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "11"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
//        maxHeapSize = "1G"
//        jvmArgs = listOf("-Xss1m", "-Xms128m", "-Xmx2g", "-XX:+UnlockExperimentalVMOptions")
    }

}