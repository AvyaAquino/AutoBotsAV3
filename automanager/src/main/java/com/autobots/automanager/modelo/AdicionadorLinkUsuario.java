package com.autobots.automanager.modelo;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.entidades.Usuario;

@Component
public class AdicionadorLinkUsuario implements AdicionadorLink<Usuario> {

	@Override
	public void adicionarLink(List<Usuario> lista) {
		for (Usuario usuario : lista) {
			adicionarLinkProprio(usuario);
		}
	}

	@Override
	public void adicionarLink(Usuario objeto) {
		adicionarLinkProprio(objeto);
		Link linkColecao = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(UsuarioControle.class)
						.obterUsuarios())
				.withRel("usuarios");
		objeto.add(linkColecao);
	}

	public void adicionarLinkProprio(Usuario objeto) {
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder
						.methodOn(UsuarioControle.class)
						.obterUsuario(objeto.getId()))
				.withSelfRel();
		objeto.add(linkProprio);
	}
}