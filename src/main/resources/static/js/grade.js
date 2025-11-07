document.addEventListener('DOMContentLoaded', function () {

    const horarios = {
        matutino: ['07:40 às 08:30', '08:30 às 09:20', '09:30 às 10:20', '10:20 às 11:10', '11:10 às 12:00', '12:00 às 12:50'],
        noturno: ['18:40 às 19:30', '19:30 às 20:20', '20:30 às 21:20', '21:20 às 22:10', '22:10 às 23:00']
    };
    const diasDaSemanaJava = { MONDAY: 'Segunda', TUESDAY: 'Terça', WEDNESDAY: 'Quarta', THURSDAY: 'Quinta', FRIDAY: 'Sexta', SATURDAY: 'Sábado' };
    const diasDaSemana = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'];

    const cursoSelect = document.getElementById('curso');
    const semestreSelect = document.getElementById('semestre')
    const gradeBody = document.getElementById('gradeBody');

    // Modal
    const modalElement = document.getElementById('modalAula');
    const modal = modalElement ? new bootstrap.Modal(modalElement) : null; 
    const modalTitle = document.getElementById('modalAulaTitle');
    const modalUsuarioSelect = document.getElementById('modalUsuario');
    const modalSalaSelect = document.getElementById('modalSala');
    const modalDiaInput = document.getElementById('modalDiaSemana');
    const modalHorarioInput = document.getElementById('modalHorario');
    const btnSalvarAula = document.getElementById('btnSalvarAula');

    // CSRF Tokens
    const csrfToken = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    // --- EVENTOS ---
    if (cursoSelect) {
        cursoSelect.addEventListener('change', carregarUsuariosEGrade);
    }
    if (semestreSelect) {
        semestreSelect.addEventListener('change', carregarUsuariosEGrade);
    }
    if (btnSalvarAula) {
        btnSalvarAula.addEventListener('click', salvarAulaSemestre);
    }

    // --- FUNÇÕES ---

    function mostrarPlaceholderGrade() {
        if (!gradeBody) return;
        gradeBody.innerHTML = '';
        const placeholder = document.createElement('div');
        placeholder.className = 'grade-placeholder';
        placeholder.textContent = 'Selecione um curso e um semestre para exibir a grade.';
        gradeBody.appendChild(placeholder);
    }
   
    async function carregarUsuariosEGrade() {
        if (!cursoSelect || !semestreSelect || !modalUsuarioSelect || !gradeBody) {
             console.error("Elementos essenciais do filtro ou grade não encontrados.");
             return;
        }

        const cursoId = cursoSelect.value;
        const selectedOption = cursoSelect.options[cursoSelect.selectedIndex];
        const periodo = selectedOption?.dataset.periodo;
        const semestre = semestreSelect.value;

        // Limpa grade e selects
        gradeBody.innerHTML = '<tr><td colspan="7">Carregando...</td></tr>';
        limparSelect(modalUsuarioSelect, "Carregando...");

        if (!cursoId || !periodo) {
            mostrarPlaceholderGrade(); 
            limparSelect(modalUsuarioSelect, "Selecione um curso");
            return;
        }

        const usuariosDoCurso = await buscarUsuariosDoCurso(cursoId);
        if (usuariosDoCurso) {
            popularSelectUsuarios(usuariosDoCurso);
        }

        await gerarGrade(cursoId, periodo, semestre); 
    }

    async function buscarUsuariosDoCurso(cursoId) {
        try {
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

    function popularSelectUsuarios(usuarios) {
        limparSelect(modalUsuarioSelect, "Selecione..."); // Popula SÓ o modal
        usuarios.forEach(user => {
            const displayText = user.nome || user.email;
            if (user.id) {
                modalUsuarioSelect.options.add(new Option(displayText, user.id));
            }
        });
    }

    async function gerarGrade(cursoId, periodo, semestre) {
        gradeBody.innerHTML = '<tr><td colspan="7">Carregando grade...</td></tr>';
        const horariosDoPeriodo = horarios[periodo];
        if (!horariosDoPeriodo) {
            console.error("Período inválido ou não encontrado:", periodo);
            mostrarPlaceholderGrade(); 
            return;
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
                            <p><strong>${aula.professorNome}</strong></p>
                            <p style="font-size: 0.8rem">${aula.salaNumero}</p>
                        </div>
                    `;
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

    function abrirModalParaAdicionar(dia, horario) {
        if (!modal) { console.error("Instância do modal não encontrada."); return; }
        modalTitle.textContent = `Alocar Horário (${dia} - ${horario})`;
        
        modalUsuarioSelect.value = ""; 
        modalUsuarioSelect.disabled = false; 

        modalSalaSelect.value = "";
        modalDiaInput.value = dia;
        modalHorarioInput.value = horario;
        modal.show();
    }

    async function salvarAulaSemestre() {
         if (!modalUsuarioSelect || !modalSalaSelect || !cursoSelect || !semestreSelect || !modalDiaInput || !modalHorarioInput || !btnSalvarAula) {
             console.error("Elementos necessários para salvar não encontrados.");
             alert("Erro interno. Recarregue a página.");
             return;
         }
        const usuarioId = modalUsuarioSelect.value;
        const salaId = modalSalaSelect.value;
        const cursoId = cursoSelect.value;
        if (!usuarioId || !salaId || !cursoId) {
            alert('Por favor, selecione Curso, Professor/Monitor e Sala.');
            return;
        }
        const payload = {
            cursoId: cursoId,
            semestre: semestreSelect.value,
            usuarioId: usuarioId,
            salaId: salaId,
            diaSemana: modalDiaInput.value,
            horario: modalHorarioInput.value
        };
        if (!csrfHeader || !csrfToken) {
            alert("Erro de configuração de segurança (CSRF). Recarregue a página.");
            return;
        }
        try {
            btnSalvarAula.disabled = true;
            btnSalvarAula.textContent = "Salvando...";
            const response = await fetch('/api/grade/salvar-semestre', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
                body: JSON.stringify(payload)
            });
            if (response.ok) {
                modal.hide();
                atualizarCelulaNaGrade(payload);
            } else {
                 let errorMessage = `Erro ${response.status}: ${response.statusText}`;
                 try { errorMessage = await response.text(); } catch (e) { /* Ignora */ }
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

    function atualizarCelulaNaGrade(payload) {
         if (!modalUsuarioSelect || !modalSalaSelect || !gradeBody) return;
        const horarioDaCelula = payload.horario;
        const diaDaCelula = payload.diaSemana;
        const usuarioNome = modalUsuarioSelect.options[modalUsuarioSelect.selectedIndex]?.text || 'N/A';
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
                                <p><strong>${usuarioNome}</strong></p>
                                <p style="font-size: 0.8rem">${salaNome}</p>
                            </div>
                        `;
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

    if (cursoSelect && cursoSelect.value) {
        carregarUsuariosEGrade();
    } else if (gradeBody) {
         mostrarPlaceholderGrade();
    } else {
        console.error("Elemento 'curso' ou 'gradeBody' não encontrado na inicialização.");
    }

}); 