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
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.modelo.AdicionadorLinkServico;
import com.autobots.automanager.modelo.ServicoAtualizador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.ServicoRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@RestController
@RequestMapping("/servico")
public class ServicoControle {
	@Autowired
	private ServicoRepositorio repositorio;
	@Autowired
	private EmpresaRepositorio empresaRepositorio;
	@Autowired
	private VendaRepositorio vendaRepositorio;
	@Autowired
	private AdicionadorLinkServico adicionadorLink;

	@GetMapping("/{id}")
	public ResponseEntity<Servico> obterServico(@PathVariable long id) {
		Servico servico = repositorio.findById(id).orElse(null);
		if (servico == null) {
			return ResponseEntity.notFound().build();
		}
		adicionadorLink.adicionarLink(servico);
		return ResponseEntity.ok(servico);
	}

	@GetMapping("/servicos")
	public ResponseEntity<List<Servico>> obterServicos() {
		List<Servico> servicos = repositorio.findAll();
		adicionadorLink.adicionarLink(servicos);
		return ResponseEntity.ok(servicos);
	}

	@PostMapping("/cadastro/{empresaId}")
	public ResponseEntity<Servico> cadastrarServico(@PathVariable long empresaId, @RequestBody Servico servico) {
		if (servico.getId() != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		Empresa empresa = empresaRepositorio.findById(empresaId).orElse(null);
		if (empresa == null) {
			return ResponseEntity.notFound().build();
		}
		try {
			repositorio.save(servico);
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.badRequest().build();
		}
		empresa.getServicos().add(servico);
		empresaRepositorio.save(empresa);
		adicionadorLink.adicionarLink(servico);
		return ResponseEntity.status(HttpStatus.CREATED).body(servico);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<Servico> atualizarServico(@RequestBody Servico atualizacao) {
		if (atualizacao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Servico servico = repositorio.findById(atualizacao.getId()).orElse(null);
		if (servico == null) {
			return ResponseEntity.notFound().build();
		}
		new ServicoAtualizador().atualizar(servico, atualizacao);
		repositorio.save(servico);
		adicionadorLink.adicionarLink(servico);
		return ResponseEntity.ok(servico);
	}

	@DeleteMapping("/excluir")
	public ResponseEntity<Void> excluirServico(@RequestBody Servico exclusao) {
		if (exclusao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Long id = exclusao.getId();
		Servico servico = repositorio.findById(id).orElse(null);
		if (servico == null) {
			return ResponseEntity.notFound().build();
		}
		if (vendaRepositorio.existsByServicosId(id)) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		Empresa empresa = empresaRepositorio.findByServicosId(id);
		if (empresa != null) {
			empresa.getServicos().removeIf(s -> id.equals(s.getId()));
			empresaRepositorio.save(empresa);
		}
		repositorio.delete(servico);
		return ResponseEntity.ok().build();
	}
}