package com.example.consoleapp.ui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


final class TestConsole {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final ConsoleIO io;

    private TestConsole(String input) {
        this.io = new ConsoleIO(new Scanner(input),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }


    static TestConsole withInput(String... lines) {
        return new TestConsole(lines.length == 0 ? "" : String.join("\n", lines) + "\n");
    }

    ConsoleIO io() {
        return io;
    }

    String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
