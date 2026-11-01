package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Mercadoria;

public class MercadoriaAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Mercadoria mercadoria, Mercadoria atualizacao) {
		if (mercadoria == null || atualizacao == null) {
			return;
		}
		if (!verificador.verificar(atualizacao.getNome())) {
			mercadoria.setNome(atualizacao.getNome());
		}
		if (!verificador.verificar(atualizacao.getDescricao())) {
			mercadoria.setDescricao(atualizacao.getDescricao());
		}
		if (atualizacao.getValidade() != null) {
			mercadoria.setValidade(atualizacao.getValidade());
		}
		if (atualizacao.getFabricao() != null) {
			mercadoria.setFabricao(atualizacao.getFabricao());
		}
		if (atualizacao.getQuantidade() > 0) {
			mercadoria.setQuantidade(atualizacao.getQuantidade());
		}
		if (atualizacao.getValor() > 0) {
			mercadoria.setValor(atualizacao.getValor());
		}
	}
}