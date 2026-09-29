
import java.io.IOException;
import java.util.Scanner;

public class Main {

    // Valor de M usado pelo QuickSort Híbrido e Aprimorado (-1 = ainda não definido)
    static int M = -1;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                clearScreen();
                System.out.println("=== MENU PRINCIPAL ===");
                System.out.println("M atual: " + (M == -1 ? "nao definido" : M));
                System.out.println("Resultados em: " + ArquivoResultados.pasta());
                System.out.println();
                System.out.println("[1] - Teste para definir M");
                System.out.println("[2] - Definir M manualmente");
                System.out.println("[3] - Bateria de testes (QuickSort Recursivo, Hibrido e Aprimorado)");
                System.out.println("[4] - Apagar resultados salvos");
                System.out.println("[5] - Sair");
                System.out.println("[6] - Limpar tela");
                System.out.print("\nEscolha uma opcao: ");

                switch (scanner.nextLine().trim()) {
                    case "1" -> {
                        M = TesteEmpirico.run(scanner);
                        pressEnterToClear(scanner);
                    }
                    case "2" -> {
                        System.out.print("Novo valor de M (Enter = cancelar): ");
                        String entradaM = scanner.nextLine().trim();
                        if (entradaM.isEmpty()) {
                            System.out.println("M nao foi alterado.");
                        } else {
                            int novoM = lerM(entradaM);
                            if (novoM == -1) {
                                System.out.println("M invalido: use um inteiro maior ou igual a 2. M nao foi alterado.");
                            } else {
                                M = novoM;
                                System.out.println("[OK] M definido como " + M + ".");
                            }
                        }
                        pressEnterToClear(scanner);
                    }
                    case "3" -> {
                        if (M == -1) {
                            System.out.print("M ainda nao foi definido. Digite um valor para M (Enter = rodar o Teste para definir M): ");
                            String entradaM = scanner.nextLine().trim();
                            if (entradaM.isEmpty()) {
                                M = TesteEmpirico.run(scanner);
                            } else {
                                M = lerM(entradaM);
                                if (M == -1) {
                                    System.out.println("M invalido: use um inteiro maior ou igual a 2.");
                                    pressEnterToClear(scanner);
                                    continue;
                                }
                            }
                        }
                        BateriaTestes.executar(scanner, M);
                        pressEnterToClear(scanner);
                    }
                    case "4" -> {
                        ArquivoResultados.apagarTodos(scanner);
                        pressEnterToClear(scanner);
                    }
                    case "5" -> {
                        clearScreen();
                        System.out.println("Saindo... Obrigado por usar o programa!");
                        return;
                    }
                    case "6" -> {
                        // a tela é limpa no início do laço
                    }
                    default -> {
                        System.out.println("Opcao invalida! Pressione Enter para continuar...");
                        scanner.nextLine();
                    }
                }
            }
        }
    }

    // Converte a entrada em M; retorna -1 se não for um inteiro >= 2
    // (M menor que 2 quebra a mediana de três em sub-vetores de 1 elemento)
    private static int lerM(String entrada) {
        try {
            int valor = Integer.parseInt(entrada);
            return valor >= 2 ? valor : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    //Clears the console screen
    public static void clearScreen() {
        try {
            // For Windows
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // For Unix/Linux/Mac
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (IOException | InterruptedException e) {
            // Fallback: print multiple newlines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    //Waits for user to press Enter and then clears the screen
    public static void pressEnterToClear(Scanner scanner) {
        System.out.println("\nPressione Enter para limpar a tela e continuar...");
        scanner.nextLine();
        clearScreen();
    }
}
