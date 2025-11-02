package br.senai.sc.communitex.service;

import br.senai.sc.communitex.model.Usuario;

import java.util.Optional;

public interface UsuarioService {
    Optional<Usuario> findByUsername(String username);
    Usuario save(Usuario usuario);
}
