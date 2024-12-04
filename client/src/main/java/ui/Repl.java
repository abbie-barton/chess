package ui;

import chess.ChessGame;
import model.GameData;
import model.ModifiedGameData;
import ui.websocket.NotificationHandler;
import websocket.messages.ServerMessage;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl implements NotificationHandler {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl, this);
    }

    public void run() {
        System.out.println(SET_TEXT_BOLD
                            + " ♔ Let's play chess! Sign in to start. ♔ ");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_FAINT + SET_TEXT_COLOR_MAGENTA + RESET_BG_COLOR + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE + RESET_BG_COLOR + "[" + client.state + "] ");
    }

    public void notify(ServerMessage notification) {
        if (notification.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
            ModifiedGameData game = notification.getGame();
            GameData newGame = new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(),
                    game.gameName(), game.game());
            client.setGame(game);
            client.drawBoard(newGame, null, null);
            printPrompt();
        } else {
            if (notification.getMessage() == null) {
                return;
            }
            if (notification.getMessage().contains("resign")) {
                ModifiedGameData game = client.getGame();
                // set is_over to true
                ModifiedGameData newGame = new ModifiedGameData(game.gameID(), game.whiteUsername(),
                        game.blackUsername(), game.gameName(), game.game(), 1);
                client.setGame(newGame);
            }
            System.out.println(RESET_BG_COLOR + "\n\n       " + SET_TEXT_BOLD + SET_TEXT_COLOR_LIGHT_GREY
                    + notification.getMessage());
            printPrompt();
        }
    }

}
