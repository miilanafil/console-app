package com.example.consoleapp.validation;


@FunctionalInterface
public interface InputParser<T> {

    T parse(String input);
}
