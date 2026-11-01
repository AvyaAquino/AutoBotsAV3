package com.autobots.automanager.entidades;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false, exclude = { "cliente", "funcionario", "veiculo", "mercadorias", "servicos" })
@ToString(exclude = { "cliente", "funcionario", "veiculo" })
@Entity
public class Venda extends RepresentationModel<Venda> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private Date cadastro;
	@Column(nullable = false, unique = true)
	private String identificacao;
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
	@JsonIgnoreProperties({ "vendas", "veiculos", "mercadorias", "credenciais" })
	private Usuario cliente;
	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
	@JsonIgnoreProperties({ "vendas", "veiculos", "mercadorias", "credenciais" })
	private Usuario funcionario;
	// ManyToMany: a mesma mercadoria/serviço do catálogo pode aparecer em várias vendas
	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
	private Set<Mercadoria> mercadorias = new HashSet<>();
	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
	private Set<Servico> servicos = new HashSet<>();
	@OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
	@JsonIgnoreProperties({ "vendas", "proprietario" })
	private Veiculo veiculo;
}