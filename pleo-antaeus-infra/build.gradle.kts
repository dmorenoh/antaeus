plugins {
    kotlin("jvm")
}

kotlinProject()

dataLibs()
schedulerLibs()

dependencies {
    implementation(project(":pleo-antaeus-core"))
}