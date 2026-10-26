package com.torneos.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class ActualizarContrasena {
    public static void main(String[] args) throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("12345");

        System.out.println("Hash generado: " + hash);
        System.out.println("Verificacion: " + encoder.matches("12345", hash));

        String url = "jdbc:postgresql://localhost:5434/torneos";
        String user = "postgres";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "UPDATE usuarios SET password = ? WHERE username IN ('admin', 'organizador1', 'jugador1')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, hash);
            int rows = stmt.executeUpdate();
            System.out.println("Usuarios actualizados: " + rows);
        }
    }
}
