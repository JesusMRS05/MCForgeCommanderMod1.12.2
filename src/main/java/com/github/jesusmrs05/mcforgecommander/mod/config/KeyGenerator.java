package com.github.jesusmrs05.mcforgecommander.mod.config;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KeyGenerator {
    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+{}[]|;:,.<>?";

    public static String generateKey(int length) {
        if (length < 1) throw new IllegalArgumentException("Length must be greater than 0");

        SecureRandom random = new SecureRandom();
        List<Character> characters = CHARACTERS.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());

        Collections.shuffle(characters, random);

        return characters.stream()
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
