document.addEventListener('DOMContentLoaded', function () {

    const horarios = {
        matutino: ['07:40 às 08:30', '08:30 às 09:20', '09:30 às 10:20', '10:20 às 11:10', '11:10 às 12:00', '12:00 às 12:50'],
        noturno: ['18:40 às 19:30', '19:30 às 20:20', '20:30 às 21:20', '21:20 às 22:10', '22:10 às 23:00']
    };
    const diasDaSemana = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'];

    const cursoSelect = document.getElementById('curso');
    const periodoSelect = document.getElementById('periodo');
    const semestreSelect = document.getElementById('semestre');
    const professorFiltroSelect = document.getElementById('professorFiltro');
    const gradeBody = document.getElementById('gradeBody');
    const modalElement = document.getElementById('modalAula');
    const modal = new bootstrap.Modal(modalElement);
    const modalTitle = document.getElementById('modalAulaTitle');
    const modalProfessorSelect = document.getElementById('modalProfessor');
    const modalSalaSelect = document.getElementById('modalSala');
    const modalDiaInput = document.getElementById('modalDiaSemana');
    const modalHorarioInput = document.getElementById('modalHorario');
    const btnSalvarAula = document.getElementById('btnSalvarAula');


    // Quando o ADMIN muda o CURSO
    cursoSelect.addEventListener('change', async function() {
        const cursoId = this.value;
        const selectedOption = this.options[this.selectedIndex];
        const periodo = selectedOption.dataset.periodo; 
        
        
        periodoSelect.value = periodo.toLowerCase();
        
        
        gradeBody.innerHTML = '';
        limparSelect(professorFiltroSelect, "Selecione um professor...");
        limparSelect(modalProfessorSelect, "Selecione um professor...");
        
        if (!cursoId) return;

        
        await popularProfessores(cursoId);
        
        
        gerarGradeVazia(periodo.toLowerCase());
    });
    
    // Quando o ADMIN muda o SEMESTRE (recarrega a grade)
    semestreSelect.addEventListener('change', () => {
        const periodo = periodoSelect.value;
        gerarGradeVazia(periodo);
    });

    btnSalvarAula.addEventListener('click', salvarAulaSemestre);


    // Busca professores na API e popula os <select>
    async function popularProfessores(cursoId) {
        try {
            const response = await fetch(`/api/professores?cursoId=${cursoId}`);
            if (!response.ok) return;
            
            const professores = await response.json();
            
            professores.forEach(prof => {
                professorFiltroSelect.options.add(new Option(prof.nome, prof.id));
                modalProfessorSelect.options.add(new Option(prof.nome, prof.id));
            });
        } catch (error) {
            console.error('Erro ao buscar professores:', error);
        }
    }

    // Gera a grade vazia com os botões '+'
    function gerarGradeVazia(periodo) {
        gradeBody.innerHTML = '';
        const horariosDoPeriodo = horarios[periodo];
        if (!horariosDoPeriodo) return;

        horariosDoPeriodo.forEach(horario => {
            const linhaHorario = document.createElement('div');
            linhaHorario.className = 'linha-horario';
            linhaHorario.innerHTML = `<div class="horario-celula">${horario}</div>`;

            diasDaSemana.forEach(dia => {
                const celulaGrade = document.createElement('div');
                celulaGrade.className = 'celula-grade';

                
                const btnAdd = document.createElement('button');
                btnAdd.className = 'btn btn-sm btn-outline-primary btn-add';
                btnAdd.innerHTML = '+';
                btnAdd.addEventListener('click', () => {
                    abrirModalParaAdicionar(dia, horario);
                });
                celulaGrade.appendChild(btnAdd);
                linhaHorario.appendChild(celulaGrade);
            });
            gradeBody.appendChild(linhaHorario);
        });
    }

    
    function abrirModalParaAdicionar(dia, horario) {
        modalTitle.textContent = `Alocar Horário (${dia} - ${horario})`;
        
        
        modalProfessorSelect.value = "";
        modalSalaSelect.value = "";
        
        
        modalDiaInput.value = dia;
        modalHorarioInput.value = horario;
        
        modal.show();
    }

    // SALVA A GRADE (chama a API do Controller)
    async function salvarAulaSemestre() {
        const professorId = modalProfessorSelect.value;
        const salaId = modalSalaSelect.value;
        const selectedCursoOpt = cursoSelect.options[cursoSelect.selectedIndex];
        
        if (!professorId || !salaId) {
            alert('Por favor, selecione um Professor e uma Sala.');
            return;
        }

        const payload = {
            cursoSigla: selectedCursoOpt.dataset.sigla,
            periodo: selectedCursoOpt.dataset.periodo,
            semestre: semestreSelect.value,
            professorId: professorId,
            salaId: salaId,
            diaSemana: modalDiaInput.value,
            horario: modalHorarioInput.value
        };

        try {
            btnSalvarAula.disabled = true;
            btnSalvarAula.textContent = "Salvando...";

            const response = await fetch('/api/grade/salvar-semestre', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                modal.hide();
                // Simplesmente atualiza a célula salva
                atualizarCelulaNaGrade(payload); 
            } else {
                const error = await response.text();
                alert('Erro ao salvar: ' + error);
            }
        } catch (error) {
            console.error('Erro ao salvar grade:', error);
            alert('Erro de conexão ao salvar.');
        } finally {
             btnSalvarAula.disabled = false;
             btnSalvarAula.textContent = "Salvar Semestre";
        }
    }
    
    // Atualiza a célula na tela após salvar (para não ter que recarregar tudo)
    function atualizarCelulaNaGrade(payload) {
        const horarioDaCelula = payload.horario;
        const diaDaCelula = payload.diaSemana;
        const profNome = modalProfessorSelect.options[modalProfessorSelect.selectedIndex].text;
        const salaNome = modalSalaSelect.options[modalSalaSelect.selectedIndex].text;

        const linhas = gradeBody.querySelectorAll('.linha-horario');
        
        linhas.forEach(linha => {
            const horario = linha.querySelector('.horario-celula').textContent;
            if (horario === horarioDaCelula) {
                const indiceDoDia = diasDaSemana.indexOf(diaDaCelula);
                const celula = linha.children[indiceDoDia + 1]; // +1 por causa da célula de horário
                
                celula.innerHTML = `
                    <div class="detalhes-aula">
                        <p><strong>${profNome}</strong></p>
                        <p style="font-size: 0.8rem">${salaNome}</p>
                    </div>
                `;
            }
        });
    }

    function limparSelect(selectElement, placeholder) {
        selectElement.innerHTML = `<option value="">${placeholder}</option>`;
    }

});