document.addEventListener('DOMContentLoaded', function () {

    const horarios = {
        matutino: ['07:40 às 08:30', '08:30 às 09:20', '09:30 às 10:20', '10:20 às 11:10', '11:10 às 12:00', '12:00 às 12:50'],
        noturno: ['18:40 às 19:30', '19:30 às 20:20', '20:30 às 21:20', '21:20 às 22:10', '22:10 às 23:00']
    };
    const diasDaSemanaJava = { MONDAY: 'Segunda', TUESDAY: 'Terça', WEDNESDAY: 'Quarta', THURSDAY: 'Quinta', FRIDAY: 'Sexta', SATURDAY: 'Sábado' };
    const diasDaSemana = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'];

    // Seletores DOM (IDs atualizados)
    const cursoSelect = document.getElementById('curso');
    const periodoSelect = document.getElementById('periodo');
    const semestreSelect = document.getElementById('semestre');
    const usuarioFiltroSelect = document.getElementById('usuarioFiltro'); // ALTERADO
    const gradeBody = document.getElementById('gradeBody');

    // Modal (IDs atualizados)
    const modalElement = document.getElementById('modalAula');
    const modal = new bootstrap.Modal(modalElement);
    const modalTitle = document.getElementById('modalAulaTitle');
    const modalUsuarioSelect = document.getElementById('modalUsuario'); // ALTERADO
    const modalSalaSelect = document.getElementById('modalSala');
    const modalDiaInput = document.getElementById('modalDiaSemana');
    const modalHorarioInput = document.getElementById('modalHorario');
    const btnSalvarAula = document.getElementById('btnSalvarAula');

    // CSRF Tokens
    const csrfToken = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    // --- EVENTOS ---
    cursoSelect.addEventListener('change', carregarUsuariosEGrade);
    semestreSelect.addEventListener('change', carregarUsuariosEGrade);
    btnSalvarAula.addEventListener('click', salvarAulaSemestre);

    // --- FUNÇÕES ---

    async function carregarUsuariosEGrade() {
        const cursoId = cursoSelect.value;
        const selectedOption = cursoSelect.options[cursoSelect.selectedIndex];
        // Adiciona verificação se selectedOption existe
        const periodo = selectedOption?.dataset.periodo; 
        const semestre = semestreSelect.value;

        periodoSelect.value = periodo ? periodo.toLowerCase() : "";

        gradeBody.innerHTML = '<tr><td colspan="7">Carregando...</td></tr>';
        limparSelect(usuarioFiltroSelect, "Carregando..."); // ALTERADO
        limparSelect(modalUsuarioSelect, "Carregando...");   // ALTERADO

        if (!cursoId || !periodo) { // Verifica periodo também
            gradeBody.innerHTML = '';
            limparSelect(usuarioFiltroSelect, "Selecione um curso");
            limparSelect(modalUsuarioSelect, "Selecione um curso");
            return;
        }

        const usuariosDoCurso = await buscarUsuariosDoCurso(cursoId);
        if (usuariosDoCurso) {
            popularSelectUsuarios(usuariosDoCurso);
        }

        await gerarGrade(cursoId, periodo.toLowerCase(), semestre);
    }

    async function buscarUsuariosDoCurso(cursoId) {
        try {
            // A URL da API continua /api/professores por compatibilidade
            const response = await fetch(`/api/professores?cursoId=${cursoId}`); 
            if (!response.ok) {
                console.error("Erro ao buscar usuários:", response.status);
                return null;
            }
            return await response.json();
        } catch (error) {
            console.error('Erro de conexão ao buscar usuários:', error);
            return null;
        }
    }

    // ALTERADO: Popula selects de Usuario
    function popularSelectUsuarios(usuarios) {
        limparSelect(usuarioFiltroSelect, "Todos"); 
        limparSelect(modalUsuarioSelect, "Selecione..."); 
        usuarios.forEach(user => {
            // Usar user.email como texto (ajuste se Usuario tiver campo 'nome')
            const displayText = user.email; 
            usuarioFiltroSelect.options.add(new Option(displayText, user.id));
            modalUsuarioSelect.options.add(new Option(displayText, user.id));
        });
    }

    async function gerarGrade(cursoId, periodo, semestre) {
        gradeBody.innerHTML = '<tr><td colspan="7">Carregando grade...</td></tr>';
        const horariosDoPeriodo = horarios[periodo];
        if (!horariosDoPeriodo) {
            gradeBody.innerHTML = ''; return;
        }

        let gradeSalva = [];
        try {
            const response = await fetch(`/api/grade/reservas?cursoId=${cursoId}&semestre=${semestre}`);
            if (response.ok) {
                gradeSalva = await response.json();
            } else {
                 console.error("Erro ao buscar grade:", response.status);
            }
        } catch (error) {
            console.error('Erro de conexão ao buscar grade:', error);
        }

        gradeBody.innerHTML = ''; 

        horariosDoPeriodo.forEach(horario => {
            const linhaHorario = document.createElement('div');
            linhaHorario.className = 'linha-horario';
            linhaHorario.innerHTML = `<div class="horario-celula">${horario}</div>`;

            diasDaSemana.forEach(dia => {
                const celulaGrade = document.createElement('div');
                celulaGrade.className = 'celula-grade';

                const aula = gradeSalva.find(dto =>
                    diasDaSemanaJava[dto.diaSemana] === dia && dto.horario === horario
                );

                if (aula) {
                    celulaGrade.innerHTML = `
                        <div class="detalhes-aula">
                            <p><strong>${aula.professorNome}</strong></p> <p style="font-size: 0.8rem">${aula.salaNumero}</p>
                        </div>
                    `;
                    // Adicionar evento de clique para EDITAR (futuro)
                } else {
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

    // ALTERADO: Usa modalUsuarioSelect
    function abrirModalParaAdicionar(dia, horario) {
        modalTitle.textContent = `Alocar Horário (${dia} - ${horario})`;
        modalUsuarioSelect.value = ""; 
        modalSalaSelect.value = "";
        modalDiaInput.value = dia;
        modalHorarioInput.value = horario;
        modal.show();
    }

    // ALTERADO: Envia usuarioId
    async function salvarAulaSemestre() {
        const usuarioId = modalUsuarioSelect.value; // ALTERADO
        const salaId = modalSalaSelect.value;
        const cursoId = cursoSelect.value;

        if (!usuarioId || !salaId || !cursoId) { // Verifica cursoId também
            alert('Por favor, selecione Curso, Professor/Monitor e Sala.');
            return;
        }

        const payload = {
            cursoId: cursoId,
            semestre: semestreSelect.value,
            usuarioId: usuarioId, // ALTERADO
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
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                modal.hide();
                atualizarCelulaNaGrade(payload); // Atualiza visualmente
            } else {
                 let errorMessage = `Erro ${response.status}: ${response.statusText}`;
                 try {
                     errorMessage = await response.text();
                 } catch (e) { /* Ignora se não conseguir ler o texto */ }
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

    // ALTERADO: Pega nome/email do modalUsuarioSelect
    function atualizarCelulaNaGrade(payload) {
        const horarioDaCelula = payload.horario;
        const diaDaCelula = payload.diaSemana;
        // Pega o TEXTO da opção selecionada (Email ou Nome do Usuário)
        const usuarioNome = modalUsuarioSelect.options[modalUsuarioSelect.selectedIndex]?.text || 'N/A'; // ALTERADO
        const salaNome = modalSalaSelect.options[modalSalaSelect.selectedIndex]?.text || 'N/A';

        const linhas = gradeBody.querySelectorAll('.linha-horario');

        linhas.forEach(linha => {
            const horario = linha.querySelector('.horario-celula')?.textContent;
            if (horario === horarioDaCelula) {
                const indiceDoDia = diasDaSemana.indexOf(diaDaCelula);
                if (indiceDoDia !== -1) {
                    const celula = linha.children[indiceDoDia + 1];
                    if(celula) {
                        celula.innerHTML = `
                            <div class="detalhes-aula">
                                <p><strong>${usuarioNome}</strong></p> <p style="font-size: 0.8rem">${salaNome}</p>
                            </div>
                        `;
                        // Remover botão '+' se existir e adicionar evento de edição (futuro)
                    }
                }
            }
        });
    }

    function limparSelect(selectElement, placeholder) {
        if(selectElement) {
             selectElement.innerHTML = `<option value="">${placeholder || 'Selecione...'}</option>`;
        }
    }

    // --- INICIALIZAÇÃO ---
    if (cursoSelect.value) {
        carregarUsuariosEGrade();
    } else {
         gradeBody.innerHTML = ''; 
    }

}); // Fim do DOMContentLoaded