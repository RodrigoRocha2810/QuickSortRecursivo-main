import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

// Gravação e remoção dos arquivos JSON na pasta Resultados do projeto
public class ArquivoResultados {

    // Pasta Resultados na raiz do projeto (vizinha de src e bin)
    private static final String PASTA_RESULTADOS = "Resultados";

    // Salva o conteúdo em Resultados/<nomeBase>.json; se já existir, usa " (2)", " (3)"...
    public static Path salvar(String nomeBase, String conteudo) throws IOException {
        Path pasta = pasta();
        Files.createDirectories(pasta);
        Path arquivo = pasta.resolve(nomeBase + ".json");
        for (int n = 2; Files.exists(arquivo); n++) {
            arquivo = pasta.resolve(nomeBase + " (" + n + ").json");
        }
        Files.writeString(arquivo, conteudo, StandardCharsets.UTF_8);
        return arquivo;
    }

    // Apaga os arquivos .json da pasta Resultados, após confirmação
    public static void apagarTodos(Scanner scanner) {
        Path pasta = pasta();
        if (!Files.isDirectory(pasta)) {
            System.out.println("Nenhum resultado salvo em " + pasta);
            return;
        }
        List<Path> arquivos = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pasta, "*.json")) {
            for (Path arquivo : stream) {
                arquivos.add(arquivo);
            }
        } catch (IOException e) {
            System.out.println("Erro ao listar a pasta " + pasta.toAbsolutePath() + ": " + e.getMessage());
            return;
        }

        if (arquivos.isEmpty()) {
            System.out.println("Nenhum resultado salvo em " + pasta.toAbsolutePath());
            return;
        }

        System.out.println("\nArquivos de resultado em " + pasta.toAbsolutePath() + ":");
        for (Path arquivo : arquivos) {
            System.out.println("  " + arquivo.getFileName());
        }
        System.out.print("\nApagar estes " + arquivos.size() + " arquivo(s)? Nao ha como desfazer. (S/N): ");
        String confirmacao = scanner.nextLine().trim();
        if (!confirmacao.equalsIgnoreCase("S")) {
            System.out.println("Nada foi apagado.");
            return;
        }

        int apagados = 0;
        for (Path arquivo : arquivos) {
            try {
                Files.delete(arquivo);
                apagados++;
            } catch (IOException e) {
                System.out.println("Nao foi possivel apagar " + arquivo.getFileName() + ": " + e.getMessage());
            }
        }
        System.out.println("[OK] " + apagados + " arquivo(s) apagado(s).");
    }

    // Caminho absoluto da pasta Resultados na raiz do projeto, calculado uma vez
    private static Path pasta;

    public static Path pasta() {
        if (pasta == null) {
            pasta = localizarRaizProjeto().resolve(PASTA_RESULTADOS).toAbsolutePath().normalize();
        }
        return pasta;
    }

    // Procura a raiz do projeto (pasta que contém src/Main.java) a partir de onde os
    // .class estão e da pasta do terminal, subindo na árvore e olhando também uma pasta
    // abaixo. Assim o caminho absoluto é descoberto em qualquer máquina, sem configuração.
    private static Path localizarRaizProjeto() {
        List<Path> pontosDePartida = new ArrayList<>();
        try {
            pontosDePartida.add(Paths.get(ArquivoResultados.class.getProtectionDomain()
                                          .getCodeSource().getLocation().toURI()));
        } catch (Exception e) {
            // segue só com a pasta do terminal
        }
        Path terminal = Paths.get("").toAbsolutePath();
        pontosDePartida.add(terminal);

        for (Path inicio : pontosDePartida) {
            for (Path p = inicio.toAbsolutePath(); p != null; p = p.getParent()) {
                if (ehRaizProjeto(p)) {
                    return p;
                }
                try (DirectoryStream<Path> filhos = Files.newDirectoryStream(p, Files::isDirectory)) {
                    for (Path filho : filhos) {
                        if (ehRaizProjeto(filho)) {
                            return filho;
                        }
                    }
                } catch (IOException e) {
                    // pasta sem permissão de leitura: continua subindo
                }
            }
        }
        return terminal;
    }

    private static boolean ehRaizProjeto(Path p) {
        return Files.isRegularFile(p.resolve("src").resolve("Main.java"));
    }

    // Quantidade em milhares para nomes de arquivo: 1000 -> "1k", 100000 -> "100k",
    // 1500 -> "1.5k"; abaixo de 1000 fica como está
    public static String emMilhares(int valor) {
        if (valor < 1000) {
            return String.valueOf(valor);
        }
        return BigDecimal.valueOf(valor, 3).stripTrailingZeros().toPlainString() + "k";
    }

    // ---------- Auxiliares de JSON ----------

    public static String numero(double valor) {
        return String.format(Locale.ROOT, "%.4f", valor);
    }

    public static String texto(String valor) {
        return "\"" + valor.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    // ---------- Leitura de parâmetros ----------

    // Pergunta um inteiro até receber um valor válido; Enter usa o padrão
    public static int perguntarInteiro(Scanner scanner, String rotulo, int padrao, int minimo) {
        while (true) {
            System.out.print(rotulo + " (Enter = " + padrao + "): ");
            String entrada = scanner.nextLine().trim().replace(".", "").replace("_", "");
            if (entrada.isEmpty()) {
                return padrao;
            }
            try {
                int valor = Integer.parseInt(entrada);
                if (valor >= minimo) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // pergunta de novo
            }
            System.out.println("  Valor invalido: digite um inteiro maior ou igual a " + minimo + ".");
        }
    }
}
