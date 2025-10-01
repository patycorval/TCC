 // Script para o ícone de olho do PRIMEIRO campo de senha
        const toggleSenha = document.querySelector('#toggleSenha');
        const inputSenha = document.querySelector('#senha');
        const eyeIconSenha = document.querySelector('#eyeIconSenha');

        toggleSenha.addEventListener('click', function () {
            const type = inputSenha.getAttribute('type') === 'password' ? 'text' : 'password';
            inputSenha.setAttribute('type', type);
            eyeIconSenha.classList.toggle('bi-eye');
            eyeIconSenha.classList.toggle('bi-eye-slash');
        });

        // Script para o ícone de olho do SEGUNDO campo de senha (Confirmar Senha)
        const toggleConfirmarSenha = document.querySelector('#toggleConfirmarSenha');
        const inputConfirmarSenha = document.querySelector('#confirmarSenha');
        const eyeIconConfirmarSenha = document.querySelector('#eyeIconConfirmarSenha');

        toggleConfirmarSenha.addEventListener('click', function () {
            const type = inputConfirmarSenha.getAttribute('type') === 'password' ? 'text' : 'password';
            inputConfirmarSenha.setAttribute('type', type);
            eyeIconConfirmarSenha.classList.toggle('bi-eye');
            eyeIconConfirmarSenha.classList.toggle('bi-eye-slash');
        });

        // Script para VALIDAR o formulário no lado do cliente (antes de enviar)
        const form = document.querySelector('.register-form');
        const erroClienteDiv = document.querySelector('#erroCliente');

        form.addEventListener('submit', function (event) {
            erroClienteDiv.style.display = 'none';

            if (inputSenha.value !== inputConfirmarSenha.value) {
                event.preventDefault(); 
                erroClienteDiv.innerText = 'As senhas não conferem!';
                erroClienteDiv.style.display = 'block'; 
                return; 
            }
        });