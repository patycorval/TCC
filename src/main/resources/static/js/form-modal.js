document.addEventListener('DOMContentLoaded', () => {

    const botoesSolicitacao = document.querySelectorAll('.btn-solicitar-reserva');
    
    const overlay = document.getElementById('overlay-reserva');
    const botaoFechar = document.getElementById('fechar-form');

    // 1. Adiciona o evento de clique a cada botão de solicitação
    botoesSolicitacao.forEach(botao => {
        botao.addEventListener('click', (event) => {
            // Previne o comportamento padrão do botão (se for um form)
            event.preventDefault();

            // Mostra o overlay
            overlay.style.display = 'flex';

            // Pega o número do dia a partir do elemento pai
            const diaElemento = event.target.closest('.dia');
            const diaNumero = diaElemento.querySelector('span').textContent;

            // Garante que o dia e o mês tenham 2 dígitos (ex: 5 -> 05)
            const diaFormatado = diaNumero.padStart(2, '0');

            // Pega a data atual
            const dataAtual = new Date();
            const ano = dataAtual.getFullYear();
            const mes = (dataAtual.getMonth() + 1).toString().padStart(2, '0');

            // Formata a data para o padrão ISO 8601 (yyyy-MM-dd) que o Spring espera
            const dataFormatada = `${ano}-${mes}-${diaFormatado}`;

            // Preenche o campo do formulário
            const campoData = document.getElementById('dataEvento');
            campoData.value = dataFormatada;
        });
    });

    botaoFechar.addEventListener('click', () => {
        overlay.style.display = 'none';
    });

    overlay.addEventListener('click', (event) => {
        if (event.target.id === 'overlay-reserva') {
            overlay.style.display = 'none';
        }
    });
});