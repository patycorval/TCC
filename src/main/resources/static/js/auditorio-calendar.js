document.addEventListener('DOMContentLoaded', () => {
    const overlayReserva = document.getElementById('overlay-reserva');
    const formReserva = document.querySelector('.solicitacao-form');
    const campoDataEvento = document.getElementById('dataEvento');
    const modalEvento = new bootstrap.Modal(document.getElementById('evento-detalhes-modal'));
    const listaEventosModal = document.getElementById('lista-eventos-modal');
    const dataEventoModalSpan = document.getElementById('data-evento-modal');

    // Mapeia os dados de reservas do servidor para uma estrutura fácil de usar
    const reservasDoMes = JSON.parse(document.getElementById('reservas-data').textContent);
console.log("jsss")
    // Evento de clique para os dias do calendário
    document.querySelectorAll('.dia.mensal').forEach(diaElemento => {
        diaElemento.addEventListener('click', (event) => {
            const diaNumero = diaElemento.querySelector('span').textContent;
            if (!diaNumero || diaNumero === '0') return; // Ignora dias vazios

            const dataSelecionada = `${anoAtual}-${mesAtual.toString().padStart(2, '0')}-${diaNumero.padStart(2, '0')}`;

            // Filtra as reservas para a data selecionada
            const eventosDoDia = reservasDoMes.filter(reserva => reserva.data === dataSelecionada);

            if (eventosDoDia.length > 0) {
                // Se houver eventos, mostra o modal de detalhes
                listaEventosModal.innerHTML = '';
                eventosDoDia.forEach(evento => {
                    const li = document.createElement('li');
                    li.classList.add('list-group-item');
                    li.innerHTML = `<strong>${evento.evento}</strong><br/>Horário: ${evento.hora} - ${evento.horaFim}`;
                    listaEventosModal.appendChild(li);
                });
                dataEventoModalSpan.textContent = eventosDoDia[0].data.split('-').reverse().join('/');
                modalEvento.show();
            } else {
                // Se o dia estiver disponível, mostra o formulário de solicitação
                campoDataEvento.value = dataSelecionada;
                overlayReserva.style.display = 'flex';
            }
        });
    });

    // Evento para fechar a modal de reserva
    document.getElementById('fechar-form').addEventListener('click', () => {
        overlayReserva.style.display = 'none';
    });

    overlayReserva.addEventListener('click', (event) => {
        if (event.target.id === 'overlay-reserva') {
            overlayReserva.style.display = 'none';
        }
    });

    // Evento para a modal de detalhes de evento
    document.getElementById('evento-detalhes-modal').addEventListener('hidden.bs.modal', function () {
        listaEventosModal.innerHTML = '';
    });
});