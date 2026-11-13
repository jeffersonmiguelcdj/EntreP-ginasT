package servidor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexaoBanco {
    private static Connection conexao;

    private ConexaoBanco() {}

    public static synchronized Connection abrir() {
        try {
            if (conexao == null || !conexao.isValid(2)) {
                Properties props = lerPropriedades();
                conexao = DriverManager.getConnection(props.getProperty("dburl"), props);
            }
            return conexao;
        } catch (SQLException e) {
            conexao = null;
            throw new ErroNegocio(traduzir(e));
        }
    }

    static String traduzir(SQLException e) {
        int erro = e.getErrorCode();
        if (erro == 1045) return "Erro: usuário ou senha do banco de dados incorretos (confira o db.properties).";
        if (erro == 1049) return "Erro: banco de dados (schema) não encontrado no servidor (confira o nome no dburl do db.properties).";
        if (erro == 0 || erro == 2002 || erro == 2003 || "08S01".equals(e.getSQLState())) {
            return "Erro: não foi possível conectar ao servidor MySQL (ele está ligado?).";
        }
        return "Erro ao conectar ao banco de dados. Código: " + erro;
    }

    /** Lê o db.properties (se existir) e deixa as variáveis de ambiente DB_URL, DB_USER e DB_PASSWORD sobrescreverem. */
    private static Properties lerPropriedades() {
        Properties props = new Properties();
        boolean temArquivo = Files.isRegularFile(Path.of("db.properties"));
        if (temArquivo) {
            try (FileInputStream entrada = new FileInputStream("db.properties")) {
                props.load(entrada);
            } catch (IOException e) {
                throw new ErroNegocio("Erro ao ler o db.properties: " + e.getMessage());
            }
        }
        String url = System.getenv("DB_URL");
        String usuario = System.getenv("DB_USER");
        String senha = System.getenv("DB_PASSWORD");
        if (url != null && !url.isBlank()) props.setProperty("dburl", url);
        if (usuario != null && !usuario.isBlank()) props.setProperty("user", usuario);
        if (senha != null) props.setProperty("password", senha);

        if (props.getProperty("dburl") == null || props.getProperty("dburl").isBlank()) {
            throw new ErroNegocio("Erro: configure a conexão com o banco no db.properties (pasta entrepaginas-java) ou nas variáveis DB_URL, DB_USER e DB_PASSWORD.");
        }
        return props;
    }
}
