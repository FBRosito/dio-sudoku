import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import model.Board;
import model.Space;
import util.BoardTemplate;

public class Main {

    private final static Scanner scanner = new Scanner(System.in);

    private static Board board;

    private final static int BOARD_LIMIT = 9;

    public static void main(String[] args) throws Exception {
        final var positions = Stream.of(args)
                .collect(Collectors.toMap(
                        k -> k.split(";")[0],
                        v -> v.split(";")[1]
                        ));
        System.out.println("Bem-vindo ao jogo de Sudoku!");

        var option = -1;
        while (true){
            System.out.println("Selecione uma das opções a seguir");
            System.out.println("1 - Iniciar um novo Jogo");
            System.out.println("2 - Colocar um novo número");
            System.out.println("3 - Remover um número");
            System.out.println("4 - Visualizar jogo atual");
            System.out.println("5 - Verificar status do jogo");
            System.out.println("6 - limpar jogo");
            System.out.println("7 - Finalizar jogo");
            System.out.println("8 - Sair");

            option = scanner.nextInt();

            switch (option){
                case 1 -> startGame(positions);
                case 2 -> inputNumber();
                case 3 -> removeNumber();
                case 4 -> showCurrentGame();
                case 5 -> showGameStatus();
                case 6 -> clearGame();
                case 7 -> finishGame();
                case 8 -> System.exit(0);
                default -> System.out.println("Opção inválida, selecione uma das opções do menu");
            }
        } 
    }

    private static void startGame(final Map<String, String> positions) {
        if (nonNull(board)) {
            System.out.println("Já existe um jogo em andamento, finalize o jogo atual antes de iniciar um novo");
            return;
        }
        List<List<Space>> spaces = new ArrayList<>();
        for (int i = 0; i < BOARD_LIMIT; i++) {
            spaces.add(new ArrayList<>());
            for (int j = 0; j < BOARD_LIMIT; j++) {
                var positionConfig = positions.get("%s,%s".formatted(i, j));
                var expected = Integer.parseInt(positionConfig.split(",")[0]);
                var fixed = Boolean.parseBoolean(positionConfig.split(",")[1]);
                var currentSpace = new Space(expected, fixed);
                spaces.get(i).add(currentSpace);
            }
        }

        board = new Board(spaces);
        System.out.println("O jogo está pronto para começar.");
    }

    private static void inputNumber() {
        if (isNull(board)) {
            System.out.println("Não existe um jogo em andamento, inicie um novo jogo");
            return;
        }
        System.out.println("Informe a coluna que deseja colocar o número.");
        var col = runUntilGetValidNumber(0, BOARD_LIMIT - 1);

        System.out.println("Informe a linha que deseja colocar o número.");
        var row = runUntilGetValidNumber(0, BOARD_LIMIT - 1);

        System.out.println("Informe o número que deseja colocar na posição [%s, %s]".formatted(col, row));
        var value = runUntilGetValidNumber(1, BOARD_LIMIT);

        if (!board.changeValue(col, row, value)) {
            System.out.println("A posição [%s, %s] possui um valor fixo.\n".formatted(col, row));
        }
    }

    private static void removeNumber() {
        if (isNull(board)) {
            System.out.println("Não existe um jogo em andamento, inicie um novo jogo");
            return;
        }

        System.out.println("Informe a coluna que deseja remover o número.");
        var col = runUntilGetValidNumber(0, BOARD_LIMIT - 1);

        System.out.println("Informe a linha que deseja remover o número.");
        var row = runUntilGetValidNumber(0, BOARD_LIMIT - 1);

        if (!board.clearValue(col, row)) {
            System.out.println("A posição [%s, %s] possui um valor fixo.\n".formatted(col, row));
        }

        System.out.println("Número da posição [%s, %s] removido com sucesso.\n".formatted(col, row));
    }

    private static void showCurrentGame() {
        if (isNull(board)) {
            System.out.println("Não existe um jogo em andamento, inicie um novo jogo");
            return;
        }

        var args = new Object[81];
        var argsPos = 0;
        for (int i = 0; i < BOARD_LIMIT; i++) {
            for (var col: board.getSpaces()) {
                args[argsPos++] = " " + (isNull(col.get(i).getActual()) ? " " : col.get(i).getActual());
            }
        }
        System.out.println("Seu jogo se encontra da seguinte forma:");
        System.out.printf((BoardTemplate.BOARD_TEMPLATE) + "\n", args);
    }

    private static void showGameStatus() {
        if (isNull(board)) {
            System.out.println("Não existe um jogo em andamento, inicie um novo jogo");
            return;
        }

        System.out.printf("O jogo atualmente se encontra no status: %s\n", board.getStatus().getLabel());
        if (board.hasErrors()) {
            System.out.println("O jogo possui erros, verifique os números colocados");
        } else {
            System.out.println("O jogo não possui erros");
        }
    }

    private static void clearGame() {
        if (isNull(board)) {
            System.out.println("Não existe um jogo em andamento, inicie um novo jogo");
            return;
        }

        System.out.println("Você tem certeza que deseja limpar o jogo atual? (s/n)");
        var confirm = scanner.next();
        while (confirm.equalsIgnoreCase("s") || confirm.equalsIgnoreCase("n")) {
            if (confirm.equalsIgnoreCase("s")) {
                board.reset();
                System.out.println("O jogo foi limpo com sucesso");
                break;
            } else if (confirm.equalsIgnoreCase("n")) {
                System.out.println("O jogo não foi limpo");
                break;
            } else {
                System.out.println("Opção inválida, selecione s ou n");
                confirm = scanner.next();
            }
        }
    }

    private static void finishGame() {
        if (isNull(board)) {
            System.out.println("Não existe um jogo em andamento, inicie um novo jogo");
            return;
        }
        if (board.gameIsFinished()) {
            System.out.println("Parabéns, você finalizou o jogo com sucesso!");
            showCurrentGame();
            board = null;
        } else {
            System.out.println("O jogo não foi finalizado, verifique os números colocados");
        }
    }

    

    private static int runUntilGetValidNumber(final int min, final int max) {
        var current = scanner.nextInt();
        while (current < min || current > max) {
            System.out.println("Informe um número entre %s e %s".formatted(min, max));
            current = scanner.nextInt();
        }
        return current;
    }

}
