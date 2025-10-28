document.addEventListener('DOMContentLoaded', function() {
    const tipoSelect = document.getElementById('tipo');
    const cursoSelectDiv = document.getElementById('cursoSelectDiv'); 
    const cursoSelect = document.getElementById('cursos'); 

    if (tipoSelect && cursoSelectDiv && cursoSelect) {
        tipoSelect.addEventListener('change', function() {
            const selectedType = this.value;
            if (selectedType === 'MONITOR' || selectedType === 'PROFESSOR') {
                cursoSelectDiv.style.display = 'block'; 
                cursoSelect.required = true;
            } else {
                cursoSelectDiv.style.display = 'none';
                cursoSelect.required = false;
            }
        });
        
    } else {
        console.warn("Elementos do select de tipo/curso não encontrados."); 
    }


    const toggleSenha = document.querySelector('#toggleSenha'); 
    const inputSenha = document.querySelector('#senha');       
    const eyeIconSenha = document.querySelector('#eyeIconSenha'); 

    
    if (toggleSenha && inputSenha && eyeIconSenha) {
        toggleSenha.addEventListener('click', function () {
            const type = inputSenha.getAttribute('type') === 'password' ? 'text' : 'password';
            inputSenha.setAttribute('type', type);
            eyeIconSenha.classList.toggle('bi-eye');
            eyeIconSenha.classList.toggle('bi-eye-slash');
        });
    } else {
         console.warn("Elementos do toggle da senha principal não encontrados.");
    }


    
    const toggleConfirmarSenha = document.querySelector('#toggleConfirmarSenha');
    const inputConfirmarSenha = document.querySelector('#confirmarSenha');
    const eyeIconConfirmarSenha = document.querySelector('#eyeIconConfirmarSenha');

    
    if (toggleConfirmarSenha && inputConfirmarSenha && eyeIconConfirmarSenha) {
        toggleConfirmarSenha.addEventListener('click', function () {
            const type = inputConfirmarSenha.getAttribute('type') === 'password' ? 'text' : 'password';
            inputConfirmarSenha.setAttribute('type', type);
            eyeIconConfirmarSenha.classList.toggle('bi-eye');
            eyeIconConfirmarSenha.classList.toggle('bi-eye-slash');
        });
    } else {
        console.warn("Elementos do toggle da confirmação de senha não encontrados.");
    }

    const form = document.querySelector('.register-form');
    const erroClienteDiv = document.querySelector('#erroCliente'); 

    if (form && inputSenha && inputConfirmarSenha && erroClienteDiv) {
        form.addEventListener('submit', function (event) {
            erroClienteDiv.style.display = 'none'; 

            // Validação de senha
            if (inputSenha.value !== inputConfirmarSenha.value) {
                event.preventDefault(); 
                erroClienteDiv.innerText = 'As senhas não conferem!'; 
                erroClienteDiv.style.display = 'block'; 
                return; 
            }
        });
    } else {
        console.warn("Elementos do formulário ou div de erro não encontrados para validação.");
    }

});