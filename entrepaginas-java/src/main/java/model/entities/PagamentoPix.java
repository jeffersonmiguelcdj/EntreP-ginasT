package model.entities;

import java.math.BigDecimal;

public class PagamentoPix extends FormaPagamento {

    public PagamentoPix() {
        super();
    }

    public PagamentoPix(Integer idFormaPagamento) {
        super(idFormaPagamento, "Pix");
    }

    @Override
    public void processarPagamento(BigDecimal valor) {
        System.out.println("Processando pagamento via Pix no valor de R$ " + valor + " (QR Code gerado).");
    }
}