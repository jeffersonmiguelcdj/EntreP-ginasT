package model.entities;

import java.math.BigDecimal;

public class PagamentoDinheiro extends FormaPagamento {

    public PagamentoDinheiro() {
        super(null, "Dinheiro");
    }

    public PagamentoDinheiro(Integer idFormaPagamento) {
        super(idFormaPagamento, "Dinheiro");
    }

    public PagamentoDinheiro(Integer idFormaPagamento, String tipo) {
        super(idFormaPagamento, tipo);
    }

    @Override
    public void processarPagamento(BigDecimal valor) {
        System.out.println("Pagamento em Dinheiro no valor de R$ " + valor + " recebido com sucesso.");
    }
}