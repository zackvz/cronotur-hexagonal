# Aeroporto Hexagonal

Demonstração prática da **Arquitetura Hexagonal (Ports and Adapters)** em Java, sem frameworks nem bibliotecas externas. O sistema agenda voos com modelos de avião famosos.

A explicação do padrão, da estrutura de pastas, dos componentes e dos conectores, além do roteiro da apresentação, está em [PLANEJAMENTO.md](PLANEJAMENTO.md).

## Requisitos

JDK 17 ou superior (testado no JDK 21). Os scripts usam o `JAVA_HOME`, se estiver definido. Caso contrário, usam o `java` do PATH.

## Executar

No PowerShell ou no Prompt de Comando, a partir da pasta do projeto:

```powershell
.\rodar.bat                  # terminal, dados em memória
.\rodar.bat --http           # interface web e API em http://localhost:8080
.\rodar.bat --arquivo        # dados em dados/voos.csv
.\rodar.bat --http --arquivo # interface web e API com dados em arquivo
.\testar.bat                 # testes
```

Use o PowerShell, não o Git Bash nem o `!` do Claude Code: o Bash remove a barra de `.\rodar.bat`. Para parar o servidor, use `Ctrl+C`.

## Onde fica cada coisa

| O que | Caminho ou endereço |
|---|---|
| Executar o sistema | `rodar.bat` |
| Executar os testes | `testar.bat` |
| Classe principal (`main`) | `src/main/java/br/com/aeroporto/configuracao/AeroportoApp.java` |
| Testes | `src/test/java/br/com/aeroporto/TestesAeroporto.java` |
| Regras de negócio | `src/main/java/br/com/aeroporto/dominio/Voo.java` |
| Catálogo de aviões | `src/main/java/br/com/aeroporto/dominio/Aviao.java` |
| Portas e serviço | `src/main/java/br/com/aeroporto/aplicacao/` |
| Adaptadores de entrada | `src/main/java/br/com/aeroporto/adaptador/entrada/` |
| Adaptadores de saída | `src/main/java/br/com/aeroporto/adaptador/saida/` |
| Página da interface web | `src/main/resources/web/index.html` |
| Dados salvos com `--arquivo` | `dados/voos.csv` |
| Interface web | `http://localhost:8080` |
| Lista de voos | `http://localhost:8080/voos` |
| Códigos de voo | `http://localhost:8080/voos/codigos` ou opção 4 do menu |
| Catálogo pela API | `http://localhost:8080/avioes` |
| Roteiro da apresentação | `PLANEJAMENTO.md`, seção 7 |

## API HTTP

| Método | Rota | Dados | Resposta |
|---|---|---|---|
| `GET` | `/` | | `200` com a interface web |
| `POST` | `/voos` | Formulário: `codigo`, `modelo`, `destino`, `distancia`, `passageiros` | `201` com o voo |
| `GET` | `/voos` | | `200` com a lista de voos |
| `GET` | `/voos/codigos` | | `200` com a lista de códigos de voo |
| `GET` | `/avioes` | | `200` com o catálogo de modelos |

Erros: `400` para requisição inválida, `404` para rota inexistente e `422` para voo recusado por uma regra de negócio.

No Windows PowerShell, use `curl.exe`:

```powershell
curl.exe -X POST -d "codigo=LA100&modelo=A350&destino=Lisboa&distancia=7900&passageiros=300" http://localhost:8080/voos
curl.exe http://localhost:8080/voos
```

## Abrir no IntelliJ

Abra a pasta do projeto, marque `src/main/java` como *Sources Root*, `src/main/resources` como *Resources Root* e `src/test/java` como *Test Sources Root*, selecione um JDK 17 ou superior e execute `AeroportoApp`.
