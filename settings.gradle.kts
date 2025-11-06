rootProject.name = "employee-service"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://packages.confluent.io/maven/")
    }
}

include(":server")
