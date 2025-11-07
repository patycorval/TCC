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

    // Função para formatar a hora (se o backend enviar como array)
    const formatarHora = (horaInput) => {
        if (typeof horaInput === 'string') {
            return horaInput.substring(0, 5); // Retorna HH:mm se já for string
        }
        if (Array.isArray(horaInput) && horaInput.length >= 2) {
            const hora = horaInput[0].toString().padStart(2, '0');
            const minuto = horaInput[1].toString().padStart(2, '0');
            return `${hora}:${minuto}`;
        }
        return '00:00';
    };
    
    // Listeners para fechar e alternar entre os modais
    const fecharTodosModais = () => {
        modalDiaView.style.display = 'none';
        modalReservaForm.style.display = 'none';
    };

    btnFecharView.addEventListener('click', fecharTodosModais);
    btnFecharForm.addEventListener('click', fecharTodosModais);
    btnVoltarListagem.addEventListener('click', () => {
        modalReservaForm.style.display = 'none';
        modalDiaView.style.display = 'flex';
    });
     btnAbrirFormReserva.addEventListener('click', () => {
        const dataSelecionada = btnAbrirFormReserva.getAttribute('data-dia-selecionado');
        campoDataForm.value = dataSelecionada;
        modalDiaView.style.display = 'none';
        modalReservaForm.style.display = 'flex';
    });

    // Listener principal para os dias do calendário
    document.querySelectorAll('.dia.mensal:not(.vazio, .bloqueado, .indisponivel)').forEach(diaElemento => {
        diaElemento.addEventListener('click', (event) => {
            if (event.target.closest('.btn-solicitar-reserva')) {
                return;
            }

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

                    let elemento;
                    let conteudoItem = `
                        <div class="evento-info d-flex flex-column">
                            <strong class="evento-nome-modal">${evento.evento}</strong>
                            ${!isOwner ? `<small class="text-muted">Solicitado por: ${evento.nome}</small>` : ''}
                        </div>
                        <div class="evento-horario text-end d-flex flex-column align-items-end">
                            <span class="evento-hora-modal">${horaInicio} - ${horaFim}</span>
                        </div>
                    `;

                    if (isOwner) {
                        elemento = document.createElement('a');
                        elemento.href = `/listagem?periodo=${evento.periodoIdeal}#reserva-${evento.id}`;
                        elemento.className = 'list-group-item list-group-item-action d-flex justify-content-between align-items-center';
                    } else {
                        elemento = document.createElement('li');
                        elemento.className = 'list-group-item d-flex justify-content-between align-items-center';
                    }
                    let statusBadge = '';
                    if (isOwner) {
                        const statusText = evento.status.charAt(0).toUpperCase() + evento.status.slice(1).toLowerCase();
                        statusBadge = `<span class="badge status-${evento.status.toLowerCase()}">${statusText}</span>`;
                    }

                    elemento.innerHTML = `
                        <div class="evento-info d-flex flex-column">
                            <strong class="evento-nome-modal">${evento.evento}</strong>
                            ${!isOwner ? `<small class="text-muted">Solicitado por: ${evento.nome}</small>` : ''}
                        </div>
                        <div class="evento-horario text-end d-flex flex-column align-items-end">
                            <span class="evento-hora-modal">${horaInicio} - ${horaFim}</span>
                            ${statusBadge} 
                        </div>
                    `;

                    listaEventosContainer.appendChild(elemento);
                });
            } else {
                semEventosAviso.style.display = 'block';
                listaEventosContainer.style.display = 'none';
            }
            
            modalDiaView.style.display = 'flex';
        });
    });

    // Listener para o botão de solicitar reserva (símbolo '+')
    document.querySelectorAll('.btn-solicitar-reserva').forEach(button => {
        button.addEventListener('click', (event) => {
            event.stopPropagation();
            
            const diaElemento = event.currentTarget.closest('.dia.mensal');
            const dia = diaElemento.getAttribute('data-dia');
            
            const urlParams = new URLSearchParams(window.location.search);
            const ano = urlParams.get('ano') || new Date().getFullYear();
            const mes = urlParams.get('mes') || (new Date().getMonth() + 1);
            
            const dataParaForm = `${ano}-${mes.toString().padStart(2, '0')}-${dia.padStart(2, '0')}`;
            campoDataForm.value = dataParaForm;
            
            modalReservaForm.style.display = 'flex';
        });
    });

    window.addEventListener('click', (event) => {
        // Se o alvo do clique for o overlay do modal de visualização, fecha
        if (event.target === modalDiaView) {
            fecharTodosModais();
        }
        // Se o alvo do clique for o overlay do modal de formulário, fecha
        if (event.target === modalReservaForm) {
            fecharTodosModais();
        }
    });

});