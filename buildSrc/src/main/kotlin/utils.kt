import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

const val junitVersion = "5.6.0"
const val arrowVersion = "0.10.4"

/**
 * Configures the current project as a Kotlin project by adding the Kotlin `stdlib` as a dependency.
 */
fun Project.kotlinProject() {

    dependencies {
        // Kotlin libs
        "implementation"(kotlin("stdlib"))
        "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
        // Logging
        "implementation"("org.slf4j:slf4j-simple:1.7.30")
        "implementation"("io.github.microutils:kotlin-logging:1.7.8")

        // Mockk
        "testImplementation"("io.mockk:mockk:1.9.3")

        // JUnit 5
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        "testImplementation"("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        "runtime"("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

        // Vertx
        "implementation"("io.vertx:vertx-core:3.9.3")
        "implementation"("io.vertx:vertx-lang-kotlin-coroutines:3.9.3")
        "compile"("io.vertx:vertx-circuit-breaker:3.9.4")
        "testImplementation"("io.vertx:vertx-junit5:3.9.3")

        //Arrow
        "compile" ("io.arrow-kt:arrow-core:$arrowVersion")
        "compile" ("io.arrow-kt:arrow-syntax:$arrowVersion")
        "kapt"    ("io.arrow-kt:arrow-meta:$arrowVersion")

    }
}

/**
 * Configures data layer libs needed for interacting with the DB
 */
fun Project.dataLibs() {
    dependencies {
        "implementation"("org.jetbrains.exposed:exposed:0.17.7")
        "implementation"("org.xerial:sqlite-jdbc:3.30.1")
    }
}

fun Project.schedulerLibs() {
    dependencies {
        "compile"("org.quartz-scheduler:quartz:2.3.0")
    }
}