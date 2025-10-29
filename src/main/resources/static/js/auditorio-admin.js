document.addEventListener('DOMContentLoaded', () => {
    const csrfToken = document.querySelector('input[name="_csrf"]').value;

    // Referências aos modais e botões
    const modalView = document.getElementById('modal-dia-view');
    const modalReservaForm = document.getElementById('overlay-reserva');
    const modalTitulo = document.getElementById('modal-dia-titulo');
    const listaEventosContainer = document.getElementById('lista-eventos-modal');
    const semEventosAviso = document.getElementById('sem-eventos-aviso');
    const btnAplicarMudancas = document.getElementById('btn-aplicar-mudancas');
    const btnAbrirFormReserva = document.getElementById('btn-abrir-form-reserva-admin');
    const campoDataForm = document.getElementById('dataEvento');

    const btnFecharView = document.getElementById('fechar-modal-view');
    const btnFecharForm = document.getElementById('fechar-modal-reserva');
    const btnVoltarListagem = document.getElementById('btn-voltar-listagem');
    
    const modalFooter = document.getElementById('modal-gestao-footer');
    // Objeto para armazenar as mudanças de status pendentes
    let pendingChanges = {};
    let diaElementoAtivo = null;

    const formatarHora = (horaInput) => {
        if (typeof horaInput === 'string') return horaInput.substring(0, 5);
        if (Array.isArray(horaInput)) return `${horaInput[0].toString().padStart(2, '0')}:${horaInput[1].toString().padStart(2, '0')}`;
        return '00:00';
    };

     // Listeners para fechar e alternar entre os modais
    const fecharTodosModais = () => {
        modalView.style.display = 'none';
        if (modalReservaForm) modalReservaForm.style.display = 'none';
        diaElementoAtivo = null;
    };

    btnFecharView.addEventListener('click', fecharTodosModais);
    btnFecharForm.addEventListener('click', fecharTodosModais);
    btnVoltarListagem.addEventListener('click', () => {
        modalReservaForm.style.display = 'none';
        modalView.style.display = 'flex';
    });

     btnAbrirFormReserva.addEventListener('click', () => {
        const dataSelecionada = btnAbrirFormReserva.getAttribute('data-dia-selecionado');
        campoDataForm.value = dataSelecionada;
        modalView.style.display = 'none';
        modalReservaForm.style.display = 'flex';
    });


    // Função para enviar as alterações em massa
    const aplicarMudancas = async () => {
        const changesArray = Object.values(pendingChanges);
        if (changesArray.length === 0) {
            fecharTodosModais(); // Se não houver mudanças, apenas fecha o modal
            return;
        }

        try {
            const response = await fetch('/admin/auditorio/atualizar-status-massa', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrfToken },
                body: JSON.stringify(changesArray)
            });

        if (response.ok) {
                // ✨ INÍCIO DA ATUALIZAÇÃO DINÂMICA (SEM RELOAD) ✨
                
                let eventos = JSON.parse(diaElementoAtivo.getAttribute('data-eventos'));
                
                // Filtra os eventos, removendo os que foram rejeitados
                const eventosAtualizados = eventos.map(evento => {
                    const change = pendingChanges[evento.id];
                    if (change) {
                        evento.status = change.novoStatus;
                    }
                    return evento;
                }).filter(evento => evento.status !== 'REJEITADA');

                diaElementoAtivo.setAttribute('data-eventos', JSON.stringify(eventosAtualizados));

                const indicadoresContainer = diaElementoAtivo.querySelector('.indicadores-evento');
                if (indicadoresContainer) {
                    indicadoresContainer.innerHTML = ''; // Limpa os indicadores antigos
                    if (eventosAtualizados.length > 0) {
                        eventosAtualizados.forEach(reserva => {
                            const indicadorSpan = document.createElement('span');
                            indicadorSpan.className = `indicador status-${reserva.status.toLowerCase()}`;
                            indicadoresContainer.appendChild(indicadorSpan);
                        });
                    } else {
                        // Se não sobraram eventos, o dia volta a ser disponível
                        diaElementoAtivo.classList.remove('evento');
                        diaElementoAtivo.classList.add('disponivel');
                    }
                }

                pendingChanges = {};
                fecharTodosModais();

            } else {
                alert('Ocorreu um erro ao salvar as alterações.');
            }
        } catch (error) {
            console.error('Erro:', error);
            alert('Erro de conexão ao tentar salvar as alterações.');
        }
    };

    btnAplicarMudancas.addEventListener('click', aplicarMudancas);

    // Listener para o botão "Solicitar Nova Reserva"
    btnAbrirFormReserva.addEventListener('click', () => {
        const dataSelecionada = btnAbrirFormReserva.getAttribute('data-dia-selecionado');
        if (campoDataForm) campoDataForm.value = dataSelecionada;
        fecharTodosModais();
        if (modalReservaForm) modalReservaForm.style.display = 'flex';
    });

    // Listener principal para os dias do calendário
    document.querySelectorAll('.dia.mensal:not(.vazio)').forEach(diaElemento => {
        diaElemento.addEventListener('click', () => {
            diaElementoAtivo = diaElemento;
            pendingChanges = {}; // Limpa as mudanças pendentes ao abrir um novo dia
            btnAplicarMudancas.disabled = true;

            const dia = diaElemento.getAttribute('data-dia');
            const eventos = JSON.parse(diaElemento.getAttribute('data-eventos'));
            const urlParams = new URLSearchParams(window.location.search);
            const ano = urlParams.get('ano') || new Date().getFullYear();
            const mes = urlParams.get('mes') || (new Date().getMonth() + 1);

            modalTitulo.textContent = `Gestão de Reservas - ${dia.padStart(2, '0')}/${mes.toString().padStart(2, '0')}/${ano}`;
            
            const dataParaForm = `${ano}-${mes.toString().padStart(2, '0')}-${dia.padStart(2, '0')}`;
            btnAbrirFormReserva.setAttribute('data-dia-selecionado', dataParaForm);
            
            listaEventosContainer.innerHTML = '';

           if (eventos && eventos.length > 0) {
                semEventosAviso.style.display = 'none';
                modalFooter.className = 'modal-footer d-flex justify-content-between'; // Alinhamento dividido
                btnAplicarMudancas.style.display = 'block';
                eventos.forEach(evento => {
                    const li = document.createElement('li');
                    li.className = 'list-group-item';
                    const statusText = evento.status.charAt(0).toUpperCase() + evento.status.slice(1).toLowerCase();
                    
                    li.innerHTML = `
                        <div class="d-flex w-100 justify-content-between">
                            <div>
                                <h6 class="mb-1">${evento.evento}</h6>
                                <small class="text-muted">Solicitante: ${evento.nome} (${evento.emailRequisitor})</small>
                                <br>
                                <small class="text-muted">Horário: ${formatarHora(evento.hora)} - ${formatarHora(evento.horaFim)}</small>
                            </div>
                            <div class="status-container" id="status-container-${evento.id}">
                                <span class="badge status-${evento.status.toLowerCase()}">${statusText}</span>
                                <button class="btn btn-link btn-sm p-0 btn-edit-status"><i class="bi bi-pencil-square"></i></button>
                                <select class="form-select form-select-sm select-status" style="display: none;">
                                    <option value="APROVADA" ${evento.status === 'APROVADA' ? 'selected' : ''}>Aprovada</option>
                                    <option value="PENDENTE" ${evento.status === 'PENDENTE' ? 'selected' : ''}>Pendente</option>
                                    <option value="REJEITADA" ${evento.status === 'REJEITADA' ? 'selected' : ''}>Rejeitada</option>
                                </select>
                            </div>
                        </div>
                    `;
                    listaEventosContainer.appendChild(li);

                    const statusContainer = li.querySelector(`#status-container-${evento.id}`);
                    const editButton = statusContainer.querySelector('.btn-edit-status');
                    const statusBadge = statusContainer.querySelector('.badge');
                    const statusSelect = statusContainer.querySelector('.select-status');

                    editButton.addEventListener('click', () => {
                        statusBadge.style.display = 'none';
                        editButton.style.display = 'none';
                        statusSelect.style.display = 'block';
                        statusSelect.focus();
                    });

                    // Este listener agora funciona corretamente para habilitar o botão
                    statusSelect.addEventListener('change', () => {
                        pendingChanges[evento.id] = { reservaId: evento.id, novoStatus: statusSelect.value };
                        
                        // Habilita o botão "Aplicar Alterações"
                        btnAplicarMudancas.disabled = false; 

                        // Atualiza a interface para dar feedback imediato
                        const newStatusText = statusSelect.options[statusSelect.selectedIndex].text;
                        statusBadge.textContent = newStatusText;
                        statusBadge.className = `badge status-${statusSelect.value.toLowerCase()}`;
                        
                        // Esconde o select e volta a mostrar o badge e o botão de editar
                        statusSelect.style.display = 'none';
                        statusBadge.style.display = 'inline-block';
                        editButton.style.display = 'inline-block';
                    });
                });
                
            } else {
                semEventosAviso.style.display = 'block';
                modalFooter.className = 'modal-footer d-flex justify-content-end'; // Alinhamento à direita
                btnAplicarMudancas.style.display = 'none'; // Esconde o botão "Aplicar"
            }
            modalView.style.display = 'flex';
        });
    });

        // Listener para o botão de solicitar reserva (símbolo '+')
    document.querySelectorAll('.btn-solicitar-reserva').forEach(button => {
        button.addEventListener('click', (event) => {
            event.stopPropagation(); // Impede que o modal de visualização abra
            
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
        if (event.target === modalView) {
            fecharTodosModais();
        }
        // Se o alvo do clique for o overlay do modal de formulário, fecha
        if (event.target === modalReservaForm) {
            fecharTodosModais();
        }
    });

});