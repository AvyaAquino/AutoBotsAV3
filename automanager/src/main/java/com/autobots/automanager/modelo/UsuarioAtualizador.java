package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Usuario;

public class UsuarioAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();
	private EnderecoAtualizador enderecoAtualizador = new EnderecoAtualizador();
	private TelefoneAtualizador telefoneAtualizador = new TelefoneAtualizador();
	private DocumentoAtualizador documentoAtualizador = new DocumentoAtualizador();
	private EmailAtualizador emailAtualizador = new EmailAtualizador();

	public void atualizar(Usuario usuario, Usuario atualizacao) {
		if (usuario == null || atualizacao == null) {
			return;
		}
		if (!verificador.verificar(atualizacao.getNome())) {
			usuario.setNome(atualizacao.getNome());
		}
		if (!verificador.verificar(atualizacao.getNomeSocial())) {
			usuario.setNomeSocial(atualizacao.getNomeSocial());
		}
		if (atualizacao.getPerfis() != null && !atualizacao.getPerfis().isEmpty()) {
			usuario.getPerfis().clear();
			usuario.getPerfis().addAll(atualizacao.getPerfis());
		}
		if (atualizacao.getEndereco() != null) {
			if (usuario.getEndereco() == null) {
				atualizacao.getEndereco().setId(null);
				usuario.setEndereco(atualizacao.getEndereco());
			} else {
				enderecoAtualizador.atualizar(usuario.getEndereco(), atualizacao.getEndereco());
			}
		}
		telefoneAtualizador.atualizar(usuario.getTelefones(), atualizacao.getTelefones());
		documentoAtualizador.atualizar(usuario.getDocumentos(), atualizacao.getDocumentos());
		emailAtualizador.atualizar(usuario.getEmails(), atualizacao.getEmails());
	}
}