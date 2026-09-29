import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

// Executa os três algoritmos sobre vários tipos e tamanhos de vetor, mais o
// experimento de pior caso, e salva tudo em JSON
public class BateriaTestes {

    private static final String TAMANHOS_PADRAO = "1000,10000,100000";
    private static final int REPETICOES_PADRAO = 5;

    // Acima deste tamanho, os algoritmos sem mediana de três não rodam em vetores
    // ordenados, inversos ou com repetidos: o pior caso O(n²) levaria horas.
    private static final int LIMITE_PIOR_CASO = 100_000;

    // Experimento de pior caso: tamanhos dobrando, para que o crescimento O(n²)
    // apareça como razão ~4 entre um tamanho e o anterior
    private static final int[] TAMANHOS_PIOR_CASO = {2000, 4000, 8000, 16000, 32000};

    // Pilha grande para a recursão profunda do pior caso (profundidade ~n)
    private static final long TAMANHO_PILHA = 1L << 30; // 1 GB

    private static final int RECURSIVO = 0, HIBRIDO = 1, APRIMORADO = 2;
    private static final String[] ALGORITMOS = {
        "QuickSort Recursivo", "QuickSort Hibrido", "QuickSort Aprimorado"
    };

    // Resultado agregado de um algoritmo sobre um tipo/tamanho de vetor
    private static class Registro {
        String algoritmo;
        TipoVetor tipo;
        int tamanho;
        long semente;
        String status = "OK";
        String motivo;
        long trocas;
        long comparacoes;
        List<Double> temposMs = new ArrayList<>();
        // Só no experimento de pior caso: crescimento em relação ao tamanho anterior
        Double razaoTempo;
        Double razaoComparacoes;
    }

    public static void executar(Scanner scanner, int M) {
        System.out.println("\n=== BATERIA DE TESTES POR TIPO DE VETOR ===");
        System.out.println("Tipos: aleatorio, ja ordenado, inverso, muitos repetidos");
        System.out.println("Inclui o experimento de pior caso (vetor ordenado + pivo no ultimo elemento).");
        System.out.print("Tamanhos separados por virgula (Enter = " + TAMANHOS_PADRAO + "): ");
        int[] tamanhos = lerTamanhos(scanner.nextLine().trim());
        if (tamanhos == null) {
            System.out.println("Tamanhos invalidos. Use numeros inteiros positivos, ex.: 1000,50000");
            return;
        }

        int repeticoes = ArquivoResultados.perguntarInteiro(scanner, "Repeticoes por combinacao", REPETICOES_PADRAO, 1);

        List<Registro> registros = new ArrayList<>();
        List<Registro> registrosPiorCaso = new ArrayList<>();
        String inicio = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Roda em uma thread com pilha grande para não estourar a recursão no pior caso
        Thread execucao = new Thread(null, () -> {
            aquecerJvm(M);
            rodarBateria(tamanhos, repeticoes, M, registros);
            rodarPiorCaso(repeticoes, M, registrosPiorCaso);
        }, "bateria-testes", TAMANHO_PILHA);
        execucao.start();
        try {
            execucao.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Bateria interrompida.");
            return;
        }

        try {
            Path arquivo = salvarJson(registros, registrosPiorCaso, tamanhos, repeticoes, M, inicio);
            System.out.println("\n[OK] Resultados salvos em: " + arquivo);
        } catch (IOException e) {
            System.out.println("Erro ao salvar o arquivo JSON: " + e.getMessage());
        }
    }

