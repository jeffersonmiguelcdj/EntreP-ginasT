package model.entities;

import java.math.BigDecimal;

public class PagamentoCartao extends FormaPagamento {

    public PagamentoCartao() {
        super();
    }

    public PagamentoCartao(Integer idFormaPagamento, String tipoCartao) {
        super(idFormaPagamento, tipoCartao);
    }

    @Override
    public void processarPagamento(BigDecimal valor) {
        System.out.println("Processando transação com operadora de cartão: R$ " + valor);
    }
}