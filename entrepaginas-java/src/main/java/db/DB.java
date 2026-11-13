package db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DB {

    private static Connection conn = null;

    public static Connection getConnection() {
        if (conn == null) {
            try {
                Properties props = loadProperties();
                String url = props.getProperty("dburl");
                conn = DriverManager.getConnection(url, props);
            } catch (SQLException e) {
                throw new RuntimeException("Falha ao ligar ao MySQL: " + e.getMessage(), e);
            }
        }
        return conn;
    }


    private static Properties loadProperties() {
        Properties props = new Properties();
        try (FileInputStream fs = new FileInputStream("db.properties")) {
            props.load(fs);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler o ficheiro db.properties: " + e.getMessage(), e);
        }
        // Variáveis de ambiente (opcionais) sobrescrevem o arquivo
        String url = System.getenv("DB_URL");
        String usuario = System.getenv("DB_USER");
        String senha = System.getenv("DB_PASSWORD");
        if (url != null && !url.isBlank()) props.setProperty("dburl", url);
        if (usuario != null && !usuario.isBlank()) props.setProperty("user", usuario);
        if (senha != null) props.setProperty("password", senha);
        return props;
    }


    public static void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}