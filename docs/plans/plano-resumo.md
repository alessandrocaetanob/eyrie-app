# Plano do Eyrie: resumo para quem trabalha no código

O plano completo é o documento de [plano de projeto](https://claude.ai/artifact/MfZ2k1Pfe5vFBYvxvyr7BW). Este arquivo guarda o que muda com o código: os invariantes, os módulos, as fases e a estratégia de testes. Uma mudança que quebre um invariante exige rever o plano antes.

## Invariantes

São as regras que o código mantém em qualquer fase.

- Os arquivos da biblioteca no provedor são a fonte da verdade. O índice, os vetores e o cache são derivados e podem ser reconstruídos.
- O app não apaga objetos no provedor. A interface de armazenamento não tem operação de apagar.
- O app só grava um arquivo depois de conferir que a versão no provedor é a que ele leu: na própria gravação, onde o provedor aceita, ou logo depois, pelo histórico de versões. Nenhuma regra deixa uma versão vencer a outra sem você escolher.
- Campos que o app não conhece num JSON do Eagle são preservados na regravação.
- Só o módulo `formato` lê e grava arquivos do Eagle, e só o módulo `armazenamento` e o adaptador da pasta do aparelho, em `armazenamento-local`, falam com provedores. As telas não usam nenhum dos dois diretamente.
- Nada que a IA produz é gravado na biblioteca sem aceite. Sugestões pendentes e a marca de origem "gerada por IA" ficam só no índice e se perdem se ele for reconstruído.

## Módulos

| Módulo | Responsabilidade | Depende de |
| --- | --- | --- |
| `formato` | Ler e gravar `metadata.json`, `tags.json`, `mtime.json` e a pasta `images/<ID>.info/`, incluindo nota, paleta de cores e URL de origem, e o arquivo de favoritos do Eyrie (`.eyrie/favoritos.json`). Kotlin/JVM, sem Android. | nenhum |
| `armazenamento` | Interface comum (listar, ler intervalo, gravar, conferir versão e, onde houver, gerar link temporário) e um adaptador por provedor remoto. Kotlin/JVM, sem Android. | nenhum |
| `armazenamento-local` | Adaptador da pasta do aparelho, sobre o Storage Access Framework. Biblioteca Android, ligada pelo `app`. Ainda não existe no código: entra com a #12. | `armazenamento` |
| `indice` | Banco local: itens, pastas, tags, FTS, paleta de cores, hash de conteúdo, sugestões pendentes da IA, fila de alterações, estado e registro da sincronização. Kotlin/JVM, com o Room 3 e o `BundledSQLiteDriver`. O `app` cria o banco e o injeta. | nenhum |
| `sincronizacao` | Comparar provedor e índice, baixar metadados, aplicar a fila de alterações, tratar conflitos campo a campo e agendar o trabalho com as restrições de Ajustes. Kotlin/JVM. | `formato`, `armazenamento`, `indice` |
| `importacao` | Entrada de itens pelo aparelho, câmera, outro app ou link: cópia, miniatura, hash, checagem de duplicados e remoção opcional da galeria. | `formato`, `indice` |
| `ia` | Duas interfaces. Vetores: modelos no aparelho (leve, intermediário, pesado ou próprio), Gemini Embedding 2 na nuvem e tags básicas por similaridade. Geração de texto: Claude, Gemini e endpoint compatível com OpenAI. | nenhum |
| `busca` | Consulta de texto, consulta vetorial, filtro por cor e avaliação das regras de smart folder. | `indice`, `ia` |
| `leitor` | Itens de várias páginas: CBZ lido por intervalo, PDF renderizado depois de baixado, e progresso de leitura. | `armazenamento` |
| `designsystem` | Tokens do design system do Eyrie em tema Compose (Noturno e Claro, cores de destaque, tipografia, espaço, movimento) e os componentes `ey-`. | nenhum |
| `app` | Telas em Compose, navegação e ViewModels. | todos |

Decisão de 20/11/2026: `armazenamento` e `indice` são módulos Kotlin/JVM, como `formato` e `sincronizacao`. Um módulo JVM não resolve dependência de biblioteca Android, e `sincronizacao` depende dos dois. Por isso o adaptador da pasta do aparelho, que usa o Storage Access Framework, fica em `armazenamento-local`, fora de `armazenamento`.

`minSdk` e `targetSdk` são 37: o Galaxy S23 Ultra (SM-S918B) está no Android 17, API 37, One UI 9.

## Fases

São nove fases, cada uma com um marco no GitHub. As quatro primeiras formam a base (formato, armazenamento, leitura e escrita) e seguem em sequência. As fases 5 a 9 dependem só dessa base e podem trocar de ordem. A fase 1 é um portão: se um critério dela falhar, a seção de escrita e sincronização do plano é revista antes de começar o código do app. O marco "Fase 0 · Base" cobre o esqueleto do app, a CI e esta documentação.

| Fase | Marco no GitHub | Entrega |
| --- | --- | --- |
| 1 | Fase 1 · Reconhecimento do formato | Descrição do formato, bibliotecas de amostra para os testes, medição das bibliotecas e a regra de renomear. |
| 2 | Fase 2 · Armazenamento e índice | Uma biblioteca do bucket `eaglecdn` indexada no celular. |
| 3 | Fase 3 · Navegação e leitura | Concluir o primeiro acesso num aparelho limpo, ler um CBZ e um PDF do início ao fim e assistir a um vídeo da biblioteca. |
| 4 | Fase 4 · Escrita | Uma tag adicionada no celular aparece no Eagle desktop, e uma biblioteca criada no celular abre no desktop. |
| 5 | Fase 5 · Pastas e smart folders | Uma smart folder criada no celular mostra os mesmos itens no desktop. |
| 6 | Fase 6 · Compartilhamento | Uma página de CBZ enviada pelo WhatsApp, uma imagem recebida do Teams importada numa biblioteca e uma imagem salva a partir de um link. |
| 7 | Fase 7 · Busca | Achar uma imagem por um objeto pequeno nela, descrito em texto ou recortado de outra imagem. |
| 8 | Fase 8 · IA | Um lote de itens sem tags recebe tags, nome e descrição revisáveis antes de gravar. |
| 9 | Fase 9 · Demais provedores e guia | A mesma biblioteca funcionando em cada provedor, com os testes de contrato passando. |

Escopo de cada fase:

1. Reconhecimento do formato: registrar os campos dos arquivos da Mangas.library, medir o número de itens e o tamanho de cada biblioteca do bucket, criar uma biblioteca do zero fora do Eagle e abrir no desktop, alterar por fora o `metadata.json` de um item e observar o Eagle, registrar o que muda na pasta `.info` ao renomear, confirmar que o Eagle aceita a pasta `.eyrie/`, e verificar como o Eagle exibe CBZ e PDF e se o `vector-db` dele pode ser lido.
2. Armazenamento e índice: interface comum de armazenamento, adaptadores S3 (que atende o B2) e local, testes de contrato e índice no Room 3. O adaptador local fica em `armazenamento-local`.
3. Navegação e leitura: design system no Compose, primeiro acesso, Ajustes de armazenamento e aparência, bibliotecas, pastas, grade em cascata, visualizador com zoom, vídeo e leitor de páginas para CBZ e PDF.
4. Escrita: fila de alterações, gravação no formato do Eagle com conferência de versão, conflitos campo a campo, edição do item (nome, descrição, pasta, tags, favorito), exclusão para a lixeira, importação do aparelho e da câmera, criação de bibliotecas no celular, Ajustes de sincronização e a configuração, por biblioteca, de como o computador recebe os arquivos.
5. Pastas e smart folders: criar, renomear e mover pastas; criar, editar e avaliar smart folders, as prontas do app e as salvas a partir de uma busca.
6. Compartilhamento: enviar para outros apps no tamanho original ou em 2048 px, link temporário no S3 e no B2, receber de outros apps e salvar de um link.
7. Busca: FTS, filtro por cor, OCR e vetores da imagem inteira com modelo selecionável. Recortes e busca por trecho ficam por último, porque exigem o original de cada imagem.
8. IA: tags no aparelho; descrição e nome pela nuvem, com Claude, Gemini e endpoint compatível com OpenAI; processamento em lote com estimativa de custo e opção de gerar ao importar.
9. Demais provedores e guia: OneDrive e Dropbox, nessa ordem, e o guia do usuário.

Critérios do portão da fase 1:

- a biblioteca criada fora do Eagle abre no desktop;
- uma tag gravada por fora aparece no Eagle depois de recarregar;
- o Eagle não desfaz a alteração feita por fora ao recarregar a biblioteca.

A entrega de cada fase é conferida à mão no S23 Ultra e no Eagle desktop.

## Estratégia de testes

Os testes automatizados ficam em cinco pontos, escolhidos porque um erro ali perde dados ou devolve resultado errado sem aviso. O restante é verificado no aparelho pelos critérios de aceite dos requisitos.

| Ponto | O que o teste prova | Como |
| --- | --- | --- |
| Formato | O que o app grava é o que o Eagle grava, e campos desconhecidos sobrevivem à regravação. | Bibliotecas de amostra geradas pelo próprio Eagle na fase 1. Ler e regravar sem mudança tem de produzir JSON equivalente. Cada operação (criar item, mudar tag, renomear, mover de pasta, favoritar, excluir) é comparada com a mesma operação feita no Eagle. |
| Contrato de armazenamento | Todos os adaptadores se comportam igual ao listar, ler intervalo, gravar e conferir versão. | Um único conjunto de testes, executado contra um adaptador em memória e contra um bucket de teste de cada provedor. Adaptador novo só entra passando nele. |
| Sincronização | Nenhuma alteração é perdida nem aplicada duas vezes. | Adaptador em memória com falhas injetadas: queda no meio do envio, arquivo alterado entre a leitura e a gravação, fila reaplicada depois de reinício. Os casos ficam em tabela: estado do provedor, estado local e fila levam a um estado esperado. |
| Regras de smart folder | A mesma regra devolve os mesmos itens que o Eagle. | Smart folders criadas no Eagle sobre a biblioteca de amostra, com a lista de itens que o Eagle mostrou registrada como resultado esperado. |
| Leitor de CBZ | Ler por intervalo devolve as mesmas páginas que extrair o arquivo inteiro. | Arquivos de amostra, incluindo ZIP64 e entradas fora de ordem. |

Ficam sem teste automatizado:

- Telas em Compose, ViewModels e navegação. Um erro aí aparece no uso e não perde dados.
- Código de ligação: injeção de dependência, mapeamento entre camadas e consultas simples ao banco.
- Qualidade da busca visual e da IA. Ela é avaliada à mão, com uma lista fixa de consultas sobre a biblioteca de amostra, repetida a cada troca de modelo.

Regras para os testes que existem:

- O teste entra pela interface pública do módulo e usa o adaptador em memória. Não há mock de classe do próprio app.
- Os módulos `formato`, `armazenamento`, `indice` e `sincronizacao` e a avaliação de regras são Kotlin puro e rodam na JVM, sem emulador. Nos testes, o `indice` usa o Room 3 com o `BundledSQLiteDriver`, num banco em memória.
- Um caso novo entra quando um defeito real escapa: o caso que falhou vira uma linha na tabela do módulo correspondente.
