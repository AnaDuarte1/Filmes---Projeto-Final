document.addEventListener('DOMContentLoaded', function () {
    const colecaoFilmesContainer = document.getElementById('colecao-filmes');
    const categoriasBadges = document.querySelectorAll('.category-badge');
    const paginationContainer = document.getElementById('pagination');
    const formBusca = document.querySelector('form[action="buscar-filme"]');
    
    let todosOsFilmes = [];
    let filmesFiltrados = [];
    let paginaAtual = 1;
    const itensPorPagina = 4;

    function renderizarPagina(pagina) {
        paginaAtual = pagina;
        colecaoFilmesContainer.innerHTML = '';
        const inicio = (pagina - 1) * itensPorPagina;
        const fim = inicio + itensPorPagina;
        const filmesDaPagina = filmesFiltrados.slice(inicio, fim);
        
        if (filmesDaPagina.length === 0 && pagina === 1) {
            colecaoFilmesContainer.innerHTML = '<p class="text-center col-12">Nenhum filme encontrado para esta categoria.</p>';
        }
        
        filmesDaPagina.forEach(filme => {
            const filmeCard = `
                <div class="col mb-4 filme-card">
                    <div class="card h-100">
                        <img src="${filme.imagem || 'imagem/placeholder.png'}" class="card-img-top" alt="${filme.titulo}">
                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title">${filme.titulo}</h5>
                            <p class="card-text"><small class="text-muted">${filme.diretor} • ${filme.anoLancamento}</small></p>
                            <p class="card-text text-truncate-2">${filme.sinopse}</p>
                            <div class="mt-auto d-flex justify-content-between align-items-center">
                                <span class="badge bg-secondary">${filme.formato}</span>
                                <a href="visualizar-filme.html?id=${filme.id}" class="btn btn-sm btn-custom">Detalhes</a>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            colecaoFilmesContainer.insertAdjacentHTML('beforeend', filmeCard);
        });
        
        criarPaginacao();
    }

    function criarPaginacao() {
        paginationContainer.innerHTML = '';
        const totalPaginas = Math.ceil(filmesFiltrados.length / itensPorPagina);
        if (totalPaginas <= 1) return;
        
        const prevLi = document.createElement('li');
        prevLi.className = `page-item ${paginaAtual === 1 ? 'disabled' : ''}`;
        prevLi.innerHTML = `<a class="page-link" href="#">Anterior</a>`;
        prevLi.addEventListener('click', (e) => {
            e.preventDefault();
            if (paginaAtual > 1) renderizarPagina(paginaAtual - 1);
        });
        paginationContainer.appendChild(prevLi);
        
        for (let i = 1; i <= totalPaginas; i++) {
            const pageLi = document.createElement('li');
            pageLi.className = `page-item ${i === paginaAtual ? 'active' : ''}`;
            pageLi.innerHTML = `<a class="page-link" href="#">${i}</a>`;
            pageLi.addEventListener('click', (e) => {
                e.preventDefault();
                renderizarPagina(i);
            });
            paginationContainer.appendChild(pageLi);
        }
        
        const nextLi = document.createElement('li');
        nextLi.className = `page-item ${paginaAtual === totalPaginas ? 'disabled' : ''}`;
        nextLi.innerHTML = `<a class="page-link" href="#">Próximo</a>`;
        nextLi.addEventListener('click', (e) => {
            e.preventDefault();
            if (paginaAtual < totalPaginas) renderizarPagina(paginaAtual + 1);
        });
        paginationContainer.appendChild(nextLi);
    }
    
    function filtrarEExibirFilmes(categoria) {
        const categoriaLower = categoria.toLowerCase();
        if (categoriaLower === 'todos') {
            filmesFiltrados = todosOsFilmes;
        } else {
            filmesFiltrados = todosOsFilmes.filter(f => f.categoria.toLowerCase() === categoriaLower);
        }
        renderizarPagina(1);
    }
    
    categoriasBadges.forEach(badge => {
        badge.addEventListener('click', () => {
            categoriasBadges.forEach(b => b.classList.replace('bg-primary', 'bg-secondary'));
            badge.classList.replace('bg-secondary', 'bg-primary');
            const categoriaSelecionada = badge.textContent.trim();
            filtrarEExibirFilmes(categoriaSelecionada);
        });
    });
    
    formBusca.addEventListener('submit', function(event) {
        event.preventDefault();
        const palavraChave = event.target.elements.palavraChave.value;
        if (palavraChave) {
            sessionStorage.setItem('palavraChaveBusca', palavraChave);
            window.location.href = 'resultado-busca.html';
        }
    });

    fetch('listar-filmes')
        .then(response => response.json())
        .then(data => {
            todosOsFilmes = data;
            filtrarEExibirFilmes('Todos');
        })
        .catch(error => {
            console.error('Erro ao carregar filmes:', error);
            colecaoFilmesContainer.innerHTML = '<p class="text-center col-12 text-danger">Falha ao carregar os filmes.</p>';
        });
});