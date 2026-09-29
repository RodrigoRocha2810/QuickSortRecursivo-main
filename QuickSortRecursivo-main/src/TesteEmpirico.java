
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

// Teste empírico para definir M: mede o QuickSort Híbrido com cada M de um intervalo
public class TesteEmpirico {

    private static final int TAMANHO_PADRAO = 50000;
    private static final int M_MINIMO_PADRAO = 5;
    private static final int M_MAXIMO_PADRAO = 50;
    private static final int TESTES_POR_M_PADRAO = 1000; //para ter uma média robusta
    private static final int EXECUCOES_AQUECIMENTO = 1000; // fase de aquecimento (fixa)
    //Antes do loop principal que testa os valores de M,
    //um loop que executa o código de ordenação milhares de vezes sem medir o tempo.
    //Isso força a compilação JIT a otimizar todos os métodos (ordenar, quicksort, particionar,
    //insertionSort) antes que qualquer medição real comece. Forçando a JVM a "esquentar" e otimizar o código,
    //garantimos que os tempos medidos reflitam o desempenho otimizado do algoritmo.

    // Pergunta os parâmetros, executa o teste, salva o JSON e retorna o melhor M
    public static int run(Scanner scanner) {
        System.out.println("\n=== TESTE PARA DEFINIR M ===");
        int tamanho = ArquivoResultados.perguntarInteiro(scanner, "Tamanho do vetor", TAMANHO_PADRAO, 2);
        // M menor que 2 quebra a mediana de três em sub-vetores de 1 elemento
        int mMinimo = ArquivoResultados.perguntarInteiro(scanner, "M minimo", M_MINIMO_PADRAO, 2);
        int mMaximo = ArquivoResultados.perguntarInteiro(scanner, "M maximo", Math.max(M_MAXIMO_PADRAO, mMinimo), mMinimo);
        int testesPorM = ArquivoResultados.perguntarInteiro(scanner, "Testes por M", TESTES_POR_M_PADRAO, 1);

        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        long semente = new Random().nextLong();
        int[] vetorOriginal = gerarVetorAleatorio(tamanho, semente);

        // --- FASE DE AQUECIMENTO  ---
        System.out.println("\nIniciando fase de aquecimento da JVM...");
        QuicksortHibrido sorterAquecimento = new QuicksortHibrido(mMinimo); // Usa um M qualquer
        for (int i = 0; i < EXECUCOES_AQUECIMENTO; i++) {
            int[] copiaVetor = Arrays.copyOf(vetorOriginal, vetorOriginal.length);
            sorterAquecimento.ordenar(copiaVetor); // Executa o código para forçar a otimização JIT
        }
        System.out.println("Aquecimento concluido. Iniciando testes reais...");
        // ------------------------------------

        System.out.printf("Tamanho do vetor: %d | Testes por M: %d\n\n", tamanho, testesPorM);

        long[] temposMedios = new long[mMaximo - mMinimo + 1];
        long melhorTempo = Long.MAX_VALUE;
        int melhorM = mMinimo;

        for (int M = mMinimo; M <= mMaximo; M++) {
            QuicksortHibrido sorter = new QuicksortHibrido(M);
            long inicio = System.nanoTime();
            //Se escolhermos um M muito grande (ex: M=100 para um vetor de 1000 elementos),
            //estaremos forçando o algoritmo a parar de particionar cedo demais. Com isso,
            //o Insertion Sort final teria que ordenar blocos muito grandes. Ordenar um bloco
            //de 100 elementos com um algoritmo O(n2) será significativamente mais lento do que
            //deixar o Quicksort (O(nlogn)) particioná-lo mais algumas vezes.
            //Para valores de M extremamente pequenos (como 2, 3 ou 4), a otimização quase não traz benefícios.
            for (int i = 0; i < testesPorM; i++) {
                int[] copiaVetor = Arrays.copyOf(vetorOriginal, vetorOriginal.length);
                sorter.ordenar(copiaVetor);
            }

            // Termina a medição DEPOIS que todos os testes para M foram concluídos
            long fim = System.nanoTime();

            // O tempo médio é o tempo total dividido pelo número de testes
            long tempoMedio = (fim - inicio) / testesPorM;
            temposMedios[M - mMinimo] = tempoMedio;

            System.out.printf("M = %2d  | Tempo medio: %d nanossegundos\n", M, tempoMedio);

            if (tempoMedio < melhorTempo) {
                melhorTempo = tempoMedio;
                melhorM = M;
            }
        }

        System.out.println("\n-------------------------------------------------");
        System.out.printf("Conclusao: O melhor valor de M encontrado foi %d com um tempo medio de %d ns.\n", melhorM, melhorTempo);
        System.out.println("-------------------------------------------------");

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"dataHora\": ").append(ArquivoResultados.texto(dataHora)).append(",\n");
        sb.append("  \"versaoJava\": ").append(ArquivoResultados.texto(System.getProperty("java.version"))).append(",\n");
        sb.append("  \"tamanhoVetor\": ").append(tamanho).append(",\n");
        sb.append("  \"mMinimo\": ").append(mMinimo).append(",\n");
        sb.append("  \"mMaximo\": ").append(mMaximo).append(",\n");
        sb.append("  \"testesPorM\": ").append(testesPorM).append(",\n");
        sb.append("  \"execucoesAquecimento\": ").append(EXECUCOES_AQUECIMENTO).append(",\n");
        sb.append("  \"semente\": ").append(semente).append(",\n");
        sb.append("  \"melhorM\": ").append(melhorM).append(",\n");
        sb.append("  \"melhorTempoMedioNs\": ").append(melhorTempo).append(",\n");
        sb.append("  \"resultados\": [\n");
        for (int i = 0; i < temposMedios.length; i++) {
            sb.append("    { \"M\": ").append(mMinimo + i)
              .append(", \"tempoMedioNs\": ").append(temposMedios[i]).append(" }")
              .append(i < temposMedios.length - 1 ? "," : "").append("\n");
        }
        sb.append("  ]\n}\n");

        // Nome com os parâmetros e o resultado: melhor M e seu tempo médio
        String nome = "Teste M - Tamanho = " + ArquivoResultados.emMilhares(tamanho)
                    + " M = " + mMinimo + " a " + mMaximo
                    + " Testes = " + ArquivoResultados.emMilhares(testesPorM)
                    + " - Melhor M = " + melhorM + " Tempo medio = " + melhorTempo + " ns";
        try {
            Path arquivo = ArquivoResultados.salvar(nome, sb.toString());
            System.out.println("[OK] Resultados salvos em: " + arquivo);
        } catch (IOException e) {
            System.out.println("Erro ao salvar o arquivo JSON: " + e.getMessage());
        }
        return melhorM;
    }

    private static int[] gerarVetorAleatorio(int tamanho, long semente) {
        int[] vetor = new int[tamanho];
        Random random = new Random(semente);
        for (int i = 0; i < tamanho; i++) {
            vetor[i] = random.nextInt();
        }
        return vetor;
    }
}
