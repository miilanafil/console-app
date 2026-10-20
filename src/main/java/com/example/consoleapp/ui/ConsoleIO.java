package com.example.consoleapp.ui;

import com.example.consoleapp.validation.InputParser;
import com.example.consoleapp.validation.ValidationException;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;


public class ConsoleIO {

    private final Scanner in;
    private final PrintStream out;

    public ConsoleIO(Scanner in, PrintStream out) {
        this.in = Objects.requireNonNull(in, "in");
        this.out = Objects.requireNonNull(out, "out");
    }

    public void print(String text) {
        out.print(text);
    }

    public void println(String text) {
        out.println(text);
    }

    public void println() {
        out.println();
    }

    public void printf(String format, Object... args) {
        out.printf(format, args);
    }

    public String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        if (!in.hasNextLine()) {
            out.println();
            throw new InputClosedException();
        }
        return in.nextLine();
    }


    public <T> T read(String prompt, InputParser<T> parser) {
        while (true) {
            String line = readLine(prompt);
            try {
                return parser.parse(line);
            } catch (ValidationException e) {
                println("  ! " + e.getMessage());
            }
        }
    }


    public <T> Optional<T> readOptional(String prompt, InputParser<T> parser) {
        while (true) {
            String line = readLine(prompt);
            if (line.isBlank()) {
                return Optional.empty();
            }
            try {
                return Optional.of(parser.parse(line));
            } catch (ValidationException e) {
                println("  ! " + e.getMessage());
            }
        }
    }


    public boolean confirm(String question) {
        return read(question + " (д/н): ", ConsoleIO::parseYesNo);
    }

    static boolean parseYesNo(String input) {
        String answer = input == null ? "" : input.strip().toLowerCase(java.util.Locale.ROOT);
        switch (answer) {
            case "д", "да", "y", "yes":
                return true;
            case "н", "нет", "n", "no":
                return false;
            default:
                throw new ValidationException("Введите «д» или «н»");
        }
    }
}