    private static void rodarBateria(int[] tamanhos, int repeticoes, int M, List<Registro> registros) {
        Random geradorSementes = new Random();

        for (int tamanho : tamanhos) {
            for (TipoVetor tipo : TipoVetor.values()) {
                // O mesmo vetor base é usado pelos três algoritmos, para comparação justa
                long semente = geradorSementes.nextLong();
                int[] base = tipo.gerar(tamanho, new Random(semente));
                System.out.printf("%nTamanho %,d | %s%n", tamanho, tipo.getDescricao());

                for (int alg = 0; alg < ALGORITMOS.length; alg++) {
                    Registro r = novoRegistro(alg, tipo, tamanho, semente);
                    registros.add(r);
                    System.out.printf("  %-22s ", r.algoritmo);

                    boolean semMediana = alg != APRIMORADO;
                    if (semMediana && tipo != TipoVetor.ALEATORIO && tamanho > LIMITE_PIOR_CASO) {
                        r.status = "IGNORADO";
                        r.motivo = "Pior caso O(n^2) acima de " + LIMITE_PIOR_CASO + " elementos";
                        System.out.println("ignorado (pior caso O(n^2) muito lento)");
                        continue;
                    }
                    medir(r, alg, base, repeticoes, M);
                    imprimir(r);
                }
            }
        }
    }

    // Força o pior caso do QuickSort: vetor já ordenado com pivô = último elemento.
    // Cada partição separa só o pivô (o maior elemento), gerando n-1 níveis de
    // recursão e ~n²/2 comparações. O Aprimorado (mediana de três) roda no mesmo
    // vetor como contraste.
    private static void rodarPiorCaso(int repeticoes, int M, List<Registro> registros) {
        System.out.println("\n=== EXPERIMENTO DE PIOR CASO ===");
        System.out.println("QuickSort Recursivo com pivo = ultimo elemento em vetor ja ordenado.");
        System.out.println("Ao dobrar n, tempo e comparacoes devem crescer ~4x (O(n^2)).");

        int[] algoritmos = {RECURSIVO, APRIMORADO};
        Registro[] anterior = new Registro[ALGORITMOS.length];

        // Aquecimento próprio: o aquecimento geral usa vetores aleatórios e não
        // exercita a recursão profunda; sem isto os primeiros tamanhos saem distorcidos
        int[] aquecimento = TipoVetor.ORDENADO.gerar(TAMANHOS_PIOR_CASO[0], new Random());
        for (int i = 0; i < 30; i++) {
            for (int alg : algoritmos) {
                ordenar(alg, aquecimento.clone(), M);
            }
        }

        for (int tamanho : TAMANHOS_PIOR_CASO) {
            int[] base = TipoVetor.ORDENADO.gerar(tamanho, new Random());
            System.out.printf("%nTamanho %,d | %s%n", tamanho, TipoVetor.ORDENADO.getDescricao());

            for (int alg : algoritmos) {
                Registro r = novoRegistro(alg, TipoVetor.ORDENADO, tamanho, 0);
                registros.add(r);
                System.out.printf("  %-22s ", r.algoritmo);
                medir(r, alg, base, repeticoes, M);

                Registro ant = anterior[alg];
                if (r.status.equals("OK") && ant != null && ant.status.equals("OK")) {
                    r.razaoTempo = media(r.temposMs) / media(ant.temposMs);
                    r.razaoComparacoes = (double) r.comparacoes / ant.comparacoes;
                }
                anterior[alg] = r;

                imprimir(r);
                if (r.razaoTempo != null) {
                    System.out.printf("  %-22s crescimento vs n/2: tempo %.2fx | comparacoes %.2fx%n",
                                      "", r.razaoTempo, r.razaoComparacoes);
                }
            }
        }
    }

    private static Registro novoRegistro(int alg, TipoVetor tipo, int tamanho, long semente) {
        Registro r = new Registro();
        r.algoritmo = ALGORITMOS[alg];
        r.tipo = tipo;
        r.tamanho = tamanho;
        r.semente = semente;
        return r;
    }

    // Ordena cópias do vetor base 'repeticoes' vezes, guardando tempos e contadores
    private static void medir(Registro r, int alg, int[] base, int repeticoes, int M) {
        for (int rep = 0; rep < repeticoes; rep++) {
            int[] vetor = base.clone();
            QuickSortR.trocas = 0;
            QuickSortR.comparacoes = 0;
            try {
                long t0 = System.nanoTime();
                ordenar(alg, vetor, M);
                long t1 = System.nanoTime();
                if (!estaOrdenado(vetor)) {
                    r.status = "ERRO";
                    r.motivo = "Vetor nao ficou ordenado";
                    return;
                }
                r.temposMs.add((t1 - t0) / 1_000_000.0);
                r.trocas = QuickSortR.trocas;
                r.comparacoes = QuickSortR.comparacoes;
            } catch (StackOverflowError e) {
                r.status = "ERRO";
                r.motivo = "StackOverflowError (recursao profunda demais)";
                return;
            }
        }
    }

