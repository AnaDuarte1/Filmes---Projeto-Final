document.addEventListener('DOMContentLoaded', () => {
    carregarComponente('header-placeholder', 'header.html', inicializarPagina);
    carregarComponente('footer-placeholder', 'footer.html');
});

function carregarComponente(idPlaceholder, urlComponente, callback) {
    const placeholder = document.getElementById(idPlaceholder);
    if (placeholder) {
        fetch(urlComponente)
            .then(response => {
                if (response.ok) {
                    return response.text();
                } else {
                    return Promise.reject(`Falha ao carregar ${urlComponente} (Status: ${response.status})`);
                }
            })
            .then(data => {
                placeholder.innerHTML = data;
                if (callback) {
                    callback();
                }
            })
            .catch(error => {
                placeholder.innerHTML = `<div class="alert alert-warning">Não foi possível carregar o componente: ${urlComponente}. Erro: ${error}</div>`;
                console.error(error);
            });
    }
}

function inicializarPagina() {
    fetch('/filmes/session-status') 
        .then(response => {
           
            const contentType = response.headers.get("content-type");
            if (response.ok && contentType && contentType.includes("application/json")) {
                return response.json();
            } else if (response.ok) {
               
                return { logado: false, tipo: 'visitante' };
            } else {
               
                return response.text().then(text => {
                    console.error("Erro na resposta do session-status (não JSON):", text);
                    throw new Error(`Serviço de sessão indisponível ou resposta inválida. Status: ${response.status}`);
                });
            }
        })
        .then(usuarioLogado => {
            const usuario = usuarioLogado && usuarioLogado.logado !== false ? { ...usuarioLogado, logado: true } : { logado: false, tipo: 'visitante' };
            atualizarHeader(usuario);
            if (document.getElementById('detalhes-filme-container')) {
                carregarDetalhesFilme(usuario);
            }
        })
        .catch(error => {
            console.error("Erro ao buscar status da sessão:", error);
            atualizarHeader({ logado: false, tipo: 'visitante' });
            if (document.getElementById('detalhes-filme-container')) {
                carregarDetalhesFilme({ logado: false, tipo: 'visitante' });
            }
        });
}

function atualizarHeader(usuario) {
    const adminLink = document.getElementById('admin-link-add-filme');
    const userNavLinks = document.getElementById('user-nav-links');

    if (!adminLink || !userNavLinks) {
        return;
    }

    if (usuario.logado && usuario.tipo === 'admin') {
        adminLink.style.display = 'block';
        userNavLinks.innerHTML = `
            <li class="nav-item dropdown">
                <a class="nav-link dropdown-toggle" href="#" id="navbarUserDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                    <i class="fas fa-user me-1"></i> ${usuario.nome} (Admin)
                </a>
                <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="navbarUserDropdown">
                    <li><a class="dropdown-item" href="#" onclick="logout()">Sair</a></li>
                </ul>
            </li>
        `;
    } else if (usuario.logado && (usuario.tipo === 'avaliador' || usuario.tipo === 'comum')) {
        adminLink.style.display = 'none';
        userNavLinks.innerHTML = `
            <li class="nav-item dropdown">
                <a class="nav-link dropdown-toggle" href="#" id="navbarUserDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                    <i class="fas fa-user me-1"></i> ${usuario.nome}
                </a>
                <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="navbarUserDropdown">
                    <li><a class="dropdown-item" href="#" onclick="logout()">Sair</a></li>
                </ul>
            </li>
        `;
    } else {
        adminLink.style.display = 'none';
        userNavLinks.innerHTML = `
            <li class="nav-item">
                <a class="nav-link" href="login.html">Login</a>
            </li>
        `;
    }
}

function logout() {
    fetch('/filmes/logout', { method: 'POST' })
        .then(response => {
            if (response.ok) {
                window.location.href = 'home.html'; 
            } else {
                console.error('Falha ao fazer logout.'); 
            }
        })
        .catch(error => {
            console.error('Erro na requisição de logout:', error); 
        });
}

