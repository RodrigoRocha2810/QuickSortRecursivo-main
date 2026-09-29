import java.util.Random;

// Tipos de vetor de entrada usados na bateria de testes
public enum TipoVetor {
    ALEATORIO("Aleatorio"),
    ORDENADO("Ja ordenado"),
    INVERSO("Ordenado de forma inversa"),
    REPETIDOS("Muitos elementos repetidos");

    // Quantidade de valores distintos no vetor com muitos repetidos
    public static final int VALORES_DISTINTOS_REPETIDOS = 10;

    private final String descricao;

    TipoVetor(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    // Gera um vetor deste tipo com o tamanho informado
    public int[] gerar(int tamanho, Random random) {
        int[] vetor = new int[tamanho];
        switch (this) {
            case ALEATORIO -> {
                for (int i = 0; i < tamanho; i++) {
                    vetor[i] = random.nextInt(tamanho) + 1;
                }
            }
            case ORDENADO -> {
                for (int i = 0; i < tamanho; i++) {
                    vetor[i] = i + 1;
                }
            }
            case INVERSO -> {
                for (int i = 0; i < tamanho; i++) {
                    vetor[i] = tamanho - i;
                }
            }
            case REPETIDOS -> {
                for (int i = 0; i < tamanho; i++) {
                    vetor[i] = random.nextInt(VALORES_DISTINTOS_REPETIDOS) + 1;
                }
            }
        }
        return vetor;
    }
}
