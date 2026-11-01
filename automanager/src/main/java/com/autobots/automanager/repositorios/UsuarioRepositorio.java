package com.autobots.automanager.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entidades.Usuario;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
	Usuario findByVeiculosId(Long veiculoId);

	Usuario findByMercadoriasId(Long mercadoriaId);

    Usuario findByVendasId(Long vendaId);

	Usuario findByEmailsId(Long emailId);

	Usuario findByDocumentosId(Long documentoId);

	Usuario findByCredenciaisId(Long credencialId);

	Usuario findByTelefonesId(Long telefoneId);

	Usuario findByEnderecoId(Long enderecoId);
}