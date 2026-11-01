package com.autobots.automanager.modelo;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.VendaControle;
import com.autobots.automanager.entidades.Venda;

@Component
public class AdicionadorLinkVenda implements AdicionadorLink<Venda> {

	@Override
	public void adicionarLink(List<Venda> lista) {
		for (Venda venda : lista) {
			adicionarLinkProprio(venda);
		}
	}

	@Override
	public void adicionarLink(Venda objeto) {
		adicionarLinkProprio(objeto);
		Link linkColecao = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(VendaControle.class)
						.obterVendas())
				.withRel("vendas");
		objeto.add(linkColecao);
	}

	public void adicionarLinkProprio(Venda objeto) {
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(VendaControle.class)
						.obterVenda(objeto.getId()))
				.withSelfRel();
		objeto.add(linkProprio);
	}
}