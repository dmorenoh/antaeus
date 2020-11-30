plugins {
    kotlin("jvm")
    kotlin("kapt")
}

kotlinProject()

dataLibs()

dependencies {
    implementation(project(":pleo-antaeus-core"))
}