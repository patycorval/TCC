document.addEventListener('DOMContentLoaded', () => {
    
    const btnAplicar = document.getElementById('btnAplicar');
    const selectProfessor = document.getElementById('professor');
    const selectCurso = document.getElementById('curso');
    const selectPeriodo = document.getElementById('periodo');
    const selectSemestre = document.getElementById('semestre');

    btnAplicar.addEventListener('click', () => {
        const professorSelecionado = selectProfessor.value;
        const cursoSelecionado = selectCurso.value;
        const periodoSelecionado = selectPeriodo.value;
        const semestreSelecionado = selectSemestre.value;

        // Log para verificação
        console.log('Filtros Aplicados:');
        console.log(`Professor: ${professorSelecionado}`);
        console.log(`Curso: ${cursoSelecionado}`);
        console.log(`Período: ${periodoSelecionado}`);
        console.log(`Semestre: ${semestreSelecionado}`);

        alert(`Filtros aplicados!\nProfessor: ${professorSelecionado}\nCurso: ${cursoSelecionado}\nPeríodo: ${periodoSelecionado}\nSemestre: ${semestreSelecionado}`);
    });

    const modal = document.getElementById('modal-edicao');
    const fecharModal = document.querySelector('.fechar-modal');
    const celulasGrade = document.querySelectorAll('.celula-grade');
    const modalDia = document.getElementById('modal-dia');
    const modalHorario = document.getElementById('modal-horario');
    const selectProfessorModal = document.getElementById('select-professor-modal');

    celulasGrade.forEach(celula => {
        celula.addEventListener('click', () => {
            // Pega os dados da célula clicada
            const dia = celula.getAttribute('data-dia');
            const horario = celula.getAttribute('data-horario');

            // Preenche as informações no modal
            modalDia.textContent = dia.charAt(0).toUpperCase() + dia.slice(1);
            modalHorario.textContent = horario;

            // Preenche o dropdown de professores
            selectProfessorModal.innerHTML = `
                <option value="">Nenhum</option>
                <option value="felipe">Felipe</option>
                <option value="garcia">Garcia</option>
            `;

            modal.style.display = 'flex';
        });
    });

    // Ação para fechar o modal
    fecharModal.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    window.addEventListener('click', (event) => {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });

    // Ação para salvar a edição (lógica de backend virá aqui)
    document.getElementById('salvar-edicao').addEventListener('click', () => {
        const professorSelecionado = selectProfessorModal.value;
        alert(`Professor ${professorSelecionado} atribuído para a célula!`);
        modal.style.display = 'none';
    });
});