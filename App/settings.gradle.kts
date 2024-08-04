pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials.username = "mapbox"
            credentials.password = "sk.eyJ1IjoiZ2VvcmdldGhla2tlMDMiLCJhIjoiY2x2a3diNHM3MGswazJqbXc3cjl3aWFrMiJ9.3ODfuGVIwyLQNVVlSGUmAg"
            authentication.create<BasicAuthentication>("basic")

        }
    }
}

rootProject.name = "TripSage"
include(":app")
 