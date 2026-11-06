# Planejamento: Arquitetura Hexagonal na prática com um aeroporto

## 1. O exemplo

Um aeroporto agenda voos usando modelos de avião famosos. Cada modelo tem capacidade e alcance, e o sistema recusa o voo que o avião não consegue fazer.

O tema foi escolhido porque as regras são fáceis de explicar e não têm relação com tecnologia. Isso deixa visível a separação que o padrão propõe.

| Regra de negócio | Exemplo |
|---|---|
| Passageiros não podem passar da capacidade do avião | Embraer E195-E2 com 147 passageiros: recusado |
| A distância não pode passar do alcance do avião | Airbus A320neo até Lisboa (7.900 km): recusado |
| O código do voo não pode se repetir | Dois voos `LA100`: o segundo é recusado |
| O modelo precisa existir no catálogo | `CONCORDE`: recusado |

Modelos do catálogo (valores aproximados):

| Código | Modelo | Passageiros | Alcance |
|---|---|---|---|
| `737` | Boeing 737-800 | 189 | 5.400 km |
| `747` | Boeing 747-8 | 467 | 14.300 km |
| `777` | Boeing 777-300ER | 396 | 13.600 km |
| `787` | Boeing 787-9 Dreamliner | 296 | 14.000 km |
| `A320` | Airbus A320neo | 194 | 6.300 km |
| `A330` | Airbus A330-300 | 300 | 11.700 km |
| `A350` | Airbus A350-900 | 325 | 15.000 km |
| `A380` | Airbus A380 | 545 | 14.800 km |
| `E195` | Embraer E195-E2 | 146 | 4.800 km |

## 2. Como o padrão foi implementado

A Arquitetura Hexagonal (Ports and Adapters, Alistair Cockburn) isola o núcleo do sistema de toda tecnologia externa. O núcleo só fala com o exterior por **portas**, que são contratos, e **adaptadores**, que são implementações desses contratos.

```
  ADAPTADORES DE ENTRADA                          ADAPTADORES DE SAÍDA

                       +-----------------------+
  AdaptadorTerminal -->|                       |
                       |  [GerenciarVoos]      |
  AdaptadorHttp ------>|        |              |
                       |        v              |
  TestesAeroporto ---->|   ServicoDeVoos       |
                       |     |       |         |
                       |     v       v         |
                       |  DOMÍNIO  [RepositorioDeVoos] <-- RepositorioEmMemoria
                       |  Voo,Aviao            |       <-- RepositorioEmArquivo
                       +-----------------------+

  [ ] = porta (interface Java)
  AeroportoApp, a raiz de composição, cria as peças e conecta tudo ao iniciar.
```

**A regra central: as dependências sempre apontam para dentro.**

- `dominio` não importa nada do projeto além de si mesmo.
- `aplicacao` importa apenas `dominio`.
- `adaptador` importa `aplicacao` e `dominio`.
- `configuracao` importa todas as camadas, e nenhuma camada a importa.

O ponto mais importante está na saída. O serviço precisa salvar um voo, mas não conhece o `RepositorioEmArquivo`: ele usa a interface `RepositorioDeVoos`, que pertence ao núcleo, e o adaptador a implementa. Isso é a **inversão de dependência**. Na execução, a chamada vai de dentro para fora; no código, a dependência vai de fora para dentro.

A regra é verificada: o `TestesAeroporto` lê os `import` do código-fonte e falha se alguma camada a violar.

### Fluxo de uma requisição

`POST /voos` com `codigo=LA100&modelo=A350&destino=Lisboa&distancia=7900&passageiros=300`:

1. O `AdaptadorHttp` lê o formulário.
2. Ele chama a porta `GerenciarVoos`, sem saber quem a implementa.
3. O `ServicoDeVoos` cria o `Voo`, que aplica as regras, e confere o código pela porta `RepositorioDeVoos`.
4. O serviço salva o voo pela mesma porta.
5. O adaptador traduz o resultado: `201` com JSON, ou `422` se o voo foi recusado.

No terminal, apenas os passos 1 e 5 mudam. Os passos 2, 3 e 4 são exatamente o mesmo código.

## 3. Estrutura de pastas

```
├── rodar.bat, testar.bat
└── src/
    ├── main/resources/web/      index.html: interface web servida pelo AdaptadorHttp
    ├── main/java/br/com/aeroporto/
    │   ├── dominio/             NÚCLEO: Aviao, Voo, VooRecusadoException
    │   ├── aplicacao/           NÚCLEO: GerenciarVoos, RepositorioDeVoos, ServicoDeVoos
    │   ├── adaptador/
    │   │   ├── entrada/         AdaptadorTerminal, AdaptadorHttp
    │   │   └── saida/           RepositorioEmMemoria, RepositorioEmArquivo
    │   └── configuracao/        AeroportoApp: raiz de composição (main)
    └── test/java/br/com/aeroporto/   TestesAeroporto
```

As pastas `dados/` e `build/` são geradas durante a execução.

## 4. Componentes estruturais

| Componente | Classes | Responsabilidade | Não pode |
|---|---|---|---|
| **Domínio** | `Voo`, `Aviao`, `VooRecusadoException` | Regras de negócio | Conhecer portas, I/O ou frameworks |
| **Serviço de aplicação** | `ServicoDeVoos` | Orquestrar: criar o voo, conferir, salvar | Conter regra de negócio ou tecnologia |
| **Porta de entrada** | `GerenciarVoos` | Definir o que o sistema oferece ao mundo externo | Ter implementação |
| **Porta de saída** | `RepositorioDeVoos` | Definir o que o sistema precisa do mundo externo | Mencionar tecnologia |
| **Adaptadores de entrada** | `AdaptadorTerminal`, `AdaptadorHttp` | Traduzir estímulos externos em chamadas à porta | Decidir regra de negócio |
| **Adaptadores de saída** | `RepositorioEmMemoria`, `RepositorioEmArquivo` | Implementar a porta de saída com uma tecnologia | Decidir regra de negócio |
| **Raiz de composição** | `AeroportoApp` | Instanciar os adaptadores e conectá-los ao núcleo | Conter lógica de negócio |

