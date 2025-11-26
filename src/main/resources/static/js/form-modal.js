document.addEventListener('DOMContentLoaded', () => {
    const botoesSolicitacao = document.querySelectorAll('.btn-solicitar-reserva');
    const overlay = document.getElementById('overlay-reserva');
    const botaoFechar = document.getElementById('fechar-form');
    const campoData = document.getElementById('dataEvento');

    botoesSolicitacao.forEach(botao => {
        botao.addEventListener('click', (event) => {
            event.preventDefault();
            
            overlay.style.display = 'flex';
            
            const diaElemento = event.currentTarget.closest('.dia');
            
            const diaNumero = diaElemento.querySelector('.numero-dia').textContent;
            
            const urlParams = new URLSearchParams(window.location.search);
            const ano = urlParams.get('ano') || new Date().getFullYear();
            
            const mes = urlParams.get('mes') || (new Date().getMonth() + 1);

            const diaFormatado = diaNumero.padStart(2, '0');
            const mesFormatado = mes.toString().padStart(2, '0');
            const dataFormatada = `${ano}-${mesFormatado}-${diaFormatado}`;
            
            campoData.value = dataFormatada;
        });
    });

    const fecharModal = () => {
        overlay.style.display = 'none';
    };

    botaoFechar.addEventListener('click', fecharModal);

    overlay.addEventListener('click', (event) => {
        if (event.target.id === 'overlay-reserva') {
            fecharModal();
        }
    });
});