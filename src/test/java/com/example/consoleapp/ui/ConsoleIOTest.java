package com.example.consoleapp.ui;

import com.example.consoleapp.validation.UserInputParsers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleIOTest {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();


    private ConsoleIO consoleWithInput(String input) {
        return new ConsoleIO(
                new Scanner(input),
                new PrintStream(output, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return output.toString(StandardCharsets.UTF_8);
    }

    @Test
    void readLine_printsPromptAndReturnsLineAsIs() {
        ConsoleIO io = consoleWithInput("  hello  \n");

        String line = io.readLine("Введите: ");

        assertEquals("  hello  ", line);
        assertTrue(output().startsWith("Введите: "));
    }

    @Test
    void readLine_whenInputEnded_throwsInputClosedException() {
        ConsoleIO io = consoleWithInput("");

        assertThrows(InputClosedException.class, () -> io.readLine("> "));
    }

    @Test
    void read_validInput_returnsParsedValue() {
        ConsoleIO io = consoleWithInput("25\n");

        assertEquals(25, io.read("Возраст: ", UserInputParsers.AGE));
    }

    @Test
    void read_invalidThenValid_showsErrorAndAsksAgain() {
        ConsoleIO io = consoleWithInput("abc\n-1\n30\n");

        Integer age = io.read("Возраст: ", UserInputParsers.AGE);

        assertEquals(30, age);
        String out = output();
        assertEquals(3, countOccurrences(out, "Возраст: "), "приглашение должно быть показано 3 раза");
        assertTrue(out.contains("целым числом"));
        assertTrue(out.contains("от 0 до 150"));
    }

    @Test
    void read_onlyInvalidInputUntilEnd_throwsInputClosedException() {
        ConsoleIO io = consoleWithInput("abc\nxyz\n");

        assertThrows(InputClosedException.class, () -> io.read("Возраст: ", UserInputParsers.AGE));
    }

    @Test
    void read_doesNotLeaveNewlineInBuffer() {
        // Регрессия на баг scanner.nextInt(): после чтения числа следующая строка должна читаться целиком
        ConsoleIO io = consoleWithInput("25\nnext line\n");

        io.read("Возраст: ", UserInputParsers.AGE);

        assertEquals("next line", io.readLine("> "));
    }

    @ParameterizedTest(name = "[{0}]")
    @ValueSource(strings = {"\n", "   \n"})
    void readOptional_emptyInput_returnsEmpty(String input) {
        ConsoleIO io = consoleWithInput(input);

        assertEquals(Optional.empty(), io.readOptional("Имя: ", UserInputParsers.NAME));
    }

    @Test
    void readOptional_validInput_returnsValue() {
        ConsoleIO io = consoleWithInput("  Ivan \n");

        assertEquals(Optional.of("Ivan"), io.readOptional("Имя: ", UserInputParsers.NAME));
    }

    @Test
    void readOptional_invalidThenEmpty_returnsEmpty() {
        ConsoleIO io = consoleWithInput("Ivan1\n\n");

        assertEquals(Optional.empty(), io.readOptional("Имя: ", UserInputParsers.NAME));
        assertTrue(output().contains("только буквы"));
    }

    @ParameterizedTest(name = "\"{0}\" → да")
    @ValueSource(strings = {"д", "да", "Да", "y", "YES", " д "})
    void confirm_yes(String answer) {
        assertTrue(consoleWithInput(answer + "\n").confirm("Удалить?"));
    }

    @ParameterizedTest(name = "\"{0}\" → нет")
    @ValueSource(strings = {"н", "нет", "n", "No"})
    void confirm_no(String answer) {
        assertFalse(consoleWithInput(answer + "\n").confirm("Удалить?"));
    }

    @Test
    void confirm_invalidThenValid_asksAgain() {
        ConsoleIO io = consoleWithInput("может быть\nд\n");

        assertTrue(io.confirm("Удалить?"));
        assertTrue(output().contains("Введите «д» или «н»"));
    }

    @Test
    void constructor_nullArguments_throwNpe() {
        PrintStream out = new PrintStream(output);
        assertThrows(NullPointerException.class, () -> new ConsoleIO(null, out));
        assertThrows(NullPointerException.class, () -> new ConsoleIO(new Scanner(""), null));
    }

    private static int countOccurrences(String text, String fragment) {
        int count = 0;
        for (int i = text.indexOf(fragment); i >= 0; i = text.indexOf(fragment, i + fragment.length())) {
            count++;
        }
        return count;
    }
}