function carregarDetalhesFilme(usuario) {
    const container = document.getElementById('detalhes-filme-container');
    const urlParams = new URLSearchParams(window.location.search);
    const filmeId = urlParams.get('id');

    if (!filmeId) {
        container.innerHTML = `<div class="alert alert-danger">Erro: ID do filme não fornecido na URL.</div>`;
        return;
    }

    fetch(`/filmes/visualizar-filme?id=${filmeId}`)
        .then(response => {
            const contentType = response.headers.get("content-type");
            if (!response.ok || !(contentType && contentType.includes("application/json"))) {
                return response.text().then(text => {
                    console.error("Erro na resposta do visualizar-filme (não JSON ou não OK):", text);
                    throw new Error(`Filme não encontrado ou erro no servidor (Status: ${response.status}).`);
                });
            }
            return response.json();
        })
        .then(filme => {
            if (!filme || Object.keys(filme).length === 0) {
                throw new Error("Dados do filme retornados vazios.");
            }
            renderizarHtmlFilme(container, filme, usuario);
            carregarComentariosDoFilme(filme.id, usuario); 
        })
        .catch(error => {
            container.innerHTML = `<div class="alert alert-danger text-center"><h4>Ops! Não foi possível carregar o filme.</h4><p>${error.message}</p></div>`;
            console.error("Erro ao buscar detalhes do filme:", error); 
        });
}

function renderizarHtmlFilme(container, filme, usuario) {
    let adminButtons = '';
    if (usuario.logado && usuario.tipo === 'admin') {
        adminButtons = `
            <a href="editar-filme.html?id=${filme.id}" class="btn btn-warning"><i class="fas fa-edit me-1"></i> Editar</a>
            <button onclick="excluirFilme(${filme.id}, '${filme.titulo.replace(/'/g, "\\'")}')" class="btn btn-danger"><i class="fas fa-trash me-1"></i> Excluir</button>
        `;
    }

    container.innerHTML = `
        <div class="card shadow-lg border-0 rounded-4">
            <div class="row g-0">
                <div class="col-md-4 d-flex align-items-center justify-content-center p-4">
                    <img src="${filme.imagem || 'https://placehold.co/300x450/6c757d/ffffff?text=Capa'}" alt="Capa de ${filme.titulo}" class="img-fluid rounded-3 shadow-sm">
                </div>
                <div class="col-md-8">
                    <div class="card-body p-4">
                        <h2 class="card-title text-primary fw-bold mb-3">${filme.titulo}</h2>
                        <div class="mb-4">
                            <dl class="row">
                                <dt class="col-sm-4 fw-semibold">🎬 Diretor:</dt>
                                <dd class="col-sm-8">${filme.diretor || 'Não informado'}</dd>
                                <dt class="col-sm-4 fw-semibold">📅 Lançamento:</dt>
                                <dd class="col-sm-8">${filme.anoLancamento || 'Não informado'}</dd>
                                <dt class="col-sm-4 fw-semibold">🗣️ Idioma:</dt>
                                <dd class="col-sm-8">${filme.idioma || 'Não informado'}</dd>
                                <dt class="col-sm-4 fw-semibold">⏱️ Duração:</dt>
                                <dd class="col-sm-8">${filme.duracao ? filme.duracao + ' min' : 'Não informado'}</dd>
                                <dt class="col-sm-4 fw-semibold">🏷️ Categoria:</dt>
                                <dd class="col-sm-8">${filme.categoria || 'Não informado'}</dd>
                            </dl>
                        </div>
                        <div>
                            <h5 class="text-secondary fw-bold">📝 Sinopse</h5>
                            <p class="text-justify">${filme.sinopse || 'Sinopse não disponível.'}</p>
                        </div>
                        <div class="mt-4 d-flex justify-content-end gap-2">
                            ${adminButtons}
                            <a href="listar-filmes.html" class="btn btn-outline-primary">⬅️ Voltar ao Catálogo</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>`;
}

function excluirFilme(id, titulo) {
    const tituloEscapado = titulo.replace(/'/g, "\\'"); 
    console.log(`Tentando excluir o filme: "${tituloEscapado}" (ID: ${id})`); 
    
    fetch('/filmes/excluir-filme', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: `id=${id}`
    })
    .then(response => {
        const contentType = response.headers.get("content-type");
        if (!response.ok || !(contentType && contentType.includes("application/json"))) {
            return response.text().then(text => {
                console.error("Erro na resposta de exclusão (não JSON ou não OK):", text); 
                throw new Error(`Erro ao excluir filme (Status: ${response.status}).`);
            });
        }
        return response.json();
    })
    .then(data => {
        if (data.status === 'sucesso') {
            console.log(data.mensagem);
            window.location.href = 'listar-filmes.html'; 
        } else {
            console.error(`Erro ao excluir: ${data.mensagem}`); 
        }
    })
    .catch(error => {
        console.error('Erro na requisição de exclusão:', error); 
    });
}

