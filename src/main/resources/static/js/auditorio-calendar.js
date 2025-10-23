document.addEventListener('DOMContentLoaded', () => {
    const usuarioLogadoEmail = document.body.getAttribute('data-usuario-logado');

    // Referências aos elementos
    const modalDiaView = document.getElementById('modal-dia-view');
    const modalReservaForm = document.getElementById('overlay-reserva');
    const modalDiaTitulo = document.getElementById('modal-dia-titulo');
    const listaEventosContainer = document.getElementById('lista-eventos-modal');
    const semEventosAviso = document.getElementById('sem-eventos-aviso');
    const campoDataForm = document.getElementById('dataEvento');

    // Botões
    const btnAbrirFormReserva = document.getElementById('btn-abrir-form-reserva');
    const btnVoltarListagem = document.getElementById('btn-voltar-listagem');
    const btnFecharView = document.getElementById('fechar-modal-view');
    const btnFecharForm = document.getElementById('fechar-modal-reserva');

    const formatarHora = (horaArray) => {
        if (!Array.isArray(horaArray) || horaArray.length < 2) {
            return '00:00';
        }
        const hora = horaArray[0].toString().padStart(2, '0');
        const minuto = horaArray[1].toString().padStart(2, '0');
        return `${hora}:${minuto}`;
    };

    // --- CORREÇÃO 1: Impede que cliques na checkbox se espalhem ---
    // Adicionamos um listener que para o evento do clique imediatamente.
    document.querySelectorAll('.checkbox-bloqueio').forEach(checkbox => {
        checkbox.addEventListener('click', (event) => {
            event.stopPropagation();
        });
    });

    // --- CORREÇÃO 2: Listener específico para o botão '+' (btn-solicitar-reserva) ---
    // Este listener agora cuida de abrir o modal de reserva DIRETAMENTE.
    document.querySelectorAll('.btn-solicitar-reserva').forEach(button => {
        button.addEventListener('click', (event) => {
            event.stopPropagation(); // Impede que o clique acione o modal da lista de eventos.

            const diaElemento = event.currentTarget.closest('.dia.mensal');
            const dia = diaElemento.getAttribute('data-dia');

            const urlParams = new URLSearchParams(window.location.search);
            const ano = urlParams.get('ano') || new Date().getFullYear();
            const mes = urlParams.get('mes') || (new Date().getMonth() + 1);
            
            const dataParaForm = `${ano}-${mes.toString().padStart(2, '0')}-${dia.padStart(2, '0')}`;
            campoDataForm.value = dataParaForm;

            // Abre diretamente o modal do formulário de reserva
            modalReservaForm.style.display = 'flex';
        });
    });

    // Abre o modal de visualização
    document.querySelectorAll('.dia.mensal:not(.vazio, .passado, .indisponivel)').forEach(diaElemento => {
        diaElemento.addEventListener('click', () => {
            const dia = diaElemento.getAttribute('data-dia');
            const eventos = JSON.parse(diaElemento.getAttribute('data-eventos'));
            
            const urlParams = new URLSearchParams(window.location.search);
            const ano = urlParams.get('ano') || new Date().getFullYear();
            const mes = urlParams.get('mes') || (new Date().getMonth() + 1);
            
            const dataFormatada = `${dia.padStart(2, '0')}/${mes.toString().padStart(2, '0')}/${ano}`;
            modalDiaTitulo.textContent = `Eventos para ${dataFormatada}`;
            
            const dataParaForm = `${ano}-${mes.toString().padStart(2, '0')}-${dia.padStart(2, '0')}`;
            btnAbrirFormReserva.setAttribute('data-dia-selecionado', dataParaForm);

            listaEventosContainer.innerHTML = '';

            if (eventos && eventos.length > 0) {
                semEventosAviso.style.display = 'none';
                listaEventosContainer.style.display = 'block';
                
                eventos.forEach(evento => {
                    const horaInicio = formatarHora(evento.hora);
                    const horaFim = formatarHora(evento.horaFim);
                    
                    const isOwner = evento.emailRequisitor === usuarioLogadoEmail;

                    let solicitanteHtml = '';
                    let statusHtml = '';

                    if (isOwner) {
                        let statusClass = '';
                        if (evento.status === 'APROVADA') {
                        statusClass = 'bg-success';
                        } else if (evento.status === 'PENDENTE') {
                        statusClass = 'bg-warning text-dark';
                        } else if (evento.status === 'REJEITADA') {
                        statusClass = 'bg-danger'; 
                        } else {
                        statusClass = 'bg-secondary'; 
                        }
                        statusHtml = `<span class="badge ${statusClass}">${evento.status}</span>`;
            
                    } else {
                        // A tag <br>
                        solicitanteHtml = `<small class="text-muted">Solicitado por: ${evento.nome}</small>`;
                    }
                    
                    const conteudoItem = `
                        <div class="evento-info d-flex flex-column">
                            <strong class="evento-nome-modal">${evento.evento}</strong>
                            ${solicitanteHtml}
                        </div>
                        <div class="evento-horario text-end d-flex flex-column align-items-end">
                            <span class="evento-hora-modal">${horaInicio} - ${horaFim}</span>
                            ${statusHtml}
                        </div>
                    `;

                    let elemento;
                    if (isOwner) {
                        elemento = document.createElement('a');
                        elemento.href = `/listagem#reserva-${evento.id}`;
                        elemento.className = 'list-group-item list-group-item-action d-flex justify-content-between'; 
                    } else {
                        elemento = document.createElement('li');
                        elemento.className = 'list-group-item d-flex justify-content-between';
                    }
                    
                    elemento.innerHTML = conteudoItem;
                    listaEventosContainer.appendChild(elemento);
                });
            } else {
                semEventosAviso.style.display = 'block';
                listaEventosContainer.style.display = 'none';
            }
            
            modalDiaView.style.display = 'flex';
        });
    });


    // --- LÓGICA PARA FECHAR E TROCAR MODAIS ---
    const fecharTodosModais = () => {
        modalDiaView.style.display = 'none';
        modalReservaForm.style.display = 'none';
    };

    btnFecharView.addEventListener('click', fecharTodosModais);
    btnFecharForm.addEventListener('click', fecharTodosModais);

    btnAbrirFormReserva.addEventListener('click', () => {
        const dataSelecionada = btnAbrirFormReserva.getAttribute('data-dia-selecionado');
        campoDataForm.value = dataSelecionada;
        modalDiaView.style.display = 'none';
        modalReservaForm.style.display = 'flex';
    });

    btnVoltarListagem.addEventListener('click', () => {
        modalReservaForm.style.display = 'none';
        modalDiaView.style.display = 'flex';
    });

    window.addEventListener('click', (event) => {
        if (event.target === modalDiaView || event.target === modalReservaForm) {
            fecharTodosModais();
        }
    });
});