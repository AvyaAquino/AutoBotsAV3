package com.autobots.automanager.modelo;

import java.util.Collection;

import com.autobots.automanager.entidades.Telefone;

public class TelefoneAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Telefone telefone, Telefone atualizacao) {
		if (telefone == null || atualizacao == null) {
			return;
		}
		if (!verificador.verificar(atualizacao.getDdd())) {
			telefone.setDdd(atualizacao.getDdd());
		}
		if (!verificador.verificar(atualizacao.getNumero())) {
			telefone.setNumero(atualizacao.getNumero());
		}
	}

	public void atualizar(Collection<Telefone> telefones, Collection<Telefone> atualizacoes) {
		if (telefones == null || atualizacoes == null) {
			return;
		}
		for (Telefone atualizacao : atualizacoes) {
			if (atualizacao.getId() == null) {
				continue;
			}
			for (Telefone telefone : telefones) {
				if (atualizacao.getId().equals(telefone.getId())) {
					atualizar(telefone, atualizacao);
				}
			}
		}
	}
}