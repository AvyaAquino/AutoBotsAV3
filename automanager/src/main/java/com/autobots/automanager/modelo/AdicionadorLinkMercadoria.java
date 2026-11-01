package com.autobots.automanager.modelo;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.MercadoriaControle;
import com.autobots.automanager.entidades.Mercadoria;

@Component
public class AdicionadorLinkMercadoria implements AdicionadorLink<Mercadoria> {

	@Override
	public void adicionarLink(List<Mercadoria> lista) {
		for (Mercadoria mercadoria : lista) {
			adicionarLinkProprio(mercadoria);
		}
	}

	@Override
	public void adicionarLink(Mercadoria objeto) {
		adicionarLinkProprio(objeto);
		Link linkColecao = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(MercadoriaControle.class)
						.obterMercadorias())
				.withRel("mercadorias");
		objeto.add(linkColecao);
	}

	public void adicionarLinkProprio(Mercadoria objeto) {
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(MercadoriaControle.class)
						.obterMercadoria(objeto.getId()))
				.withSelfRel();
		objeto.add(linkProprio);
	}
}