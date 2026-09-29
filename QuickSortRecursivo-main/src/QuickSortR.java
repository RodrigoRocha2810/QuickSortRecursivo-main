
public class QuickSortR {
    // long: no pior caso O(n²) as contagens passam do limite de int
    static long trocas;
    static long comparacoes;
    // Implementação do QuickSort Recursivo
    public static void quicksort(int[] vetor, int inicio, int fim) {
        comparacoes++;
        if (inicio < fim) {
            int indicePivo = particiona(vetor, inicio, fim);
            quicksort(vetor, inicio, indicePivo - 1);
            quicksort(vetor, indicePivo + 1, fim);
        }
    }
    // Implementação do QuickSort Recursivo com tamanho M
    public static void quicksortM(int[] array, int inicio, int fim, int M) {
        // A condição de parada é baseada no tamanho M
        comparacoes++;
        if ((fim - inicio + 1) >= M) {
            int indicePivo = particiona(array, inicio, fim);
            quicksortM(array, inicio, indicePivo - 1, M);
            quicksortM(array, indicePivo + 1, fim, M);
        }
    }
    // Implementação do QuickSort Recursivo com mediana de três e tamanho M
    public static void quicksortA(int[] array, int inicio, int fim, int M) {
        // A condição de parada é baseada no tamanho M
        comparacoes++;
        if ((fim - inicio + 1) >= M) {
            int indicePivo = particiona3(array, inicio, fim);
            quicksortA(array, inicio, indicePivo - 1, M);
            quicksortA(array, indicePivo + 1, fim, M);
        }
    }

    
     // Método responsável APENAS por particionar um sub-vetor. Utiliza a mediana de três
    private static int particiona3(int[] array, int inicio, int fim) {
        int pivo = medianaDeTres(array, inicio, fim);

        int i = inicio;
        int j = fim - 1;

        while (true) {
            comparacoes++;
            while (array[i] < pivo) {
                comparacoes++;
                i++;
            }
            while (j > inicio && array[j] > pivo) {
                comparacoes= comparacoes + 2;
                j--;
            }
            comparacoes++;
            if (i >= j) {
                break;
            }

            troca(array, i, j);
            i++;
            j--;
        }

        // Coloca o pivô (que estava em fim-1) na sua posição final
        troca(array, i, fim - 1);
        return i;
    }

    //Encontra a mediana de três e posiciona o pivô.
    private static int medianaDeTres(int[] array, int inicio, int fim) {
        int meio = inicio + (fim - inicio) / 2;
        // Ordena os três valores: array[inicio], array[meio], array[fim]
         comparacoes= comparacoes + 3;
        if (array[inicio] > array[meio]) {
            troca(array, inicio, meio);
        }
        if (array[inicio] > array[fim]) {
            troca(array, inicio, fim);
        }
        if (array[meio] > array[fim]) {
            troca(array, meio, fim);
        }

        // A mediana (array[meio]) é movida para a penúltima posição
        troca(array, meio, fim - 1);
        return array[fim - 1];
    }

    // Método responsável APENAS por particionar um sub-vetor.
    private static int particiona(int[] vetor, int inicio, int fim) {

        int pivo = vetor[fim];
        int i = inicio - 1;
        for (int j = inicio; j < fim; j++, comparacoes++) {
            comparacoes++;
            if (vetor[j] <= pivo) {
                i++;
                troca(vetor, i, j);
            }
        }
        troca(vetor, i + 1, fim);
        return i + 1;
    }

    private static void troca(int[] vetor, int i, int j) {
        int temp = vetor[i];
        vetor[i] = vetor[j];
        vetor[j] = temp;
        trocas++;
    }
}
