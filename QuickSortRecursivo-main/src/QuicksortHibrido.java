
public class QuicksortHibrido {

     // Este valor será determinado empiricamente.
    private int M;

    public QuicksortHibrido(int m) {
        if (m < 1) {
            throw new IllegalArgumentException("M deve ser pelo menos 1.");
        }
        this.M = m;
    }

    // Método público que inicia a ordenação.
    public void ordenar(int[] array) {
        if (array == null || array.length == 0) {
            return;
        }
        // Inicia o processo recursivo do Quicksort
        QuickSortR.quicksortM(array, 0, array.length - 1, M);
        // Ao final, o Insertion Sort ordena os pequenos sub-vetores restantes
        InsertionSort.sortTest(array, 0, array.length - 1);
    }
}