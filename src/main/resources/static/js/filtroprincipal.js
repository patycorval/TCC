// Adicione este código ao final do seu arquivo filtroprincipal.js

// Esta função será executada quando o documento HTML estiver totalmente carregado
document.addEventListener('DOMContentLoaded', function() {
    // Pega os parâmetros da URL atual
    const params = new URLSearchParams(window.location.search);
    const andarFiltro = params.get('andar'); // Pega o valor do parâmetro 'andar'

    // Pega os elementos do HTML pelos IDs que adicionamos
    const tituloAndar3 = document.getElementById('andar3-titulo');
    const secaoAndar3 = document.getElementById('andar3-section');
    const tituloAndar5 = document.getElementById('andar5-titulo');
    const secaoAndar5 = document.getElementById('andar5-section');

    if (andarFiltro) {
        if (andarFiltro.includes('3')) {
            tituloAndar5.style.display = 'none';
            secaoAndar5.style.display = 'none';
        } 
        else if (andarFiltro.includes('5')) {
            tituloAndar3.style.display = 'none';
            secaoAndar3.style.display = 'none';
        }
    }
});

function aplicarFiltros() {
    const andarFiltro = document.getElementById('andar').value;
    const recursoFiltro = document.getElementById('recurso').value;
    const tipoSalaFiltro = document.getElementById('tiposala').value;

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

    window.location.href = '/' + (params.toString() ? '?' + params.toString() : '');
}