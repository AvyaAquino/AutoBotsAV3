package com.autobots.automanager.modelo;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmpresaControle;
import com.autobots.automanager.entidades.Empresa;

@Component
public class AdicionadorLinkEmpresa implements AdicionadorLink<Empresa> {

	@Override
	public void adicionarLink(List<Empresa> lista) {
		for (Empresa empresa : lista) {
			adicionarLinkProprio(empresa);
		}
	}

	@Override
	public void adicionarLink(Empresa objeto) {
		adicionarLinkProprio(objeto);
		Link linkColecao = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(EmpresaControle.class)
						.obterEmpresas())
				.withRel("empresas");
		objeto.add(linkColecao);
	}

	public void adicionarLinkProprio(Empresa objeto) {
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(EmpresaControle.class)
						.obterEmpresa(objeto.getId()))
				.withSelfRel();
		objeto.add(linkProprio);
	}
}