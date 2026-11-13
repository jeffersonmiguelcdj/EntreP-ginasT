// ============================================================
// app.js: funções usadas por várias páginas
// (capas, carrinho, avisos, sugestões de busca, cabeçalho e máscaras).
// ============================================================

// Uma cor de capa para cada categoria (as capas são desenhadas com CSS).
const CORES_DAS_CAPAS = ["#1f4d3a", "#7a3e2b", "#2e3a59", "#5b4a7a", "#8a6a2f", "#3d5a5a", "#6b2f45", "#c06a3b"];

// Recebe o id da categoria e devolve o nome dela ("Romance", por exemplo).
function nomeDaCategoria(idCategoria) {
  const categoria = CATEGORIAS.find(categoria => categoria.id_categoria === idCategoria);
  return categoria ? categoria.nome : "";
}

// Transforma 34.9 em "R$ 34,90".
function formatarPreco(valor) {
  return valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

// HTML da capa colorida de um livro (título e primeiro autor).
function htmlCapa(livro) {
  const cor = CORES_DAS_CAPAS[(livro.id_categoria - 1) % CORES_DAS_CAPAS.length];
  return `<div class="capa" style="background:${cor}">
      <span class="capa__titulo">${livro.titulo}</span>
      <span class="capa__autor">${livro.autores[0]}</span>
    </div>`;
}

// HTML do cartão de livro usado nas vitrines e no catálogo (leva para a página do livro).
function htmlCartao(livro) {
  return `<a class="cartao" href="livro.html?id=${livro.id_livro}">
      ${htmlCapa(livro)}
      <div>
        <div class="cartao__titulo">${livro.titulo}</div>
        <div class="cartao__autor">${livro.autores.join(", ")} · ${livro.ano_publicacao}</div>
      </div>
      <div class="cartao__preco">${formatarPreco(livro.preco_venda)}</div>
    </a>`;
}

// Lê um valor do endereço da página. Em "livro.html?id=9", lerParametroDaUrl("id") devolve "9".
function lerParametroDaUrl(nome) {
  return new URLSearchParams(window.location.search).get(nome);
}

// ----- Carrinho -----
// O carrinho é uma lista de ids de livros guardada no navegador, uma por cliente.
// Exemplo: [9, 9, 3] = dois exemplares do livro 9 e um do livro 3.

function chaveDoCarrinho() {
  const sessao = Sessao.atual();
  return sessao && sessao.tipo === "Cliente" ? `carrinho_atual_${sessao.id}` : null;
}

function lerCarrinho() {
  const chave = chaveDoCarrinho();
  if (!chave) return [];
  try { return JSON.parse(localStorage.getItem(chave)) || []; } catch { return []; }
}

function salvarCarrinho(idsDosLivros) {
  const chave = chaveDoCarrinho();
  if (chave) try { localStorage.setItem(chave, JSON.stringify(idsDosLivros)); } catch {}
  atualizarContadorDoCarrinho();
}

// Só cliente compra. Sem login, manda para o login e volta para esta página depois.
function exigirLoginCliente() {
  const sessao = Sessao.atual();
  if (sessao && sessao.tipo === "Cliente") return true;
  if (sessao) { mostrarAviso("Funcionários registram vendas pelo painel"); return false; }
  const paginaAtual = location.pathname.split("/").pop() + location.search;
  window.location.href = "login.html?voltar=" + encodeURIComponent(paginaAtual);
  return false;
}

// Apaga o carrinho antigo de versões anteriores do site (ele não era separado por cliente).
try { localStorage.removeItem("carrinho"); } catch {}

function quantidadeNoCarrinho(idLivro) {
  return lerCarrinho().filter(id => id === idLivro).length;
}

// Adiciona um exemplar ao carrinho, sem passar do estoque. Devolve true se deu certo.
function adicionarAoCarrinho(idLivro) {
  if (!exigirLoginCliente()) return false;
  const livro = LIVROS.find(livro => livro.id_livro === idLivro);
  const estoque = livro ? livro.disponiveis_venda : 0;
  if (quantidadeNoCarrinho(idLivro) >= estoque) {
    mostrarAviso(estoque === 0 ? "Livro esgotado" : `Só há ${estoque} ${estoque === 1 ? "exemplar disponível" : "exemplares disponíveis"}`);
    return false;
  }
  salvarCarrinho([...lerCarrinho(), idLivro]);
  return true;
}

// Tira um exemplar do livro (botão "−").
function removerUmDoCarrinho(idLivro) {
  const carrinho = lerCarrinho();
  const posicao = carrinho.lastIndexOf(idLivro);
  if (posicao >= 0) carrinho.splice(posicao, 1);
  salvarCarrinho(carrinho);
}

// Tira todos os exemplares do livro (botão "Remover").
function removerDoCarrinho(idLivro) {
  salvarCarrinho(lerCarrinho().filter(id => id !== idLivro));
}

// Se alguém comprou antes e o estoque diminuiu, corta o excesso do carrinho.
// Devolve true quando precisou mudar algo.
function ajustarCarrinhoAoEstoque() {
  const carrinhoAjustado = [];
  lerCarrinho().forEach(idLivro => {
    const livro = LIVROS.find(livro => livro.id_livro === idLivro);
    const jaColocados = carrinhoAjustado.filter(id => id === idLivro).length;
    if (livro && jaColocados < livro.disponiveis_venda) carrinhoAjustado.push(idLivro);
  });
  const mudou = carrinhoAjustado.length !== lerCarrinho().length;
  if (mudou) salvarCarrinho(carrinhoAjustado);
  return mudou;
}

// Número que aparece em cima do ícone do carrinho.
function atualizarContadorDoCarrinho() {
  const contador = document.getElementById("contador-carrinho");
  if (!contador) return;
  const quantidade = lerCarrinho().length;
  contador.textContent = quantidade;
  contador.classList.toggle("oculto", quantidade === 0);
}

// ----- Aviso -----
// Caixinha que aparece embaixo da tela por 2 segundos ("Livro adicionado", erros do Java...).
function mostrarAviso(texto) {
  let aviso = document.querySelector(".toast");
  if (!aviso) {
    aviso = document.createElement("div");
    aviso.className = "toast";
    document.body.appendChild(aviso);
  }
  aviso.textContent = texto;
  aviso.classList.add("visivel");
  clearTimeout(aviso.temporizador);
  aviso.temporizador = setTimeout(() => aviso.classList.remove("visivel"), 2200);
}

// ----- Sugestões enquanto digita -----
// Mostra uma lista embaixo do campo com as opções que combinam com o texto (sem ligar para acentos).
// Cada opção tem: rotulo (texto principal), detalhe (texto menor) e valor (o que vai para o campo).
// aoEscolher é chamada quando a pessoa clica numa opção ou escolhe com as setas + Enter.
function ativarSugestoes(campo, opcoes, aoEscolher) {
  if (!campo) return;
  const caixa = campo.parentElement;
  caixa.classList.add("combo");
  campo.setAttribute("autocomplete", "off");
  const lista = document.createElement("ul");
  lista.className = "combo__lista oculto";
  caixa.appendChild(lista);
  let posicaoMarcada = -1;
  let opcoesVisiveis = [];

  const semAcento = texto => texto.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase();

  function mostrarLista() {
    const textoDigitado = semAcento(campo.value.trim());
    opcoesVisiveis = opcoes
      .filter(opcao => !textoDigitado || semAcento(`${opcao.rotulo} ${opcao.detalhe || ""} ${opcao.valor}`).includes(textoDigitado))
      .slice(0, 60);
    posicaoMarcada = -1;
    lista.innerHTML = opcoesVisiveis.length
      ? opcoesVisiveis.map((opcao, posicao) => `<li data-posicao="${posicao}"><span>${opcao.rotulo}</span>${opcao.detalhe ? `<small>${opcao.detalhe}</small>` : ""}</li>`).join("")
      : `<li class="combo__vazio">Nada encontrado</li>`;
    lista.classList.remove("oculto");
  }

  function escolher(posicao) {
    const opcao = opcoesVisiveis[posicao];
    if (!opcao) return;
    campo.value = opcao.valor;
    lista.classList.add("oculto");
    if (aoEscolher) aoEscolher(opcao);
  }

  // Destaca uma opção ao usar as setas do teclado (volta ao início depois da última).
  function marcar(posicao) {
    const itens = lista.querySelectorAll("li[data-posicao]");
    if (!itens.length) return;
    posicaoMarcada = (posicao + itens.length) % itens.length;
    itens.forEach((item, indice) => item.classList.toggle("marcado", indice === posicaoMarcada));
    itens[posicaoMarcada].scrollIntoView({ block: "nearest" });
  }

  campo.addEventListener("focus", mostrarLista);
  campo.addEventListener("input", mostrarLista);
  campo.addEventListener("blur", () => setTimeout(() => lista.classList.add("oculto"), 150));
  campo.addEventListener("keydown", evento => {
    if (lista.classList.contains("oculto")) return;
    if (evento.key === "ArrowDown") { evento.preventDefault(); marcar(posicaoMarcada + 1); }
    else if (evento.key === "ArrowUp") { evento.preventDefault(); marcar(posicaoMarcada - 1); }
    else if (evento.key === "Enter" && posicaoMarcada >= 0) { evento.preventDefault(); evento.stopPropagation(); escolher(posicaoMarcada); }
    else if (evento.key === "Escape") lista.classList.add("oculto");
  });
  lista.addEventListener("mousedown", evento => {
    const item = evento.target.closest("li[data-posicao]");
    if (item) { evento.preventDefault(); escolher(Number(item.dataset.posicao)); }
  });
}

// ----- Cabeçalho -----
// Preenche o menu Categorias, o ícone de conta, a busca (com sugestões) e o contador do carrinho.
function montarCabecalho() {
  const listaDeCategorias = document.getElementById("lista-categorias");
  if (listaDeCategorias) {
    const categoriaAberta = Number(lerParametroDaUrl("categoria"));
    listaDeCategorias.innerHTML = CATEGORIAS.map(categoria =>
      `<li><a href="catalogo.html?categoria=${categoria.id_categoria}" class="${categoria.id_categoria === categoriaAberta ? "ativo" : ""}">${categoria.nome}</a></li>`
    ).join("");
  }
  montarMenuCategorias();

  // Conectado: o ícone de conta leva para a conta (cliente) ou para o painel (funcionário).
  const linkDaConta = document.getElementById("link-conta");
  const sessao = Sessao.atual();
  if (linkDaConta && sessao) {
    linkDaConta.href = sessao.tipo === "Cliente" ? "conta.html" : "painel.html";
    linkDaConta.title = `Conectado como ${sessao.nome}`;
    linkDaConta.classList.add("conectado");
  }

  // A busca mantém o texto pesquisado e sugere livros por título ou autor.
  const campoDeBusca = document.querySelector(".busca input");
  if (campoDeBusca && lerParametroDaUrl("q")) campoDeBusca.value = lerParametroDaUrl("q");
  const livrosSugeridos = LIVROS.map(livro => ({ rotulo: livro.titulo, detalhe: livro.autores.join(", "), valor: livro.titulo, idLivro: livro.id_livro }));
  ativarSugestoes(campoDeBusca, livrosSugeridos, opcao => { window.location.href = `livro.html?id=${opcao.idLivro}`; });

  atualizarContadorDoCarrinho();
}

// Abre o menu Categorias passando o mouse (computador) ou clicando (celular). Esc fecha.
function montarMenuCategorias() {
  const menu = document.getElementById("menu-cat");
  if (!menu) return;
  const botao = menu.querySelector(".menu-cat__botao");
  const abrirMenu = aberto => { menu.classList.toggle("aberto", aberto); botao.setAttribute("aria-expanded", aberto); };
  let temporizador;
  const temMouse = window.matchMedia("(hover: hover)").matches;
  if (temMouse) {
    menu.addEventListener("mouseenter", () => { clearTimeout(temporizador); abrirMenu(true); });
    menu.addEventListener("mouseleave", () => { temporizador = setTimeout(() => abrirMenu(false), 150); });
  }
  botao.addEventListener("click", () => abrirMenu(!menu.classList.contains("aberto")));
  document.addEventListener("click", evento => { if (!menu.contains(evento.target)) abrirMenu(false); });
  document.addEventListener("keydown", evento => { if (evento.key === "Escape") abrirMenu(false); });
}

document.addEventListener("DOMContentLoaded", montarCabecalho);

// Ao voltar com o botão "voltar" do navegador, recarrega para não mostrar dados velhos.
window.addEventListener("pageshow", evento => { if (evento.persisted) window.location.reload(); });

// Se o servidor Java estiver desligado, mostra uma faixa no topo da página.
document.addEventListener("DOMContentLoaded", () => {
  if (servidorLigado) return;
  const faixa = document.createElement("div");
  faixa.className = "faixa-versao";
  faixa.textContent = "Servidor Java desligado: rode o ServidorWeb.java para carregar os dados do banco";
  document.body.prepend(faixa);
});

// ----- CPF e telefone -----

function somenteDigitos(texto) {
  return (texto || "").replace(/\D/g, "");
}

// "12345678901" vira "123.456.789-01" (no máximo 11 números).
function formatarCpf(cpf) {
  return somenteDigitos(cpf).slice(0, 11)
    .replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d{1,2})$/, "$1-$2");
}

// "11987654321" vira "(11) 98765-4321" e "1134567890" vira "(11) 3456-7890".
// Números com mais de 11 dígitos (cadastros antigos errados) aparecem como estão.
function formatarTelefone(telefone) {
  const digitos = somenteDigitos(telefone);
  if (digitos.length > 11) return telefone;
  if (digitos.length > 10) return digitos.replace(/^(\d{2})(\d{5})(\d{0,4})/, "($1) $2-$3");
  if (digitos.length > 6) return digitos.replace(/^(\d{2})(\d{4})(\d{0,4})/, "($1) $2-$3");
  if (digitos.length > 2) return digitos.replace(/^(\d{2})(\d*)/, "($1) $2");
  return digitos;
}

// Máscara automática: todo campo com data-mascara="cpf" ou "telefone" é formatado enquanto a pessoa digita.
document.addEventListener("input", evento => {
  const campo = evento.target;
  const mascara = campo.dataset && campo.dataset.mascara;
  if (mascara === "cpf") campo.value = formatarCpf(campo.value);
  if (mascara === "telefone") campo.value = formatarTelefone(somenteDigitos(campo.value).slice(0, 11));
}, true);
