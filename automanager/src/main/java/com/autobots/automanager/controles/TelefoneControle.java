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

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.AdicionadorLinkTelefone;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.modelo.TelefoneAtualizador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.TelefoneRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@RestController
@RequestMapping("/telefone")
public class TelefoneControle {
	@Autowired
	private TelefoneRepositorio repositorio;
	@Autowired
	private UsuarioRepositorio usuarioRepositorio;
	@Autowired
	private EmpresaRepositorio empresaRepositorio;
	@Autowired
	private AdicionadorLinkTelefone adicionadorLink;

	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	@GetMapping("/{id}")
	public ResponseEntity<Telefone> obterTelefone(@PathVariable long id) {
		Telefone telefone = repositorio.findById(id).orElse(null);
		if (telefone == null) {
			return ResponseEntity.notFound().build();
		}
		adicionadorLink.adicionarLink(telefone);
		return ResponseEntity.ok(telefone);
	}

	@GetMapping("/telefones")
	public ResponseEntity<List<Telefone>> obterTelefones() {
		List<Telefone> telefones = repositorio.findAll();
		adicionadorLink.adicionarLink(telefones);
		return ResponseEntity.ok(telefones);
	}

	@PostMapping("/cadastro/usuario/{usuarioId}")
	public ResponseEntity<Telefone> cadastrarTelefoneUsuario(@PathVariable long usuarioId,
			@RequestBody Telefone telefone) {
		if (telefone.getId() != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		if (verificador.verificar(telefone.getDdd()) || verificador.verificar(telefone.getNumero())) {
			return ResponseEntity.badRequest().build();
		}
		Usuario usuario = usuarioRepositorio.findById(usuarioId).orElse(null);
		if (usuario == null) {
			return ResponseEntity.notFound().build();
		}
		repositorio.save(telefone);
		usuario.getTelefones().add(telefone);
		usuarioRepositorio.save(usuario);
		adicionadorLink.adicionarLink(telefone);
		return ResponseEntity.status(HttpStatus.CREATED).body(telefone);
	}

	@PostMapping("/cadastro/empresa/{empresaId}")
	public ResponseEntity<Telefone> cadastrarTelefoneEmpresa(@PathVariable long empresaId,
			@RequestBody Telefone telefone) {
		if (telefone.getId() != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		if (verificador.verificar(telefone.getDdd()) || verificador.verificar(telefone.getNumero())) {
			return ResponseEntity.badRequest().build();
		}
		Empresa empresa = empresaRepositorio.findById(empresaId).orElse(null);
		if (empresa == null) {
			return ResponseEntity.notFound().build();
		}
		repositorio.save(telefone);
		empresa.getTelefones().add(telefone);
		empresaRepositorio.save(empresa);
		adicionadorLink.adicionarLink(telefone);
		return ResponseEntity.status(HttpStatus.CREATED).body(telefone);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<Telefone> atualizarTelefone(@RequestBody Telefone atualizacao) {
		if (atualizacao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Telefone telefone = repositorio.findById(atualizacao.getId()).orElse(null);
		if (telefone == null) {
			return ResponseEntity.notFound().build();
		}
		new TelefoneAtualizador().atualizar(telefone, atualizacao);
		repositorio.save(telefone);
		adicionadorLink.adicionarLink(telefone);
		return ResponseEntity.ok(telefone);
	}

	@DeleteMapping("/excluir")
	public ResponseEntity<Void> excluirTelefone(@RequestBody Telefone exclusao) {
		if (exclusao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Long id = exclusao.getId();
		Telefone telefone = repositorio.findById(id).orElse(null);
		if (telefone == null) {
			return ResponseEntity.notFound().build();
		}
		Usuario usuario = usuarioRepositorio.findByTelefonesId(id);
		Empresa empresa = empresaRepositorio.findByTelefonesId(id);
		if (usuario != null) {
			usuario.getTelefones().removeIf(t -> id.equals(t.getId()));
			usuarioRepositorio.save(usuario);
		} else if (empresa != null) {
			empresa.getTelefones().removeIf(t -> id.equals(t.getId()));
			empresaRepositorio.save(empresa);
		} else {
			repositorio.delete(telefone);
		}
		return ResponseEntity.ok().build();
	}
}