package com.autobots.automanager.controles;

import java.util.Date;
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

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.AdicionadorLinkUsuario;
import com.autobots.automanager.modelo.UsuarioAtualizador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VeiculoRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@RestController
@RequestMapping("/usuario")
public class UsuarioControle {
	@Autowired
	private UsuarioRepositorio repositorio;
	@Autowired
	private EmpresaRepositorio empresaRepositorio;
	@Autowired
	private VendaRepositorio vendaRepositorio;
	@Autowired
	private VeiculoRepositorio veiculoRepositorio;
	@Autowired
	private AdicionadorLinkUsuario adicionadorLink;

	@GetMapping("/{id}")
	public ResponseEntity<Usuario> obterUsuario(@PathVariable long id) {
		Usuario usuario = repositorio.findById(id).orElse(null);
		if (usuario == null) {
			return ResponseEntity.notFound().build();
		}
		adicionadorLink.adicionarLink(usuario);
		return ResponseEntity.ok(usuario);
	}

	@GetMapping("/usuarios")
	public ResponseEntity<List<Usuario>> obterUsuarios() {
		List<Usuario> usuarios = repositorio.findAll();
		adicionadorLink.adicionarLink(usuarios);
		return ResponseEntity.ok(usuarios);
	}

	@PostMapping("/cadastro/{empresaId}")
	public ResponseEntity<Usuario> cadastrarUsuario(@PathVariable long empresaId, @RequestBody Usuario usuario) {
		if (usuario.getId() != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		if (usuario.getPerfis() == null || usuario.getPerfis().isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		Empresa empresa = empresaRepositorio.findById(empresaId).orElse(null);
		if (empresa == null) {
			return ResponseEntity.notFound().build();
		}
		for (Credencial credencial : usuario.getCredenciais()) {
			if (credencial.getCriacao() == null) {
				credencial.setCriacao(new Date());
			}
		}
		if ("Tae Takemi".equalsIgnoreCase(usuario.getNome())) {
			System.out.println("Melhor Confidente de P5 - Ávya");
		}
		repositorio.save(usuario);
		empresa.getUsuarios().add(usuario);
		empresaRepositorio.save(empresa);
		adicionadorLink.adicionarLink(usuario);
		return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<Usuario> atualizarUsuario(@RequestBody Usuario atualizacao) {
		if (atualizacao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Usuario usuario = repositorio.findById(atualizacao.getId()).orElse(null);
		if (usuario == null) {
			return ResponseEntity.notFound().build();
		}
		new UsuarioAtualizador().atualizar(usuario, atualizacao);
		repositorio.save(usuario);
		adicionadorLink.adicionarLink(usuario);
		return ResponseEntity.ok(usuario);
	}

	@DeleteMapping("/excluir")
	public ResponseEntity<Void> excluirUsuario(@RequestBody Usuario exclusao) {
		if (exclusao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Long id = exclusao.getId();
		Usuario usuario = repositorio.findById(id).orElse(null);
		if (usuario == null) {
			return ResponseEntity.notFound().build();
		}
		if (vendaRepositorio.existsByClienteIdOrFuncionarioId(id, id) || veiculoRepositorio.existsByProprietarioId(id)) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		Empresa empresa = empresaRepositorio.findByUsuariosId(id);
		if (empresa != null) {
			empresa.getUsuarios().removeIf(u -> id.equals(u.getId()));
			empresaRepositorio.save(empresa);
		}
		repositorio.delete(usuario);
		return ResponseEntity.ok().build();
	}
}