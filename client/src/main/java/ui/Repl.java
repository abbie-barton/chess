package ui;

import chess.ChessGame;
import model.GameData;
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
        System.out.println("\n\n       " + SET_TEXT_BOLD + SET_TEXT_COLOR_LIGHT_GREY + notification.getMessage());
        printPrompt();
    }

}
