// ============================================================
// banco.js: toda a conversa do site com o servidor Java.
// O navegador nunca fala direto com o MySQL: ele manda pedidos
// para o ServidorWeb.java (/api/...), que usa os DAOs com JDBC.
// ============================================================

// Regras do negócio usadas nos textos da tela (quem calcula de verdade é o Java).
const PRAZO_EMPRESTIMO = 14;
const MULTA_POR_DIA = 1.00;
const FORMAS_PAGAMENTO = [];

// Data de hoje no formato do banco (ano-mês-dia), pelo horário do computador.
function dataDeHoje() {
  const hoje = new Date();
  const doisDigitos = numero => String(numero).padStart(2, "0");
  return `${hoje.getFullYear()}-${doisDigitos(hoje.getMonth() + 1)}-${doisDigitos(hoje.getDate())}`;
}

// Quantos dias existem entre duas datas no formato do banco.
function diasEntre(dataInicial, dataFinal) {
  const umDiaEmMilissegundos = 24 * 60 * 60 * 1000;
  return Math.round((new Date(dataFinal + "T12:00") - new Date(dataInicial + "T12:00")) / umDiaEmMilissegundos);
}

// Transforma "2026-11-26" em "26/11/2026". Sem data, mostra um travessão.
function formatarData(data) {
  return data ? data.split("-").reverse().join("/") : "—";
}

// Endereço do servidor Java. Vazio = o site foi aberto pelo próprio Java (porta 8080).
// Se o site for aberto pelo Live Server, os pedidos vão para http://localhost:8080.
// Se o API_URL do config.js estiver preenchido (site no Netlify), ele é usado.
const abertoNoComputador = location.hostname === "localhost" || location.hostname === "127.0.0.1" || location.protocol === "file:";
const ENDERECO_DO_SERVIDOR = API_URL !== "" ? API_URL : (abertoNoComputador && location.port !== "8080" ? "http://localhost:8080" : "");

// Envia um pedido ao servidor e devolve a resposta já convertida de JSON.
// Se o Java recusar (status 400 ou mais), lança um erro com a mensagem dele.
function chamarServidor(metodo, caminho, dadosEnviados) {
  const pedido = new XMLHttpRequest();
  try {
    pedido.open(metodo, ENDERECO_DO_SERVIDOR + "/api/" + caminho, false);
    pedido.setRequestHeader("Content-Type", "application/json");
    pedido.send(dadosEnviados ? JSON.stringify(dadosEnviados) : null);
  } catch {
    throw new Error("Não foi possível falar com o servidor Java (desligado ou endereço da API incorreto em js/config.js). Verifique e recarregue a página.");
  }
  const resposta = pedido.responseText ? JSON.parse(pedido.responseText) : null;
  if (pedido.status >= 400) throw new Error(resposta && resposta.erro ? resposta.erro : "Erro " + pedido.status);
  return resposta;
}

// Cópia de todas as tabelas, pedida uma vez só (GET /api/estado).
// Depois de qualquer gravação a cópia é apagada, para a próxima leitura vir atualizada.
let copiaDoBanco = null;
const Banco = {
  ler() {
    if (!copiaDoBanco) copiaDoBanco = chamarServidor("GET", "estado");
    return copiaDoBanco;
  },
  limpar() { copiaDoBanco = null; }
};

// Grava algo no banco (POST ou DELETE) e descarta a cópia antiga.
function gravarNoBanco(metodo, caminho, dadosEnviados) {
  const resposta = chamarServidor(metodo, caminho, dadosEnviados);
  Banco.limpar();
  return resposta;
}

// Quem está conectado: { tipo, id, nome }, guardado no navegador (localStorage).
const CHAVE_DA_SESSAO = "sessao_ep_integrada";
const Sessao = {
  atual() { try { return JSON.parse(localStorage.getItem(CHAVE_DA_SESSAO)); } catch { return null; } },
  entrar(dadosDaSessao) { try { localStorage.setItem(CHAVE_DA_SESSAO, JSON.stringify(dadosDaSessao)); } catch {} },
  sair() { try { localStorage.removeItem(CHAVE_DA_SESSAO); } catch {} }
};

// Os DAOs abaixo têm os mesmos nomes dos DAOs do Java: cada um cuida de uma tabela.

