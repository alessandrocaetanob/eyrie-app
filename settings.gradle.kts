pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "eyrie"

include(
    ":formato",
    ":armazenamento",
    ":indice",
    ":sincronizacao",
    ":importacao",
    ":ia",
    ":busca",
    ":leitor",
    ":designsystem",
    ":app",
)
