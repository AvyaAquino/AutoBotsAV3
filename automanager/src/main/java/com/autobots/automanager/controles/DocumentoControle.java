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

import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.AdicionadorLinkDocumento;
import com.autobots.automanager.modelo.DocumentoAtualizador;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.repositorios.DocumentoRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@RestController
@RequestMapping("/documento")
public class DocumentoControle {
	@Autowired
	private DocumentoRepositorio repositorio;
	@Autowired
	private UsuarioRepositorio usuarioRepositorio;
	@Autowired
	private AdicionadorLinkDocumento adicionadorLink;

	@GetMapping("/{id}")
	public ResponseEntity<Documento> obterDocumento(@PathVariable long id) {
		Documento documento = repositorio.findById(id).orElse(null);
		if (documento == null) {
			return ResponseEntity.notFound().build();
		}
		adicionadorLink.adicionarLink(documento);
		return ResponseEntity.ok(documento);
	}

	@GetMapping("/documentos")
	public ResponseEntity<List<Documento>> obterDocumentos() {
		List<Documento> documentos = repositorio.findAll();
		adicionadorLink.adicionarLink(documentos);
		return ResponseEntity.ok(documentos);
	}

	@PostMapping("/cadastro/{usuarioId}")
	public ResponseEntity<Documento> cadastrarDocumento(@PathVariable long usuarioId, @RequestBody Documento documento) {
		if (documento.getId() != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		if (documento.getTipo() == null || documento.getDataEmissao() == null
				|| new StringVerificadorNulo().verificar(documento.getNumero())) {
			return ResponseEntity.badRequest().build();
		}
		Usuario usuario = usuarioRepositorio.findById(usuarioId).orElse(null);
		if (usuario == null) {
			return ResponseEntity.notFound().build();
		}
		try {
			repositorio.save(documento);
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		usuario.getDocumentos().add(documento);
		usuarioRepositorio.save(usuario);
		adicionadorLink.adicionarLink(documento);
		return ResponseEntity.status(HttpStatus.CREATED).body(documento);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<Documento> atualizarDocumento(@RequestBody Documento atualizacao) {
		if (atualizacao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Documento documento = repositorio.findById(atualizacao.getId()).orElse(null);
		if (documento == null) {
			return ResponseEntity.notFound().build();
		}
		new DocumentoAtualizador().atualizar(documento, atualizacao);
		try {
			repositorio.save(documento);
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		adicionadorLink.adicionarLink(documento);
		return ResponseEntity.ok(documento);
	}

	@DeleteMapping("/excluir")
	public ResponseEntity<Void> excluirDocumento(@RequestBody Documento exclusao) {
		if (exclusao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Long id = exclusao.getId();
		Documento documento = repositorio.findById(id).orElse(null);
		if (documento == null) {
			return ResponseEntity.notFound().build();
		}
		Usuario usuario = usuarioRepositorio.findByDocumentosId(id);
		if (usuario != null) {
			usuario.getDocumentos().removeIf(d -> id.equals(d.getId()));
			usuarioRepositorio.save(usuario);
		} else {
			repositorio.delete(documento);
		}
		return ResponseEntity.ok().build();
	}
}