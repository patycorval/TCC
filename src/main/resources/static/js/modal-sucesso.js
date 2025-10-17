document.addEventListener('DOMContentLoaded', () => {

    const modal = document.getElementById("modal-sucesso");
    const closeBtn = document.getElementById("closeModal");
    const body = document.body;

    if (!modal || !closeBtn) {
        console.error("Elementos do modal não foram encontrados no HTML.");
        return;
    }

    const reservaStatus = body.dataset.reservaStatus;

    if (reservaStatus === 'sucesso') {
        modal.classList.add('open');
    }

    closeBtn.addEventListener("click", () => {
        modal.classList.remove('open');
    });

});