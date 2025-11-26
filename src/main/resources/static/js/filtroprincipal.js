function aplicarFiltros() {
    const andarFiltro = document.getElementById('andar').value;
    const recursoFiltro = document.getElementById('recurso').value;
    const tipoSalaFiltro = document.getElementById('tiposala').value;
    const dataFiltro = document.getElementById('dataFiltro').value;
    const horaInicioFiltro = document.getElementById('horaInicioFiltro').value;
    const horaFimFiltro = document.getElementById('horaFimFiltro').value;
    const timeAlert = document.getElementById('filtro-time-alert');

    const params = new URLSearchParams();

    if (andarFiltro) {
        params.append('andar', andarFiltro);
    }
    if (recursoFiltro) {
        params.append('recurso', recursoFiltro);
    }
    if (tipoSalaFiltro) {
        params.append('tiposala', tipoSalaFiltro);
    }
    const dataHoraPreenchidos = dataFiltro && horaInicioFiltro && horaFimFiltro;
    const dataHoraParcial = (dataFiltro || horaInicioFiltro || horaFimFiltro) && !dataHoraPreenchidos;

    if (dataHoraParcial) {
        timeAlert.classList.remove('d-none');
        return;

    } else if (dataHoraPreenchidos) {
        timeAlert.classList.add('d-none'); 
        params.append('dataFiltro', dataFiltro);
        params.append('horaInicioFiltro', horaInicioFiltro);
        params.append('horaFimFiltro', horaFimFiltro);
    } else {
        timeAlert.classList.add('d-none');
    }

    window.location.href = '/' + (params.toString() ? '?' + params.toString() : '');
}

document.addEventListener('DOMContentLoaded', function() {
   
    try {
        const today = new Date();
        const yyyy = today.getFullYear();
        const mm = String(today.getMonth() + 1).padStart(2, '0');
        const dd = String(today.getDate()).padStart(2, '0');
        const todayString = `${yyyy}-${mm}-${dd}`;

        const dataInput = document.getElementById('dataFiltro');
        if (dataInput) {
            dataInput.setAttribute('min', todayString);
        }
    } catch (e) {
        console.error("Erro ao definir data mínima:", e);
    }
    const fields = ['dataFiltro', 'horaInicioFiltro', 'horaFimFiltro'];
    const timeAlert = document.getElementById('filtro-time-alert');

    const checkFields = () => {
        if (!timeAlert) return;

        const dataFiltro = document.getElementById('dataFiltro').value;
        const horaInicioFiltro = document.getElementById('horaInicioFiltro').value;
        const horaFimFiltro = document.getElementById('horaFimFiltro').value;
        
        if (dataFiltro && horaInicioFiltro && horaFimFiltro && !timeAlert.classList.contains('d-none')) {
            timeAlert.classList.add('d-none');
        }
    };

    fields.forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.addEventListener('input', checkFields);
        }
    });
});