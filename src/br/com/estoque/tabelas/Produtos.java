package br.com.estoque.tabelas;

/**
 *
 * @author Romuel Dias de Oliveira - Java Full Teoria e Prática - pg.272
 */
public final class Produtos {

	private Integer id;
	private String descricao;
	private String unidade;
	private Double valorUnitario;
	private Integer quantidade;
	private String obs;

	public Produtos() {
	}

	public Produtos(Integer id) {
		setId(id);
	}

	public Produtos(String descricao) {
		setDescricao(descricao);
	}

	public Produtos(String descricao, String unidade, Double valorUnitario, String obs) {
		setDescricao(descricao);
		setUnidade(unidade);
		setValorUnitario(valorUnitario);
		setObs(obs);
	}

	public Produtos(Integer id, String descricao, Double valorUnitario, Integer quantidade, String obs) {
		setId(id);
		setDescricao(descricao);
		setValorUnitario(valorUnitario);
		setObs(obs);
	}

	public Produtos(Integer id, String descricao, String unidade, Double valorUnitario, Integer quantidade, String obs) {
		setId(id);
		setDescricao(descricao);
		setUnidade(unidade);
		setValorUnitario(valorUnitario);
		setQuantidade(quantidade);
		setObs(obs);
	}

	public Integer getId() {
		if (this.id == null) {
			setId(0);
		}
		return this.id;
	}

	public void setId(Integer id) {
		if (this.id == null || this.id < 0) {
			this.id = 0;
		}
		this.id = id;
	}

	public String getDescricao() {
		if (this.descricao == null) {
			setDescricao("");
		}
		return this.descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getUnidade() {
		if (this.unidade == null) {
			setUnidade("");
		}
		return this.unidade;
	}

	public void setUnidade(String unidade) {
		this.unidade = unidade;
	}

	public Double getValorUnitario() {
		if (this.valorUnitario == null) {
			setValorUnitario(0.00);
		}
		return this.valorUnitario;
	}

	public void setValorUnitario(Double valorUnitario) {
		if (this.valorUnitario == null || this.valorUnitario < 0) {
			this.valorUnitario = 0.00;
		}
		this.valorUnitario = valorUnitario;
	}

	public Integer getQuantidade() {
		if (this.quantidade == null) {
			setQuantidade(0);
		}
		return this.quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		if (this.quantidade == null || this.quantidade < 0) {
			this.quantidade = 0;
		} else {
			this.quantidade = quantidade;
		}
	}

	public String getObs() {
		if (this.obs == null) {
			setObs("");
		}
		return this.obs;
	}

	public void setObs(String obs) {
		this.obs = obs;
	}
}
