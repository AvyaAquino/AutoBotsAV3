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

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.modelo.AdicionadorLinkEmpresa;
import com.autobots.automanager.modelo.EmpresaAtualizador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;

@RestController
@RequestMapping("/empresa")
public class EmpresaControle {
	@Autowired
	private EmpresaRepositorio repositorio;
	@Autowired
	private AdicionadorLinkEmpresa adicionadorLink;

	@GetMapping("/{id}")
	public ResponseEntity<Empresa> obterEmpresa(@PathVariable long id) {
		Empresa empresa = repositorio.findById(id).orElse(null);
		if (empresa == null) {
			return ResponseEntity.notFound().build();
		}
		adicionadorLink.adicionarLink(empresa);
		return ResponseEntity.ok(empresa);
	}

	@GetMapping("/empresas")
	public ResponseEntity<List<Empresa>> obterEmpresas() {
		List<Empresa> empresas = repositorio.findAll();
		adicionadorLink.adicionarLink(empresas);
		return ResponseEntity.ok(empresas);
	}

	@PostMapping("/cadastro")
	public ResponseEntity<Empresa> cadastrarEmpresa(@RequestBody Empresa empresa) {
		if (empresa.getId() != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		if (empresa.getCadastro() == null) {
			empresa.setCadastro(new Date());
		}
		Empresa salva = repositorio.save(empresa);
		adicionadorLink.adicionarLink(salva);
		return ResponseEntity.status(HttpStatus.CREATED).body(salva);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<Empresa> atualizarEmpresa(@RequestBody Empresa atualizacao) {
		if (atualizacao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Empresa empresa = repositorio.findById(atualizacao.getId()).orElse(null);
		if (empresa == null) {
			return ResponseEntity.notFound().build();
		}
		new EmpresaAtualizador().atualizar(empresa, atualizacao);
		repositorio.save(empresa);
		adicionadorLink.adicionarLink(empresa);
		return ResponseEntity.ok(empresa);
	}

	@DeleteMapping("/excluir")
	public ResponseEntity<Void> excluirEmpresa(@RequestBody Empresa exclusao) {
		if (exclusao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Empresa empresa = repositorio.findById(exclusao.getId()).orElse(null);
		if (empresa == null) {
			return ResponseEntity.notFound().build();
		}
		try {
			repositorio.delete(empresa);
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		return ResponseEntity.ok().build();
	}
}