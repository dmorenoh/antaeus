plugins {
    kotlin("jvm")
    kotlin("kapt")
}

kotlinProject()

dataLibs()
schedulerLibs()

dependencies {
    implementation(project(":pleo-antaeus-core"))
}