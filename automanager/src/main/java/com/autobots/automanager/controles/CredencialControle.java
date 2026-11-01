package com.autobots.automanager.controles;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.AdicionadorLinkCredencial;
import com.autobots.automanager.modelo.CredencialAtualizador;
import com.autobots.automanager.repositorios.CredencialRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@RestController
@RequestMapping("/credencial")
public class CredencialControle {
	@Autowired
	private CredencialRepositorio repositorio;
	@Autowired
	private UsuarioRepositorio usuarioRepositorio;
	@Autowired
	private AdicionadorLinkCredencial adicionadorLink;

	@GetMapping("/{id}")
	public ResponseEntity<Credencial> obterCredencial(@PathVariable long id) {
		Credencial credencial = repositorio.findById(id).orElse(null);
		if (credencial == null) {
			return ResponseEntity.notFound().build();
		}
		adicionadorLink.adicionarLink(credencial);
		return ResponseEntity.ok(credencial);
	}

	@GetMapping("/credenciais")
	public ResponseEntity<List<Credencial>> obterCredenciais() {
		List<Credencial> credenciais = repositorio.findAll();
		adicionadorLink.adicionarLink(credenciais);
		return ResponseEntity.ok(credenciais);
	}

	@PostMapping("/cadastro/{usuarioId}")
	public ResponseEntity<Credencial> cadastrarCredencial(@PathVariable long usuarioId,
			@RequestBody Credencial credencial) {
		if (credencial.getId() != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		Usuario usuario = usuarioRepositorio.findById(usuarioId).orElse(null);
		if (usuario == null) {
			return ResponseEntity.notFound().build();
		}
		if (credencial.getCriacao() == null) {
			credencial.setCriacao(new Date());
		}
		try {
			repositorio.save(credencial);
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		usuario.getCredenciais().add(credencial);
		usuarioRepositorio.save(usuario);
		adicionadorLink.adicionarLink(credencial);
		return ResponseEntity.status(HttpStatus.CREATED).body(credencial);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<Credencial> atualizarCredencial(@RequestBody Credencial atualizacao) {
		if (atualizacao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Credencial credencial = repositorio.findById(atualizacao.getId()).orElse(null);
		if (credencial == null) {
			return ResponseEntity.notFound().build();
		}
		new CredencialAtualizador().atualizar(credencial, atualizacao);
		try {
			repositorio.save(credencial);
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		adicionadorLink.adicionarLink(credencial);
		return ResponseEntity.ok(credencial);
	}

	@DeleteMapping("/excluir")
	public ResponseEntity<Void> excluirCredencial(@RequestBody Credencial exclusao) {
		if (exclusao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Long id = exclusao.getId();
		Credencial credencial = repositorio.findById(id).orElse(null);
		if (credencial == null) {
			return ResponseEntity.notFound().build();
		}
		Usuario usuario = usuarioRepositorio.findByCredenciaisId(id);
		if (usuario != null) {
			usuario.getCredenciais().removeIf(c -> id.equals(c.getId()));
			usuarioRepositorio.save(usuario);
		} else {
			repositorio.delete(credencial);
		}
		return ResponseEntity.ok().build();
	}
}