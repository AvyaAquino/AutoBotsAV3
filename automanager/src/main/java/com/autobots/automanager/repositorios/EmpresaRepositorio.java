package com.autobots.automanager.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entidades.Empresa;

public interface EmpresaRepositorio extends JpaRepository<Empresa, Long> {
	Empresa findByUsuariosId(Long usuarioId);

	Empresa findByMercadoriasId(Long mercadoriaId);

	Empresa findByServicosId(Long servicoId);

    Empresa findByVendasId(Long vendaId);

	Empresa findByTelefonesId(Long telefoneId);

	Empresa findByEnderecoId(Long enderecoId);
}