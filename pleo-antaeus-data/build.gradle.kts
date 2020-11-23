plugins {
    kotlin("jvm")
    kotlin("kapt")
}

kotlinProject()

dataLibs()

dependencies {
    api(project(":pleo-antaeus-models"))
}
