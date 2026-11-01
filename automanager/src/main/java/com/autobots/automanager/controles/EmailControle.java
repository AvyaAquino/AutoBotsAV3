package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.AdicionadorLinkEmail;
import com.autobots.automanager.modelo.EmailAtualizador;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.repositorios.EmailRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@RestController
@RequestMapping("/email")
public class EmailControle {
	@Autowired
	private EmailRepositorio repositorio;
	@Autowired
	private UsuarioRepositorio usuarioRepositorio;
	@Autowired
	private AdicionadorLinkEmail adicionadorLink;

	@GetMapping("/{id}")
	public ResponseEntity<Email> obterEmail(@PathVariable long id) {
		Email email = repositorio.findById(id).orElse(null);
		if (email == null) {
			return ResponseEntity.notFound().build();
		}
		adicionadorLink.adicionarLink(email);
		return ResponseEntity.ok(email);
	}

	@GetMapping("/emails")
	public ResponseEntity<List<Email>> obterEmails() {
		List<Email> emails = repositorio.findAll();
		adicionadorLink.adicionarLink(emails);
		return ResponseEntity.ok(emails);
	}

	@PostMapping("/cadastro/{usuarioId}")
	public ResponseEntity<Email> cadastrarEmail(@PathVariable long usuarioId, @RequestBody Email email) {
		if (email.getId() != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		if (new StringVerificadorNulo().verificar(email.getEndereco())) {
			return ResponseEntity.badRequest().build();
		}
		Usuario usuario = usuarioRepositorio.findById(usuarioId).orElse(null);
		if (usuario == null) {
			return ResponseEntity.notFound().build();
		}
		repositorio.save(email);
		usuario.getEmails().add(email);
		usuarioRepositorio.save(usuario);
		adicionadorLink.adicionarLink(email);
		return ResponseEntity.status(HttpStatus.CREATED).body(email);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<Email> atualizarEmail(@RequestBody Email atualizacao) {
		if (atualizacao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Email email = repositorio.findById(atualizacao.getId()).orElse(null);
		if (email == null) {
			return ResponseEntity.notFound().build();
		}
		new EmailAtualizador().atualizar(email, atualizacao);
		repositorio.save(email);
		adicionadorLink.adicionarLink(email);
		return ResponseEntity.ok(email);
	}

	@DeleteMapping("/excluir")
	public ResponseEntity<Void> excluirEmail(@RequestBody Email exclusao) {
		if (exclusao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Long id = exclusao.getId();
		Email email = repositorio.findById(id).orElse(null);
		if (email == null) {
			return ResponseEntity.notFound().build();
		}
		Usuario usuario = usuarioRepositorio.findByEmailsId(id);
		if (usuario != null) {
			usuario.getEmails().removeIf(e -> id.equals(e.getId()));
			usuarioRepositorio.save(usuario);
		} else {
			repositorio.delete(email);
		}
		return ResponseEntity.ok().build();
	}
}