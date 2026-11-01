package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;

public class CredencialAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Credencial credencial, Credencial atualizacao) {
		if (credencial == null || atualizacao == null) {
			return;
		}
		credencial.setInativo(atualizacao.isInativo());
		if (atualizacao.getUltimoAcesso() != null) {
			credencial.setUltimoAcesso(atualizacao.getUltimoAcesso());
		}
		if (credencial instanceof CredencialUsuarioSenha && atualizacao instanceof CredencialUsuarioSenha) {
			CredencialUsuarioSenha salva = (CredencialUsuarioSenha) credencial;
			CredencialUsuarioSenha nova = (CredencialUsuarioSenha) atualizacao;
			if (!verificador.verificar(nova.getNomeUsuario())) {
				salva.setNomeUsuario(nova.getNomeUsuario());
			}
			if (!verificador.verificar(nova.getSenha())) {
				salva.setSenha(nova.getSenha());
			}
		}
		if (credencial instanceof CredencialCodigoBarra && atualizacao instanceof CredencialCodigoBarra) {
			CredencialCodigoBarra salva = (CredencialCodigoBarra) credencial;
			CredencialCodigoBarra nova = (CredencialCodigoBarra) atualizacao;
			if (nova.getCodigo() > 0) {
				salva.setCodigo(nova.getCodigo());
			}
		}
	}
}