# Eyrie

Eyrie é um app Android pessoal que lê e grava bibliotecas do Eagle. Ele sincroniza com S3 e B2 (OneDrive e Dropbox vêm depois) e usa IA para organizar e buscar. Roda só num Galaxy S23 Ultra.

## Referências

- [Plano de projeto](https://claude.ai/artifact/MfZ2k1Pfe5vFBYvxvyr7BW). Resumo para o código em `docs/plans/plano-resumo.md`.
- [Design system](https://claude.ai/artifact/MpVMFsgMowKLUFmYD6r2qZ).
- [Canvas de telas "Conceitos de interface v1"](https://claude.ai/artifact/WJmkW4Xy4NXeaPW3oWGZ9Y).
- [Issues](https://github.com/alessandrocaetanob/eyrie-app/issues) e [marcos](https://github.com/alessandrocaetanob/eyrie-app/milestones), um marco por fase.
- Formato `.library` do Eagle: `docs/formato-eagle.md`.

Telas novas seguem a regra de ouro: uma issue com o rótulo `interface` e o desenho no corpo, e código só depois do rótulo `aprovado`.

## Build

Requer JDK 17 ou mais novo (o projeto compila para Java 17) e o Android SDK com a plataforma 37.1. O AGP baixa a plataforma sozinha se a licença estiver aceita.

```
./gradlew assembleDebug
./gradlew test
```

O APK sai em `app/build/outputs/apk/debug/`. Crie `local.properties` com `sdk.dir=<caminho do SDK>`.

A CI (`.github/workflows/ci.yml`) roda `./gradlew test assembleDebug` com JDK 25 em todo push e pull request para `main` e publica o APK de debug como artefato do workflow.

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

## Versão do Android

`minSdk` e `targetSdk` são 37 (`gradle/libs.versions.toml`). É a versão instalada no aparelho: Galaxy S23 Ultra (SM-S918B), Android 17 (API 37), One UI 9.0.
