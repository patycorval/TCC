document.addEventListener('DOMContentLoaded', () => {
    // Referências aos elementos dos modais
    const modalDiaView = document.getElementById('modal-dia-view');
    const modalReservaForm = document.getElementById('overlay-reserva');
    const modalDiaTitulo = document.getElementById('modal-dia-titulo');
    const listaEventosContainer = document.getElementById('lista-eventos-modal');
    const semEventosAviso = document.getElementById('sem-eventos-aviso');
    const btnAbrirFormReserva = document.getElementById('btn-abrir-form-reserva');
    const campoDataForm = document.getElementById('dataEvento');

    // Botões para fechar os modais
    const btnFecharView = document.getElementById('fechar-modal-view');
    const btnFecharForm = document.getElementById('fechar-modal-view');

    // --- NOVA FUNÇÃO PARA FORMATAR HORA ---
    const formatarHora = (horaArray) => {
        if (!Array.isArray(horaArray) || horaArray.length < 2) {
            return horaArray; // Retorna o valor original se não for um array de hora
        }
        const horas = horaArray[0].toString().padStart(2, '0');
        const minutos = horaArray[1].toString().padStart(2, '0');
        return `${horas}:${minutos}`;
    };

    const formatarData = (dia, mes, ano) => {
        const data = new Date(ano, mes - 1, dia);
        return data.toLocaleDateString('pt-BR', { timeZone: 'UTC' });
    };

    document.querySelectorAll('.dia.mensal:not(.vazio)').forEach(diaElemento => {
        diaElemento.addEventListener('click', () => {
            const dia = diaElemento.getAttribute('data-dia');
            
            const urlParams = new URLSearchParams(window.location.search);
            const ano = urlParams.get('ano') || new Date().getFullYear();
            const mes = urlParams.get('mes') || (new Date().getMonth() + 1);
            
            modalDiaTitulo.textContent = `Eventos para ${formatarData(dia, mes, ano)}`;
            btnAbrirFormReserva.setAttribute('data-dia-selecionado', `${ano}-${mes.toString().padStart(2, '0')}-${dia.toString().padStart(2, '0')}`);

            listaEventosContainer.innerHTML = '';
            
            const eventos = JSON.parse(diaElemento.getAttribute('data-eventos'));

            if (eventos && eventos.length > 0) {
                semEventosAviso.style.display = 'none';
                listaEventosContainer.style.display = 'block';

                eventos.forEach(evento => {
                    // --- APLICA A FORMATAÇÃO DE HORA AQUI ---
                    const horaInicioFormatada = formatarHora(evento.hora);
                    const horaFimFormatada = formatarHora(evento.horaFim);

                    const statusClass = `status-${evento.status.toLowerCase()}`;
                    const itemHTML = `
                        <li class="list-group-item evento-modal-item">
                            <div class="evento-info">
                                <span class="evento-nome-modal">${evento.evento}</span>
                                <span class="evento-solicitante-modal">Solicitado por: ${evento.nome}</span>
                            </div>
                            <div class="evento-horario">
                                <span class="evento-hora-modal">${horaInicioFormatada} - ${horaFimFormatada}</span>
                                <span class="badge ${statusClass}">${evento.status}</span>
                            </div>
                        </li>
                    `;
                    listaEventosContainer.innerHTML += itemHTML;
                });
            } else {
                semEventosAviso.style.display = 'block';
                listaEventosContainer.style.display = 'none';
            }
            
            btnAbrirFormReserva.disabled = diaElemento.classList.contains('indisponivel');
            modalDiaView.style.display = 'flex';
        });
    });

    btnAbrirFormReserva.addEventListener('click', () => {
        const dataSelecionada = btnAbrirFormReserva.getAttribute('data-dia-selecionado');
        campoDataForm.value = dataSelecionada;
        modalDiaView.style.display = 'none';
        modalReservaForm.style.display = 'flex';
    });

    const fecharTodosModais = () => {
        modalDiaView.style.display = 'none';
        modalReservaForm.style.display = 'none';
    };

    btnFecharView.addEventListener('click', fecharTodosModais);
    btnFecharForm.addEventListener('click', fecharTodosModais);
    modalDiaView.addEventListener('click', (e) => { if (e.target.id === 'modal-dia-view') fecharTodosModais(); });
    modalReservaForm.addEventListener('click', (e) => { if (e.target.id === 'overlay-reserva') fecharTodosModais(); });
});