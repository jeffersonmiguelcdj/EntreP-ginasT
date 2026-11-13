package servidor;

public class ErroNegocio extends RuntimeException {
    public ErroNegocio(String mensagem) {
        super(mensagem);
    }
}
