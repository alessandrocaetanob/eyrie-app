# Eyrie

App Android pessoal que lê e grava bibliotecas do Eagle. Roda só num Galaxy S23 Ultra.

## Build

Requer JDK 17 ou mais novo (o projeto compila para Java 17) e o Android SDK com a plataforma 37.1. O AGP baixa a plataforma sozinha se a licença estiver aceita.

```
./gradlew assembleDebug
./gradlew test
```

O APK sai em `app/build/outputs/apk/debug/`. Crie `local.properties` com `sdk.dir=<caminho do SDK>`.

## Módulos

| Módulo | Tipo | Depende de |
| --- | --- | --- |
| `formato` | Kotlin/JVM | nenhum |
| `armazenamento` | Kotlin/JVM | nenhum |
| `indice` | Kotlin/JVM | nenhum |
| `sincronizacao` | Kotlin/JVM | formato, armazenamento, indice |
| `importacao` | biblioteca Android | formato, indice |
| `ia` | biblioteca Android | nenhum |
| `busca` | biblioteca Android | indice, ia |
| `leitor` | biblioteca Android | armazenamento |
| `designsystem` | biblioteca Android | nenhum |
| `app` | aplicação Android | todos |

`armazenamento` e `indice` são Kotlin/JVM porque um módulo Kotlin/JVM (`sincronizacao`) não resolve dependência de biblioteca Android.

## Pendente

`minSdk` está em 36 como valor provisório (`gradle/libs.versions.toml`). Falta conferir a versão do Android no S23 Ultra e ajustar.
