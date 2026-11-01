package com.autobots.automanager.modelo;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.CredencialControle;
import com.autobots.automanager.entidades.Credencial;

@Component
public class AdicionadorLinkCredencial implements AdicionadorLink<Credencial> {

	@Override
	public void adicionarLink(List<Credencial> lista) {
		for (Credencial credencial : lista) {
			adicionarLinkProprio(credencial);
		}
	}

	@Override
	public void adicionarLink(Credencial objeto) {
		adicionarLinkProprio(objeto);
		Link linkColecao = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(CredencialControle.class)
						.obterCredenciais())
				.withRel("credenciais");
		objeto.add(linkColecao);
	}

	public void adicionarLinkProprio(Credencial objeto) {
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(CredencialControle.class)
						.obterCredencial(objeto.getId()))
				.withSelfRel();
		objeto.add(linkProprio);
	}
}