## 5. Conectores

Componentes são as peças que processam ou guardam dados. **Conectores** são os mecanismos que fazem essas peças interagirem.

| # | Conector | Tipo | Quem liga a quem | Onde está no código |
|---|---|---|---|---|
| 1 | Entrada e saída padrão | Fluxo de texto | Usuário ↔ `AdaptadorTerminal` | `Scanner` e `System.out` |
| 2 | Protocolo HTTP | Requisição e resposta pela rede | Cliente (navegador, curl, Postman) ↔ `AdaptadorHttp` | `HttpServer` do JDK, página em `/` e rotas `/voos`, `/voos/codigos` e `/avioes` |
| 3 | Chamada pela porta de entrada | Chamada de método polimórfica | Adaptador de entrada → `ServicoDeVoos` | `voos.agendar(...)` |
| 4 | Chamada direta ao domínio | Chamada de método | `ServicoDeVoos` → `Voo` | `new Voo(...)`, `voo.verificarCodigoLivre(...)` |
| 5 | Chamada pela porta de saída | Chamada de método polimórfica | `ServicoDeVoos` → adaptador de saída | `repositorio.salvar(...)` |
| 6 | Sistema de arquivos | Leitura e escrita em disco | `RepositorioEmArquivo` ↔ `dados/voos.csv` | `Files.writeString`, `Files.readAllLines` |
| 7 | Propagação de exceção | Sinalização de erro | Domínio → adaptador de entrada | `VooRecusadoException`, traduzida em mensagem ou status HTTP |
| 8 | Injeção pelo construtor | Ligação na inicialização | `AeroportoApp` conecta adaptadores e núcleo | `new ServicoDeVoos(repositorio)` |

Os conectores 3 e 5 definem o padrão: como a chamada passa por uma interface, a implementação do outro lado pode mudar sem que quem chama perceba. O conector 8 torna essa troca possível, porque a escolha acontece em um único lugar.

## 6. O que comprova o padrão

| Benefício | Evidência no projeto |
|---|---|
| Trocar a entrada sem tocar no núcleo | Terminal e HTTP usam o mesmo `ServicoDeVoos` |
| Trocar a infraestrutura sem tocar no núcleo | Opção `--arquivo` |
| Testar sem infraestrutura | Os testes de regra rodam sem terminal, rede ou disco |
| Adaptadores substituíveis | Os dois repositórios passam pelo mesmo teste de contrato |
| Regra de dependência verificada | Teste que analisa os `import` de cada camada |

### Decisões de projeto

- **Sem frameworks nem bibliotecas:** só o JDK. Tudo o que aparece no código é o padrão.
- **Uma porta de cada lado:** o mínimo para mostrar o padrão completo. Um sistema real pode ter várias.
- **Limitação assumida:** a aplicação não trata acesso simultâneo. O servidor HTTP atende uma requisição por vez, o que basta para a demonstração.

## 7. Roteiro de demonstração (cerca de 5 minutos)

Execute os comandos no PowerShell, a partir da pasta do projeto. Use `curl.exe`, porque no Windows PowerShell `curl` sozinho é outro comando. Apague a pasta `dados/` antes de começar, se ela existir.

**1. Regras no terminal**

```powershell
.\rodar.bat
```

Veja os aviões (opção 3). Agende `LA100`, modelo `A320`, destino `Lisboa`, 7900 km, 150 passageiros: é recusado por alcance. Repita com o modelo `A350`: é aceito.

**2. Mesmo núcleo, outra entrada**

```powershell
.\rodar.bat --http
```

Em outro terminal:

```powershell
curl.exe -i -X POST -d "codigo=LA100&modelo=A350&destino=Lisboa&distancia=7900&passageiros=300" http://localhost:8080/voos
curl.exe -i -X POST -d "codigo=LA100&modelo=A350&destino=Lisboa&distancia=7900&passageiros=300" http://localhost:8080/voos
```

O segundo pedido retorna `422`. A mensagem é a mesma do terminal, porque vem do domínio.

Abra `http://localhost:8080` no navegador e tente agendar o mesmo voo: a página mostra a mesma recusa, porque é só mais um cliente da API.

**3. Mesmo núcleo, outra saída**

Pare o servidor com `Ctrl+C` e suba com o repositório em arquivo:

```powershell
.\rodar.bat --http --arquivo
```

Agende um voo, reinicie o servidor e consulte. O voo continua lá:

```powershell
curl.exe -X POST -d "codigo=GO200&modelo=737&destino=Recife&distancia=2100&passageiros=180" http://localhost:8080/voos
curl.exe http://localhost:8080/voos
```

Mostre o arquivo `dados/voos.csv`. Nenhuma linha do hexágono mudou entre os passos 1, 2 e 3.

**4. Fechamento: a arquitetura se defende**

```powershell
.\testar.bat
```

Depois, adicione esta linha em `dominio/Voo.java`, logo abaixo do `package`:

```java
import br.com.aeroporto.adaptador.saida.RepositorioEmMemoria;
```

Rode `.\testar.bat` de novo: o teste de arquitetura falha. Remova a linha ao terminar.
