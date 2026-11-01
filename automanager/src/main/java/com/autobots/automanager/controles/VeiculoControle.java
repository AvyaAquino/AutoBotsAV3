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

import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.modelo.AdicionadorLinkVeiculo;
import com.autobots.automanager.modelo.VeiculoAtualizador;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VeiculoRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@RestController
@RequestMapping("/veiculo")
public class VeiculoControle {
	@Autowired
	private VeiculoRepositorio repositorio;
	@Autowired
	private UsuarioRepositorio usuarioRepositorio;
	@Autowired
	private VendaRepositorio vendaRepositorio;
	@Autowired
	private AdicionadorLinkVeiculo adicionadorLink;

	@GetMapping("/{id}")
	public ResponseEntity<Veiculo> obterVeiculo(@PathVariable long id) {
		Veiculo veiculo = repositorio.findById(id).orElse(null);
		if (veiculo == null) {
			return ResponseEntity.notFound().build();
		}
		adicionadorLink.adicionarLink(veiculo);
		return ResponseEntity.ok(veiculo);
	}

	@GetMapping("/veiculos")
	public ResponseEntity<List<Veiculo>> obterVeiculos() {
		List<Veiculo> veiculos = repositorio.findAll();
		adicionadorLink.adicionarLink(veiculos);
		return ResponseEntity.ok(veiculos);
	}

	@PostMapping("/cadastro/{usuarioId}")
	public ResponseEntity<Veiculo> cadastrarVeiculo(@PathVariable long usuarioId, @RequestBody Veiculo veiculo) {
		if (veiculo.getId() != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		Usuario usuario = usuarioRepositorio.findById(usuarioId).orElse(null);
		if (usuario == null) {
			return ResponseEntity.notFound().build();
		}
		if (!usuario.getPerfis().contains(PerfilUsuario.CLIENTE)) {
			return ResponseEntity.badRequest().build();
		}
		veiculo.setProprietario(usuario);
		repositorio.save(veiculo);
		usuario.getVeiculos().add(veiculo);
		usuarioRepositorio.save(usuario);
		adicionadorLink.adicionarLink(veiculo);
		return ResponseEntity.status(HttpStatus.CREATED).body(veiculo);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<Veiculo> atualizarVeiculo(@RequestBody Veiculo atualizacao) {
		if (atualizacao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Veiculo veiculo = repositorio.findById(atualizacao.getId()).orElse(null);
		if (veiculo == null) {
			return ResponseEntity.notFound().build();
		}
		new VeiculoAtualizador().atualizar(veiculo, atualizacao);
		repositorio.save(veiculo);
		adicionadorLink.adicionarLink(veiculo);
		return ResponseEntity.ok(veiculo);
	}

	@DeleteMapping("/excluir")
	public ResponseEntity<Void> excluirVeiculo(@RequestBody Veiculo exclusao) {
		if (exclusao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Long id = exclusao.getId();
		Veiculo veiculo = repositorio.findById(id).orElse(null);
		if (veiculo == null) {
			return ResponseEntity.notFound().build();
		}
		if (vendaRepositorio.existsByVeiculoId(id)) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		Usuario usuario = usuarioRepositorio.findByVeiculosId(id);
		if (usuario != null) {
			usuario.getVeiculos().removeIf(v -> id.equals(v.getId()));
			usuarioRepositorio.save(usuario);
		}
		repositorio.delete(veiculo);
		return ResponseEntity.ok().build();
	}
}