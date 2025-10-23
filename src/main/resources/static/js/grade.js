document.addEventListener('DOMContentLoaded', function () {

    const horarios = {
        matutino: ['07:40 às 08:30', '08:30 às 09:20', '09:30 às 10:20', '10:20 às 11:10', '11:10 às 12:00', '12:00 às 12:50'],
        noturno: ['18:40 às 19:30', '19:30 às 20:20', '20:30 às 21:20', '21:20 às 22:10', '22:10 às 23:00']
    };
    // Mapeamento de DayOfWeek (Java) para String (Português)
    const diasDaSemanaJava = { MONDAY: 'Segunda', TUESDAY: 'Terça', WEDNESDAY: 'Quarta', THURSDAY: 'Quinta', FRIDAY: 'Sexta', SATURDAY: 'Sábado' };
    const diasDaSemana = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'];

    // Seletores do DOM
    const cursoSelect = document.getElementById('curso');
    const periodoSelect = document.getElementById('periodo');
    const semestreSelect = document.getElementById('semestre');
    const professorFiltroSelect = document.getElementById('professorFiltro');
    const gradeBody = document.getElementById('gradeBody');
    
    // Modal
    const modalElement = document.getElementById('modalAula');
    const modal = new bootstrap.Modal(modalElement);
    const modalTitle = document.getElementById('modalAulaTitle');
    const modalProfessorSelect = document.getElementById('modalProfessor');
    const modalSalaSelect = document.getElementById('modalSala');
    const modalDiaInput = document.getElementById('modalDiaSemana');
    const modalHorarioInput = document.getElementById('modalHorario');
    const btnSalvarAula = document.getElementById('btnSalvarAula');

    // --- EVENTOS PRINCIPAIS ---

    // Quando o ADMIN muda o CURSO
    cursoSelect.addEventListener('change', async function() {
        await carregarGradeECursos();
    });
    
    // Quando o ADMIN muda o SEMESTRE
    semestreSelect.addEventListener('change', async () => {
        await carregarGradeECursos();
    });

    // Quando o ADMIN clica em SALVAR no MODAL
    btnSalvarAula.addEventListener('click', salvarAulaSemestre);

    // --- FUNÇÕES ---

    // Função principal: Carrega professores e depois a grade
    async function carregarGradeECursos() {
        const cursoId = cursoSelect.value;
        const selectedOption = cursoSelect.options[cursoSelect.selectedIndex];
        const periodo = selectedOption.dataset.periodo; // 'matutino' ou 'noturno'
        
        // Atualiza o <select> de período
        periodoSelect.value = periodo ? periodo.toLowerCase() : "";
        
        // Limpa a grade e os filtros de professor
        gradeBody.innerHTML = '<tr><td colspan="7">Carregando grade...</td></tr>'; // Feedback visual
        limparSelect(professorFiltroSelect, "Selecione um professor...");
        limparSelect(modalProfessorSelect, "Selecione um professor...");
        
        if (!cursoId) {
             gradeBody.innerHTML = ''; // Limpa se nenhum curso selecionado
             return;
        }

        // Busca os professores DESSE curso na API
        await popularProfessores(cursoId);
        
        // Gera a grade buscando dados da API
        await gerarGrade(cursoId, periodo.toLowerCase(), semestreSelect.value);
    }

    // Busca professores na API e popula os <select>
    async function popularProfessores(cursoId) {
        try {
            const response = await fetch(`/api/professores?cursoId=${cursoId}`);
            if (!response.ok) {
                 limparSelect(professorFiltroSelect, "Erro ao carregar");
                 limparSelect(modalProfessorSelect, "Erro ao carregar");
                 return;
            }
            
            const professores = await response.json();
             limparSelect(professorFiltroSelect, "Selecione um professor...");
             limparSelect(modalProfessorSelect, "Selecione um professor...");
            
            professores.forEach(prof => {
                professorFiltroSelect.options.add(new Option(prof.nome, prof.id));
                modalProfessorSelect.options.add(new Option(prof.nome, prof.id));
            });
        } catch (error) {
            console.error('Erro ao buscar professores:', error);
             limparSelect(professorFiltroSelect, "Erro de conexão");
             limparSelect(modalProfessorSelect, "Erro de conexão");
        }
    }

    // -- FUNÇÃO MODIFICADA: Gera a grade buscando dados --
    async function gerarGrade(cursoId, periodo, semestre) {
        gradeBody.innerHTML = '<tr><td colspan="7">Carregando dados...</td></tr>'; // Feedback
        const horariosDoPeriodo = horarios[periodo];
        if (!horariosDoPeriodo) {
            gradeBody.innerHTML = ''; // Limpa se período inválido
            return;
        }

        // ** BUSCA DADOS REAIS DA API **
        let dadosDaGrade = [];
        try {
            // Chama a API que busca as reservas da primeira semana
            const response = await fetch(`/api/grade/reservas?cursoId=${cursoId}&semestre=${semestre}`);
            if (response.ok) {
                dadosDaGrade = await response.json(); // Espera os dados como ReservaDTO
                 console.log("Dados recebidos da API:", dadosDaGrade);
            } else {
                 console.error("Erro ao buscar dados da API:", response.status, response.statusText);
            }
        } catch (error) {
            console.error('Erro ao buscar dados da grade:', error);
        }
        
        gradeBody.innerHTML = ''; // Limpa o "Carregando"

        // Cria a grade
        horariosDoPeriodo.forEach(horario => {
            const linhaHorario = document.createElement('div');
            linhaHorario.className = 'linha-horario';
            linhaHorario.innerHTML = `<div class="horario-celula">${horario}</div>`;

            diasDaSemana.forEach(dia => {
                const celulaGrade = document.createElement('div');
                celulaGrade.className = 'celula-grade';

                // Verifica se existe uma aula para esta célula nos dados recebidos
                // Compara o dia da semana (convertido) e o horário
                const aula = dadosDaGrade.find(dto => 
                    diasDaSemanaJava[dto.diaSemana] === dia && dto.horario === horario
                );

                if (aula) {
                    // Célula preenchida com dados da API
                    celulaGrade.innerHTML = `
                        <div class="detalhes-aula">
                            <p><strong>${aula.professorNome}</strong></p>
                            <p style="font-size: 0.8rem">${aula.salaNumero}</p> 
                        </div>
                    `;
                    // Adicionar evento de clique para editar/remover (futuro)
                } else {
                    // Célula vazia com botão de adicionar
                    const btnAdd = document.createElement('button');
                    btnAdd.className = 'btn btn-sm btn-outline-primary btn-add';
                    btnAdd.innerHTML = '+';
                    btnAdd.addEventListener('click', () => {
                        abrirModalParaAdicionar(dia, horario);
                    });
                    celulaGrade.appendChild(btnAdd);
                }
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

    async function salvarAulaSemestre() {
        const professorId = modalProfessorSelect.value;
        const salaId = modalSalaSelect.value;
        const cursoId = cursoSelect.value; // Pega o ID do curso direto
        
        if (!professorId || !salaId || !cursoId) {
            alert('Por favor, selecione Curso, Professor e Sala.');
            return;
        }

        const payload = {
            // cursoSigla: não precisamos mais, enviamos ID
            // periodo: não precisamos mais, controller pega do Curso
            cursoId: cursoId, // Envia o ID do curso
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
                headers: { 
                    'Content-Type': 'application/json',
                    // Adiciona o Header CSRF
                    [document.querySelector("meta[name='_csrf_header']").getAttribute("content")]: document.querySelector("meta[name='_csrf']").getAttribute("content") 
                },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                modal.hide();
                atualizarCelulaNaGrade(payload); 
            } else {
                 let errorMessage = `Erro ${response.status}: ${response.statusText}`;
                 try {
                     const errorData = await response.json(); 
                     errorMessage = errorData.message || JSON.stringify(errorData);
                 } catch (e) {
                     errorMessage = await response.text();
                 }
                 console.error("Detalhes do erro:", response);
                 alert('Erro ao salvar: ' + errorMessage);
            }
        } catch (error) {
            console.error('Erro ao salvar grade:', error);
            alert('Erro de conexão ao salvar.');
        } finally {
             btnSalvarAula.disabled = false;
             btnSalvarAula.textContent = "Salvar Semestre";
        }
    }
    
    // Atualiza a célula (sem mudanças aqui)
    function atualizarCelulaNaGrade(payload) {
        const horarioDaCelula = payload.horario;
        const diaDaCelula = payload.diaSemana;
        const profNome = modalProfessorSelect.options[modalProfessorSelect.selectedIndex].text;
        const salaNome = modalSalaSelect.options[modalSalaSelect.selectedIndex].text; // Assume que o texto é "Tipo Numero"

        const linhas = gradeBody.querySelectorAll('.linha-horario');
        
        linhas.forEach(linha => {
            const horario = linha.querySelector('.horario-celula').textContent;
            if (horario === horarioDaCelula) {
                const indiceDoDia = diasDaSemana.indexOf(diaDaCelula);
                if (indiceDoDia !== -1) {
                    const celula = linha.children[indiceDoDia + 1]; 
                    if(celula) {
                        celula.innerHTML = `
                            <div class="detalhes-aula">
                                <p><strong>${profNome}</strong></p>
                                <p style="font-size: 0.8rem">${salaNome}</p>
                            </div>
                        `;
                    }
                }
            }
        });
    }

    function limparSelect(selectElement, placeholder) {
        selectElement.innerHTML = `<option value="">${placeholder}</option>`;
    }

    carregarGradeECursos(); 

});