function carregarComentariosDoFilme(filmeId, usuario) {
    const comentariosSection = document.getElementById('comentarios-section');
    comentariosSection.innerHTML = `<h3>Comentários</h3><hr>`;

    if (usuario.logado && usuario.tipo === 'avaliador') {
        comentariosSection.innerHTML += `
            <div class="card mb-4 p-3 shadow-sm">
                <h5>Deixe seu Comentário</h5>
                <form id="form-comentario">
                    <input type="hidden" id="filmeIdComentario" name="filmeId" value="${filmeId}">
                    <div class="mb-3">
                        <label for="textoComentario" class="form-label">Seu Comentário</label>
                        <textarea class="form-control" id="textoComentario" name="texto" rows="3" required></textarea>
                    </div>
                    <div class="mb-3">
                        <label for="notaComentario" class="form-label">Sua Nota (1-5)</label>
                        <input type="number" class="form-control" id="notaComentario" name="nota" min="1" max="5" required>
                    </div>
                    <button type="submit" class="btn btn-primary">Enviar Comentário</button>
                </form>
                <div id="feedback-comentario" class="mt-3"></div>
            </div>
        `;

        document.getElementById('form-comentario').addEventListener('submit', function(event) {
            event.preventDefault();
            const form = event.target;
            const formData = new FormData(form);
            const feedbackDiv = document.getElementById('feedback-comentario');
            feedbackDiv.innerHTML = ''; 

            
            fetch('/filmes/adicionar-comentario', {
                method: 'POST',
                body: new URLSearchParams(formData)
            })
            .then(response => {
                const contentType = response.headers.get("content-type");
                if (!response.ok || !(contentType && contentType.includes("application/json"))) {
                    return response.text().then(text => {
                        console.error("Erro na resposta de adicionar comentário (não JSON ou não OK):", text); // Log para depuração
                        throw new Error(`Erro ao adicionar comentário (Status: ${response.status}).`);
                    });
                }
                return response.json();
            })
            .then(data => {
                if (data.status === 'sucesso') {
                    feedbackDiv.innerHTML = `<div class="alert alert-success">${data.mensagem}</div>`; // Mensagem na div
                    form.reset();
                    carregarComentariosDoFilme(filmeId, usuario); // Recarrega comentários
                } else {
                    feedbackDiv.innerHTML = `<div class="alert alert-danger">${data.mensagem}</div>`; // Mensagem na div
                }
            })
            .catch(error => {
                console.error('Erro na requisição de comentário:', error); // Log para depuração
                feedbackDiv.innerHTML = `<div class="alert alert-danger">Ocorreu um erro na comunicação com o servidor ao adicionar comentário.</div>`; // Mensagem na div
            });
        });
    } else if (usuario.logado) {
        comentariosSection.innerHTML += `<p class="text-muted">Faça login como um "avaliador" para deixar seu comentário.</p>`;
    } else {
        comentariosSection.innerHTML += `<p class="text-muted">Faça <a href="login.html">login</a> para deixar seu comentário.</p>`;
    }

   
    fetch(`/filmes/listar-comentarios?filmeId=${filmeId}`) 
        .then(response => {
            const contentType = response.headers.get("content-type");
            if (!response.ok || !(contentType && contentType.includes("application/json"))) {
                return response.text().then(text => {
                    console.error("Erro na resposta de listar comentários (não JSON ou não OK):", text); // Log para depuração
                    throw new Error(`Erro ao carregar comentários (Status: ${response.status}).`);
                });
            }
            return response.json();
        })
        .then(comentarios => {
            const listaComentariosDiv = document.createElement('div');
            listaComentariosDiv.className = 'mt-4';
            if (comentarios && comentarios.length > 0) {
                comentarios.forEach(comentario => {
                    
                    const autorNome = comentario.autor ? comentario.autor.nome : 'Anônimo';
                    listaComentariosDiv.innerHTML += `
                        <div class="card mb-2 p-3 shadow-sm">
                            <p class="mb-1"><strong>${autorNome}</strong> - Nota: ${comentario.nota}/5</p>
                            <p class="mb-0">${comentario.texto}</p>
                        </div>
                    `;
                });
            } else {
                listaComentariosDiv.innerHTML = '<p class="text-muted">Nenhum comentário ainda. Seja o primeiro a comentar!</p>';
            }
            comentariosSection.appendChild(listaComentariosDiv);
        })
        .catch(error => {
            console.error('Erro ao carregar comentários:', error); // Log para depuração
            comentariosSection.innerHTML += `<div class="alert alert-warning mt-3">Não foi possível carregar os comentários: ${error.message}</div>`; // Mensagem na div
        });
}