    private static void imprimir(Registro r) {
        if (r.status.equals("OK")) {
            System.out.printf("media %10.3f ms | trocas %,d | comparacoes %,d%n",
                              media(r.temposMs), r.trocas, r.comparacoes);
        } else {
            System.out.println(r.status + ": " + r.motivo);
        }
    }

    private static void ordenar(int algoritmo, int[] vetor, int M) {
        switch (algoritmo) {
            case RECURSIVO -> QuickSortR.quicksort(vetor, 0, vetor.length - 1);
            case HIBRIDO -> new QuicksortHibrido(M).ordenar(vetor);
            case APRIMORADO -> new QuicksortAprimorado(M).ordenar(vetor);
        }
    }

    // Executa os algoritmos algumas vezes sem medir, para o JIT otimizar o código
    private static void aquecerJvm(int M) {
        System.out.println("Aquecendo a JVM...");
        int[] base = TipoVetor.ALEATORIO.gerar(10_000, new Random());
        for (int i = 0; i < 200; i++) {
            for (int alg = 0; alg < ALGORITMOS.length; alg++) {
                ordenar(alg, base.clone(), M);
            }
        }
    }

    private static boolean estaOrdenado(int[] vetor) {
        for (int i = 1; i < vetor.length; i++) {
            if (vetor[i - 1] > vetor[i]) {
                return false;
            }
        }
        return true;
    }

    private static int[] lerTamanhos(String entrada) {
        if (entrada.isEmpty()) {
            entrada = TAMANHOS_PADRAO;
        }
        String[] partes = entrada.split(",");
        int[] tamanhos = new int[partes.length];
        for (int i = 0; i < partes.length; i++) {
            try {
                tamanhos[i] = Integer.parseInt(partes[i].trim().replace("_", "").replace(".", ""));
            } catch (NumberFormatException e) {
                return null;
            }
            if (tamanhos[i] < 1) {
                return null;
            }
        }
        return tamanhos;
    }

    // ---------- Estatísticas ----------

    private static double media(List<Double> valores) {
        double soma = 0;
        for (double v : valores) soma += v;
        return valores.isEmpty() ? 0 : soma / valores.size();
    }

    private static double desvioPadrao(List<Double> valores) {
        if (valores.size() < 2) return 0;
        double m = media(valores);
        double soma = 0;
        for (double v : valores) soma += (v - m) * (v - m);
        return Math.sqrt(soma / (valores.size() - 1));
    }

    // ---------- Gravação do JSON ----------

