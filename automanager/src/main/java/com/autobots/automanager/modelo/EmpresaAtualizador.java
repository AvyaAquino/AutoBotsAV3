package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Empresa;

public class EmpresaAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();
	private EnderecoAtualizador enderecoAtualizador = new EnderecoAtualizador();
	private TelefoneAtualizador telefoneAtualizador = new TelefoneAtualizador();

	public void atualizar(Empresa empresa, Empresa atualizacao) {
		if (empresa == null || atualizacao == null) {
			return;
		}
		if (!verificador.verificar(atualizacao.getRazaoSocial())) {
			empresa.setRazaoSocial(atualizacao.getRazaoSocial());
		}
		if (!verificador.verificar(atualizacao.getNomeFantasia())) {
			empresa.setNomeFantasia(atualizacao.getNomeFantasia());
		}
		if (atualizacao.getEndereco() != null) {
			if (empresa.getEndereco() == null) {
				atualizacao.getEndereco().setId(null);
				empresa.setEndereco(atualizacao.getEndereco());
			} else {
				enderecoAtualizador.atualizar(empresa.getEndereco(), atualizacao.getEndereco());
			}
		}
		telefoneAtualizador.atualizar(empresa.getTelefones(), atualizacao.getTelefones());
	}
}