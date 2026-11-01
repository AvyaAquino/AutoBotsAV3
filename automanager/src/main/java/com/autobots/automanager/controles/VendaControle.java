package com.autobots.automanager.controles;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.modelo.AdicionadorLinkVenda;
import com.autobots.automanager.modelo.StringVerificadorNulo;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.MercadoriaRepositorio;
import com.autobots.automanager.repositorios.ServicoRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VeiculoRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@RestController
@RequestMapping("/venda")
public class VendaControle {
	@Autowired
	private VendaRepositorio repositorio;
	@Autowired
	private EmpresaRepositorio empresaRepositorio;
	@Autowired
	private UsuarioRepositorio usuarioRepositorio;
	@Autowired
	private VeiculoRepositorio veiculoRepositorio;
	@Autowired
	private MercadoriaRepositorio mercadoriaRepositorio;
	@Autowired
	private ServicoRepositorio servicoRepositorio;
	@Autowired
	private AdicionadorLinkVenda adicionadorLink;

	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	@GetMapping("/{id}")
	public ResponseEntity<Venda> obterVenda(@PathVariable long id) {
		Venda venda = repositorio.findById(id).orElse(null);
		if (venda == null) {
			return ResponseEntity.notFound().build();
		}
		adicionadorLink.adicionarLink(venda);
		return ResponseEntity.ok(venda);
	}

	@GetMapping("/vendas")
	public ResponseEntity<List<Venda>> obterVendas() {
		List<Venda> vendas = repositorio.findAll();
		adicionadorLink.adicionarLink(vendas);
		return ResponseEntity.ok(vendas);
	}