const UsuarioDAO = {
  // Devolve a sessão se e-mail e senha estiverem certos, ou null se estiverem errados.
  autenticar(email, senha) {
    try { return chamarServidor("POST", "login", { email, senha }); }
    catch (erro) { if (/incorretos/.test(erro.message)) return null; throw erro; }
  },
  cadastrar(novoCliente) { return gravarNoBanco("POST", "usuarios", novoCliente); },
  buscarPorCpf(cpf) {
    const cpfSoNumeros = (cpf || "").replace(/\D/g, "");
    return Banco.ler().usuarios.find(cliente => cliente.cpf === cpfSoNumeros);
  }
};

const LivroDAO = {
  buscar(idLivro) { return Banco.ler().livros.find(livro => livro.id_livro === idLivro); },
  salvar(livro) { return gravarNoBanco("POST", "livros", livro); },
  remover(idLivro) { return gravarNoBanco("DELETE", "livros/" + idLivro); }
};

const ExemplarDAO = {
  cadastrar(idLivro, finalidade, idFuncionario) {
    return gravarNoBanco("POST", "exemplares", { id_livro: idLivro, finalidade, id_funcionario: idFuncionario });
  },
  remover(idExemplar) { return gravarNoBanco("DELETE", "exemplares/" + idExemplar); }
};

const EmprestimoDAO = {
  doCliente(idCliente) { return Banco.ler().emprestimos.filter(emprestimo => emprestimo.id_usuario === idCliente); },
  registrar(cpf, codigoDoExemplar, idFuncionario) {
    return gravarNoBanco("POST", "emprestimos", { cpf, codigo: codigoDoExemplar, id_funcionario: idFuncionario });
  },
  // Devolve a multa gerada pelo Java (ou null, se não houve atraso).
  devolver(idEmprestimo) {
    return gravarNoBanco("POST", `emprestimos/${idEmprestimo}/devolver`).multa;
  }
};

const MultaDAO = {
  // As multas não guardam o cliente: elas apontam para um empréstimo, que aponta para o cliente.
  doCliente(idCliente) {
    const idsDosEmprestimos = EmprestimoDAO.doCliente(idCliente).map(emprestimo => emprestimo.id_emprestimo);
    return Banco.ler().multas.filter(multa => idsDosEmprestimos.includes(multa.id_emprestimo));
  },
  pagar(idMulta) { return gravarNoBanco("POST", `multas/${idMulta}/pagar`); }
};

const VendaDAO = {
  doCliente(idCliente) { return Banco.ler().vendas.filter(venda => venda.id_usuario === idCliente); },
  itensDaVenda(idVenda) { return Banco.ler().itens_venda.filter(item => item.id_venda === idVenda); },
  // Compras do site não têm funcionário, então vão no nome do funcionário 1.
  registrar(idCliente, idsDosLivros, idFormaPagamento, idFuncionario) {
    if (!idsDosLivros.length) throw new Error("Carrinho vazio");
    return gravarNoBanco("POST", "vendas", { id_usuario: idCliente, ids_livros: idsDosLivros, id_forma_pagamento: idFormaPagamento, id_funcionario: idFuncionario || 1 });
  }
};

// Assim que a página abre: busca os dados e preenche as listas do dados.js.
// Cada livro ganha dois campos: quantos exemplares estão disponíveis para venda e para empréstimo.
let servidorLigado = true;
(function carregarDadosDoServidor() {
  let banco;
  try {
    banco = Banco.ler();
  } catch (erro) {
    servidorLigado = false;
    document.addEventListener("DOMContentLoaded", () => mostrarAviso(erro.message));
    return;
  }
  const disponiveis = (idLivro, finalidade) => banco.exemplares
    .filter(exemplar => exemplar.id_livro === idLivro && exemplar.finalidade === finalidade && exemplar.status === "Disponível").length;

  CATEGORIAS.push(...banco.categorias);
  FORMAS_PAGAMENTO.push(...banco.formas_pagamento);
  LIVROS.push(...banco.livros.map(livro => ({
    ...livro,
    disponiveis_venda: disponiveis(livro.id_livro, "Venda"),
    disponiveis_emprestimo: disponiveis(livro.id_livro, "Empréstimo")
  })));
})();
