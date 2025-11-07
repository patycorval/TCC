// Esta função será executada quando o documento HTML estiver totalmente carregado
// document.addEventListener('DOMContentLoaded', function() {
//     // Pega os parâmetros da URL atual
//     const params = new URLSearchParams(window.location.search);
//     const andarFiltro = params.get('andar'); // Pega o valor do parâmetro 'andar'

//     // Pega os elementos do HTML pelos IDs que adicionamos
//     const tituloAndar3 = document.getElementById('andar3-titulo');
//     const secaoAndar3 = document.getElementById('andar3-section');
//     const tituloAndar5 = document.getElementById('andar5-titulo');
//     const secaoAndar5 = document.getElementById('andar5-section');

//     if (andarFiltro) {
//         if (andarFiltro.includes('3')) {
//             tituloAndar5.style.display = 'none';
//             secaoAndar5.style.display = 'none';
//         } 
//         else if (andarFiltro.includes('5')) {
//             tituloAndar3.style.display = 'none';
//             secaoAndar3.style.display = 'none';
//         }
//     }
// });

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
    // 2. Valida e adiciona filtros de data/hora
    const dataHoraPreenchidos = dataFiltro && horaInicioFiltro && horaFimFiltro;
    const dataHoraParcial = (dataFiltro || horaInicioFiltro || horaFimFiltro) && !dataHoraPreenchidos;

    if (dataHoraParcial) {
        // Cenário A: Preencheu parcialmente (Ex: só data, ou só data e início)
        timeAlert.classList.remove('d-none'); // Mostra o alerta
        
        // ===== ESTA É A CORREÇÃO =====
        // Impede que o resto da função (o recarregamento) seja executado
        return;

    } else if (dataHoraPreenchidos) {
        // Cenário B: Preencheu todos os 3 campos
        // Esconde o alerta e ADICIONA os parâmetros de data/hora
        timeAlert.classList.add('d-none'); // Esconde o alerta
        params.append('dataFiltro', dataFiltro);
        params.append('horaInicioFiltro', horaInicioFiltro);
        params.append('horaFimFiltro', horaFimFiltro);
    } else {
        // Cenário C: Não preencheu nenhum dos 3 campos de data/hora
        // Esconde o alerta e não faz nada (filtra só pelos estáticos)
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
        
        // Se todos estiverem preenchidos e o alerta estiver visível, esconde
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