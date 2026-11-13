package model.entities;

import java.math.BigDecimal;

public class PagamentoBoleto extends FormaPagamento {

    public PagamentoBoleto() {
        super();
    }

    public PagamentoBoleto(Integer idFormaPagamento) {
        super(idFormaPagamento, "Boleto");
    }

    @Override
    public void processarPagamento(BigDecimal valor) {
        System.out.println("Gerando linha digitável do boleto no valor de R$ " + valor);
    }
}