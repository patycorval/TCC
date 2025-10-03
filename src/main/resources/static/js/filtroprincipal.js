function aplicarFiltros() {
    const andarFiltro = document.getElementById('andar').value;
    const recursoFiltro = document.getElementById('recurso').value;
    const tipoSalaFiltro = document.getElementById('tiposala').value;

    // Constrói a URL com os parâmetros de filtro (query parameters)
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

    // Redireciona para a página principal com os filtros
    window.location.href = '/' + (params.toString() ? '?' + params.toString() : '');
}