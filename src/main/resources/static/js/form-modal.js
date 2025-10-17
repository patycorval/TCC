document.addEventListener('DOMContentLoaded', () => {
    const botoesSolicitacao = document.querySelectorAll('.btn-solicitar-reserva');
    const overlay = document.getElementById('overlay-reserva');
    const botaoFechar = document.getElementById('fechar-form');
    const campoData = document.getElementById('dataEvento');

    botoesSolicitacao.forEach(botao => {
        botao.addEventListener('click', (event) => {
            // Garante que o evento de clique padrão não ocorra
            event.preventDefault();
            
            // Exibe o modal
            overlay.style.display = 'flex';
            
            // Lógica para pegar o número do dia do elemento pai mais próximo
            const diaElemento = event.currentTarget.closest('.dia');
            
            // Procura pelo span que tem a classe 'numero-dia' para pegar o texto
            const diaNumero = diaElemento.querySelector('.numero-dia').textContent;
            
            // Pega o mês e o ano da URL da página
            const urlParams = new URLSearchParams(window.location.search);
            const ano = urlParams.get('ano') || new Date().getFullYear();
            
            // O 'mes' na URL é de 1 a 12, então não precisa somar 1.
            const mes = urlParams.get('mes') || (new Date().getMonth() + 1);

            // Formata a data para o padrão YYYY-MM-DD
            const diaFormatado = diaNumero.padStart(2, '0');
            const mesFormatado = mes.toString().padStart(2, '0');
            const dataFormatada = `${ano}-${mesFormatado}-${diaFormatado}`;
            
            // Define o valor no campo de data do formulário
            campoData.value = dataFormatada;
        });
    });

    // Função para fechar o modal
    const fecharModal = () => {
        overlay.style.display = 'none';
    };

    botaoFechar.addEventListener('click', fecharModal);

    overlay.addEventListener('click', (event) => {
        // Fecha o modal apenas se o clique for no fundo escuro (o próprio overlay)
        if (event.target.id === 'overlay-reserva') {
            fecharModal();
        }
    });
});