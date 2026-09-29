public class InsertionSort {
    // Implementação do Insertion Sort para um sub-vetor.
    public static void sortTest(int[] array, int inicio, int fim) {
        for (int i = inicio + 1; i <= fim; i++) {
            int chave = array[i];
            int j = i - 1;

            // Move os elementos de array[inicio..i-1] que são maiores que a chave
            // para uma posição à frente de sua posição atual
            while (j >= inicio && array[j] > chave) {
                array[j + 1] = array[j];
                j = j - 1;
            }
            array[j + 1] = chave;
        }
    }


}
