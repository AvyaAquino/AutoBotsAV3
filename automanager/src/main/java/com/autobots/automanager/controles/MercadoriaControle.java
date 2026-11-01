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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.modelo.AdicionadorLinkMercadoria;
import com.autobots.automanager.modelo.MercadoriaAtualizador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.MercadoriaRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@RestController
@RequestMapping("/mercadoria")
public class MercadoriaControle {
	@Autowired
	private MercadoriaRepositorio repositorio;
	@Autowired
	private EmpresaRepositorio empresaRepositorio;
	@Autowired
	private UsuarioRepositorio usuarioRepositorio;
	@Autowired
	private VendaRepositorio vendaRepositorio;
	@Autowired
	private AdicionadorLinkMercadoria adicionadorLink;

	@GetMapping("/{id}")
	public ResponseEntity<Mercadoria> obterMercadoria(@PathVariable long id) {
		Mercadoria mercadoria = repositorio.findById(id).orElse(null);
		if (mercadoria == null) {
			return ResponseEntity.notFound().build();
		}
		adicionadorLink.adicionarLink(mercadoria);
		return ResponseEntity.ok(mercadoria);
	}

	@GetMapping("/mercadorias")
	public ResponseEntity<List<Mercadoria>> obterMercadorias() {
		List<Mercadoria> mercadorias = repositorio.findAll();
		adicionadorLink.adicionarLink(mercadorias);
		return ResponseEntity.ok(mercadorias);
	}

	@PostMapping("/cadastro/{empresaId}")
	public ResponseEntity<Mercadoria> cadastrarMercadoria(@PathVariable long empresaId,
			@RequestParam(required = false) Long fornecedorId, @RequestBody Mercadoria mercadoria) {
		if (mercadoria.getId() != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		Empresa empresa = empresaRepositorio.findById(empresaId).orElse(null);
		if (empresa == null) {
			return ResponseEntity.notFound().build();
		}
		Usuario fornecedor = null;
		if (fornecedorId != null) {
			fornecedor = usuarioRepositorio.findById(fornecedorId).orElse(null);
			if (fornecedor == null) {
				return ResponseEntity.notFound().build();
			}
			if (!fornecedor.getPerfis().contains(PerfilUsuario.FORNECEDOR)) {
				return ResponseEntity.badRequest().build();
			}
		}
		if (mercadoria.getCadastro() == null) {
			mercadoria.setCadastro(new Date());
		}
		try {
			repositorio.save(mercadoria);
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.badRequest().build();
		}
		empresa.getMercadorias().add(mercadoria);
		empresaRepositorio.save(empresa);
		if (fornecedor != null) {
			fornecedor.getMercadorias().add(mercadoria);
			usuarioRepositorio.save(fornecedor);
		}
		adicionadorLink.adicionarLink(mercadoria);
		return ResponseEntity.status(HttpStatus.CREATED).body(mercadoria);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<Mercadoria> atualizarMercadoria(@RequestBody Mercadoria atualizacao) {
		if (atualizacao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Mercadoria mercadoria = repositorio.findById(atualizacao.getId()).orElse(null);
		if (mercadoria == null) {
			return ResponseEntity.notFound().build();
		}
		new MercadoriaAtualizador().atualizar(mercadoria, atualizacao);
		repositorio.save(mercadoria);
		adicionadorLink.adicionarLink(mercadoria);
		return ResponseEntity.ok(mercadoria);
	}

	@DeleteMapping("/excluir")
	public ResponseEntity<Void> excluirMercadoria(@RequestBody Mercadoria exclusao) {
		if (exclusao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Long id = exclusao.getId();
		Mercadoria mercadoria = repositorio.findById(id).orElse(null);
		if (mercadoria == null) {
			return ResponseEntity.notFound().build();
		}
		if (vendaRepositorio.existsByMercadoriasId(id)) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		Empresa empresa = empresaRepositorio.findByMercadoriasId(id);
		if (empresa != null) {
			empresa.getMercadorias().removeIf(m -> id.equals(m.getId()));
			empresaRepositorio.save(empresa);
		}
		Usuario fornecedor = usuarioRepositorio.findByMercadoriasId(id);
		if (fornecedor != null) {
			fornecedor.getMercadorias().removeIf(m -> id.equals(m.getId()));
			usuarioRepositorio.save(fornecedor);
		}
		repositorio.delete(mercadoria);
		return ResponseEntity.ok().build();
	}
}