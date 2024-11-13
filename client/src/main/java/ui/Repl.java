package ui;

import java.util.Scanner;

public class Repl {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);
    }

    public void run() {
        System.out.println("\uD83D\uDC36 Let's play chess! Sign in to start.");

        Scanner scanner = new Scanner(System.in);
        var result = "";

        System.out.println();
    }
}
