# QuickSort Recursivo - Programa de Análise Comparativa

Programa Java que compara três variantes do QuickSort em diferentes tipos e tamanhos de vetor, medindo tempo de execução, trocas e comparações. Os resultados são salvos em arquivos JSON para análise posterior.

## Algoritmos

| Algoritmo | Classe | Pivô | Usa M? |
|---|---|---|---|
| **QuickSort Recursivo** | `QuickSortR.quicksort` | último elemento | não |
| **QuickSort Híbrido** | `QuicksortHibrido` | último elemento | sim |
| **QuickSort Aprimorado** | `QuicksortAprimorado` | mediana de três (início, meio e fim) | sim |

### O que é M

O QuickSort é eficiente em vetores grandes, mas em sub-vetores pequenos o custo da recursão e do particionamento supera o ganho. Nas versões Híbrida e Aprimorada, o QuickSort **para de particionar quando um sub-vetor tem menos de M elementos**. Ao final, o vetor fica "quase ordenado" e uma única passada de **Insertion Sort** (`InsertionSort.java`) termina a ordenação.

- M muito pequeno: quase não há ganho.
- M muito grande: sobram blocos grandes para o Insertion Sort, que é O(n²).

O melhor M depende da máquina, por isso é encontrado empiricamente (opção [1] do menu).

## Menu

```
[1] - Teste para definir M
[2] - Definir M manualmente
[3] - Bateria de testes (QuickSort Recursivo, Hibrido e Aprimorado)
[4] - Apagar resultados salvos
[5] - Sair
[6] - Limpar tela
```

O menu mostra o M atual (ou "nao definido") e o caminho absoluto da pasta onde os resultados são salvos.

### [1] Teste para definir M

Mede o QuickSort Híbrido com cada valor de M dentro de um intervalo e escolhe o de menor tempo médio. Esse valor passa a ser o M usado na bateria.

Parâmetros (Enter usa o padrão):

| Parâmetro | Padrão | Descrição |
|---|---|---|
| Tamanho do vetor | 50000 | tamanho do vetor aleatório usado no teste |
| M mínimo | 5 | primeiro M testado (mínimo 2) |
| M máximo | 50 | último M testado |
| Testes por M | 1000 | ordenações por valor de M; o tempo é a média |