	@PostMapping("/cadastro/{empresaId}")
	public ResponseEntity<Venda> cadastrarVenda(@PathVariable long empresaId, @RequestBody Venda dados) {
		if (dados.getId() != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		if (verificador.verificar(dados.getIdentificacao()) || dados.getCliente() == null
				|| dados.getCliente().getId() == null || dados.getFuncionario() == null
				|| dados.getFuncionario().getId() == null || dados.getVeiculo() == null
				|| dados.getVeiculo().getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Empresa empresa = empresaRepositorio.findById(empresaId).orElse(null);
		Usuario cliente = usuarioRepositorio.findById(dados.getCliente().getId()).orElse(null);
		Usuario funcionario = usuarioRepositorio.findById(dados.getFuncionario().getId()).orElse(null);
		Veiculo veiculo = veiculoRepositorio.findById(dados.getVeiculo().getId()).orElse(null);
		Set<Mercadoria> mercadorias = buscarMercadorias(dados.getMercadorias());
		Set<Servico> servicos = buscarServicos(dados.getServicos());
		if (empresa == null || cliente == null || funcionario == null || veiculo == null || mercadorias == null
				|| servicos == null) {
			return ResponseEntity.notFound().build();
		}
		if (!cliente.getPerfis().contains(PerfilUsuario.CLIENTE)
				|| !funcionario.getPerfis().contains(PerfilUsuario.FUNCIONARIO)
				|| veiculo.getProprietario() == null || !cliente.getId().equals(veiculo.getProprietario().getId())) {
			return ResponseEntity.badRequest().build();
		}
		Venda venda = new Venda();
		venda.setIdentificacao(dados.getIdentificacao());
		venda.setCadastro(dados.getCadastro() != null ? dados.getCadastro() : new Date());
		venda.setCliente(cliente);
		venda.setFuncionario(funcionario);
		venda.setVeiculo(veiculo);
		venda.setMercadorias(mercadorias);
		venda.setServicos(servicos);
		try {
			repositorio.save(venda);
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		empresa.getVendas().add(venda);
		empresaRepositorio.save(empresa);
		veiculo.getVendas().add(venda);
		veiculoRepositorio.save(veiculo);
		cliente.getVendas().add(venda);
		usuarioRepositorio.save(cliente);
		adicionadorLink.adicionarLink(venda);
		return ResponseEntity.status(HttpStatus.CREATED).body(venda);
	}

	@PutMapping("/atualizar")
	public ResponseEntity<Venda> atualizarVenda(@RequestBody Venda atualizacao) {
		if (atualizacao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Venda venda = repositorio.findById(atualizacao.getId()).orElse(null);
		if (venda == null) {
			return ResponseEntity.notFound().build();
		}
		if (!verificador.verificar(atualizacao.getIdentificacao())) {
			venda.setIdentificacao(atualizacao.getIdentificacao());
		}
		if (atualizacao.getFuncionario() != null && atualizacao.getFuncionario().getId() != null) {
			Usuario funcionario = usuarioRepositorio.findById(atualizacao.getFuncionario().getId()).orElse(null);
			if (funcionario == null) {
				return ResponseEntity.notFound().build();
			}
			if (!funcionario.getPerfis().contains(PerfilUsuario.FUNCIONARIO)) {
				return ResponseEntity.badRequest().build();
			}
			venda.setFuncionario(funcionario);
		}
		if (atualizacao.getMercadorias() != null && !atualizacao.getMercadorias().isEmpty()) {
			Set<Mercadoria> mercadorias = buscarMercadorias(atualizacao.getMercadorias());
			if (mercadorias == null) {
				return ResponseEntity.notFound().build();
			}
			venda.getMercadorias().clear();
			venda.getMercadorias().addAll(mercadorias);
		}
		if (atualizacao.getServicos() != null && !atualizacao.getServicos().isEmpty()) {
			Set<Servico> servicos = buscarServicos(atualizacao.getServicos());
			if (servicos == null) {
				return ResponseEntity.notFound().build();
			}
			venda.getServicos().clear();
			venda.getServicos().addAll(servicos);
		}
		try {
			repositorio.save(venda);
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		adicionadorLink.adicionarLink(venda);
		return ResponseEntity.ok(venda);
	}

	@DeleteMapping("/excluir")
	public ResponseEntity<Void> excluirVenda(@RequestBody Venda exclusao) {
		if (exclusao.getId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Long id = exclusao.getId();
		Venda venda = repositorio.findById(id).orElse(null);
		if (venda == null) {
			return ResponseEntity.notFound().build();
		}
		Empresa empresa = empresaRepositorio.findByVendasId(id);
		if (empresa != null) {
			empresa.getVendas().removeIf(v -> id.equals(v.getId()));
			empresaRepositorio.save(empresa);
		}
		Veiculo veiculo = veiculoRepositorio.findByVendasId(id);
		if (veiculo != null) {
			veiculo.getVendas().removeIf(v -> id.equals(v.getId()));
			veiculoRepositorio.save(veiculo);
		}
		Usuario usuario = usuarioRepositorio.findByVendasId(id);
		if (usuario != null) {
			usuario.getVendas().removeIf(v -> id.equals(v.getId()));
			usuarioRepositorio.save(usuario);
		}
		repositorio.delete(venda);
		return ResponseEntity.ok().build();
	}

	private Set<Mercadoria> buscarMercadorias(Set<Mercadoria> lista) {
		Set<Mercadoria> encontradas = new HashSet<>();
		if (lista == null) {
			return encontradas;
		}
		for (Mercadoria item : lista) {
			Mercadoria mercadoria = item.getId() == null ? null : mercadoriaRepositorio.findById(item.getId()).orElse(null);
			if (mercadoria == null) {
				return null;
			}
			encontradas.add(mercadoria);
		}
		return encontradas;
	}

	private Set<Servico> buscarServicos(Set<Servico> lista) {
		Set<Servico> encontrados = new HashSet<>();
		if (lista == null) {
			return encontrados;
		}
		for (Servico item : lista) {
			Servico servico = item.getId() == null ? null : servicoRepositorio.findById(item.getId()).orElse(null);
			if (servico == null) {
				return null;
			}
			encontrados.add(servico);
		}
		return encontrados;
	}
}