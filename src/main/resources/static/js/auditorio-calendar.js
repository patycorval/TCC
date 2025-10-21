document.addEventListener('DOMContentLoaded', () => {
    // Referências aos elementos dos modais
    const modalDiaView = document.getElementById('modal-dia-view');
    const modalReservaForm = document.getElementById('overlay-reserva');
    const modalDiaTitulo = document.getElementById('modal-dia-titulo');
    const listaEventosContainer = document.getElementById('lista-eventos-modal');
    const semEventosAviso = document.getElementById('sem-eventos-aviso');
    const campoDataForm = document.getElementById('dataEvento');

    // Botões de Ação
    const btnAbrirFormReserva = document.getElementById('btn-abrir-form-reserva');
    const btnVoltarListagem = document.getElementById('btn-voltar-listagem');
    const btnFecharView = document.getElementById('fechar-modal-view');
    const btnFecharForm = document.getElementById('fechar-modal-reserva'); 

    // --- LÓGICA PARA ABRIR E TROCAR MODAIS ---

    // Abre o formulário a partir da listagem
    btnAbrirFormReserva.addEventListener('click', () => {
        const dataSelecionada = btnAbrirFormReserva.getAttribute('data-dia-selecionado');
        campoDataForm.value = dataSelecionada;
        modalDiaView.style.display = 'none';
        modalReservaForm.style.display = 'flex';
    });

    // Volta do formulário para a listagem
    btnVoltarListagem.addEventListener('click', () => {
        modalReservaForm.style.display = 'none';
        modalDiaView.style.display = 'flex';
    });


    // --- LÓGICA PARA EXIBIR DADOS NO MODAL DE VISUALIZAÇÃO ---

    const formatarHora = (horaArray) => {
        if (!Array.isArray(horaArray) || horaArray.length < 2) return horaArray;
        return `${horaArray[0].toString().padStart(2, '0')}:${horaArray[1].toString().padStart(2, '0')}`;
    };

    const formatarData = (dia, mes, ano) => {
        const data = new Date(ano, mes - 1, dia);
        return data.toLocaleDateString('pt-BR', { timeZone: 'UTC' });
    };

    document.querySelectorAll('.dia.mensal:not(.vazio)').forEach(diaElemento => {
        diaElemento.addEventListener('click', () => {
            if (diaElemento.classList.contains('indisponivel')) return;

            const dia = diaElemento.getAttribute('data-dia');
            const urlParams = new URLSearchParams(window.location.search);
            const ano = urlParams.get('ano') || new Date().getFullYear();
            const mes = urlParams.get('mes') || (new Date().getMonth() + 1);
            
            modalDiaTitulo.textContent = `Eventos para ${formatarData(dia, mes, ano)}`;
            btnAbrirFormReserva.setAttribute('data-dia-selecionado', `${ano}-${mes.toString().padStart(2, '0')}-${dia.toString().padStart(2, '0')}`);

            const eventos = JSON.parse(diaElemento.getAttribute('data-eventos'));
            listaEventosContainer.innerHTML = '';

            if (eventos && eventos.length > 0) {
                semEventosAviso.style.display = 'none';
                listaEventosContainer.style.display = 'block';
                eventos.forEach(evento => {
                const horaInicioFormatada = formatarHora(evento.hora);
                const horaFimFormatada = formatarHora(evento.horaFim);
                const statusClass = `status-${evento.status.toLowerCase()}`;

                // 1. Monta o conteúdo interno do item da lista
                const conteudoItem = `
                    <div class="evento-info">
                        <span class="evento-nome-modal">${evento.evento}</span>
                        <span class="evento-solicitante-modal">Solicitado por: ${evento.nome}</span>
                    </div>
                    <div class="evento-horario">
                        <span class="evento-hora-modal">${horaInicioFormatada} - ${horaFimFormatada}</span>
                        <span class="badge ${statusClass}">${evento.status}</span>
                    </div>
                `;

                let itemHTML;
                // 2. Verifica se o evento pertence ao usuário logado (o backend envia 'owner: true')
                if (evento.owner) {
                    // Se for o dono, cria um link <a> que leva para a listagem com o ID da reserva
                    itemHTML = `<a href="/listagem?destaque=${evento.id}" class="list-group-item evento-modal-item link-reserva">${conteudoItem}</a>`;
                } else {
                    // Se não for, cria um <li> normal, sem link
                    itemHTML = `<li class="list-group-item evento-modal-item">${conteudoItem}</li>`;
                }

                // 3. Adiciona o HTML gerado (seja <a> ou <li>) ao container
                listaEventosContainer.innerHTML += itemHTML;
            });
            } else {
                semEventosAviso.style.display = 'block';
                listaEventosContainer.style.display = 'none';
            }
            
            modalDiaView.style.display = 'flex';
        });
    });

    // --- LÓGICA UNIFICADA PARA FECHAR OS MODAIS ---

    const fecharTodosModais = () => {
        modalDiaView.style.display = 'none';
        modalReservaForm.style.display = 'none';
    };

    btnFecharView.addEventListener('click', fecharTodosModais);
    btnFecharForm.addEventListener('click', fecharTodosModais);
    // Fecha ao clicar fora (no overlay)
    modalDiaView.addEventListener('click', (e) => { if (e.target.id === 'modal-dia-view') fecharTodosModais(); });
    modalReservaForm.addEventListener('click', (e) => { if (e.target.id === 'overlay-reserva') fecharTodosModais(); });
});