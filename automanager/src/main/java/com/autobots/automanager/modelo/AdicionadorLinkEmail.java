package com.autobots.automanager.modelo;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmailControle;
import com.autobots.automanager.entidades.Email;

@Component
public class AdicionadorLinkEmail implements AdicionadorLink<Email> {

	@Override
	public void adicionarLink(List<Email> lista) {
		for (Email email : lista) {
			adicionarLinkProprio(email);
		}
	}

	@Override
	public void adicionarLink(Email objeto) {
		adicionarLinkProprio(objeto);
		Link linkColecao = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(EmailControle.class)
						.obterEmails())
				.withRel("emails");
		objeto.add(linkColecao);
	}

	public void adicionarLinkProprio(Email objeto) {
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(EmailControle.class)
						.obterEmail(objeto.getId()))
				.withSelfRel();
		objeto.add(linkProprio);
	}
}