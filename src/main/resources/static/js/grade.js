document.addEventListener('DOMContentLoaded', function () {

    // Mapeamento dos horários para cada período
    const horarios = {
        matutino: [
            '07:40 às 08:30', '08:30 às 09:20', '09:30 às 10:20',
            '10:20 às 11:10', '11:10 às 12:00', '12:00 às 12:50'
        ],
        noturno: [
            '18:40 às 19:30', '19:30 às 20:20', '20:30 às 21:20',
            '21:20 às 22:10', '22:10 às 23:00'
        ]
    };

    // Selecionando os elementos do DOM
    const periodoSelect = document.getElementById('periodo');
    const gradeBody = document.getElementById('gradeBody');
    const btnAplicar = document.getElementById('btnAplicar');
    
    // Função para gerar a estrutura da grade de horários vazia
    function gerarGradeVazia(periodo) {
        // Limpa a tabela atual
        gradeBody.innerHTML = '';

        const horariosDoPeriodo = horarios[periodo];
        if (!horariosDoPeriodo) return; // Segurança

        // Cria uma linha para cada horário
        horariosDoPeriodo.forEach(horario => {
            const linhaHorario = document.createElement('div');
            linhaHorario.className = 'linha-horario';

            // Adiciona a célula do horário
            linhaHorario.innerHTML = `<div class="horario-celula">${horario}</div>`;

            // Cria as células para os dias da semana
            for (let i = 0; i < 6; i++) { 
                const celulaGrade = document.createElement('div');
                celulaGrade.className = 'celula-grade';
                celulaGrade.innerHTML = `<button class="btn btn-sm btn-outline-primary btn-add">+</button>`;
                linhaHorario.appendChild(celulaGrade);
            }
            gradeBody.appendChild(linhaHorario);
        });
    }
    
    async function carregarDadosDaGrade() {
        const curso = document.getElementById('curso').value;
        const periodo = periodoSelect.value;
        const semestre = document.getElementById('semestre').value;
        
        // Por enquanto, vamos usar dados de exemplo.
        // Ex: const response = await fetch(`/api/grade?curso=${curso}&periodo=${periodo}&semestre=${semestre}`);
        // const dados = await response.json();
        
        const dadosMock = [
            { dia: 'Segunda', horario: '08:30 às 09:20', professor: 'Garcia', sala: 'Lab 3.3'},
            { dia: 'Terça', horario: '08:30 às 09:20', professor: 'Felipe', sala: 'Lab 5.2'},
            { dia: 'Terça', horario: '09:30 às 10:20', professor: 'Vanina', sala: 'Sala 511'}
        ];

        // Primeiro, gera a grade vazia correspondente ao período selecionado
        gerarGradeVazia(periodo);

        // Agora, preenche a grade com os dados recebidos
        const diasDaSemana = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'];
        const linhas = gradeBody.querySelectorAll('.linha-horario');

        linhas.forEach(linha => {
            const horarioDaLinha = linha.querySelector('.horario-celula').textContent;
            
            dadosMock.forEach(aula => {
                if (aula.horario === horarioDaLinha) {
                    const indiceDoDia = diasDaSemana.indexOf(aula.dia);
                    if (indiceDoDia !== -1) {
                        const celula = linha.children[indiceDoDia + 1]; 
                        celula.innerHTML = `
                            <div class="detalhes-aula">
                                <p>${aula.professor}</p>
                                <p>${aula.sala}</p>
                            </div>
                        `;
                    }
                }
            });
        });
    }

    periodoSelect.addEventListener('change', () => gerarGradeVazia(periodoSelect.value));
    btnAplicar.addEventListener('click', carregarDadosDaGrade);
    gerarGradeVazia(periodoSelect.value);
});