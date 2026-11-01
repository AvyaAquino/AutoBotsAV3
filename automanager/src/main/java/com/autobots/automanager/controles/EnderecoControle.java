package com.autobots.automanager.controles;

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

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.AdicionadorLinkEndereco;
import com.autobots.automanager.modelo.EnderecoAtualizador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.EnderecoRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@RestController
@RequestMapping("/endereco")
public class EnderecoControle {
	@Autowired
	private EnderecoRepositorio repositorio;
	@Autowired
	private UsuarioRepositorio usuarioRepositorio;
	@Autowired
	private EmpresaRepositorio empresaRepositorio;
	@Autowired
	private AdicionadorLinkEndereco adicionadorLink;

	@GetMapping("/{id}")
	public ResponseEntity<Endereco> obterEndereco(@PathVariable long id) {
		Endereco endereco = repositorio.findById(id).orElse(null);
		if (endereco == null) {
			return ResponseEntity.notFound().build();
		}
		adicionadorLink.adicionarLink(endereco);
		return ResponseEntity.ok(endereco);
	}

	@GetMapping("/enderecos")
	public ResponseEntity<List<Endereco>> obterEnderecos() {
		List<Endereco> enderecos = repositorio.findAll();
		adicionadorLink.adicionarLink(enderecos);
		return ResponseEntity.ok(enderecos);
	}

	@PostMapping("/cadastro/usuario/{usuarioId}")
	public ResponseEntity<Endereco> cadastrarEnderecoUsuario(@PathVariable long usuarioId,
			@RequestBody Endereco endereco) {
		if (endereco.getId() != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		Usuario usuario = usuarioRepositorio.findById(usuarioId).orElse(null);
		if (usuario == null) {
			return ResponseEntity.notFound().build();
		}
		if (usuario.getEndereco() != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		try {
			repositorio.save(endereco);
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.badRequest().build();
		}
		usuario.setEndereco(endereco);
		usuarioRepositorio.save(usuario);
		adicionadorLink.adicionarLink(endereco);
		return ResponseEntity.status(HttpStatus.CREATED).body(endereco);
	}

	@PostMapping("/cadastro/empresa/{empresaId}")
	public ResponseEntity<Endereco> cadastrarEnderecoEmpresa(@PathVariable long empresaId,
			@RequestBody Endereco endereco) {
		if (endereco.getId() != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		Empresa empresa = empresaRepositorio.findById(empresaId).orElse(null);
		if (empresa == null) {
			return ResponseEntity.notFound().build();
		}
		if (empresa.getEndereco() != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		try {
			repositorio.save(endereco);
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.badRequest().build();
		}
		empresa.setEndereco(endereco);
		empresaRepositorio.save(empresa);
		adicionadorLink.adicionarLink(endereco);
		return ResponseEntity.status(HttpStatus.CREATED).body(endereco);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<Endereco> atualizarEndereco(@RequestBody Endereco atualizacao) {
		if (atualizacao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Endereco endereco = repositorio.findById(atualizacao.getId()).orElse(null);
		if (endereco == null) {
			return ResponseEntity.notFound().build();
		}
		new EnderecoAtualizador().atualizar(endereco, atualizacao);
		repositorio.save(endereco);
		adicionadorLink.adicionarLink(endereco);
		return ResponseEntity.ok(endereco);
	}

	@DeleteMapping("/excluir")
	public ResponseEntity<Void> excluirEndereco(@RequestBody Endereco exclusao) {
		if (exclusao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Long id = exclusao.getId();
		Endereco endereco = repositorio.findById(id).orElse(null);
		if (endereco == null) {
			return ResponseEntity.notFound().build();
		}
		Usuario usuario = usuarioRepositorio.findByEnderecoId(id);
		Empresa empresa = empresaRepositorio.findByEnderecoId(id);
		if (usuario != null) {
			usuario.setEndereco(null);
			usuarioRepositorio.save(usuario);
		} else if (empresa != null) {
			empresa.setEndereco(null);
			empresaRepositorio.save(empresa);
		} else {
			repositorio.delete(endereco);
		}
		return ResponseEntity.ok().build();
	}
}