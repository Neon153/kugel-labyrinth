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
    }
}

rootProject.name = "KugelLabyrinth"

// core: gemeinsame Spiellogik, Sensor und Spielfeld (Android-Bibliothek)
// app:  Handy-App
// wear: Uhr-App für Wear OS (Pixel Watch)
include(":core", ":app", ":wear")