Antes das medições, o teste sempre faz **1000 execuções de aquecimento** (valor fixo; ver [Aquecimento da JVM](#aquecimento-da-jvm)).

> Com os valores padrão o teste leva alguns minutos. Para um teste rápido, reduza os testes por M (ex.: 200).

### [2] Definir M manualmente

Define o M sem rodar o teste, por exemplo para reutilizar um valor encontrado antes (ele está no nome do arquivo do Teste M). O valor deve ser um inteiro maior ou igual a 2. Enter cancela e mantém o M atual.

### [3] Bateria de testes

Executa os três algoritmos sobre quatro tipos de vetor, em cada tamanho informado:

| Tipo | Conteúdo |
|---|---|
| `ALEATORIO` | valores aleatórios entre 1 e n |
| `ORDENADO` | 1, 2, 3, ..., n |
| `INVERSO` | n, n-1, ..., 1 |
| `REPETIDOS` | apenas 10 valores distintos |

Parâmetros:
- **Tamanhos**, separados por vírgula (padrão `1000,10000,100000`).
- **Repetições** por combinação (padrão 5): o tempo é medido várias vezes e o arquivo guarda média, mínimo, máximo e desvio padrão.

Se M ainda não foi definido, o programa pede um valor ou oferece rodar o teste da opção [1].

Detalhes da execução:
- Em cada combinação de tipo e tamanho, os três algoritmos ordenam **cópias do mesmo vetor**, para uma comparação justa.
- Todo vetor é conferido depois da ordenação. Se não estiver ordenado, o registro fica com `status: "ERRO"`.
- **Pior caso O(n²):** o Recursivo e o Híbrido escolhem o último elemento como pivô e degradam em vetores ordenados, inversos ou com muitos repetidos. Acima de 100.000 elementos, esses casos levariam horas, então são pulados e registrados com `status: "IGNORADO"`. O limite fica na constante `LIMITE_PIOR_CASO`, em `BateriaTestes.java`.
- A bateria roda com uma pilha de 1 GB, porque no pior caso a recursão chega a profundidade n.

#### Experimento de pior caso

Ao final de toda bateria, o programa executa um experimento que **força explicitamente o pior caso do QuickSort**: o QuickSort Recursivo, com pivô no último elemento, ordena um vetor **já ordenado**. Nesse caso o pivô é sempre o maior elemento do sub-vetor, então cada partição separa apenas o pivô: um lado fica com n-1 elementos e o outro vazio. O resultado é uma recursão de profundidade n e cerca de n²/2 comparações.

- Tamanhos fixos que dobram: 2000, 4000, 8000, 16000 e 32000 (constante `TAMANHOS_PIOR_CASO`).
- O QuickSort Aprimorado, com mediana de três, ordena o mesmo vetor como contraste.
- Para cada tamanho, o arquivo registra `razaoTempo` e `razaoComparacoes`, isto é, o valor dividido pelo valor do tamanho anterior. Como n dobra, uma razão de **~4 indica O(n²)** e uma de **~2 indica O(n log n)**.
- Antes das medições há um aquecimento próprio em vetor ordenado, porque o aquecimento geral usa vetores aleatórios e não exercita a recursão profunda.

Exemplo de saída (as comparações crescem exatamente 4×):

```
Tamanho 32.000 | Ja ordenado
  QuickSort Recursivo    media    930,879 ms | trocas 512.015.999 | comparacoes 1.024.031.999
                         crescimento vs n/2: tempo 3,85x | comparacoes 4,00x
  QuickSort Aprimorado   media      0,382 ms | trocas 10.203 | comparacoes 566.502
                         crescimento vs n/2: tempo 2,32x | comparacoes 2,19x
```

### [4] Apagar resultados salvos

Lista os arquivos `.json` da pasta `Resultados` e os apaga depois de confirmação (S/N).

## Resultados salvos

Os arquivos ficam em `Resultados/`, na raiz do projeto. O programa descobre sozinho o **caminho absoluto** dessa pasta, procurando a pasta que contém `src/Main.java` a partir de onde estão as classes compiladas e da pasta do terminal. Por isso funciona em qualquer máquina, sem configuração, e não depende de onde o programa é executado. O caminho aparece no menu.

Cada execução gera um arquivo novo. Se o nome já existir, o novo arquivo recebe o sufixo ` (2)`, ` (3)` e assim por diante.

### Teste para definir M

Nome (parâmetros e resultado): `Teste M - Tamanho = 50000 M = 5 a 50 Testes = 1000 - Melhor M = 12 Tempo medio = 2710654 ns.json`

```json
{
  "dataHora": "2026-09-29T16:54:02",
  "versaoJava": "21.0.12.1",
  "tamanhoVetor": 50000,
  "mMinimo": 5,
  "mMaximo": 50,
  "testesPorM": 1000,
  "execucoesAquecimento": 1000,
  "semente": 8425091198179764010,
  "melhorM": 12,
  "melhorTempoMedioNs": 2710654,
  "resultados": [
    { "M": 5, "tempoMedioNs": 2847750 },
    { "M": 6, "tempoMedioNs": 2800550 }
  ]
}
```

### Bateria de testes

Nome: `Tamanho = 1000,10000,100000 M = 12.json`

```json
{
  "dataHora": "2026-09-29T16:17:47",
  "versaoJava": "21.0.12.1",
  "valorM": 12,
  "repeticoes": 5,
  "valoresDistintosRepetidos": 10,
  "tamanhos": [1000, 10000, 100000],
  "resultados": [
    {
      "algoritmo": "QuickSort Recursivo",
      "tipoVetor": "ORDENADO",
      "tamanho": 1000,
      "semente": 3772113013593555151,
      "status": "OK",
      "trocas": 500499,
      "comparacoes": 1000999,
      "tempoMedioMs": 0.6820,
      "tempoMinMs": 0.6512,
      "tempoMaxMs": 0.7128,
      "desvioPadraoMs": 0.0436,
      "temposMs": [0.7128, 0.6512]
    }
  ]
}
```

O arquivo também tem o objeto `experimentoPiorCaso`, com a descrição do experimento, os tamanhos usados e os registros do Recursivo e do Aprimorado, incluindo `razaoTempo` e `razaoComparacoes`.

- `status`: `OK`, `IGNORADO` (pior caso muito lento, com `motivo`) ou `ERRO` (com `motivo`).
- `semente`: permite recriar exatamente o vetor aleatório usado. Não aparece nos vetores ordenados e inversos, que são sempre iguais.
- Trocas e comparações são iguais em todas as repetições, porque a entrada é a mesma. Só o tempo varia.

## Aquecimento da JVM

O Java começa interpretando o código e só depois o compilador JIT o otimiza. Sem aquecimento, as primeiras medições (em especial as de vetores pequenos e do primeiro algoritmo executado) incluiriam esse custo de compilação. Por isso os dois testes executam os algoritmos repetidamente, sem medir, antes de começar as medições.

## Limitações da contagem de operações

O **tempo de execução** mede o algoritmo completo. Os contadores de **trocas** e **comparações**, porém:
- **não incluem o Insertion Sort final** do Híbrido e do Aprimorado, que por isso aparecem com menos operações do que realmente executam;
- incluem algumas comparações de índices e de controle de laço, além das comparações entre elementos.

Use esses contadores para observar tendências, como O(n²) contra O(n log n), e não para comparar valores exatos entre algoritmos.

## Como executar

### Pré-requisitos
- **JDK 14 ou superior**, com o compilador `javac` (recomendado: JDK 21). O código usa `switch` com `->`, que não compila no Java 8.

A extensão Java do VS Code já traz um JDK. Se `javac` não for reconhecido no terminal, instale um JDK (por exemplo, Eclipse Temurin) e marque a opção de adicionar ao PATH.

### Compilar e rodar

Na pasta do projeto (onde ficam `src` e `bin`):

```powershell
chcp 65001
javac -encoding UTF-8 -d bin src/*.java
java -cp bin Main
```

## Estrutura do código

```
src/
├── Main.java                # Menu principal
├── TesteEmpirico.java       # Teste para definir M (salva JSON)
├── BateriaTestes.java       # Bateria dos três algoritmos por tipo e tamanho de vetor + experimento de pior caso (salva JSON)
├── TipoVetor.java           # Geração dos vetores: aleatório, ordenado, inverso e repetidos
├── ArquivoResultados.java   # Pasta Resultados: gravação, remoção e leitura de parâmetros
├── QuickSortR.java          # QuickSort recursivo, com M e com mediana de três; contadores
├── QuicksortHibrido.java    # QuickSort + Insertion Sort
├── QuicksortAprimorado.java # QuickSort com mediana de três + Insertion Sort
└── InsertionSort.java       # Insertion Sort em sub-vetor
bin/                         # Classes compiladas
Resultados/                  # Arquivos JSON gerados (criada automaticamente)
```

**Autor**: Rodrigo Rocha, Rafael Brandão, Yuri Martins
**Versão**: 2.0
**Data**: Setembro 2026
