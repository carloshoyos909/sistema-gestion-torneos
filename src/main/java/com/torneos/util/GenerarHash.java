package com.torneos.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerarHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("12345");
        System.out.println("Hash BCrypt de '12345': " + hash);
        System.out.println("Verificacion: " + encoder.matches("12345", hash));
    }
}