    private static Path salvarJson(List<Registro> registros, List<Registro> registrosPiorCaso, int[] tamanhos,
                                   int repeticoes, int M, String inicio) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"dataHora\": ").append(ArquivoResultados.texto(inicio)).append(",\n");
        sb.append("  \"versaoJava\": ").append(ArquivoResultados.texto(System.getProperty("java.version"))).append(",\n");
        sb.append("  \"valorM\": ").append(M).append(",\n");
        sb.append("  \"repeticoes\": ").append(repeticoes).append(",\n");
        sb.append("  \"valoresDistintosRepetidos\": ").append(TipoVetor.VALORES_DISTINTOS_REPETIDOS).append(",\n");
        sb.append("  \"tamanhos\": ").append(listaJson(tamanhos)).append(",\n");
        sb.append("  \"resultados\": [\n");
        for (int i = 0; i < registros.size(); i++) {
            escreverRegistro(sb, registros.get(i), "    ", i == registros.size() - 1);
        }
        sb.append("  ],\n");

        sb.append("  \"experimentoPiorCaso\": {\n");
        sb.append("    \"descricao\": ").append(ArquivoResultados.texto(
            "Pior caso forcado: QuickSort Recursivo com pivo = ultimo elemento em vetor ja ordenado. "
            + "Cada particao separa apenas o pivo, gerando recursao de profundidade n e ~n^2/2 comparacoes. "
            + "QuickSort Aprimorado (mediana de tres) no mesmo vetor como contraste. "
            + "razaoTempo/razaoComparacoes = valor / valor do tamanho anterior (n dobra): ~4 indica O(n^2), ~2 indica O(n log n).")).append(",\n");
        sb.append("    \"tipoVetor\": \"ORDENADO\",\n");
        sb.append("    \"pivoRecursivo\": \"ultimo elemento\",\n");
        sb.append("    \"complexidadeEsperadaRecursivo\": \"O(n^2)\",\n");
        sb.append("    \"tamanhos\": ").append(listaJson(TAMANHOS_PIOR_CASO)).append(",\n");
        sb.append("    \"resultados\": [\n");
        for (int i = 0; i < registrosPiorCaso.size(); i++) {
            escreverRegistro(sb, registrosPiorCaso.get(i), "      ", i == registrosPiorCaso.size() - 1);
        }
        sb.append("    ]\n");
        sb.append("  }\n}\n");

        // Nome: "Tamanho = 1000,10000 M = 12"; se já existir, ganha " (2)", " (3)"...
        String listaTamanhos = listaJson(tamanhos).replace("[", "").replace("]", "").replace(" ", "");
        return ArquivoResultados.salvar("Tamanho = " + listaTamanhos + " M = " + M, sb.toString());
    }

    private static void escreverRegistro(StringBuilder sb, Registro r, String recuo, boolean ultimo) {
        String c = recuo + "  ";
        sb.append(recuo).append("{\n");
        sb.append(c).append("\"algoritmo\": ").append(ArquivoResultados.texto(r.algoritmo)).append(",\n");
        sb.append(c).append("\"tipoVetor\": ").append(ArquivoResultados.texto(r.tipo.name())).append(",\n");
        sb.append(c).append("\"tamanho\": ").append(r.tamanho).append(",\n");
        if (r.tipo != TipoVetor.ORDENADO && r.tipo != TipoVetor.INVERSO) {
            sb.append(c).append("\"semente\": ").append(r.semente).append(",\n");
        }
        sb.append(c).append("\"status\": ").append(ArquivoResultados.texto(r.status));
        if (r.motivo != null) {
            sb.append(",\n").append(c).append("\"motivo\": ").append(ArquivoResultados.texto(r.motivo));
        }
        if (r.status.equals("OK")) {
            sb.append(",\n").append(c).append("\"trocas\": ").append(r.trocas);
            sb.append(",\n").append(c).append("\"comparacoes\": ").append(r.comparacoes);
            sb.append(",\n").append(c).append("\"tempoMedioMs\": ").append(ArquivoResultados.numero(media(r.temposMs)));
            sb.append(",\n").append(c).append("\"tempoMinMs\": ").append(ArquivoResultados.numero(r.temposMs.stream().mapToDouble(d -> d).min().orElse(0)));
            sb.append(",\n").append(c).append("\"tempoMaxMs\": ").append(ArquivoResultados.numero(r.temposMs.stream().mapToDouble(d -> d).max().orElse(0)));
            sb.append(",\n").append(c).append("\"desvioPadraoMs\": ").append(ArquivoResultados.numero(desvioPadrao(r.temposMs)));
            if (r.razaoTempo != null) {
                sb.append(",\n").append(c).append("\"razaoTempo\": ").append(ArquivoResultados.numero(r.razaoTempo));
                sb.append(",\n").append(c).append("\"razaoComparacoes\": ").append(ArquivoResultados.numero(r.razaoComparacoes));
            }
            sb.append(",\n").append(c).append("\"temposMs\": [");
            for (int j = 0; j < r.temposMs.size(); j++) {
                sb.append(j > 0 ? ", " : "").append(ArquivoResultados.numero(r.temposMs.get(j)));
            }
            sb.append("]");
        }
        sb.append("\n").append(recuo).append("}").append(ultimo ? "" : ",").append("\n");
    }

    private static String listaJson(int[] valores) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < valores.length; i++) {
            sb.append(i > 0 ? ", " : "").append(valores[i]);
        }
        return sb.append("]").toString();
    }
}
