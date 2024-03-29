package br.com.estoque;

import br.com.estoque.relatorios.Relatorios;
import br.com.estoque.tabelas.Produtos;
import br.com.estoque.tabelas.Usuarios;
import br.com.estoque.util.Dados;
import br.com.estoque.util.Data;
import br.com.estoque.util.Ferramentas;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author danil
 */
public class Estoque extends javax.swing.JFrame {

	private Timer tempo;
	private Ferramentas tools;
	private String[][] listaProdutos;
	private String[][] listaUsuarios;
	private Integer registroAtual;
	private boolean novoRegistroCadastro;
	private Integer codigoProdutoSelecionado;
	private boolean clique;

	public Estoque() {
		tools = new Ferramentas();
		initComponents();
		try {
			setLookAndFeel("Windows");
			EventQueue.invokeLater(() -> {
				ActionListener actionListener = (ActionEvent e) -> {
					Data data = new Data();
					rotuloBarraStatusDataSistema.setText(" " + data.dataEspecial);
					rotuloBarraStatusHoraSistema.setText(" " + data.hora24);
				};
				tempo = new Timer(1000, actionListener);
				tempo.start();
			});
		} catch (Exception e) {
		}
		this.setTitle("Controle de Estoque 1.0");
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		tools.criarJanelaDialogo("Login", janelaLogin, this.getSize(), this.getLocation());
	}

	private void setLookAndFeel(String estilo) throws Exception {
		for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if (estilo.equals(info.getName())) {
				UIManager.setLookAndFeel(info.getClassName());
				SwingUtilities.updateComponentTreeUI(this);
				break;
			}
		}
	}

	private void reorganizarComponentes() {
		Data data = new Data();
		JTextField[] jtfNew, jtfOld = {
			campoTextoCadastroProdutosDescricao, campoTextoCadastroProdutosUnidade,
			campoTextoFormatadoCadastroProdutosValorUnitario, campoTextoCadastroProdutosObs,
			campoTextoCadastroUsuariosLogin, campoTextoMovimentoEstoquePesquisa,
			campoTextoMovimentoEstoqueUnid
		};

		jtfOld = tools.limparComponentes(jtfOld);

		this.campoTextoFormatadoMovimentoEstoqueValorUnitario.setText("0,00");
		this.campoSenhaCadastroUsuariosNovaSenha.setText("123456");
		this.campoSenhaCadastroUsuariosRepetirSenha.setText("123456");
		this.botaoRadioMovimentoOpcoesEstoqueEntrada.setSelected(true);
		this.campoTextoMovimentoEstoqueQuantidade.setText("0");
		this.botaoRadioEstiloOpcaoMetal.setSelected(true);
		this.botaoRadioBackupBackup.setSelected(true);
		this.rotuloBackupDestino.setText(String.format("C:\\Backup\\BackupEstoque%s%s%s.db", data.ano, data.mes, data.dia));
	}

	private void ajustarJanelaMovimentoEstoque(String botaoConfirma, String titulo) {
		this.rotuloBarraStatusNomeAtividade.setText(titulo);
		this.botaoRadioMovimentoEstoqueCodigo.setSelected(true);
		this.botaoMovimentaEstoqueOk.setText(botaoConfirma);
		tools.criarJanelaDialogo(titulo, this.janelaMovimentoEstoque, this.getSize(), this.getLocation());
		this.campoTextoMovimentoEstoquePesquisa.grabFocus();
	}

	private void campoTextoJanelaPesquisaProdutos(String textoPesquisa) {
		Dados produto = null;
		if (this.botaoRadioMovimentoEstoqueCodigo.isSelected()) {
			if (tools.isNumero(textoPesquisa)) {
				this.codigoProdutoSelecionado = Integer.parseInt(textoPesquisa);
				produto = new Dados();
				String[] encontrado = produto.findProductById(this.codigoProdutoSelecionado);
				if (encontrado[0] == null) {
					JOptionPane.showMessageDialog(null, "Nenhum produto encontrado!",
						"PESQUISA PRODUTO", JOptionPane.ERROR_MESSAGE);
				} else {
					entregarResultadoPesquisa(encontrado);
					this.campoTextoMovimentoEstoqueQuantidade.grabFocus();
				}
			} else {
				JOptionPane.showMessageDialog(null, "Entre com um número "
					+ "ao selecionar a opção código.", "VALOR INVÁLIDO!",
					JOptionPane.ERROR_MESSAGE);
			}
		} else {
			produto = new Dados();
			String[][] encontrados = produto.findProductByDecriptionLike(true, textoPesquisa);
			if (encontrados.length == 0) {
				JOptionPane.showMessageDialog(null, "Nenhum produto encontrado!",
					"PESQUISA PRODUTO", JOptionPane.ERROR_MESSAGE);
			} else {
				atualizaTabelaJanelaPesquisaProdutos(encontrados);
				tools.criarJanelaDialogo("Selecione um produto",
					this.janelaMovimentoEstoqueSelecionaProdutoTabela,
					this.getSize(), this.getLocation());
			}
		}
	}

	private void entregarResultadoPesquisa(String[] item) {
		int ultimo = this.caixaCombinacaoMovimentoEstoqueDescricao.getItemCount();
		for (int i = 0; i < ultimo; i++) {
			if (this.caixaCombinacaoMovimentoEstoqueDescricao.getItemAt(i).equals(item[1])) {
				this.caixaCombinacaoMovimentoEstoqueDescricao.setSelectedIndex(i);
			}
		}
		this.campoTextoMovimentoEstoqueUnid.setText(item[2]);
		this.campoTextoFormatadoMovimentoEstoqueValorUnitario.setText(
			String.format("%,3.2f", Double.parseDouble(item[3])));
	}

	private void atualizaTabelaJanelaPesquisaProdutos(String[][] produtosEncontrados) {
		DefaultTableModel conteudoTabelaCadastro
			= (DefaultTableModel) this.tabelaMovimentaEstoqueSelecionaProduto.getModel();
		conteudoTabelaCadastro.setNumRows(0);
		for (int i = 0; i < produtosEncontrados.length; i++) {
			conteudoTabelaCadastro.addRow(new Object[]{
				String.format("%04d", Integer.parseInt(produtosEncontrados[i][0])),
				produtosEncontrados[i][1]
			});
		}
		this.tabelaMovimentaEstoqueSelecionaProduto.getColumnModel().getColumn(0)
			.setPreferredWidth(produtosEncontrados[0][0].length());
	}

	private void respostaCaixaCombinacaoMovimentoEstoqueDescrição() {
		String pesquisaLinha = this.caixaCombinacaoMovimentoEstoqueDescricao.
			getSelectedItem().toString();
		Dados produto = new Dados();
		String[] encontrado = produto.findProductByDescription(pesquisaLinha);
		this.codigoProdutoSelecionado = Integer.parseInt(encontrado[0]);
		entregarResultadoPesquisa(encontrado);
		this.campoTextoMovimentoEstoqueQuantidade.grabFocus();
	}

	private boolean confereNovaSenha() {
		String texto = "Senha com valores incorretos!";
		String mensagem = "SENHA NÂO VÁLIDA!";
		boolean check = false;
		if (this.campoSenhaCadastroUsuariosNovaSenha.getPassword().length > 0) {
			if (!Arrays.equals(this.campoSenhaCadastroUsuariosNovaSenha.getPassword(),
				this.campoSenhaCadastroUsuariosRepetirSenha.getPassword())) {
				JOptionPane.showMessageDialog(null, texto, mensagem, JOptionPane.ERROR_MESSAGE);
				return check;
			}
		} else {
			JOptionPane.showMessageDialog(null, texto, mensagem, JOptionPane.ERROR_MESSAGE);
			return check;
		}
		check = true;
		return check;
	}

	private void movimentoOpcoesEstoque() {
		if (this.botaoRadioMovimentoOpcoesEstoqueEntrada.isSelected()) {
			ajustarJanelaMovimentoEstoque("Confirmar Entrada", "Entrada de Produtos no Estoque");
		}
		if (this.botaoRadioMovimentoOpcoesEstoqueRetirada.isSelected()) {
			ajustarJanelaMovimentoEstoque("Confirmar Retirada", "Retirada de Produtos no Estoque");
		}
	}

	private void confirmaMovimentoEstoque() {
		if (this.codigoProdutoSelecionado != null) {
			Dados dados = new Dados();
			Integer qtd = Integer.parseInt(
				this.campoTextoMovimentoEstoqueQuantidade.getText().trim());
			Produtos produto = dados.findProductObjById(this.codigoProdutoSelecionado);
			Integer qtdAtual = produto.getQuantidade();
			Integer qtdNova;
			if (botaoMovimentaEstoqueOk.getText().equals("Confirmar Entrada")) {
				qtdNova = qtdAtual + qtd;
				if ((qtdAtual == 99998) || (qtdAtual > 99998) || (qtd <= 0)) {
					JOptionPane.showMessageDialog(null,
						"Quantidade de entradas é maior que o permitido,\n"
						+ "ou valor informado é menor ou igual a zero.",
						"ERRO DE ENTRADA", JOptionPane.ERROR_MESSAGE);
				} else {
					int opcao
						= JOptionPane.showConfirmDialog(null,
							"Ao confirmar a operação, não será possível "
							+ "recuperar valors anteriores.\n"
							+ "Deseja confirmar a entrada de produto?",
							"ENTRADA DE PRODUTOS", JOptionPane.YES_NO_OPTION);
					if (opcao == JOptionPane.YES_OPTION) {
						dados = new Dados();
						produto = new Produtos(this.codigoProdutoSelecionado,
							produto.getDescricao(), produto.getUnidade(), produto.getValorUnitario(),
							qtdNova, produto.getObs());
						dados.updateProduct(produto);
					}
				}
			} else {
				qtdNova = qtdAtual - qtd;
				if ((qtdAtual == 0) || (qtdNova < 0) || (qtd <= 0)) {
					JOptionPane.showMessageDialog(null,
						"Quantidade da retirada maior que o permitido,\n"
						+ " ou valor informado, é menor ou igual a zero.",
						"ERRO DE RETIRADA", JOptionPane.ERROR_MESSAGE);
				} else {
					int opcao
						= JOptionPane.showConfirmDialog(null,
							"Ao confirmar a operação, não será possível "
							+ "recuperar valors anteriores.\n"
							+ "Deseja confirmar a saída de produto?",
							"SAÍDA DE PRODUTOS", JOptionPane.YES_NO_OPTION);
					if (opcao == JOptionPane.YES_OPTION) {
						dados = new Dados();
						produto = new Produtos(this.codigoProdutoSelecionado,
							produto.getDescricao(), produto.getUnidade(), produto.getValorUnitario(),
							qtdNova, produto.getObs());
						dados.updateProduct(produto);
					}
				}
			}
		} else {
			JOptionPane.showMessageDialog(null,
				"É necessário informar um produto!",
				"ERRO DE ENTRADA", JOptionPane.ERROR_MESSAGE);
		}
		reorganizarComponentes();
		this.botaoRadioMovimentoEstoqueCodigo.setSelected(true);
		this.campoTextoMovimentoEstoquePesquisa.grabFocus();
	}

	private void ajustarTabelaJanelaRelatorioEstoque(String[][] produtos) {
		DefaultTableModel conteudoTabelaRelatorio
			= (DefaultTableModel) this.tabelaRelatorioEstoque.getModel();
		conteudoTabelaRelatorio.setNumRows(0);
		int tamanho = 0;
		for (String[] produto : produtos) {
			conteudoTabelaRelatorio.addRow(new Object[]{
				String.format("%04d", Integer.parseInt(produto[0])),
				produto[1].trim(),
				produto[2].trim().toUpperCase(),
				String.format("%,3.2f", Double.parseDouble(produto[3])),
				String.format("%4d", Integer.parseInt(produto[4])),
				String.format("%,3.2f", (Double.parseDouble(produto[3])) * (Double.parseDouble(produto[4])))
			});
			int x = produto[1].length();
			tamanho = (tamanho < x) ? x : tamanho;
		}
		DefaultTableCellRenderer esquerda = new DefaultTableCellRenderer();
		DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
		DefaultTableCellRenderer direita = new DefaultTableCellRenderer();

		esquerda.setHorizontalAlignment(SwingConstants.LEFT);
		centralizado.setHorizontalAlignment(SwingConstants.CENTER);
		direita.setHorizontalAlignment(SwingConstants.RIGHT);

		this.tabelaRelatorioEstoque.getColumnModel().getColumn(0).setPreferredWidth(40);
		this.tabelaRelatorioEstoque.getColumnModel().getColumn(0).setCellRenderer(esquerda);
		this.tabelaRelatorioEstoque.getColumnModel().getColumn(1).setPreferredWidth(tamanho * 8);
		this.tabelaRelatorioEstoque.getColumnModel().getColumn(1).setCellRenderer(esquerda);
		this.tabelaRelatorioEstoque.getColumnModel().getColumn(2).setPreferredWidth(60);
		this.tabelaRelatorioEstoque.getColumnModel().getColumn(2).setCellRenderer(centralizado);
		this.tabelaRelatorioEstoque.getColumnModel().getColumn(3).setPreferredWidth(100);
		this.tabelaRelatorioEstoque.getColumnModel().getColumn(3).setCellRenderer(direita);
		this.tabelaRelatorioEstoque.getColumnModel().getColumn(4).setPreferredWidth(80);
		this.tabelaRelatorioEstoque.getColumnModel().getColumn(4).setCellRenderer(centralizado);
		this.tabelaRelatorioEstoque.getColumnModel().getColumn(5).setPreferredWidth(100);
		this.tabelaRelatorioEstoque.getColumnModel().getColumn(5).setCellRenderer(direita);
	}

	private void confirmaAcessoSistema(String login, String senha) {
		System.out.println("Usuário: " + login + "\nSenha: " + senha);
		Dados usuario = new Dados();
		String[] autentica = usuario.findUserByLogin(login);
		if (autentica[0] != null) {
			if (login.equalsIgnoreCase(autentica[1]) && senha.equals(autentica[3])) {
				this.rotuloBarraStatusNomeUsuario.setText(login.toLowerCase());
				this.janelaLogin.dispose();
			} else {
				JOptionPane.showMessageDialog(null, "Senha não confere!",
					"ERRO DE LOGIN", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(null,
				"Usuário não existe no sistema",
				"ERRO DE LOGIN", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void zeraListaProdutos() {
		this.listaProdutos = null;
	}

	private void zeraListaUsuarios() {
		this.listaUsuarios = null;
	}

	private void registrosCadastroProdutos() {

		this.novoRegistroCadastro = false;

		this.campoTextoCadastroProdutosCodigo.setText(String.format("%04d",
			Integer.parseInt(this.listaProdutos[this.registroAtual][0])));
		this.campoTextoCadastroProdutosDescricao.setText(
			this.listaProdutos[this.registroAtual][1]);
		this.campoTextoCadastroProdutosUnidade.setText(
			this.listaProdutos[this.registroAtual][2]);
		this.campoTextoFormatadoCadastroProdutosValorUnitario.setText(
			String.format("%,3.2f", Double.parseDouble(
				this.listaProdutos[this.registroAtual][3])));
		this.campoTextoCadastroProdutosObs.setText(
			this.listaProdutos[this.registroAtual][5]);
	}

	private void registrosCadastroUsuarios() {

		this.novoRegistroCadastro = false;

		this.campoTextoCadastroUsuariosCodigo.setText(String.format("%04d",
			Integer.parseInt(this.listaUsuarios[this.registroAtual][0])));
		this.campoTextoCadastroUsuariosLogin.setText(
			this.listaUsuarios[this.registroAtual][1]);
		if (this.listaUsuarios[this.registroAtual][2].equals("0")) {
			this.botaoRadioCadastroUsuariosAdministrador.setSelected(true);
		} else {
			this.botaoRadioCadastroUsuariosComum.setSelected(true);
		}
		this.campoSenhaCadastroUsuariosNovaSenha.setText(
			this.listaUsuarios[this.registroAtual][3]);
		this.campoSenhaCadastroUsuariosRepetirSenha.setText(
			this.listaUsuarios[this.registroAtual][3]);
	}

	private boolean verificaCadastroUsuario() {
		boolean check = false;
		if (this.campoTextoCadastroUsuariosLogin.getText().equals("")
			|| this.campoSenhaCadastroUsuariosNovaSenha.getPassword().length <= 0
			|| this.campoSenhaCadastroUsuariosRepetirSenha.getPassword().length <= 0) {
			JOptionPane.showMessageDialog(null,
				"Campos: Login, Nova Senha e Repetir Senha; são obrigatórios.",
				"DADOS OBRIGATÓRIOS!", JOptionPane.WARNING_MESSAGE);
			return check;
		}
		if (this.campoTextoCadastroUsuariosLogin.getText().length() > 10) {
			JOptionPane.showMessageDialog(null,
				"Campos de login excedem máximo de 10 caractes.",
				"LOGIN INCORRETO!", JOptionPane.WARNING_MESSAGE);
			return check;
		}
		if (this.campoSenhaCadastroUsuariosNovaSenha.getPassword().length > 10
			|| this.campoSenhaCadastroUsuariosRepetirSenha.getPassword().length > 10) {
			JOptionPane.showMessageDialog(null,
				"Campos de senha excedem máximo de 10 caractes.",
				"SENHA INCORRETA!", JOptionPane.WARNING_MESSAGE);
			return check;
		}
		check = confereNovaSenha() && !check;
		return check;
	}

	private boolean verificaCadastroProduto() {
		boolean check = false;
		if (this.campoTextoCadastroProdutosDescricao.getText().equals("")
			|| this.campoTextoCadastroProdutosUnidade.getText().equals("")) {
			JOptionPane.showMessageDialog(null,
				"Campos: Descrição e Unidade; são obrigatórios.",
				"DADOS OBRIGATÓRIOS!", JOptionPane.WARNING_MESSAGE);
			return check;
		}
		if (this.campoTextoCadastroProdutosDescricao.getText().length() > 30) {
			JOptionPane.showMessageDialog(null,
				"Campo de descrição excedem máximo de 30 caractes.",
				"LIMITE DE CARACTERES!", JOptionPane.WARNING_MESSAGE);
			return check;
		}
		if (this.campoTextoCadastroProdutosUnidade.getText().length() > 10) {
			JOptionPane.showMessageDialog(null,
				"Campo de unidade excedem máximo de 10 caractes.",
				"LIMITE DE CARACTERES!", JOptionPane.WARNING_MESSAGE);
			return check;
		}
		if (this.campoTextoCadastroProdutosObs.getText().length() > 50) {
			JOptionPane.showMessageDialog(null,
				"Campo de observações excedem máximo de 50 caractes.",
				"LIMITE DE CARACTERES!", JOptionPane.WARNING_MESSAGE);
			return check;
		}
		check = true;
		return check;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        janelaLogin = new javax.swing.JDialog();
        rotuloLoginLogo = new javax.swing.JLabel();
        painelLoginLogOn = new javax.swing.JPanel();
        rotuloLoginLogin = new javax.swing.JLabel();
        campoTextoLoginLogin = new javax.swing.JTextField();
        rotuloLoginSenha = new javax.swing.JLabel();
        campoSenhaLoginSenha = new javax.swing.JPasswordField();
        botaoLoginEntrar = new javax.swing.JButton();
        botaoLoginSair = new javax.swing.JButton();
        janelaCadastroProdutos = new javax.swing.JDialog();
        rotuloCadastroProdutosCodigo = new javax.swing.JLabel();
        campoTextoCadastroProdutosCodigo = new javax.swing.JTextField();
        rotuloCadastroProdutosDescricao = new javax.swing.JLabel();
        campoTextoCadastroProdutosDescricao = new javax.swing.JTextField();
        rotuloCadastroProdutosUnidade = new javax.swing.JLabel();
        campoTextoCadastroProdutosUnidade = new javax.swing.JTextField();
        rotuloCadastroProdutosValorUnitario = new javax.swing.JLabel();
        campoTextoFormatadoCadastroProdutosValorUnitario = new javax.swing.JFormattedTextField();
        rotuloCadastroProdutosObs = new javax.swing.JLabel();
        campoTextoCadastroProdutosObs = new javax.swing.JTextField();
        botaoCadastroProdutosNovo = new javax.swing.JButton();
        botaoCadastroProdutosSalvar = new javax.swing.JButton();
        botaoCadastroProdutosExcluir = new javax.swing.JButton();
        botaoCadastroProdutosPrimeiroRegistro = new javax.swing.JButton();
        botaoCadastroProdutosRegistroAnterior = new javax.swing.JButton();
        botaoCadastroProdutosProximoRegistro = new javax.swing.JButton();
        botaoCadastroProdutosUltimoRegistro = new javax.swing.JButton();
        janelaCadastroUsuarios = new javax.swing.JDialog();
        painelCadastroUsuariosUsuario = new javax.swing.JPanel();
        rotuloCadastroUsuariosCodigo = new javax.swing.JLabel();
        campoTextoCadastroUsuariosCodigo = new javax.swing.JTextField();
        rotuloCadastroUsuariosLogin = new javax.swing.JLabel();
        campoTextoCadastroUsuariosLogin = new javax.swing.JTextField();
        painelCadastroUsuariosTipoUsuario = new javax.swing.JPanel();
        botaoRadioCadastroUsuariosAdministrador = new javax.swing.JRadioButton();
        botaoRadioCadastroUsuariosComum = new javax.swing.JRadioButton();
        painelCadastroUsuariosChaves = new javax.swing.JPanel();
        rotuloSenhaCadastroUsuariosNovaSenha = new javax.swing.JLabel();
        campoSenhaCadastroUsuariosNovaSenha = new javax.swing.JPasswordField();
        rotuloSenhaCadastroUsuariosRepetirSenha = new javax.swing.JLabel();
        campoSenhaCadastroUsuariosRepetirSenha = new javax.swing.JPasswordField();
        botaoCadastroUsuariosNovo = new javax.swing.JButton();
        botaoCadastroUsuariosSalvar = new javax.swing.JButton();
        botaoCadastroUsuariosExcluir = new javax.swing.JButton();
        botaoCadastroUsuariosPrimeiroRegistro = new javax.swing.JButton();
        botaoCadastroUsuariosRegistroAnterior = new javax.swing.JButton();
        botaoCadastroUsuariosProximoRegistro = new javax.swing.JButton();
        botaoCadastroUsuariosUltimoRegistro = new javax.swing.JButton();
        grupoBotaoCadastroUsuario = new javax.swing.ButtonGroup();
        janelaMovimentoOpcoesEstoque = new javax.swing.JDialog();
        painelMovimentoOpcoesEstoqueEstoque = new javax.swing.JPanel();
        botaoRadioMovimentoOpcoesEstoqueEntrada = new javax.swing.JRadioButton();
        botaoRadioMovimentoOpcoesEstoqueRetirada = new javax.swing.JRadioButton();
        botaoMovimentoOpcoesEstoqueSelecionar = new javax.swing.JButton();
        grupoBotaoMovimentoOpcoesEstoque = new javax.swing.ButtonGroup();
        janelaMovimentoEstoque = new javax.swing.JDialog();
        painelMovimentoEstoqueProcurarProduto = new javax.swing.JPanel();
        painelMovimentoEstoquePesquisarPor = new javax.swing.JPanel();
        botaoRadioMovimentoEstoqueCodigo = new javax.swing.JRadioButton();
        botaoRadioMovimentoEstoqueDescricao = new javax.swing.JRadioButton();
        rotuloMovimentoEstoquePesquisa = new javax.swing.JLabel();
        campoTextoMovimentoEstoquePesquisa = new javax.swing.JTextField();
        botaoMovimentoEstoquePesquisaOk = new javax.swing.JButton();
        rotuloMovimentoEstoqueDescricao = new javax.swing.JLabel();
        caixaCombinacaoMovimentoEstoqueDescricao = new javax.swing.JComboBox<>();
        botaoMovimentoEstoqueAbrirTabela = new javax.swing.JButton();
        rotuloMovimentoEstoqueUnid = new javax.swing.JLabel();
        campoTextoMovimentoEstoqueUnid = new javax.swing.JTextField();
        rotuloMovimentoEstoqueValorUnitario = new javax.swing.JLabel();
        campoTextoFormatadoMovimentoEstoqueValorUnitario = new javax.swing.JTextField();
        rotuloMovimentoEstoqueQuantidade = new javax.swing.JLabel();
        campoTextoMovimentoEstoqueQuantidade = new javax.swing.JTextField();
        botaoMovimentaEstoqueOk = new javax.swing.JButton();
        grupoBotaoMovimentoEstoquePesquisarPor = new javax.swing.ButtonGroup();
        janelaMovimentoEstoqueSelecionaProdutoTabela = new javax.swing.JDialog();
        painelMovimentoEstoqueSelecionaProdutoTabela = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaMovimentaEstoqueSelecionaProduto = new javax.swing.JTable();
        botaoMovimentoEstoqueSelecionaProdutoConfirmar = new javax.swing.JButton();
        janelaRelatorioEstoque = new javax.swing.JDialog();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaRelatorioEstoque = new javax.swing.JTable();
        botaoRelatorioEstoqueImprimir = new javax.swing.JButton();
        janelaSobre = new javax.swing.JDialog();
        rotuloSobrePrograma = new javax.swing.JLabel();
        rotuloSobreLogo = new javax.swing.JLabel();
        rotuloSobreAutor = new javax.swing.JLabel();
        botaoSobreOk = new javax.swing.JButton();
        janelaEstilo = new javax.swing.JDialog();
        painelEstiloOpcao = new javax.swing.JPanel();
        botaoRadioEstiloOpcaoMetal = new javax.swing.JRadioButton();
        botaoRadioEstiloOpcaoNimbus = new javax.swing.JRadioButton();
        botaoRadioEstiloOpcaoMotif = new javax.swing.JRadioButton();
        botaoRadioEstiloOpcaoWindows = new javax.swing.JRadioButton();
        botaoRadioEstiloOpcaoWindowsClassic = new javax.swing.JRadioButton();
        botaoEstiloAplicarEstilo = new javax.swing.JButton();
        grupoBotaoEstilo = new javax.swing.ButtonGroup();
        janelaBackup = new javax.swing.JDialog();
        painelBackup = new javax.swing.JPanel();
        botaoRadioBackupRestaurar = new javax.swing.JRadioButton();
        botaoRadioBackupBackup = new javax.swing.JRadioButton();
        botaoBackupArquivo = new javax.swing.JButton();
        rotuloBackupDestino = new javax.swing.JLabel();
        botaoBackupConfirmar = new javax.swing.JButton();
        grupoBotaoBackup = new javax.swing.ButtonGroup();
        janelaProcurarArquivoBackup = new javax.swing.JFileChooser();
        popupMenuJanelaPrincipal = new javax.swing.JPopupMenu();
        popupMenuProdutos = new javax.swing.JMenuItem();
        popupMenuUsuarios = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        popupMenuMovimentoEstoque = new javax.swing.JMenuItem();
        popupMenuRelatorios = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        popupMenuSair = new javax.swing.JMenuItem();
        barraFerramentasJanelaPrincipal = new javax.swing.JToolBar();
        botaoBarraFerramentasProdutos = new javax.swing.JButton();
        botaoBarraFerramentasUsuarios = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        botaoBarraFerramentasMovimentoEstoque = new javax.swing.JButton();
        botaoBarraFerramentasRelatorioEstoque = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        botaoBarraFerramentasSair = new javax.swing.JButton();
        painelBarraStatusJanelaPrincipal = new javax.swing.JPanel();
        painelBarraStatusSecaoUsuario = new javax.swing.JPanel();
        rotuloBarraStatusUsuario = new javax.swing.JLabel();
        rotuloBarraStatusNomeUsuario = new javax.swing.JLabel();
        painelBarraStatusSecaoRelogio = new javax.swing.JPanel();
        rotuloBarraStatusRelogioSistema = new javax.swing.JLabel();
        rotuloBarraStatusDataSistema = new javax.swing.JLabel();
        rotuloBarraStatusHoraSistema = new javax.swing.JLabel();
        painelBarraStatusAtividade = new javax.swing.JPanel();
        rotuloBarraStatusAtividade = new javax.swing.JLabel();
        rotuloBarraStatusNomeAtividade = new javax.swing.JLabel();
        rotuloJanelaPrincipalLogo = new javax.swing.JLabel();
        barraMenuJanelaPrincipal = new javax.swing.JMenuBar();
        menuJanelaPrincipalCadastro = new javax.swing.JMenu();
        itemMenuCadastroProdutos = new javax.swing.JMenuItem();
        itemMenuCadastroUsuarios = new javax.swing.JMenuItem();
        menuJanelaPrincipalMovimento = new javax.swing.JMenu();
        itemMenuMovimentoEntradas = new javax.swing.JMenuItem();
        itemMenuMovimentoRetiradas = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        itemMenuMovimentoRelatorio = new javax.swing.JMenuItem();
        menuJanelaPrincipalAjuda = new javax.swing.JMenu();
        itemMenuAjudaSobre = new javax.swing.JMenuItem();
        itemMenuAjudaEstilo = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        itemMenuAjudaBackup = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        itemMenuAjudaSair = new javax.swing.JMenuItem();

        janelaLogin.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        janelaLogin.setUndecorated(true);
        janelaLogin.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                janelaLoginWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });

        rotuloLoginLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rotuloLoginLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/logo.png"))); // NOI18N
        rotuloLoginLogo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        painelLoginLogOn.setBorder(javax.swing.BorderFactory.createTitledBorder("LogOn"));

        rotuloLoginLogin.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        rotuloLoginLogin.setText("Login:");
        rotuloLoginLogin.setToolTipText("Entre com o login do usuário");

        campoTextoLoginLogin.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        campoTextoLoginLogin.setToolTipText("Entre com o login do usuário");
        campoTextoLoginLogin.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campoTextoLoginLoginFocusGained(evt);
            }
        });
        campoTextoLoginLogin.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                campoTextoLoginLoginKeyPressed(evt);
            }
        });

        rotuloLoginSenha.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        rotuloLoginSenha.setText("Senha:");
        rotuloLoginSenha.setToolTipText("Entre com a senha do usuário");

        campoSenhaLoginSenha.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        campoSenhaLoginSenha.setText("123456");
        campoSenhaLoginSenha.setToolTipText("Entre com a senha do usuário");
        campoSenhaLoginSenha.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campoSenhaLoginSenhaFocusGained(evt);
            }
        });
        campoSenhaLoginSenha.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                campoSenhaLoginSenhaKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout painelLoginLogOnLayout = new javax.swing.GroupLayout(painelLoginLogOn);
        painelLoginLogOn.setLayout(painelLoginLogOnLayout);
        painelLoginLogOnLayout.setHorizontalGroup(
            painelLoginLogOnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelLoginLogOnLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(painelLoginLogOnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelLoginLogOnLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(rotuloLoginLogin, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE))
                    .addComponent(rotuloLoginSenha, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelLoginLogOnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(campoTextoLoginLogin)
                    .addComponent(campoSenhaLoginSenha, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelLoginLogOnLayout.setVerticalGroup(
            painelLoginLogOnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelLoginLogOnLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(painelLoginLogOnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotuloLoginLogin)
                    .addComponent(campoTextoLoginLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(painelLoginLogOnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotuloLoginSenha)
                    .addComponent(campoSenhaLoginSenha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        botaoLoginEntrar.setText("Entrar");
        botaoLoginEntrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoLoginEntrarActionPerformed(evt);
            }
        });

        botaoLoginSair.setText("Sair");
        botaoLoginSair.setToolTipText("Cancelar Login");
        botaoLoginSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoLoginSairActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout janelaLoginLayout = new javax.swing.GroupLayout(janelaLogin.getContentPane());
        janelaLogin.getContentPane().setLayout(janelaLoginLayout);
        janelaLoginLayout.setHorizontalGroup(
            janelaLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaLoginLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(rotuloLoginLogo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(janelaLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(janelaLoginLayout.createSequentialGroup()
                        .addComponent(botaoLoginEntrar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(botaoLoginSair, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(painelLoginLogOn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        janelaLoginLayout.setVerticalGroup(
            janelaLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaLoginLayout.createSequentialGroup()
                .addContainerGap(71, Short.MAX_VALUE)
                .addGroup(janelaLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(rotuloLoginLogo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(janelaLoginLayout.createSequentialGroup()
                        .addComponent(painelLoginLogOn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(janelaLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(botaoLoginEntrar)
                            .addComponent(botaoLoginSair))))
                .addContainerGap(71, Short.MAX_VALUE))
        );

        janelaCadastroProdutos.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                janelaCadastroProdutosWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });

        rotuloCadastroProdutosCodigo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        rotuloCadastroProdutosCodigo.setText("Código:");
        rotuloCadastroProdutosCodigo.setToolTipText("Código do produto");

        campoTextoCadastroProdutosCodigo.setEditable(false);
        campoTextoCadastroProdutosCodigo.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoTextoCadastroProdutosCodigo.setText("0001");
        campoTextoCadastroProdutosCodigo.setToolTipText("Código do produto");

        rotuloCadastroProdutosDescricao.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        rotuloCadastroProdutosDescricao.setText("Descrição:");
        rotuloCadastroProdutosDescricao.setToolTipText("Descrição do produto");

        campoTextoCadastroProdutosDescricao.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        campoTextoCadastroProdutosDescricao.setToolTipText("Descrição do produto");
        campoTextoCadastroProdutosDescricao.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campoTextoCadastroProdutosDescricaoFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoTextoCadastroProdutosDescricaoFocusLost(evt);
            }
        });

        rotuloCadastroProdutosUnidade.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        rotuloCadastroProdutosUnidade.setText("Unidade:");
        rotuloCadastroProdutosUnidade.setToolTipText("Tipo de unidade do produto");

        campoTextoCadastroProdutosUnidade.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        campoTextoCadastroProdutosUnidade.setToolTipText("Tipo de unidade do produto");
        campoTextoCadastroProdutosUnidade.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campoTextoCadastroProdutosUnidadeFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoTextoCadastroProdutosUnidadeFocusLost(evt);
            }
        });

        rotuloCadastroProdutosValorUnitario.setText("Valor Unitário: R$");
        rotuloCadastroProdutosValorUnitario.setToolTipText("Valor da unidade");

        campoTextoFormatadoCadastroProdutosValorUnitario.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        campoTextoFormatadoCadastroProdutosValorUnitario.setText("0,00");
        campoTextoFormatadoCadastroProdutosValorUnitario.setToolTipText("Valor da unidade");
        campoTextoFormatadoCadastroProdutosValorUnitario.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campoTextoFormatadoCadastroProdutosValorUnitarioFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoTextoFormatadoCadastroProdutosValorUnitarioFocusLost(evt);
            }
        });

        rotuloCadastroProdutosObs.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        rotuloCadastroProdutosObs.setText("Obs.:");
        rotuloCadastroProdutosObs.setToolTipText("Informações adicionais do produto");
        rotuloCadastroProdutosObs.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoTextoCadastroProdutosDescricaoFocusLost(evt);
            }
        });

        campoTextoCadastroProdutosObs.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        campoTextoCadastroProdutosObs.setToolTipText("Informações adicionais do produto");
        campoTextoCadastroProdutosObs.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campoTextoCadastroProdutosObsFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoTextoCadastroProdutosObsFocusLost(evt);
            }
        });

        botaoCadastroProdutosNovo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroRegistroNovo.png"))); // NOI18N
        botaoCadastroProdutosNovo.setToolTipText("Novo produto");
        botaoCadastroProdutosNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoCadastroProdutosNovoActionPerformed(evt);
            }
        });

        botaoCadastroProdutosSalvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroRegistroSalvar.png"))); // NOI18N
        botaoCadastroProdutosSalvar.setToolTipText("Salvar produto");
        botaoCadastroProdutosSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoCadastroProdutosSalvarActionPerformed(evt);
            }
        });

        botaoCadastroProdutosExcluir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroRegistroExcluir.png"))); // NOI18N
        botaoCadastroProdutosExcluir.setToolTipText("Excluir produto");
        botaoCadastroProdutosExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoCadastroProdutosExcluirActionPerformed(evt);
            }
        });

        botaoCadastroProdutosPrimeiroRegistro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroRegistroPrimeiro.png"))); // NOI18N
        botaoCadastroProdutosPrimeiroRegistro.setToolTipText("Primeiro registro");
        botaoCadastroProdutosPrimeiroRegistro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoCadastroProdutosPrimeiroRegistroActionPerformed(evt);
            }
        });

        botaoCadastroProdutosRegistroAnterior.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroRegistroAnterior.png"))); // NOI18N
        botaoCadastroProdutosRegistroAnterior.setToolTipText("Registro anterior");
        botaoCadastroProdutosRegistroAnterior.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoCadastroProdutosRegistroAnteriorActionPerformed(evt);
            }
        });

        botaoCadastroProdutosProximoRegistro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroRegistroProximo.png"))); // NOI18N
        botaoCadastroProdutosProximoRegistro.setToolTipText("Próximo registro");
        botaoCadastroProdutosProximoRegistro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoCadastroProdutosProximoRegistroActionPerformed(evt);
            }
        });

        botaoCadastroProdutosUltimoRegistro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroRegistroUltimo.png"))); // NOI18N
        botaoCadastroProdutosUltimoRegistro.setToolTipText("Último registro");
        botaoCadastroProdutosUltimoRegistro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoCadastroProdutosUltimoRegistroActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout janelaCadastroProdutosLayout = new javax.swing.GroupLayout(janelaCadastroProdutos.getContentPane());
        janelaCadastroProdutos.getContentPane().setLayout(janelaCadastroProdutosLayout);
        janelaCadastroProdutosLayout.setHorizontalGroup(
            janelaCadastroProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaCadastroProdutosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(janelaCadastroProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(janelaCadastroProdutosLayout.createSequentialGroup()
                        .addGroup(janelaCadastroProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(rotuloCadastroProdutosObs)
                            .addComponent(rotuloCadastroProdutosUnidade)
                            .addGroup(janelaCadastroProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(rotuloCadastroProdutosCodigo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(rotuloCadastroProdutosDescricao, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(janelaCadastroProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(campoTextoCadastroProdutosDescricao)
                            .addComponent(campoTextoCadastroProdutosObs)
                            .addGroup(janelaCadastroProdutosLayout.createSequentialGroup()
                                .addComponent(campoTextoCadastroProdutosUnidade, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(rotuloCadastroProdutosValorUnitario)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoTextoFormatadoCadastroProdutosValorUnitario))
                            .addComponent(campoTextoCadastroProdutosCodigo)))
                    .addGroup(janelaCadastroProdutosLayout.createSequentialGroup()
                        .addComponent(botaoCadastroProdutosNovo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botaoCadastroProdutosSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botaoCadastroProdutosExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                        .addComponent(botaoCadastroProdutosPrimeiroRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botaoCadastroProdutosRegistroAnterior, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botaoCadastroProdutosProximoRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botaoCadastroProdutosUltimoRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        janelaCadastroProdutosLayout.setVerticalGroup(
            janelaCadastroProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, janelaCadastroProdutosLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(janelaCadastroProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotuloCadastroProdutosCodigo)
                    .addComponent(campoTextoCadastroProdutosCodigo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(janelaCadastroProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotuloCadastroProdutosDescricao)
                    .addComponent(campoTextoCadastroProdutosDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(janelaCadastroProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotuloCadastroProdutosUnidade)
                    .addComponent(campoTextoCadastroProdutosUnidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rotuloCadastroProdutosValorUnitario)
                    .addComponent(campoTextoFormatadoCadastroProdutosValorUnitario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(janelaCadastroProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotuloCadastroProdutosObs)
                    .addComponent(campoTextoCadastroProdutosObs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(janelaCadastroProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, janelaCadastroProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(botaoCadastroProdutosSalvar, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(botaoCadastroProdutosExcluir)
                        .addComponent(botaoCadastroProdutosNovo))
                    .addComponent(botaoCadastroProdutosPrimeiroRegistro, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(botaoCadastroProdutosRegistroAnterior, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(botaoCadastroProdutosProximoRegistro, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(botaoCadastroProdutosUltimoRegistro, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        janelaCadastroUsuarios.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                janelaCadastroUsuariosWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });

        painelCadastroUsuariosUsuario.setBorder(javax.swing.BorderFactory.createTitledBorder("Usuário"));
        painelCadastroUsuariosUsuario.setToolTipText("Informações básicas");

        rotuloCadastroUsuariosCodigo.setText("Código:");
        rotuloCadastroUsuariosCodigo.setToolTipText("Código do usuário");

        campoTextoCadastroUsuariosCodigo.setEditable(false);
        campoTextoCadastroUsuariosCodigo.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoTextoCadastroUsuariosCodigo.setText("0001");
        campoTextoCadastroUsuariosCodigo.setToolTipText("Código do usuário");

        rotuloCadastroUsuariosLogin.setText("Login:");
        rotuloCadastroUsuariosLogin.setToolTipText("Login de acesso");

        campoTextoCadastroUsuariosLogin.setToolTipText("Login de acesso");
        campoTextoCadastroUsuariosLogin.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campoTextoCadastroUsuariosLoginFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoTextoCadastroUsuariosLoginFocusLost(evt);
            }
        });

        javax.swing.GroupLayout painelCadastroUsuariosUsuarioLayout = new javax.swing.GroupLayout(painelCadastroUsuariosUsuario);
        painelCadastroUsuariosUsuario.setLayout(painelCadastroUsuariosUsuarioLayout);
        painelCadastroUsuariosUsuarioLayout.setHorizontalGroup(
            painelCadastroUsuariosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCadastroUsuariosUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rotuloCadastroUsuariosCodigo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(campoTextoCadastroUsuariosCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(rotuloCadastroUsuariosLogin)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(campoTextoCadastroUsuariosLogin)
                .addContainerGap())
        );
        painelCadastroUsuariosUsuarioLayout.setVerticalGroup(
            painelCadastroUsuariosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCadastroUsuariosUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelCadastroUsuariosUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotuloCadastroUsuariosCodigo)
                    .addComponent(campoTextoCadastroUsuariosCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rotuloCadastroUsuariosLogin)
                    .addComponent(campoTextoCadastroUsuariosLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        painelCadastroUsuariosTipoUsuario.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipo de Usuário"));
        painelCadastroUsuariosTipoUsuario.setToolTipText("Tipo de acesso ao sistema");

        grupoBotaoCadastroUsuario.add(botaoRadioCadastroUsuariosAdministrador);
        botaoRadioCadastroUsuariosAdministrador.setSelected(true);
        botaoRadioCadastroUsuariosAdministrador.setText("Administrador");
        botaoRadioCadastroUsuariosAdministrador.setToolTipText("Tipo super usuário");

        grupoBotaoCadastroUsuario.add(botaoRadioCadastroUsuariosComum);
        botaoRadioCadastroUsuariosComum.setText("Comum");
        botaoRadioCadastroUsuariosComum.setToolTipText("Tipo usuário comum");

        javax.swing.GroupLayout painelCadastroUsuariosTipoUsuarioLayout = new javax.swing.GroupLayout(painelCadastroUsuariosTipoUsuario);
        painelCadastroUsuariosTipoUsuario.setLayout(painelCadastroUsuariosTipoUsuarioLayout);
        painelCadastroUsuariosTipoUsuarioLayout.setHorizontalGroup(
            painelCadastroUsuariosTipoUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCadastroUsuariosTipoUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelCadastroUsuariosTipoUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(botaoRadioCadastroUsuariosAdministrador)
                    .addComponent(botaoRadioCadastroUsuariosComum))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelCadastroUsuariosTipoUsuarioLayout.setVerticalGroup(
            painelCadastroUsuariosTipoUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCadastroUsuariosTipoUsuarioLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(botaoRadioCadastroUsuariosAdministrador)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(botaoRadioCadastroUsuariosComum)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        painelCadastroUsuariosChaves.setBorder(javax.swing.BorderFactory.createTitledBorder("Chaves"));
        painelCadastroUsuariosChaves.setToolTipText("Registro de senha de acesso");

        rotuloSenhaCadastroUsuariosNovaSenha.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        rotuloSenhaCadastroUsuariosNovaSenha.setText("Nova Senha:");
        rotuloSenhaCadastroUsuariosNovaSenha.setToolTipText("Nova senha de acesso");

        campoSenhaCadastroUsuariosNovaSenha.setText("123456");
        campoSenhaCadastroUsuariosNovaSenha.setToolTipText("Nova senha de acesso");
        campoSenhaCadastroUsuariosNovaSenha.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campoSenhaCadastroUsuariosNovaSenhaFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoSenhaCadastroUsuariosNovaSenhaFocusLost(evt);
            }
        });

        rotuloSenhaCadastroUsuariosRepetirSenha.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        rotuloSenhaCadastroUsuariosRepetirSenha.setText("Repetir Senha:");
        rotuloSenhaCadastroUsuariosRepetirSenha.setToolTipText("Repita a senha digitada");

        campoSenhaCadastroUsuariosRepetirSenha.setText("123456");
        campoSenhaCadastroUsuariosRepetirSenha.setToolTipText("Repita a senha digitada");
        campoSenhaCadastroUsuariosRepetirSenha.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campoSenhaCadastroUsuariosRepetirSenhaFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoSenhaCadastroUsuariosRepetirSenhaFocusLost(evt);
            }
        });

        javax.swing.GroupLayout painelCadastroUsuariosChavesLayout = new javax.swing.GroupLayout(painelCadastroUsuariosChaves);
        painelCadastroUsuariosChaves.setLayout(painelCadastroUsuariosChavesLayout);
        painelCadastroUsuariosChavesLayout.setHorizontalGroup(
            painelCadastroUsuariosChavesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCadastroUsuariosChavesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelCadastroUsuariosChavesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(rotuloSenhaCadastroUsuariosRepetirSenha)
                    .addComponent(rotuloSenhaCadastroUsuariosNovaSenha))
                .addGap(18, 18, 18)
                .addGroup(painelCadastroUsuariosChavesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(campoSenhaCadastroUsuariosRepetirSenha)
                    .addComponent(campoSenhaCadastroUsuariosNovaSenha))
                .addContainerGap())
        );
        painelCadastroUsuariosChavesLayout.setVerticalGroup(
            painelCadastroUsuariosChavesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCadastroUsuariosChavesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelCadastroUsuariosChavesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotuloSenhaCadastroUsuariosNovaSenha)
                    .addComponent(campoSenhaCadastroUsuariosNovaSenha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelCadastroUsuariosChavesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoSenhaCadastroUsuariosRepetirSenha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rotuloSenhaCadastroUsuariosRepetirSenha))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        botaoCadastroUsuariosNovo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroRegistroNovo.png"))); // NOI18N
        botaoCadastroUsuariosNovo.setToolTipText("Novo usuário");
        botaoCadastroUsuariosNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoCadastroUsuariosNovoActionPerformed(evt);
            }
        });

        botaoCadastroUsuariosSalvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroRegistroSalvar.png"))); // NOI18N
        botaoCadastroUsuariosSalvar.setToolTipText("Salvar usuário");
        botaoCadastroUsuariosSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoCadastroUsuariosSalvarActionPerformed(evt);
            }
        });

        botaoCadastroUsuariosExcluir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroRegistroExcluir.png"))); // NOI18N
        botaoCadastroUsuariosExcluir.setToolTipText("Excluir usuário");
        botaoCadastroUsuariosExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoCadastroUsuariosExcluirActionPerformed(evt);
            }
        });

        botaoCadastroUsuariosPrimeiroRegistro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroRegistroPrimeiro.png"))); // NOI18N
        botaoCadastroUsuariosPrimeiroRegistro.setToolTipText("Primeiro registro");
        botaoCadastroUsuariosPrimeiroRegistro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoCadastroUsuariosPrimeiroRegistroActionPerformed(evt);
            }
        });

        botaoCadastroUsuariosRegistroAnterior.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroRegistroAnterior.png"))); // NOI18N
        botaoCadastroUsuariosRegistroAnterior.setToolTipText("Registro anterior");
        botaoCadastroUsuariosRegistroAnterior.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoCadastroUsuariosRegistroAnteriorActionPerformed(evt);
            }
        });

        botaoCadastroUsuariosProximoRegistro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroRegistroProximo.png"))); // NOI18N
        botaoCadastroUsuariosProximoRegistro.setToolTipText("Próximo registro");
        botaoCadastroUsuariosProximoRegistro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoCadastroUsuariosProximoRegistroActionPerformed(evt);
            }
        });

        botaoCadastroUsuariosUltimoRegistro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroRegistroUltimo.png"))); // NOI18N
        botaoCadastroUsuariosUltimoRegistro.setToolTipText("Último registro");
        botaoCadastroUsuariosUltimoRegistro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoCadastroUsuariosUltimoRegistroActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout janelaCadastroUsuariosLayout = new javax.swing.GroupLayout(janelaCadastroUsuarios.getContentPane());
        janelaCadastroUsuarios.getContentPane().setLayout(janelaCadastroUsuariosLayout);
        janelaCadastroUsuariosLayout.setHorizontalGroup(
            janelaCadastroUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaCadastroUsuariosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(janelaCadastroUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(painelCadastroUsuariosUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(janelaCadastroUsuariosLayout.createSequentialGroup()
                        .addComponent(painelCadastroUsuariosTipoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(painelCadastroUsuariosChaves, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(janelaCadastroUsuariosLayout.createSequentialGroup()
                        .addComponent(botaoCadastroUsuariosNovo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botaoCadastroUsuariosSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botaoCadastroUsuariosExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
                        .addComponent(botaoCadastroUsuariosPrimeiroRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botaoCadastroUsuariosRegistroAnterior, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botaoCadastroUsuariosProximoRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botaoCadastroUsuariosUltimoRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        janelaCadastroUsuariosLayout.setVerticalGroup(
            janelaCadastroUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaCadastroUsuariosLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(painelCadastroUsuariosUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(janelaCadastroUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(painelCadastroUsuariosChaves, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(painelCadastroUsuariosTipoUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(janelaCadastroUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, janelaCadastroUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(botaoCadastroUsuariosSalvar, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(botaoCadastroUsuariosExcluir)
                        .addComponent(botaoCadastroUsuariosNovo))
                    .addComponent(botaoCadastroUsuariosPrimeiroRegistro, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(botaoCadastroUsuariosRegistroAnterior, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(botaoCadastroUsuariosProximoRegistro, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(botaoCadastroUsuariosUltimoRegistro, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        janelaMovimentoOpcoesEstoque.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                janelaMovimentoOpcoesEstoqueWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });

        painelMovimentoOpcoesEstoqueEstoque.setBorder(javax.swing.BorderFactory.createTitledBorder("Estoque"));
        painelMovimentoOpcoesEstoqueEstoque.setToolTipText("Opções para movimento de estoque");

        grupoBotaoMovimentoOpcoesEstoque.add(botaoRadioMovimentoOpcoesEstoqueEntrada);
        botaoRadioMovimentoOpcoesEstoqueEntrada.setSelected(true);
        botaoRadioMovimentoOpcoesEstoqueEntrada.setText("Entradas");
        botaoRadioMovimentoOpcoesEstoqueEntrada.setToolTipText("Entrada de mercadorias em estoque");

        grupoBotaoMovimentoOpcoesEstoque.add(botaoRadioMovimentoOpcoesEstoqueRetirada);
        botaoRadioMovimentoOpcoesEstoqueRetirada.setText("Retiradas");
        botaoRadioMovimentoOpcoesEstoqueRetirada.setToolTipText("Saída de mercadorias em estoque");

        javax.swing.GroupLayout painelMovimentoOpcoesEstoqueEstoqueLayout = new javax.swing.GroupLayout(painelMovimentoOpcoesEstoqueEstoque);
        painelMovimentoOpcoesEstoqueEstoque.setLayout(painelMovimentoOpcoesEstoqueEstoqueLayout);
        painelMovimentoOpcoesEstoqueEstoqueLayout.setHorizontalGroup(
            painelMovimentoOpcoesEstoqueEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelMovimentoOpcoesEstoqueEstoqueLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelMovimentoOpcoesEstoqueEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(botaoRadioMovimentoOpcoesEstoqueEntrada)
                    .addComponent(botaoRadioMovimentoOpcoesEstoqueRetirada))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelMovimentoOpcoesEstoqueEstoqueLayout.setVerticalGroup(
            painelMovimentoOpcoesEstoqueEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelMovimentoOpcoesEstoqueEstoqueLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(botaoRadioMovimentoOpcoesEstoqueEntrada)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(botaoRadioMovimentoOpcoesEstoqueRetirada)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        botaoMovimentoOpcoesEstoqueSelecionar.setText("Selecionar");
        botaoMovimentoOpcoesEstoqueSelecionar.setToolTipText("Confirmar seleção");
        botaoMovimentoOpcoesEstoqueSelecionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoMovimentoOpcoesEstoqueSelecionarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout janelaMovimentoOpcoesEstoqueLayout = new javax.swing.GroupLayout(janelaMovimentoOpcoesEstoque.getContentPane());
        janelaMovimentoOpcoesEstoque.getContentPane().setLayout(janelaMovimentoOpcoesEstoqueLayout);
        janelaMovimentoOpcoesEstoqueLayout.setHorizontalGroup(
            janelaMovimentoOpcoesEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaMovimentoOpcoesEstoqueLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(janelaMovimentoOpcoesEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(painelMovimentoOpcoesEstoqueEstoque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botaoMovimentoOpcoesEstoqueSelecionar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        janelaMovimentoOpcoesEstoqueLayout.setVerticalGroup(
            janelaMovimentoOpcoesEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaMovimentoOpcoesEstoqueLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelMovimentoOpcoesEstoqueEstoque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(botaoMovimentoOpcoesEstoqueSelecionar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        janelaMovimentoEstoque.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                janelaMovimentoEstoqueWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });
        janelaMovimentoEstoque.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                janelaMovimentoEstoqueWindowOpened(evt);
            }
        });

        painelMovimentoEstoqueProcurarProduto.setBorder(javax.swing.BorderFactory.createTitledBorder("Procurar Produto em Estoque"));
        painelMovimentoEstoqueProcurarProduto.setToolTipText("Opções para pesquisa de produtos");

        painelMovimentoEstoquePesquisarPor.setBorder(javax.swing.BorderFactory.createTitledBorder("Pesquisar por"));
        painelMovimentoEstoquePesquisarPor.setToolTipText("Pesquisar por código ou por descrição");

        grupoBotaoMovimentoEstoquePesquisarPor.add(botaoRadioMovimentoEstoqueCodigo);
        botaoRadioMovimentoEstoqueCodigo.setSelected(true);
        botaoRadioMovimentoEstoqueCodigo.setText("Código");
        botaoRadioMovimentoEstoqueCodigo.setToolTipText("Pesquisar por Código");
        botaoRadioMovimentoEstoqueCodigo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botaoRadioMovimentoEstoqueCodigoMouseClicked(evt);
            }
        });

        grupoBotaoMovimentoEstoquePesquisarPor.add(botaoRadioMovimentoEstoqueDescricao);
        botaoRadioMovimentoEstoqueDescricao.setText("Descrição");
        botaoRadioMovimentoEstoqueDescricao.setToolTipText("Pesquisar por Descrição");
        botaoRadioMovimentoEstoqueDescricao.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botaoRadioMovimentoEstoqueDescricaoMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout painelMovimentoEstoquePesquisarPorLayout = new javax.swing.GroupLayout(painelMovimentoEstoquePesquisarPor);
        painelMovimentoEstoquePesquisarPor.setLayout(painelMovimentoEstoquePesquisarPorLayout);
        painelMovimentoEstoquePesquisarPorLayout.setHorizontalGroup(
            painelMovimentoEstoquePesquisarPorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelMovimentoEstoquePesquisarPorLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(painelMovimentoEstoquePesquisarPorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(botaoRadioMovimentoEstoqueCodigo)
                    .addComponent(botaoRadioMovimentoEstoqueDescricao))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelMovimentoEstoquePesquisarPorLayout.setVerticalGroup(
            painelMovimentoEstoquePesquisarPorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelMovimentoEstoquePesquisarPorLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(botaoRadioMovimentoEstoqueCodigo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(botaoRadioMovimentoEstoqueDescricao)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        rotuloMovimentoEstoquePesquisa.setText("Pesquisa:");
        rotuloMovimentoEstoquePesquisa.setToolTipText("Informe o item a pesquisar");

        campoTextoMovimentoEstoquePesquisa.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        campoTextoMovimentoEstoquePesquisa.setToolTipText("Informe o item a pesquisar");
        campoTextoMovimentoEstoquePesquisa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoTextoMovimentoEstoquePesquisaActionPerformed(evt);
            }
        });
        campoTextoMovimentoEstoquePesquisa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                campoTextoMovimentoEstoquePesquisaKeyPressed(evt);
            }
        });

        botaoMovimentoEstoquePesquisaOk.setText("Ok");
        botaoMovimentoEstoquePesquisaOk.setToolTipText("Confirmar pesquisa por");
        botaoMovimentoEstoquePesquisaOk.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botaoMovimentoEstoquePesquisaOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoMovimentoEstoquePesquisaOkActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout painelMovimentoEstoqueProcurarProdutoLayout = new javax.swing.GroupLayout(painelMovimentoEstoqueProcurarProduto);
        painelMovimentoEstoqueProcurarProduto.setLayout(painelMovimentoEstoqueProcurarProdutoLayout);
        painelMovimentoEstoqueProcurarProdutoLayout.setHorizontalGroup(
            painelMovimentoEstoqueProcurarProdutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelMovimentoEstoqueProcurarProdutoLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(painelMovimentoEstoquePesquisarPor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(painelMovimentoEstoqueProcurarProdutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rotuloMovimentoEstoquePesquisa)
                    .addComponent(botaoMovimentoEstoquePesquisaOk)
                    .addComponent(campoTextoMovimentoEstoquePesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelMovimentoEstoqueProcurarProdutoLayout.setVerticalGroup(
            painelMovimentoEstoqueProcurarProdutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelMovimentoEstoqueProcurarProdutoLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(painelMovimentoEstoqueProcurarProdutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(painelMovimentoEstoquePesquisarPor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(painelMovimentoEstoqueProcurarProdutoLayout.createSequentialGroup()
                        .addComponent(rotuloMovimentoEstoquePesquisa)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(campoTextoMovimentoEstoquePesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addComponent(botaoMovimentoEstoquePesquisaOk)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        rotuloMovimentoEstoqueDescricao.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        rotuloMovimentoEstoqueDescricao.setText("Descrição:");
        rotuloMovimentoEstoqueDescricao.setToolTipText("Descrição do produto para movimento");

        caixaCombinacaoMovimentoEstoqueDescricao.setToolTipText("Descrição do produtoo para movimento");
        caixaCombinacaoMovimentoEstoqueDescricao.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                caixaCombinacaoMovimentoEstoqueDescricaoPopupMenuWillBecomeVisible(evt);
            }
        });
        caixaCombinacaoMovimentoEstoqueDescricao.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                caixaCombinacaoMovimentoEstoqueDescricaoMouseClicked(evt);
            }
        });
        caixaCombinacaoMovimentoEstoqueDescricao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                caixaCombinacaoMovimentoEstoqueDescricaoActionPerformed(evt);
            }
        });
        caixaCombinacaoMovimentoEstoqueDescricao.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                caixaCombinacaoMovimentoEstoqueDescricaoKeyPressed(evt);
            }
        });

        botaoMovimentoEstoqueAbrirTabela.setText("...");
        botaoMovimentoEstoqueAbrirTabela.setToolTipText("Listar todos os produtos cadastrados");
        botaoMovimentoEstoqueAbrirTabela.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botaoMovimentoEstoqueAbrirTabela.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoMovimentoEstoqueAbrirTabelaActionPerformed(evt);
            }
        });

        rotuloMovimentoEstoqueUnid.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        rotuloMovimentoEstoqueUnid.setText("Unid.:");
        rotuloMovimentoEstoqueUnid.setToolTipText("Tipo da unidade do produto");

        campoTextoMovimentoEstoqueUnid.setEditable(false);
        campoTextoMovimentoEstoqueUnid.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        campoTextoMovimentoEstoqueUnid.setToolTipText("Tipo da unidade do produto");

        rotuloMovimentoEstoqueValorUnitario.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        rotuloMovimentoEstoqueValorUnitario.setText("Valor Unitário: R$");
        rotuloMovimentoEstoqueValorUnitario.setToolTipText("Preço unitário do produto");

        campoTextoFormatadoMovimentoEstoqueValorUnitario.setEditable(false);
        campoTextoFormatadoMovimentoEstoqueValorUnitario.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        campoTextoFormatadoMovimentoEstoqueValorUnitario.setText("0,00");
        campoTextoFormatadoMovimentoEstoqueValorUnitario.setToolTipText("Preço unitário do produto");

        rotuloMovimentoEstoqueQuantidade.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        rotuloMovimentoEstoqueQuantidade.setText("Quant.:");
        rotuloMovimentoEstoqueQuantidade.setToolTipText("Quantidade de produtos para movimento");

        campoTextoMovimentoEstoqueQuantidade.setColumns(4);
        campoTextoMovimentoEstoqueQuantidade.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        campoTextoMovimentoEstoqueQuantidade.setText("0");
        campoTextoMovimentoEstoqueQuantidade.setToolTipText("Quantidade de produtos para movimento");
        campoTextoMovimentoEstoqueQuantidade.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                campoTextoMovimentoEstoqueQuantidadeFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoTextoMovimentoEstoqueQuantidadeFocusLost(evt);
            }
        });
        campoTextoMovimentoEstoqueQuantidade.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoTextoMovimentoEstoqueQuantidadeActionPerformed(evt);
            }
        });
        campoTextoMovimentoEstoqueQuantidade.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                campoTextoMovimentoEstoqueQuantidadeKeyPressed(evt);
            }
        });

        botaoMovimentaEstoqueOk.setText("Confirmar Entrada/Retirada");
        botaoMovimentaEstoqueOk.setToolTipText("Confirmar movimentação de produto");
        botaoMovimentaEstoqueOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoMovimentaEstoqueOkActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout janelaMovimentoEstoqueLayout = new javax.swing.GroupLayout(janelaMovimentoEstoque.getContentPane());
        janelaMovimentoEstoque.getContentPane().setLayout(janelaMovimentoEstoqueLayout);
        janelaMovimentoEstoqueLayout.setHorizontalGroup(
            janelaMovimentoEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaMovimentoEstoqueLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(janelaMovimentoEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rotuloMovimentoEstoqueDescricao, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(rotuloMovimentoEstoqueUnid, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(rotuloMovimentoEstoqueQuantidade, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(janelaMovimentoEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(janelaMovimentoEstoqueLayout.createSequentialGroup()
                        .addComponent(caixaCombinacaoMovimentoEstoqueDescricao, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(botaoMovimentoEstoqueAbrirTabela))
                    .addGroup(janelaMovimentoEstoqueLayout.createSequentialGroup()
                        .addGroup(janelaMovimentoEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(campoTextoMovimentoEstoqueUnid)
                            .addComponent(campoTextoMovimentoEstoqueQuantidade))
                        .addGap(18, 18, 18)
                        .addGroup(janelaMovimentoEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(janelaMovimentoEstoqueLayout.createSequentialGroup()
                                .addComponent(rotuloMovimentoEstoqueValorUnitario)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoTextoFormatadoMovimentoEstoqueValorUnitario, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(botaoMovimentaEstoqueOk, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(janelaMovimentoEstoqueLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(painelMovimentoEstoqueProcurarProduto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );
        janelaMovimentoEstoqueLayout.setVerticalGroup(
            janelaMovimentoEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaMovimentoEstoqueLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(painelMovimentoEstoqueProcurarProduto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(janelaMovimentoEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotuloMovimentoEstoqueDescricao)
                    .addComponent(caixaCombinacaoMovimentoEstoqueDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botaoMovimentoEstoqueAbrirTabela))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(janelaMovimentoEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(janelaMovimentoEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(campoTextoFormatadoMovimentoEstoqueValorUnitario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(rotuloMovimentoEstoqueValorUnitario))
                    .addGroup(janelaMovimentoEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(rotuloMovimentoEstoqueUnid)
                        .addComponent(campoTextoMovimentoEstoqueUnid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(janelaMovimentoEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotuloMovimentoEstoqueQuantidade)
                    .addComponent(campoTextoMovimentoEstoqueQuantidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botaoMovimentaEstoqueOk))
                .addContainerGap())
        );

        janelaMovimentoEstoqueSelecionaProdutoTabela.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                janelaMovimentoEstoqueSelecionaProdutoTabelaWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });
        janelaMovimentoEstoqueSelecionaProdutoTabela.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                janelaMovimentoEstoqueSelecionaProdutoTabelaWindowClosing(evt);
            }
        });

        painelMovimentoEstoqueSelecionaProdutoTabela.setBorder(javax.swing.BorderFactory.createTitledBorder("Selecione o produto"));
        painelMovimentoEstoqueSelecionaProdutoTabela.setToolTipText("Clique sobre o produto desejado");

        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        tabelaMovimentaEstoqueSelecionaProduto.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        tabelaMovimentaEstoqueSelecionaProduto.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Descrição"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabelaMovimentaEstoqueSelecionaProduto.setToolTipText("Clique sobre o produto desejado");
        tabelaMovimentaEstoqueSelecionaProduto.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabelaMovimentaEstoqueSelecionaProduto.setColumnSelectionAllowed(true);
        tabelaMovimentaEstoqueSelecionaProduto.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabelaMovimentaEstoqueSelecionaProduto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelaMovimentaEstoqueSelecionaProdutoMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tabelaMovimentaEstoqueSelecionaProduto);
        tabelaMovimentaEstoqueSelecionaProduto.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tabelaMovimentaEstoqueSelecionaProduto.getColumnModel().getColumnCount() > 0) {
            tabelaMovimentaEstoqueSelecionaProduto.getColumnModel().getColumn(0).setPreferredWidth(5);
            tabelaMovimentaEstoqueSelecionaProduto.getColumnModel().getColumn(1).setPreferredWidth(250);
        }

        javax.swing.GroupLayout painelMovimentoEstoqueSelecionaProdutoTabelaLayout = new javax.swing.GroupLayout(painelMovimentoEstoqueSelecionaProdutoTabela);
        painelMovimentoEstoqueSelecionaProdutoTabela.setLayout(painelMovimentoEstoqueSelecionaProdutoTabelaLayout);
        painelMovimentoEstoqueSelecionaProdutoTabelaLayout.setHorizontalGroup(
            painelMovimentoEstoqueSelecionaProdutoTabelaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
        );
        painelMovimentoEstoqueSelecionaProdutoTabelaLayout.setVerticalGroup(
            painelMovimentoEstoqueSelecionaProdutoTabelaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
        );

        botaoMovimentoEstoqueSelecionaProdutoConfirmar.setText("Confirmar Seleção");
        botaoMovimentoEstoqueSelecionaProdutoConfirmar.setToolTipText("Confirmar selção de produto");
        botaoMovimentoEstoqueSelecionaProdutoConfirmar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoMovimentoEstoqueSelecionaProdutoConfirmarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout janelaMovimentoEstoqueSelecionaProdutoTabelaLayout = new javax.swing.GroupLayout(janelaMovimentoEstoqueSelecionaProdutoTabela.getContentPane());
        janelaMovimentoEstoqueSelecionaProdutoTabela.getContentPane().setLayout(janelaMovimentoEstoqueSelecionaProdutoTabelaLayout);
        janelaMovimentoEstoqueSelecionaProdutoTabelaLayout.setHorizontalGroup(
            janelaMovimentoEstoqueSelecionaProdutoTabelaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaMovimentoEstoqueSelecionaProdutoTabelaLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(janelaMovimentoEstoqueSelecionaProdutoTabelaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(painelMovimentoEstoqueSelecionaProdutoTabela, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(botaoMovimentoEstoqueSelecionaProdutoConfirmar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        janelaMovimentoEstoqueSelecionaProdutoTabelaLayout.setVerticalGroup(
            janelaMovimentoEstoqueSelecionaProdutoTabelaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaMovimentoEstoqueSelecionaProdutoTabelaLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(painelMovimentoEstoqueSelecionaProdutoTabela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(botaoMovimentoEstoqueSelecionaProdutoConfirmar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        janelaRelatorioEstoque.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                janelaRelatorioEstoqueWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });

        tabelaRelatorioEstoque.setBorder(new javax.swing.border.MatteBorder(null));
        tabelaRelatorioEstoque.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Descrição", "Unidade", "Valor Unitário", "Quantidade", "Valor Total"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.Integer.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabelaRelatorioEstoque.setToolTipText("Exibição dos dados do estoque");
        tabelaRelatorioEstoque.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tabelaRelatorioEstoque.setColumnSelectionAllowed(true);
        tabelaRelatorioEstoque.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabelaRelatorioEstoque.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(tabelaRelatorioEstoque);
        tabelaRelatorioEstoque.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tabelaRelatorioEstoque.getColumnModel().getColumnCount() > 0) {
            tabelaRelatorioEstoque.getColumnModel().getColumn(0).setMinWidth(4);
            tabelaRelatorioEstoque.getColumnModel().getColumn(1).setMinWidth(150);
            tabelaRelatorioEstoque.getColumnModel().getColumn(2).setMinWidth(4);
            tabelaRelatorioEstoque.getColumnModel().getColumn(3).setMinWidth(4);
            tabelaRelatorioEstoque.getColumnModel().getColumn(4).setMinWidth(4);
            tabelaRelatorioEstoque.getColumnModel().getColumn(5).setMinWidth(4);
        }

        botaoRelatorioEstoqueImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/movimentoImprimir.png"))); // NOI18N
        botaoRelatorioEstoqueImprimir.setText("Imprimir Estoque");
        botaoRelatorioEstoqueImprimir.setToolTipText("Imprime relatório completo do estoque");
        botaoRelatorioEstoqueImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoRelatorioEstoqueImprimirActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout janelaRelatorioEstoqueLayout = new javax.swing.GroupLayout(janelaRelatorioEstoque.getContentPane());
        janelaRelatorioEstoque.getContentPane().setLayout(janelaRelatorioEstoqueLayout);
        janelaRelatorioEstoqueLayout.setHorizontalGroup(
            janelaRelatorioEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaRelatorioEstoqueLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(janelaRelatorioEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, janelaRelatorioEstoqueLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(botaoRelatorioEstoqueImprimir)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        janelaRelatorioEstoqueLayout.setVerticalGroup(
            janelaRelatorioEstoqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaRelatorioEstoqueLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(botaoRelatorioEstoqueImprimir)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        janelaSobre.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                janelaSobreWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });

        rotuloSobrePrograma.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        rotuloSobrePrograma.setText("Controle de Estoque 1.0");

        rotuloSobreLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/logo.png"))); // NOI18N

        rotuloSobreAutor.setText("By Danilo N. B.");

        botaoSobreOk.setText("Ok");
        botaoSobreOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoSobreOkActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout janelaSobreLayout = new javax.swing.GroupLayout(janelaSobre.getContentPane());
        janelaSobre.getContentPane().setLayout(janelaSobreLayout);
        janelaSobreLayout.setHorizontalGroup(
            janelaSobreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaSobreLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(janelaSobreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(rotuloSobrePrograma)
                    .addComponent(rotuloSobreLogo)
                    .addComponent(rotuloSobreAutor)
                    .addComponent(botaoSobreOk))
                .addGap(20, 20, 20))
        );
        janelaSobreLayout.setVerticalGroup(
            janelaSobreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaSobreLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(rotuloSobrePrograma)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rotuloSobreLogo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rotuloSobreAutor)
                .addGap(18, 18, 18)
                .addComponent(botaoSobreOk)
                .addGap(20, 20, 20))
        );

        janelaEstilo.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                janelaEstiloWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });

        painelEstiloOpcao.setBorder(javax.swing.BorderFactory.createTitledBorder("Opção"));
        painelEstiloOpcao.setToolTipText("Opções para Estilo");

        grupoBotaoEstilo.add(botaoRadioEstiloOpcaoMetal);
        botaoRadioEstiloOpcaoMetal.setSelected(true);
        botaoRadioEstiloOpcaoMetal.setText("Metal");
        botaoRadioEstiloOpcaoMetal.setToolTipText("Estilo Metal");

        grupoBotaoEstilo.add(botaoRadioEstiloOpcaoNimbus);
        botaoRadioEstiloOpcaoNimbus.setText("Nimbus");
        botaoRadioEstiloOpcaoNimbus.setToolTipText("Estilo Nimbus");

        grupoBotaoEstilo.add(botaoRadioEstiloOpcaoMotif);
        botaoRadioEstiloOpcaoMotif.setText("CDE/Motif");
        botaoRadioEstiloOpcaoMotif.setToolTipText("Estilo CDE/Motif");

        grupoBotaoEstilo.add(botaoRadioEstiloOpcaoWindows);
        botaoRadioEstiloOpcaoWindows.setText("Windows");
        botaoRadioEstiloOpcaoWindows.setToolTipText("Estilo Windows");

        grupoBotaoEstilo.add(botaoRadioEstiloOpcaoWindowsClassic);
        botaoRadioEstiloOpcaoWindowsClassic.setText("Windows Classic");
        botaoRadioEstiloOpcaoWindowsClassic.setToolTipText("estilo Windows Classic");

        javax.swing.GroupLayout painelEstiloOpcaoLayout = new javax.swing.GroupLayout(painelEstiloOpcao);
        painelEstiloOpcao.setLayout(painelEstiloOpcaoLayout);
        painelEstiloOpcaoLayout.setHorizontalGroup(
            painelEstiloOpcaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelEstiloOpcaoLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(painelEstiloOpcaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(botaoRadioEstiloOpcaoMetal)
                    .addComponent(botaoRadioEstiloOpcaoNimbus)
                    .addComponent(botaoRadioEstiloOpcaoMotif)
                    .addComponent(botaoRadioEstiloOpcaoWindows)
                    .addComponent(botaoRadioEstiloOpcaoWindowsClassic))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelEstiloOpcaoLayout.setVerticalGroup(
            painelEstiloOpcaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelEstiloOpcaoLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(botaoRadioEstiloOpcaoMetal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(botaoRadioEstiloOpcaoNimbus)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(botaoRadioEstiloOpcaoMotif)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(botaoRadioEstiloOpcaoWindows)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(botaoRadioEstiloOpcaoWindowsClassic)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        botaoEstiloAplicarEstilo.setText("Aplicar Estilo");
        botaoEstiloAplicarEstilo.setToolTipText("Confirmar seleção do estilo");
        botaoEstiloAplicarEstilo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoEstiloAplicarEstiloActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout janelaEstiloLayout = new javax.swing.GroupLayout(janelaEstilo.getContentPane());
        janelaEstilo.getContentPane().setLayout(janelaEstiloLayout);
        janelaEstiloLayout.setHorizontalGroup(
            janelaEstiloLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaEstiloLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(janelaEstiloLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(painelEstiloOpcao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botaoEstiloAplicarEstilo))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        janelaEstiloLayout.setVerticalGroup(
            janelaEstiloLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaEstiloLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(painelEstiloOpcao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(botaoEstiloAplicarEstilo)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        janelaBackup.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                janelaBackupWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });

        painelBackup.setBorder(javax.swing.BorderFactory.createTitledBorder("Backup"));
        painelBackup.setToolTipText("Opções de backup");

        grupoBotaoBackup.add(botaoRadioBackupRestaurar);
        botaoRadioBackupRestaurar.setText("Restaurar");

        grupoBotaoBackup.add(botaoRadioBackupBackup);
        botaoRadioBackupBackup.setSelected(true);
        botaoRadioBackupBackup.setText("Backup");

        botaoBackupArquivo.setText("Arquivo");
        botaoBackupArquivo.setToolTipText("Selecionar para restauração ou backup");
        botaoBackupArquivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoBackupArquivoActionPerformed(evt);
            }
        });

        rotuloBackupDestino.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        rotuloBackupDestino.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        rotuloBackupDestino.setText("c:/Backup");
        rotuloBackupDestino.setToolTipText("PATH absoluto para o aqruivo");

        javax.swing.GroupLayout painelBackupLayout = new javax.swing.GroupLayout(painelBackup);
        painelBackup.setLayout(painelBackupLayout);
        painelBackupLayout.setHorizontalGroup(
            painelBackupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBackupLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(painelBackupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rotuloBackupDestino, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(painelBackupLayout.createSequentialGroup()
                        .addComponent(botaoRadioBackupRestaurar)
                        .addGap(18, 18, 18)
                        .addComponent(botaoRadioBackupBackup)
                        .addGap(18, 18, 18)
                        .addComponent(botaoBackupArquivo)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelBackupLayout.setVerticalGroup(
            painelBackupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBackupLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(painelBackupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(botaoRadioBackupRestaurar)
                    .addComponent(botaoRadioBackupBackup)
                    .addComponent(botaoBackupArquivo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(rotuloBackupDestino)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        botaoBackupConfirmar.setText("Confirmar");
        botaoBackupConfirmar.setToolTipText("Confirmar restauração/backup");
        botaoBackupConfirmar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoBackupConfirmarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout janelaBackupLayout = new javax.swing.GroupLayout(janelaBackup.getContentPane());
        janelaBackup.getContentPane().setLayout(janelaBackupLayout);
        janelaBackupLayout.setHorizontalGroup(
            janelaBackupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaBackupLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(janelaBackupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(painelBackup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(botaoBackupConfirmar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        janelaBackupLayout.setVerticalGroup(
            janelaBackupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(janelaBackupLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(painelBackup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(botaoBackupConfirmar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        janelaProcurarArquivoBackup.setCurrentDirectory(new java.io.File("C:\\Backup"));
        janelaProcurarArquivoBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                janelaProcurarArquivoBackupActionPerformed(evt);
            }
        });

        popupMenuProdutos.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        popupMenuProdutos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroProduto.png"))); // NOI18N
        popupMenuProdutos.setMnemonic('P');
        popupMenuProdutos.setText("Produtos");
        popupMenuProdutos.setToolTipText("Cadastro de Produtos");
        popupMenuProdutos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupMenuProdutosActionPerformed(evt);
            }
        });
        popupMenuJanelaPrincipal.add(popupMenuProdutos);

        popupMenuUsuarios.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        popupMenuUsuarios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroUser.png"))); // NOI18N
        popupMenuUsuarios.setMnemonic('u');
        popupMenuUsuarios.setText("Usuários");
        popupMenuUsuarios.setToolTipText("Cadastro de Usuários");
        popupMenuUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupMenuUsuariosActionPerformed(evt);
            }
        });
        popupMenuJanelaPrincipal.add(popupMenuUsuarios);
        popupMenuJanelaPrincipal.add(jSeparator6);

        popupMenuMovimentoEstoque.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        popupMenuMovimentoEstoque.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/movimentoEstoque.png"))); // NOI18N
        popupMenuMovimentoEstoque.setMnemonic('m');
        popupMenuMovimentoEstoque.setText("Movimento em Estoque");
        popupMenuMovimentoEstoque.setToolTipText("Opções para movimento em estoque");
        popupMenuMovimentoEstoque.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupMenuMovimentoEstoqueActionPerformed(evt);
            }
        });
        popupMenuJanelaPrincipal.add(popupMenuMovimentoEstoque);

        popupMenuRelatorios.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        popupMenuRelatorios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/movimentoRelatorio.png"))); // NOI18N
        popupMenuRelatorios.setMnemonic('l');
        popupMenuRelatorios.setText("Relatório de Estoque");
        popupMenuRelatorios.setToolTipText("Relatório de Estoque");
        popupMenuRelatorios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupMenuRelatoriosActionPerformed(evt);
            }
        });
        popupMenuJanelaPrincipal.add(popupMenuRelatorios);
        popupMenuJanelaPrincipal.add(jSeparator7);

        popupMenuSair.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
        popupMenuSair.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/ajudaSaida.png"))); // NOI18N
        popupMenuSair.setMnemonic('r');
        popupMenuSair.setText("Sair");
        popupMenuSair.setToolTipText("Finalizar a Aplicação");
        popupMenuSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupMenuSairActionPerformed(evt);
            }
        });
        popupMenuJanelaPrincipal.add(popupMenuSair);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });

        barraFerramentasJanelaPrincipal.setRollover(true);
        barraFerramentasJanelaPrincipal.setToolTipText("Barra de ferramentas do sistema");

        botaoBarraFerramentasProdutos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroProduto.png"))); // NOI18N
        botaoBarraFerramentasProdutos.setToolTipText("Cadastro de produtos");
        botaoBarraFerramentasProdutos.setFocusable(false);
        botaoBarraFerramentasProdutos.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botaoBarraFerramentasProdutos.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botaoBarraFerramentasProdutos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoBarraFerramentasProdutosActionPerformed(evt);
            }
        });
        barraFerramentasJanelaPrincipal.add(botaoBarraFerramentasProdutos);

        botaoBarraFerramentasUsuarios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroUser.png"))); // NOI18N
        botaoBarraFerramentasUsuarios.setToolTipText("Cadastro de usuário");
        botaoBarraFerramentasUsuarios.setFocusable(false);
        botaoBarraFerramentasUsuarios.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botaoBarraFerramentasUsuarios.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botaoBarraFerramentasUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoBarraFerramentasUsuariosActionPerformed(evt);
            }
        });
        barraFerramentasJanelaPrincipal.add(botaoBarraFerramentasUsuarios);
        barraFerramentasJanelaPrincipal.add(jSeparator4);

        botaoBarraFerramentasMovimentoEstoque.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/movimentoEstoque.png"))); // NOI18N
        botaoBarraFerramentasMovimentoEstoque.setToolTipText("Opções para movimentação de estoque");
        botaoBarraFerramentasMovimentoEstoque.setFocusable(false);
        botaoBarraFerramentasMovimentoEstoque.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botaoBarraFerramentasMovimentoEstoque.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botaoBarraFerramentasMovimentoEstoque.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoBarraFerramentasMovimentoEstoqueActionPerformed(evt);
            }
        });
        barraFerramentasJanelaPrincipal.add(botaoBarraFerramentasMovimentoEstoque);

        botaoBarraFerramentasRelatorioEstoque.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/movimentoRelatorio.png"))); // NOI18N
        botaoBarraFerramentasRelatorioEstoque.setToolTipText("Relatório de estoque");
        botaoBarraFerramentasRelatorioEstoque.setFocusable(false);
        botaoBarraFerramentasRelatorioEstoque.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botaoBarraFerramentasRelatorioEstoque.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botaoBarraFerramentasRelatorioEstoque.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoBarraFerramentasRelatorioEstoqueActionPerformed(evt);
            }
        });
        barraFerramentasJanelaPrincipal.add(botaoBarraFerramentasRelatorioEstoque);
        barraFerramentasJanelaPrincipal.add(jSeparator5);

        botaoBarraFerramentasSair.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/ajudaSaida.png"))); // NOI18N
        botaoBarraFerramentasSair.setToolTipText("Finalizar a aplicação");
        botaoBarraFerramentasSair.setFocusable(false);
        botaoBarraFerramentasSair.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botaoBarraFerramentasSair.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botaoBarraFerramentasSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoBarraFerramentasSairActionPerformed(evt);
            }
        });
        barraFerramentasJanelaPrincipal.add(botaoBarraFerramentasSair);

        painelBarraStatusJanelaPrincipal.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        painelBarraStatusJanelaPrincipal.setToolTipText("Barra de Status do Sistema");

        rotuloBarraStatusUsuario.setText("Usuário:");
        rotuloBarraStatusUsuario.setToolTipText("Usuário logado no sistema");

        rotuloBarraStatusNomeUsuario.setText("Nome");
        rotuloBarraStatusNomeUsuario.setToolTipText("Usuário logado no sistema");

        javax.swing.GroupLayout painelBarraStatusSecaoUsuarioLayout = new javax.swing.GroupLayout(painelBarraStatusSecaoUsuario);
        painelBarraStatusSecaoUsuario.setLayout(painelBarraStatusSecaoUsuarioLayout);
        painelBarraStatusSecaoUsuarioLayout.setHorizontalGroup(
            painelBarraStatusSecaoUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBarraStatusSecaoUsuarioLayout.createSequentialGroup()
                .addComponent(rotuloBarraStatusUsuario)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rotuloBarraStatusNomeUsuario))
        );
        painelBarraStatusSecaoUsuarioLayout.setVerticalGroup(
            painelBarraStatusSecaoUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBarraStatusSecaoUsuarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(rotuloBarraStatusUsuario)
                .addComponent(rotuloBarraStatusNomeUsuario))
        );

        rotuloBarraStatusRelogioSistema.setText("Data:");
        rotuloBarraStatusRelogioSistema.setToolTipText("Relógio do sistema: Data e Hora");

        rotuloBarraStatusDataSistema.setText("00/00/0000");
        rotuloBarraStatusDataSistema.setToolTipText("Relógio do sistema: Data e Hora");

        rotuloBarraStatusHoraSistema.setText("00:00:00h");
        rotuloBarraStatusHoraSistema.setToolTipText("Relógio do sistema: Data e Hora");

        javax.swing.GroupLayout painelBarraStatusSecaoRelogioLayout = new javax.swing.GroupLayout(painelBarraStatusSecaoRelogio);
        painelBarraStatusSecaoRelogio.setLayout(painelBarraStatusSecaoRelogioLayout);
        painelBarraStatusSecaoRelogioLayout.setHorizontalGroup(
            painelBarraStatusSecaoRelogioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBarraStatusSecaoRelogioLayout.createSequentialGroup()
                .addComponent(rotuloBarraStatusRelogioSistema)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rotuloBarraStatusDataSistema)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rotuloBarraStatusHoraSistema)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        painelBarraStatusSecaoRelogioLayout.setVerticalGroup(
            painelBarraStatusSecaoRelogioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBarraStatusSecaoRelogioLayout.createSequentialGroup()
                .addGroup(painelBarraStatusSecaoRelogioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotuloBarraStatusRelogioSistema)
                    .addComponent(rotuloBarraStatusDataSistema)
                    .addComponent(rotuloBarraStatusHoraSistema))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        rotuloBarraStatusAtividade.setText("Opção:");
        rotuloBarraStatusAtividade.setToolTipText("Janela Ativa do Sistema");

        rotuloBarraStatusNomeAtividade.setText("Janela Principal");
        rotuloBarraStatusNomeAtividade.setToolTipText("Janela Ativa do Sistema");

        javax.swing.GroupLayout painelBarraStatusAtividadeLayout = new javax.swing.GroupLayout(painelBarraStatusAtividade);
        painelBarraStatusAtividade.setLayout(painelBarraStatusAtividadeLayout);
        painelBarraStatusAtividadeLayout.setHorizontalGroup(
            painelBarraStatusAtividadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBarraStatusAtividadeLayout.createSequentialGroup()
                .addComponent(rotuloBarraStatusAtividade)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rotuloBarraStatusNomeAtividade)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        painelBarraStatusAtividadeLayout.setVerticalGroup(
            painelBarraStatusAtividadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBarraStatusAtividadeLayout.createSequentialGroup()
                .addGroup(painelBarraStatusAtividadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotuloBarraStatusAtividade)
                    .addComponent(rotuloBarraStatusNomeAtividade))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout painelBarraStatusJanelaPrincipalLayout = new javax.swing.GroupLayout(painelBarraStatusJanelaPrincipal);
        painelBarraStatusJanelaPrincipal.setLayout(painelBarraStatusJanelaPrincipalLayout);
        painelBarraStatusJanelaPrincipalLayout.setHorizontalGroup(
            painelBarraStatusJanelaPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBarraStatusJanelaPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelBarraStatusSecaoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 277, Short.MAX_VALUE)
                .addComponent(painelBarraStatusSecaoRelogio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(painelBarraStatusAtividade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        painelBarraStatusJanelaPrincipalLayout.setVerticalGroup(
            painelBarraStatusJanelaPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBarraStatusJanelaPrincipalLayout.createSequentialGroup()
                .addGroup(painelBarraStatusJanelaPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(painelBarraStatusSecaoRelogio, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(painelBarraStatusAtividade, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(painelBarraStatusSecaoUsuario, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(82, 82, 82))
        );

        rotuloJanelaPrincipalLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/logo.png"))); // NOI18N
        rotuloJanelaPrincipalLogo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                rotuloJanelaPrincipalLogoMouseClicked(evt);
            }
        });

        barraMenuJanelaPrincipal.setToolTipText("");

        menuJanelaPrincipalCadastro.setMnemonic('c');
        menuJanelaPrincipalCadastro.setText("Cadastro");
        menuJanelaPrincipalCadastro.setToolTipText("Opções de Cadastro");

        itemMenuCadastroProdutos.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        itemMenuCadastroProdutos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroProduto.png"))); // NOI18N
        itemMenuCadastroProdutos.setMnemonic('P');
        itemMenuCadastroProdutos.setText("Produtos");
        itemMenuCadastroProdutos.setToolTipText("Cadastro de Produtos");
        itemMenuCadastroProdutos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemMenuCadastroProdutosActionPerformed(evt);
            }
        });
        menuJanelaPrincipalCadastro.add(itemMenuCadastroProdutos);

        itemMenuCadastroUsuarios.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        itemMenuCadastroUsuarios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/cadastroUser.png"))); // NOI18N
        itemMenuCadastroUsuarios.setMnemonic('u');
        itemMenuCadastroUsuarios.setText("Usuários");
        itemMenuCadastroUsuarios.setToolTipText("Cadastro de Usuários");
        itemMenuCadastroUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemMenuCadastroUsuariosActionPerformed(evt);
            }
        });
        menuJanelaPrincipalCadastro.add(itemMenuCadastroUsuarios);

        barraMenuJanelaPrincipal.add(menuJanelaPrincipalCadastro);

        menuJanelaPrincipalMovimento.setMnemonic('m');
        menuJanelaPrincipalMovimento.setText("Movimento");
        menuJanelaPrincipalMovimento.setToolTipText("Opções para movimento de estoque");

        itemMenuMovimentoEntradas.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        itemMenuMovimentoEntradas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/movimentoEntrada.png"))); // NOI18N
        itemMenuMovimentoEntradas.setMnemonic('e');
        itemMenuMovimentoEntradas.setText("Entradas");
        itemMenuMovimentoEntradas.setToolTipText("Entrada de produtos em estoque");
        itemMenuMovimentoEntradas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemMenuMovimentoEntradasActionPerformed(evt);
            }
        });
        menuJanelaPrincipalMovimento.add(itemMenuMovimentoEntradas);

        itemMenuMovimentoRetiradas.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        itemMenuMovimentoRetiradas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/movimentoSaida.png"))); // NOI18N
        itemMenuMovimentoRetiradas.setMnemonic('r');
        itemMenuMovimentoRetiradas.setText("Retiradas");
        itemMenuMovimentoRetiradas.setToolTipText("Saída de produtos em estoque");
        itemMenuMovimentoRetiradas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemMenuMovimentoRetiradasActionPerformed(evt);
            }
        });
        menuJanelaPrincipalMovimento.add(itemMenuMovimentoRetiradas);
        menuJanelaPrincipalMovimento.add(jSeparator1);

        itemMenuMovimentoRelatorio.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        itemMenuMovimentoRelatorio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/movimentoRelatorio.png"))); // NOI18N
        itemMenuMovimentoRelatorio.setMnemonic('l');
        itemMenuMovimentoRelatorio.setText("Relatórios");
        itemMenuMovimentoRelatorio.setToolTipText("Relatório de estoque");
        itemMenuMovimentoRelatorio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemMenuMovimentoRelatorioActionPerformed(evt);
            }
        });
        menuJanelaPrincipalMovimento.add(itemMenuMovimentoRelatorio);

        barraMenuJanelaPrincipal.add(menuJanelaPrincipalMovimento);

        menuJanelaPrincipalAjuda.setMnemonic('a');
        menuJanelaPrincipalAjuda.setText("Ajuda");
        menuJanelaPrincipalAjuda.setToolTipText("Outras opções do sistema");

        itemMenuAjudaSobre.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        itemMenuAjudaSobre.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/ajudaSobre.png"))); // NOI18N
        itemMenuAjudaSobre.setMnemonic('s');
        itemMenuAjudaSobre.setText("Sobre");
        itemMenuAjudaSobre.setToolTipText("Informações sobre o sistema");
        itemMenuAjudaSobre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemMenuAjudaSobreActionPerformed(evt);
            }
        });
        menuJanelaPrincipalAjuda.add(itemMenuAjudaSobre);

        itemMenuAjudaEstilo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        itemMenuAjudaEstilo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/ajudaEstilo.png"))); // NOI18N
        itemMenuAjudaEstilo.setMnemonic('e');
        itemMenuAjudaEstilo.setText("Estilo");
        itemMenuAjudaEstilo.setToolTipText("Opções para estilo das janelas do sistema");
        itemMenuAjudaEstilo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemMenuAjudaEstiloActionPerformed(evt);
            }
        });
        menuJanelaPrincipalAjuda.add(itemMenuAjudaEstilo);
        menuJanelaPrincipalAjuda.add(jSeparator2);

        itemMenuAjudaBackup.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        itemMenuAjudaBackup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/ajudaBackup.png"))); // NOI18N
        itemMenuAjudaBackup.setMnemonic('b');
        itemMenuAjudaBackup.setText("Backup");
        itemMenuAjudaBackup.setToolTipText("Efetuar ou restaurar backup da base de dados do sistema");
        itemMenuAjudaBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemMenuAjudaBackupActionPerformed(evt);
            }
        });
        menuJanelaPrincipalAjuda.add(itemMenuAjudaBackup);
        menuJanelaPrincipalAjuda.add(jSeparator3);

        itemMenuAjudaSair.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
        itemMenuAjudaSair.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/estoque/images/ajudaSaida.png"))); // NOI18N
        itemMenuAjudaSair.setMnemonic('r');
        itemMenuAjudaSair.setText("Sair");
        itemMenuAjudaSair.setToolTipText("Finalizar a aplicação");
        itemMenuAjudaSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemMenuAjudaSairActionPerformed(evt);
            }
        });
        menuJanelaPrincipalAjuda.add(itemMenuAjudaSair);

        barraMenuJanelaPrincipal.add(menuJanelaPrincipalAjuda);

        setJMenuBar(barraMenuJanelaPrincipal);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(barraFerramentasJanelaPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(painelBarraStatusJanelaPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(rotuloJanelaPrincipalLogo)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(barraFerramentasJanelaPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 111, Short.MAX_VALUE)
                .addComponent(rotuloJanelaPrincipalLogo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 111, Short.MAX_VALUE)
                .addComponent(painelBarraStatusJanelaPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
		this.rotuloBarraStatusNomeAtividade.setText("Janela Principal");
		this.reorganizarComponentes();
		zeraListaProdutos();
		zeraListaUsuarios();
    }//GEN-LAST:event_formWindowGainedFocus

    private void itemMenuCadastroProdutosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemMenuCadastroProdutosActionPerformed
		tools.criarJanelaDialogo("Cadastro de Produtos", janelaCadastroProdutos, this.getSize(), this.getLocation());
    }//GEN-LAST:event_itemMenuCadastroProdutosActionPerformed

    private void itemMenuCadastroUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemMenuCadastroUsuariosActionPerformed
		tools.criarJanelaDialogo("Cadastro de Usuários", janelaCadastroUsuarios, this.getSize(), this.getLocation());
    }//GEN-LAST:event_itemMenuCadastroUsuariosActionPerformed

    private void itemMenuMovimentoEntradasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemMenuMovimentoEntradasActionPerformed
		ajustarJanelaMovimentoEstoque("Confirmar Entrada", "Entrada de produtos no estoque");
    }//GEN-LAST:event_itemMenuMovimentoEntradasActionPerformed

    private void itemMenuMovimentoRetiradasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemMenuMovimentoRetiradasActionPerformed
		ajustarJanelaMovimentoEstoque("Confirmar Retirada", "Retirada de produtos do estoque");
    }//GEN-LAST:event_itemMenuMovimentoRetiradasActionPerformed

    private void itemMenuMovimentoRelatorioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemMenuMovimentoRelatorioActionPerformed
		tools.criarJanelaDialogo("Relatório de Estoque", janelaRelatorioEstoque, this.getSize(), this.getLocation());
    }//GEN-LAST:event_itemMenuMovimentoRelatorioActionPerformed

    private void itemMenuAjudaSobreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemMenuAjudaSobreActionPerformed
		tools.criarJanelaDialogo("Sobre", janelaSobre, this.getSize(), this.getLocation());
    }//GEN-LAST:event_itemMenuAjudaSobreActionPerformed

    private void itemMenuAjudaEstiloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemMenuAjudaEstiloActionPerformed
		tools.criarJanelaDialogo("Estilo", janelaEstilo, this.getSize(), this.getLocation());
    }//GEN-LAST:event_itemMenuAjudaEstiloActionPerformed

    private void itemMenuAjudaBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemMenuAjudaBackupActionPerformed
		tools.criarJanelaDialogo("Opções de Backup", janelaBackup, this.getSize(), this.getLocation());
    }//GEN-LAST:event_itemMenuAjudaBackupActionPerformed

    private void itemMenuAjudaSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemMenuAjudaSairActionPerformed
		System.exit(0);
    }//GEN-LAST:event_itemMenuAjudaSairActionPerformed

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
		if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
			popupMenuJanelaPrincipal.show(this, evt.getX(), evt.getY());
		}
    }//GEN-LAST:event_formMouseClicked

    private void popupMenuProdutosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popupMenuProdutosActionPerformed
		tools.criarJanelaDialogo("Cadastro de Produtos", janelaCadastroProdutos, this.getSize(), this.getLocation());
    }//GEN-LAST:event_popupMenuProdutosActionPerformed

    private void popupMenuUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popupMenuUsuariosActionPerformed
		tools.criarJanelaDialogo("Cadastro de Usuários", janelaCadastroUsuarios, this.getSize(), this.getLocation());
    }//GEN-LAST:event_popupMenuUsuariosActionPerformed

    private void popupMenuMovimentoEstoqueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popupMenuMovimentoEstoqueActionPerformed
		tools.criarJanelaDialogo("Estoque Opções", janelaMovimentoOpcoesEstoque, this.getSize(), this.getLocation());
    }//GEN-LAST:event_popupMenuMovimentoEstoqueActionPerformed

    private void popupMenuRelatoriosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popupMenuRelatoriosActionPerformed
		tools.criarJanelaDialogo("Relatório de Estque", janelaRelatorioEstoque, this.getSize(), this.getLocation());
    }//GEN-LAST:event_popupMenuRelatoriosActionPerformed

    private void popupMenuSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popupMenuSairActionPerformed
		System.exit(0);
    }//GEN-LAST:event_popupMenuSairActionPerformed

    private void botaoBarraFerramentasProdutosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoBarraFerramentasProdutosActionPerformed
		tools.criarJanelaDialogo("Cadastro de Produtos", janelaCadastroProdutos, this.getSize(), this.getLocation());
    }//GEN-LAST:event_botaoBarraFerramentasProdutosActionPerformed

    private void botaoBarraFerramentasUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoBarraFerramentasUsuariosActionPerformed
		tools.criarJanelaDialogo("Cadastro de Usuários", janelaCadastroUsuarios, this.getSize(), this.getLocation());
    }//GEN-LAST:event_botaoBarraFerramentasUsuariosActionPerformed

    private void botaoBarraFerramentasMovimentoEstoqueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoBarraFerramentasMovimentoEstoqueActionPerformed
		tools.criarJanelaDialogo("Estoque Opções", janelaMovimentoOpcoesEstoque, this.getSize(), this.getLocation());
    }//GEN-LAST:event_botaoBarraFerramentasMovimentoEstoqueActionPerformed

    private void botaoBarraFerramentasRelatorioEstoqueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoBarraFerramentasRelatorioEstoqueActionPerformed
		tools.criarJanelaDialogo("Relatório de Estque", janelaRelatorioEstoque, this.getSize(), this.getLocation());
    }//GEN-LAST:event_botaoBarraFerramentasRelatorioEstoqueActionPerformed

    private void botaoBarraFerramentasSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoBarraFerramentasSairActionPerformed
		System.exit(0);
    }//GEN-LAST:event_botaoBarraFerramentasSairActionPerformed

    private void rotuloJanelaPrincipalLogoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rotuloJanelaPrincipalLogoMouseClicked
		if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {
			tools.criarJanelaDialogo("Sobre", janelaSobre, this.getSize(), this.getLocation());
		}
    }//GEN-LAST:event_rotuloJanelaPrincipalLogoMouseClicked

    private void janelaCadastroProdutosWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_janelaCadastroProdutosWindowGainedFocus
		this.listaProdutos = new Dados().listAllProducts(false);

		this.registroAtual = 0;
		registrosCadastroProdutos();

		this.rotuloBarraStatusNomeAtividade.setText("Cadastro de Produto");
		this.setVisible(true);
		this.campoTextoCadastroProdutosDescricao.grabFocus();
    }//GEN-LAST:event_janelaCadastroProdutosWindowGainedFocus

    private void campoTextoCadastroProdutosDescricaoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoTextoCadastroProdutosDescricaoFocusGained
		this.campoTextoCadastroProdutosDescricao.selectAll();
    }//GEN-LAST:event_campoTextoCadastroProdutosDescricaoFocusGained

    private void campoTextoCadastroProdutosUnidadeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoTextoCadastroProdutosUnidadeFocusGained
		this.campoTextoCadastroProdutosUnidade.selectAll();
    }//GEN-LAST:event_campoTextoCadastroProdutosUnidadeFocusGained

    private void campoTextoFormatadoCadastroProdutosValorUnitarioFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoTextoFormatadoCadastroProdutosValorUnitarioFocusGained
		this.campoTextoFormatadoCadastroProdutosValorUnitario.selectAll();
    }//GEN-LAST:event_campoTextoFormatadoCadastroProdutosValorUnitarioFocusGained

    private void campoTextoCadastroProdutosObsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoTextoCadastroProdutosObsFocusGained
		this.campoTextoCadastroProdutosObs.selectAll();
    }//GEN-LAST:event_campoTextoCadastroProdutosObsFocusGained

    private void campoTextoCadastroProdutosDescricaoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoTextoCadastroProdutosDescricaoFocusLost
		this.campoTextoCadastroProdutosDescricao
			= tools.verificaQuantidadeCaracteres(campoTextoCadastroProdutosDescricao, 30, "Descrição");
    }//GEN-LAST:event_campoTextoCadastroProdutosDescricaoFocusLost

    private void campoTextoCadastroProdutosUnidadeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoTextoCadastroProdutosUnidadeFocusLost
		this.campoTextoCadastroProdutosUnidade
			= tools.verificaQuantidadeCaracteres(campoTextoCadastroProdutosUnidade, 10, "Unidade");
    }//GEN-LAST:event_campoTextoCadastroProdutosUnidadeFocusLost

    private void campoTextoCadastroProdutosObsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoTextoCadastroProdutosObsFocusLost
		this.campoTextoCadastroProdutosObs
			= tools.verificaQuantidadeCaracteres(campoTextoCadastroProdutosObs, 50, "Obs");
    }//GEN-LAST:event_campoTextoCadastroProdutosObsFocusLost

    private void campoTextoFormatadoCadastroProdutosValorUnitarioFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoTextoFormatadoCadastroProdutosValorUnitarioFocusLost
		if (this.campoTextoFormatadoCadastroProdutosValorUnitario.getText().isEmpty()) {
			this.campoTextoFormatadoCadastroProdutosValorUnitario = tools.verificaValorMaximoNumerico(
				campoTextoFormatadoCadastroProdutosValorUnitario, 99999.99, "Valor Unitário");
		}
    }//GEN-LAST:event_campoTextoFormatadoCadastroProdutosValorUnitarioFocusLost

    private void janelaCadastroUsuariosWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_janelaCadastroUsuariosWindowGainedFocus
		this.listaUsuarios = new Dados().listAllUsers(false);

		this.registroAtual = 0;
		registrosCadastroUsuarios();

		this.rotuloBarraStatusNomeAtividade.setText("Cadastro de Usuário");
		this.setVisible(true);
		this.campoTextoCadastroUsuariosLogin.grabFocus();
    }//GEN-LAST:event_janelaCadastroUsuariosWindowGainedFocus

    private void campoTextoCadastroUsuariosLoginFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoTextoCadastroUsuariosLoginFocusGained
		this.campoTextoCadastroUsuariosLogin.selectAll();
    }//GEN-LAST:event_campoTextoCadastroUsuariosLoginFocusGained

    private void campoTextoCadastroUsuariosLoginFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoTextoCadastroUsuariosLoginFocusLost
		if (!this.campoTextoCadastroUsuariosLogin.getText().isBlank()) {
			this.campoTextoCadastroUsuariosLogin
				= tools.verificaQuantidadeCaracteres(campoTextoCadastroUsuariosLogin, 10, "Login");
		}
    }//GEN-LAST:event_campoTextoCadastroUsuariosLoginFocusLost

    private void campoSenhaCadastroUsuariosNovaSenhaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoSenhaCadastroUsuariosNovaSenhaFocusGained
		this.campoSenhaCadastroUsuariosNovaSenha.selectAll();
    }//GEN-LAST:event_campoSenhaCadastroUsuariosNovaSenhaFocusGained

    private void campoSenhaCadastroUsuariosNovaSenhaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoSenhaCadastroUsuariosNovaSenhaFocusLost
		if (this.campoSenhaCadastroUsuariosNovaSenha.getPassword().length > 0) {
			this.campoSenhaCadastroUsuariosNovaSenha
				= tools.verificaQuantidadeCaracteres(campoSenhaCadastroUsuariosNovaSenha, 10, "Nova Senha");
		}
    }//GEN-LAST:event_campoSenhaCadastroUsuariosNovaSenhaFocusLost

    private void campoSenhaCadastroUsuariosRepetirSenhaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoSenhaCadastroUsuariosRepetirSenhaFocusGained
		this.campoSenhaCadastroUsuariosRepetirSenha.selectAll();
    }//GEN-LAST:event_campoSenhaCadastroUsuariosRepetirSenhaFocusGained

    private void campoSenhaCadastroUsuariosRepetirSenhaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoSenhaCadastroUsuariosRepetirSenhaFocusLost
		if (this.campoSenhaCadastroUsuariosRepetirSenha.getPassword().length > 0) {
			this.campoSenhaCadastroUsuariosRepetirSenha
				= tools.verificaQuantidadeCaracteres(campoSenhaCadastroUsuariosRepetirSenha, 10, "Repitir Senha");
			this.confereNovaSenha();
		}
    }//GEN-LAST:event_campoSenhaCadastroUsuariosRepetirSenhaFocusLost

    private void janelaMovimentoOpcoesEstoqueWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_janelaMovimentoOpcoesEstoqueWindowGainedFocus
		this.rotuloBarraStatusNomeAtividade.setText("Opções de Estoque");
		this.reorganizarComponentes();
		this.setVisible(true);
		this.botaoRadioMovimentoOpcoesEstoqueEntrada.isSelected();
    }//GEN-LAST:event_janelaMovimentoOpcoesEstoqueWindowGainedFocus

    private void botaoMovimentoOpcoesEstoqueSelecionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoMovimentoOpcoesEstoqueSelecionarActionPerformed
		this.movimentoOpcoesEstoque();
    }//GEN-LAST:event_botaoMovimentoOpcoesEstoqueSelecionarActionPerformed

    private void campoTextoMovimentoEstoqueQuantidadeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoTextoMovimentoEstoqueQuantidadeFocusGained
		this.campoTextoMovimentoEstoqueQuantidade.selectAll();
    }//GEN-LAST:event_campoTextoMovimentoEstoqueQuantidadeFocusGained

    private void campoTextoMovimentoEstoqueQuantidadeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoTextoMovimentoEstoqueQuantidadeFocusLost
		tools.verificaValorMaximoNumerico(campoTextoMovimentoEstoqueQuantidade, 999998, "Quantidade");
    }//GEN-LAST:event_campoTextoMovimentoEstoqueQuantidadeFocusLost

    private void botaoMovimentoEstoqueAbrirTabelaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoMovimentoEstoqueAbrirTabelaActionPerformed
		Dados produtos = new Dados();
		String[][] encontrados = produtos.listAllProducts(true);
		atualizaTabelaJanelaPesquisaProdutos(encontrados);
		tools.criarJanelaDialogo("Selecione um Produto",
			janelaMovimentoEstoqueSelecionaProdutoTabela, this.getSize(), this.getLocation());
    }//GEN-LAST:event_botaoMovimentoEstoqueAbrirTabelaActionPerformed

    private void janelaMovimentoEstoqueSelecionaProdutoTabelaWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_janelaMovimentoEstoqueSelecionaProdutoTabelaWindowGainedFocus
		this.rotuloBarraStatusNomeAtividade.setText("Selecionando um produto no estoque");
		this.setVisible(true);
    }//GEN-LAST:event_janelaMovimentoEstoqueSelecionaProdutoTabelaWindowGainedFocus

    private void janelaMovimentoEstoqueWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_janelaMovimentoEstoqueWindowGainedFocus
//		this.movimentoOpcoesEstoque();
		this.setVisible(true);
    }//GEN-LAST:event_janelaMovimentoEstoqueWindowGainedFocus

    private void botaoSobreOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoSobreOkActionPerformed
		this.janelaSobre.dispose();
    }//GEN-LAST:event_botaoSobreOkActionPerformed

    private void janelaSobreWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_janelaSobreWindowGainedFocus
		this.rotuloBarraStatusNomeAtividade.setText("Informações Sobre o Sistema");
    }//GEN-LAST:event_janelaSobreWindowGainedFocus

    private void botaoEstiloAplicarEstiloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoEstiloAplicarEstiloActionPerformed
		try {
			if (botaoRadioEstiloOpcaoMetal.isSelected()) {
				setLookAndFeel("Metal");
			} else if (botaoRadioEstiloOpcaoNimbus.isSelected()) {
				setLookAndFeel("Nimbus");
			} else if (botaoRadioEstiloOpcaoMotif.isSelected()) {
				setLookAndFeel("CDE/Motif");
			} else if (botaoRadioEstiloOpcaoWindows.isSelected()) {
				setLookAndFeel("Windows");
			} else if (botaoRadioEstiloOpcaoWindowsClassic.isSelected()) {
				setLookAndFeel("Windows Classic");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.janelaEstilo.dispose();
    }//GEN-LAST:event_botaoEstiloAplicarEstiloActionPerformed

    private void janelaEstiloWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_janelaEstiloWindowGainedFocus
		this.rotuloBarraStatusNomeAtividade.setText("Modificar Estilos das Janelas");
		this.setVisible(true);
    }//GEN-LAST:event_janelaEstiloWindowGainedFocus

    private void janelaBackupWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_janelaBackupWindowGainedFocus
		this.rotuloBarraStatusNomeAtividade.setText("Opções de Backup");
		this.setVisible(true);
		this.botaoRadioBackupBackup.setSelected(true);
    }//GEN-LAST:event_janelaBackupWindowGainedFocus

    private void botaoBackupArquivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoBackupArquivoActionPerformed
		this.janelaProcurarArquivoBackup.showDialog(janelaBackup, "Carregar");
    }//GEN-LAST:event_botaoBackupArquivoActionPerformed

    private void janelaProcurarArquivoBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_janelaProcurarArquivoBackupActionPerformed
		File file = this.janelaProcurarArquivoBackup.getSelectedFile();
		if (file != null) {
			rotuloBackupDestino.setText(file.getAbsolutePath());
		}
    }//GEN-LAST:event_janelaProcurarArquivoBackupActionPerformed

    private void botaoBackupConfirmarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoBackupConfirmarActionPerformed
		if (botaoRadioBackupRestaurar.isSelected()) {
			if (tools.BackupRestaurar(rotuloBackupDestino.getText().trim()) == 0) {
				JOptionPane.showMessageDialog(null, "Restauração realizada com sucesso!",
					"RESTAURAÇÂO CONCLUÍDA", JOptionPane.INFORMATION_MESSAGE);
				this.janelaBackup.dispose();
			} else {
				JOptionPane.showMessageDialog(null, "Erro ao restaurar os dados",
					"ERRO AO RESTAURAR", JOptionPane.ERROR_MESSAGE);
			}
		} else if (botaoRadioBackupBackup.isSelected()) {
			if (tools.BackupEfetuar(rotuloBackupDestino.getText().trim()) == 0) {
				JOptionPane.showMessageDialog(null, "Backup realizado com sucesso!",
					"BACKUP CONCLUÍDO", JOptionPane.INFORMATION_MESSAGE);
				this.janelaBackup.dispose();
			} else {
				JOptionPane.showMessageDialog(null, "Erro no backup dos dados",
					"ERRO NO BACKUP", JOptionPane.ERROR_MESSAGE);
			}
		}
    }//GEN-LAST:event_botaoBackupConfirmarActionPerformed

    private void janelaLoginWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_janelaLoginWindowGainedFocus
		this.rotuloBarraStatusNomeAtividade.setText("Realizando o Login");
		this.setVisible(true);
		this.campoTextoLoginLogin.grabFocus();
    }//GEN-LAST:event_janelaLoginWindowGainedFocus

    private void campoTextoLoginLoginFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoTextoLoginLoginFocusGained
		this.campoTextoLoginLogin.selectAll();
    }//GEN-LAST:event_campoTextoLoginLoginFocusGained

    private void campoSenhaLoginSenhaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoSenhaLoginSenhaFocusGained
		this.campoSenhaLoginSenha.selectAll();
    }//GEN-LAST:event_campoSenhaLoginSenhaFocusGained

    private void botaoLoginEntrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoLoginEntrarActionPerformed
		confirmaAcessoSistema(this.campoTextoLoginLogin.getText().trim(),
			new String(this.campoSenhaLoginSenha.getPassword()));
		this.janelaBackup.dispose();
    }//GEN-LAST:event_botaoLoginEntrarActionPerformed

    private void botaoLoginSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoLoginSairActionPerformed
		System.exit(0);
    }//GEN-LAST:event_botaoLoginSairActionPerformed

    private void campoTextoLoginLoginKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoTextoLoginLoginKeyPressed
		if (evt.getExtendedKeyCode() == KeyEvent.VK_ENTER) {
			confirmaAcessoSistema(this.campoTextoLoginLogin.getText().trim(),
				new String(this.campoSenhaLoginSenha.getPassword()));
		} else if (evt.getExtendedKeyCode() == KeyEvent.VK_ESCAPE) {
			System.exit(0);
		}
    }//GEN-LAST:event_campoTextoLoginLoginKeyPressed

    private void campoSenhaLoginSenhaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoSenhaLoginSenhaKeyPressed
		if (evt.getExtendedKeyCode() == KeyEvent.VK_ENTER) {
			confirmaAcessoSistema(this.campoTextoLoginLogin.getText().trim(),
				new String(this.campoSenhaLoginSenha.getPassword()));
		} else if (evt.getExtendedKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
			System.exit(0);
		}
    }//GEN-LAST:event_campoSenhaLoginSenhaKeyPressed

    private void botaoCadastroProdutosPrimeiroRegistroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoCadastroProdutosPrimeiroRegistroActionPerformed
		this.registroAtual = 0;
		registrosCadastroProdutos();
    }//GEN-LAST:event_botaoCadastroProdutosPrimeiroRegistroActionPerformed

    private void botaoCadastroProdutosUltimoRegistroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoCadastroProdutosUltimoRegistroActionPerformed
		this.registroAtual = this.listaProdutos.length - 1;
		registrosCadastroProdutos();
    }//GEN-LAST:event_botaoCadastroProdutosUltimoRegistroActionPerformed

    private void botaoCadastroProdutosRegistroAnteriorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoCadastroProdutosRegistroAnteriorActionPerformed
		this.registroAtual--;
		if (this.registroAtual < 0) {
			this.registroAtual = 0;
		}
		registrosCadastroProdutos();
    }//GEN-LAST:event_botaoCadastroProdutosRegistroAnteriorActionPerformed

    private void botaoCadastroProdutosProximoRegistroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoCadastroProdutosProximoRegistroActionPerformed
		this.registroAtual++;
		int ultimo = this.listaProdutos.length - 1;
		if (this.registroAtual > ultimo) {
			this.registroAtual = ultimo;
		}
		registrosCadastroProdutos();
    }//GEN-LAST:event_botaoCadastroProdutosProximoRegistroActionPerformed

    private void botaoCadastroProdutosNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoCadastroProdutosNovoActionPerformed
		reorganizarComponentes();
		this.novoRegistroCadastro = true;
		Dados dados = new Dados();
		dados.refreshTableProducts();
		this.campoTextoCadastroProdutosCodigo.setText(String.format("%04d", dados.nextIdProduct()));
		this.campoTextoFormatadoCadastroProdutosValorUnitario.setText("0,00");
		this.campoTextoCadastroProdutosDescricao.grabFocus();
    }//GEN-LAST:event_botaoCadastroProdutosNovoActionPerformed

    private void botaoCadastroProdutosSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoCadastroProdutosSalvarActionPerformed
		if (verificaCadastroProduto() != false) {
			Dados dados = new Dados();
			Produtos produto = new Produtos(
				Integer.parseInt(this.campoTextoCadastroProdutosCodigo.getText()),
				this.campoTextoCadastroProdutosDescricao.getText(),
				this.campoTextoCadastroProdutosUnidade.getText(),
				(this.campoTextoFormatadoCadastroProdutosValorUnitario.getText().isBlank()) ? 0.0
				: Double.parseDouble(this.campoTextoFormatadoCadastroProdutosValorUnitario.getText().replace(",", ".")),
				this.campoTextoCadastroProdutosObs.getText());
			if (this.novoRegistroCadastro) {
				dados.insertProduct(produto);
			} else {
				dados.updateProduct(produto);
			}
			JOptionPane.showMessageDialog(null,
				"Dados de produto gravados com sucesso!",
				"REGISTRO DE PRODUTO", JOptionPane.INFORMATION_MESSAGE);
		}
    }//GEN-LAST:event_botaoCadastroProdutosSalvarActionPerformed

    private void botaoCadastroProdutosExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoCadastroProdutosExcluirActionPerformed
		if (this.campoTextoCadastroProdutosDescricao.getText().equals("")
			|| this.campoTextoCadastroProdutosUnidade.getText().equals("")) {
			JOptionPane.showMessageDialog(null,
				"Necessário informar descrição e unidade do produto.",
				"INFORMAR DADOS!", JOptionPane.ERROR_MESSAGE);
		} else {
			int idProduct = Integer.parseInt(this.campoTextoCadastroProdutosCodigo.getText());
			String descProduct = this.campoTextoCadastroProdutosDescricao.getText();
			Dados dados = new Dados();
			int opcao = JOptionPane.showConfirmDialog(null,
				"Ao confirmar, não será possível recuperar os dados do produto:\n"
				+ "id: " + idProduct + ", descrição: " + descProduct + "?",
				"DESEJA EXCLUIR O PRODUTO?", JOptionPane.YES_NO_OPTION);
			if (opcao == JOptionPane.YES_OPTION) {
				dados.deleteProductById(idProduct);
				JOptionPane.showMessageDialog(null,
					"O produto foi excluído.",
					"PRODUTO EXCLUÌDO!", JOptionPane.INFORMATION_MESSAGE);
			}
			if (opcao == JOptionPane.NO_OPTION) {
				JOptionPane.showMessageDialog(null,
					"O produto não foi excluído.",
					"PRODUTO MANTIDO!", JOptionPane.INFORMATION_MESSAGE);
			}
		}
    }//GEN-LAST:event_botaoCadastroProdutosExcluirActionPerformed

    private void botaoCadastroUsuariosPrimeiroRegistroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoCadastroUsuariosPrimeiroRegistroActionPerformed
		this.registroAtual = 0;
		registrosCadastroUsuarios();
    }//GEN-LAST:event_botaoCadastroUsuariosPrimeiroRegistroActionPerformed

    private void botaoCadastroUsuariosUltimoRegistroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoCadastroUsuariosUltimoRegistroActionPerformed
		this.registroAtual = this.listaUsuarios.length - 1;
		registrosCadastroUsuarios();
    }//GEN-LAST:event_botaoCadastroUsuariosUltimoRegistroActionPerformed

    private void botaoCadastroUsuariosRegistroAnteriorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoCadastroUsuariosRegistroAnteriorActionPerformed
		this.registroAtual--;
		if (this.registroAtual < 0) {
			this.registroAtual = 0;
		}
		registrosCadastroUsuarios();
    }//GEN-LAST:event_botaoCadastroUsuariosRegistroAnteriorActionPerformed

    private void botaoCadastroUsuariosProximoRegistroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoCadastroUsuariosProximoRegistroActionPerformed
		this.registroAtual++;
		int ultimo = this.listaUsuarios.length - 1;
		if (this.registroAtual > ultimo) {
			this.registroAtual = ultimo;
		}
		registrosCadastroUsuarios();
    }//GEN-LAST:event_botaoCadastroUsuariosProximoRegistroActionPerformed

    private void botaoCadastroUsuariosNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoCadastroUsuariosNovoActionPerformed
		reorganizarComponentes();
		this.novoRegistroCadastro = true;
		Dados usuarios = new Dados();
		usuarios.refreshTableUsers();
		this.campoTextoCadastroUsuariosCodigo.setText(String.format("%04d", usuarios.nextIdUser()));
		this.campoTextoCadastroUsuariosLogin.grabFocus();
    }//GEN-LAST:event_botaoCadastroUsuariosNovoActionPerformed

    private void botaoCadastroUsuariosSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoCadastroUsuariosSalvarActionPerformed
		if (verificaCadastroUsuario()) {
			Dados dados = new Dados();
			Usuarios usuario = new Usuarios(
				Integer.parseInt(this.campoTextoCadastroUsuariosCodigo.getText()),
				this.campoTextoCadastroUsuariosLogin.getText(),
				this.botaoRadioCadastroUsuariosAdministrador.isSelected() ? 0 : 1,
				new String(this.campoSenhaCadastroUsuariosNovaSenha.getPassword()));
			if (this.novoRegistroCadastro) {
				dados.insertUser(usuario);
			} else {
				dados.updateUser(usuario);
			}
			JOptionPane.showMessageDialog(null,
				"Dados de usuário gravados com sucesso!",
				"REGISTRO DE USUARIO", JOptionPane.INFORMATION_MESSAGE);
		}
    }//GEN-LAST:event_botaoCadastroUsuariosSalvarActionPerformed

    private void botaoCadastroUsuariosExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoCadastroUsuariosExcluirActionPerformed
		if (this.campoTextoCadastroUsuariosLogin.getText().equals("")) {
			JOptionPane.showMessageDialog(null,
				"Necessário informar login de usuário.",
				"INFORMAR DADOS!", JOptionPane.ERROR_MESSAGE);
		} else {
			int idUser = Integer.parseInt(this.campoTextoCadastroUsuariosCodigo.getText());
			String loginUser = this.campoTextoCadastroUsuariosLogin.getText();
			Dados usuario = new Dados();
			int opcao = JOptionPane.showConfirmDialog(null,
				"Ao confirmar, não será possível recuperar os dados do usuário:\n"
				+ "id: " + idUser + ", nome: " + loginUser + "?",
				"DESEJA EXCLUIR O USUÁRIO?", JOptionPane.YES_NO_OPTION);
			if (opcao == JOptionPane.YES_OPTION) {
				usuario.deleteUserById(idUser);
				JOptionPane.showMessageDialog(null,
					"O usuário foi excluído.",
					"USUÁRIO EXCLUÌDO!", JOptionPane.INFORMATION_MESSAGE);
			}
			if (opcao == JOptionPane.NO_OPTION) {
				JOptionPane.showMessageDialog(null,
					"O usuário não foi excluído.",
					"USUÁRIO MANTIDO!", JOptionPane.INFORMATION_MESSAGE);
			}
		}
    }//GEN-LAST:event_botaoCadastroUsuariosExcluirActionPerformed

    private void janelaMovimentoEstoqueWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_janelaMovimentoEstoqueWindowOpened
		Dados produtos = new Dados();
		String[][] itens = produtos.listAllProducts(true);
		this.caixaCombinacaoMovimentoEstoqueDescricao.removeAllItems();
		for (int i = 0; i < itens.length; i++) {
			this.caixaCombinacaoMovimentoEstoqueDescricao.addItem(itens[i][1]);
		}
		this.caixaCombinacaoMovimentoEstoqueDescricao.setSelectedIndex(0);
		this.campoTextoMovimentoEstoquePesquisa.grabFocus();
    }//GEN-LAST:event_janelaMovimentoEstoqueWindowOpened

    private void botaoRadioMovimentoEstoqueCodigoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botaoRadioMovimentoEstoqueCodigoMouseClicked
		this.campoTextoMovimentoEstoquePesquisa.grabFocus();
    }//GEN-LAST:event_botaoRadioMovimentoEstoqueCodigoMouseClicked

    private void botaoRadioMovimentoEstoqueDescricaoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botaoRadioMovimentoEstoqueDescricaoMouseClicked
		this.campoTextoMovimentoEstoquePesquisa.grabFocus();
    }//GEN-LAST:event_botaoRadioMovimentoEstoqueDescricaoMouseClicked

    private void campoTextoMovimentoEstoquePesquisaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoTextoMovimentoEstoquePesquisaActionPerformed
		campoTextoJanelaPesquisaProdutos(this.campoTextoMovimentoEstoquePesquisa.getText().trim());
    }//GEN-LAST:event_campoTextoMovimentoEstoquePesquisaActionPerformed

    private void botaoMovimentoEstoquePesquisaOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoMovimentoEstoquePesquisaOkActionPerformed
		campoTextoJanelaPesquisaProdutos(this.campoTextoMovimentoEstoquePesquisa.getText().trim());
    }//GEN-LAST:event_botaoMovimentoEstoquePesquisaOkActionPerformed

    private void tabelaMovimentaEstoqueSelecionaProdutoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelaMovimentaEstoqueSelecionaProdutoMouseClicked
		this.clique = true;
		String pesquisaLinha = (String) String.valueOf(
			this.tabelaMovimentaEstoqueSelecionaProduto.getValueAt(
				this.tabelaMovimentaEstoqueSelecionaProduto.getSelectedRow(), 0));
		this.codigoProdutoSelecionado = Integer.parseInt(pesquisaLinha);
		if (evt.getClickCount() == 2) {
			Dados produto = new Dados();
			String[] encontrado = produto.findProductById(this.codigoProdutoSelecionado);
			entregarResultadoPesquisa(encontrado);
			this.clique = false;
			this.janelaMovimentoEstoqueSelecionaProdutoTabela.dispose();
			this.campoTextoMovimentoEstoqueQuantidade.grabFocus();
		}
    }//GEN-LAST:event_tabelaMovimentaEstoqueSelecionaProdutoMouseClicked

    private void botaoMovimentoEstoqueSelecionaProdutoConfirmarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoMovimentoEstoqueSelecionaProdutoConfirmarActionPerformed
		if (this.clique == true) {
			this.campoTextoMovimentoEstoquePesquisa.setText("");
			Dados produto = new Dados();
			String[] encontrado = produto.findProductById(this.codigoProdutoSelecionado);
			entregarResultadoPesquisa(encontrado);
			this.clique = false;
			this.janelaMovimentoEstoqueSelecionaProdutoTabela.dispose();
			this.campoTextoMovimentoEstoqueQuantidade.grabFocus();
		} else {
			JOptionPane.showMessageDialog(null, "Nenhum item selecionado!",
				"PESQUISA PRODUTO", JOptionPane.ERROR_MESSAGE);
		}
    }//GEN-LAST:event_botaoMovimentoEstoqueSelecionaProdutoConfirmarActionPerformed

    private void janelaMovimentoEstoqueSelecionaProdutoTabelaWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_janelaMovimentoEstoqueSelecionaProdutoTabelaWindowClosing
		this.clique = false;
    }//GEN-LAST:event_janelaMovimentoEstoqueSelecionaProdutoTabelaWindowClosing

    private void caixaCombinacaoMovimentoEstoqueDescricaoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_caixaCombinacaoMovimentoEstoqueDescricaoMouseClicked
		this.clique = true;
		if (this.registroAtual == null) {
			this.registroAtual = 1;
		}
		this.registroAtual++;
		if (this.registroAtual > 1) {
			this.registroAtual = 1;
		}
    }//GEN-LAST:event_caixaCombinacaoMovimentoEstoqueDescricaoMouseClicked

    private void caixaCombinacaoMovimentoEstoqueDescricaoPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_caixaCombinacaoMovimentoEstoqueDescricaoPopupMenuWillBecomeVisible
		this.clique = true;
		this.registroAtual = 1;
    }//GEN-LAST:event_caixaCombinacaoMovimentoEstoqueDescricaoPopupMenuWillBecomeVisible

    private void caixaCombinacaoMovimentoEstoqueDescricaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_caixaCombinacaoMovimentoEstoqueDescricaoActionPerformed
		if (this.clique) {
			this.registroAtual = 0;
			this.clique = false;
			respostaCaixaCombinacaoMovimentoEstoqueDescrição();
		}
    }//GEN-LAST:event_caixaCombinacaoMovimentoEstoqueDescricaoActionPerformed

    private void caixaCombinacaoMovimentoEstoqueDescricaoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_caixaCombinacaoMovimentoEstoqueDescricaoKeyPressed
		if (this.registroAtual == null || this.registroAtual > 1) {
			this.registroAtual = 0;
			if ((evt.getExtendedKeyCode() == KeyEvent.VK_SPACE)
				|| (evt.getExtendedKeyCode() == KeyEvent.VK_ENTER)) {
				this.registroAtual++;
				if ((evt.getExtendedKeyCode() == KeyEvent.VK_SPACE) && this.registroAtual == 2) {
					this.registroAtual = 0;
					respostaCaixaCombinacaoMovimentoEstoqueDescrição();
				}
				this.clique = true;
			} else {
				this.clique = false;
			}
		}
    }//GEN-LAST:event_caixaCombinacaoMovimentoEstoqueDescricaoKeyPressed

    private void botaoMovimentaEstoqueOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoMovimentaEstoqueOkActionPerformed
		confirmaMovimentoEstoque();
    }//GEN-LAST:event_botaoMovimentaEstoqueOkActionPerformed

    private void campoTextoMovimentoEstoqueQuantidadeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoTextoMovimentoEstoqueQuantidadeActionPerformed
		confirmaMovimentoEstoque();
    }//GEN-LAST:event_campoTextoMovimentoEstoqueQuantidadeActionPerformed

    private void campoTextoMovimentoEstoquePesquisaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoTextoMovimentoEstoquePesquisaKeyPressed
		if (evt.getExtendedKeyCode() == KeyEvent.VK_ESCAPE) {
			this.janelaMovimentoEstoque.dispose();
		}
    }//GEN-LAST:event_campoTextoMovimentoEstoquePesquisaKeyPressed

    private void campoTextoMovimentoEstoqueQuantidadeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoTextoMovimentoEstoqueQuantidadeKeyPressed
		if (evt.getExtendedKeyCode() == KeyEvent.VK_ESCAPE) {
			this.janelaMovimentoEstoque.dispose();
		}
    }//GEN-LAST:event_campoTextoMovimentoEstoqueQuantidadeKeyPressed

    private void janelaRelatorioEstoqueWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_janelaRelatorioEstoqueWindowGainedFocus
		this.setVisible(true);
		Dados dados = new Dados();
		String[][] produtos = dados.listAllProducts(false);
		ajustarTabelaJanelaRelatorioEstoque(produtos);
    }//GEN-LAST:event_janelaRelatorioEstoqueWindowGainedFocus

    private void botaoRelatorioEstoqueImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoRelatorioEstoqueImprimirActionPerformed
		String login = this.rotuloBarraStatusNomeUsuario.getText();
		Dados dados = new Dados();
		String[] dadosUsuario = dados.findUserByLogin(login);
		int nivel = Integer.parseInt(dadosUsuario[2]);
		if (nivel == 0) {
			this.janelaRelatorioEstoque.dispose();
			Relatorios relatorio = new Relatorios();
			relatorio.imprimirRelatorioEstoque();
		} else {
			JOptionPane.showMessageDialog(null, "Você não tem permissão para isso!",
				"Relatório", JOptionPane.ERROR_MESSAGE);
		}
    }//GEN-LAST:event_botaoRelatorioEstoqueImprimirActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barraFerramentasJanelaPrincipal;
    private javax.swing.JMenuBar barraMenuJanelaPrincipal;
    private javax.swing.JButton botaoBackupArquivo;
    private javax.swing.JButton botaoBackupConfirmar;
    private javax.swing.JButton botaoBarraFerramentasMovimentoEstoque;
    private javax.swing.JButton botaoBarraFerramentasProdutos;
    private javax.swing.JButton botaoBarraFerramentasRelatorioEstoque;
    private javax.swing.JButton botaoBarraFerramentasSair;
    private javax.swing.JButton botaoBarraFerramentasUsuarios;
    private javax.swing.JButton botaoCadastroProdutosExcluir;
    private javax.swing.JButton botaoCadastroProdutosNovo;
    private javax.swing.JButton botaoCadastroProdutosPrimeiroRegistro;
    private javax.swing.JButton botaoCadastroProdutosProximoRegistro;
    private javax.swing.JButton botaoCadastroProdutosRegistroAnterior;
    private javax.swing.JButton botaoCadastroProdutosSalvar;
    private javax.swing.JButton botaoCadastroProdutosUltimoRegistro;
    private javax.swing.JButton botaoCadastroUsuariosExcluir;
    private javax.swing.JButton botaoCadastroUsuariosNovo;
    private javax.swing.JButton botaoCadastroUsuariosPrimeiroRegistro;
    private javax.swing.JButton botaoCadastroUsuariosProximoRegistro;
    private javax.swing.JButton botaoCadastroUsuariosRegistroAnterior;
    private javax.swing.JButton botaoCadastroUsuariosSalvar;
    private javax.swing.JButton botaoCadastroUsuariosUltimoRegistro;
    private javax.swing.JButton botaoEstiloAplicarEstilo;
    private javax.swing.JButton botaoLoginEntrar;
    private javax.swing.JButton botaoLoginSair;
    private javax.swing.JButton botaoMovimentaEstoqueOk;
    private javax.swing.JButton botaoMovimentoEstoqueAbrirTabela;
    private javax.swing.JButton botaoMovimentoEstoquePesquisaOk;
    private javax.swing.JButton botaoMovimentoEstoqueSelecionaProdutoConfirmar;
    private javax.swing.JButton botaoMovimentoOpcoesEstoqueSelecionar;
    private javax.swing.JRadioButton botaoRadioBackupBackup;
    private javax.swing.JRadioButton botaoRadioBackupRestaurar;
    private javax.swing.JRadioButton botaoRadioCadastroUsuariosAdministrador;
    private javax.swing.JRadioButton botaoRadioCadastroUsuariosComum;
    private javax.swing.JRadioButton botaoRadioEstiloOpcaoMetal;
    private javax.swing.JRadioButton botaoRadioEstiloOpcaoMotif;
    private javax.swing.JRadioButton botaoRadioEstiloOpcaoNimbus;
    private javax.swing.JRadioButton botaoRadioEstiloOpcaoWindows;
    private javax.swing.JRadioButton botaoRadioEstiloOpcaoWindowsClassic;
    private javax.swing.JRadioButton botaoRadioMovimentoEstoqueCodigo;
    private javax.swing.JRadioButton botaoRadioMovimentoEstoqueDescricao;
    private javax.swing.JRadioButton botaoRadioMovimentoOpcoesEstoqueEntrada;
    private javax.swing.JRadioButton botaoRadioMovimentoOpcoesEstoqueRetirada;
    private javax.swing.JButton botaoRelatorioEstoqueImprimir;
    private javax.swing.JButton botaoSobreOk;
    private javax.swing.JComboBox<String> caixaCombinacaoMovimentoEstoqueDescricao;
    private javax.swing.JPasswordField campoSenhaCadastroUsuariosNovaSenha;
    private javax.swing.JPasswordField campoSenhaCadastroUsuariosRepetirSenha;
    private javax.swing.JPasswordField campoSenhaLoginSenha;
    private javax.swing.JTextField campoTextoCadastroProdutosCodigo;
    private javax.swing.JTextField campoTextoCadastroProdutosDescricao;
    private javax.swing.JTextField campoTextoCadastroProdutosObs;
    private javax.swing.JTextField campoTextoCadastroProdutosUnidade;
    private javax.swing.JTextField campoTextoCadastroUsuariosCodigo;
    private javax.swing.JTextField campoTextoCadastroUsuariosLogin;
    private javax.swing.JFormattedTextField campoTextoFormatadoCadastroProdutosValorUnitario;
    private javax.swing.JTextField campoTextoFormatadoMovimentoEstoqueValorUnitario;
    private javax.swing.JTextField campoTextoLoginLogin;
    private javax.swing.JTextField campoTextoMovimentoEstoquePesquisa;
    private javax.swing.JTextField campoTextoMovimentoEstoqueQuantidade;
    private javax.swing.JTextField campoTextoMovimentoEstoqueUnid;
    private javax.swing.ButtonGroup grupoBotaoBackup;
    private javax.swing.ButtonGroup grupoBotaoCadastroUsuario;
    private javax.swing.ButtonGroup grupoBotaoEstilo;
    private javax.swing.ButtonGroup grupoBotaoMovimentoEstoquePesquisarPor;
    private javax.swing.ButtonGroup grupoBotaoMovimentoOpcoesEstoque;
    private javax.swing.JMenuItem itemMenuAjudaBackup;
    private javax.swing.JMenuItem itemMenuAjudaEstilo;
    private javax.swing.JMenuItem itemMenuAjudaSair;
    private javax.swing.JMenuItem itemMenuAjudaSobre;
    private javax.swing.JMenuItem itemMenuCadastroProdutos;
    private javax.swing.JMenuItem itemMenuCadastroUsuarios;
    private javax.swing.JMenuItem itemMenuMovimentoEntradas;
    private javax.swing.JMenuItem itemMenuMovimentoRelatorio;
    private javax.swing.JMenuItem itemMenuMovimentoRetiradas;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JDialog janelaBackup;
    private javax.swing.JDialog janelaCadastroProdutos;
    private javax.swing.JDialog janelaCadastroUsuarios;
    private javax.swing.JDialog janelaEstilo;
    private javax.swing.JDialog janelaLogin;
    private javax.swing.JDialog janelaMovimentoEstoque;
    private javax.swing.JDialog janelaMovimentoEstoqueSelecionaProdutoTabela;
    private javax.swing.JDialog janelaMovimentoOpcoesEstoque;
    private javax.swing.JFileChooser janelaProcurarArquivoBackup;
    private javax.swing.JDialog janelaRelatorioEstoque;
    private javax.swing.JDialog janelaSobre;
    private javax.swing.JMenu menuJanelaPrincipalAjuda;
    private javax.swing.JMenu menuJanelaPrincipalCadastro;
    private javax.swing.JMenu menuJanelaPrincipalMovimento;
    private javax.swing.JPanel painelBackup;
    private javax.swing.JPanel painelBarraStatusAtividade;
    private javax.swing.JPanel painelBarraStatusJanelaPrincipal;
    private javax.swing.JPanel painelBarraStatusSecaoRelogio;
    private javax.swing.JPanel painelBarraStatusSecaoUsuario;
    private javax.swing.JPanel painelCadastroUsuariosChaves;
    private javax.swing.JPanel painelCadastroUsuariosTipoUsuario;
    private javax.swing.JPanel painelCadastroUsuariosUsuario;
    private javax.swing.JPanel painelEstiloOpcao;
    private javax.swing.JPanel painelLoginLogOn;
    private javax.swing.JPanel painelMovimentoEstoquePesquisarPor;
    private javax.swing.JPanel painelMovimentoEstoqueProcurarProduto;
    private javax.swing.JPanel painelMovimentoEstoqueSelecionaProdutoTabela;
    private javax.swing.JPanel painelMovimentoOpcoesEstoqueEstoque;
    private javax.swing.JPopupMenu popupMenuJanelaPrincipal;
    private javax.swing.JMenuItem popupMenuMovimentoEstoque;
    private javax.swing.JMenuItem popupMenuProdutos;
    private javax.swing.JMenuItem popupMenuRelatorios;
    private javax.swing.JMenuItem popupMenuSair;
    private javax.swing.JMenuItem popupMenuUsuarios;
    private javax.swing.JLabel rotuloBackupDestino;
    private javax.swing.JLabel rotuloBarraStatusAtividade;
    private javax.swing.JLabel rotuloBarraStatusDataSistema;
    private javax.swing.JLabel rotuloBarraStatusHoraSistema;
    private javax.swing.JLabel rotuloBarraStatusNomeAtividade;
    private javax.swing.JLabel rotuloBarraStatusNomeUsuario;
    private javax.swing.JLabel rotuloBarraStatusRelogioSistema;
    private javax.swing.JLabel rotuloBarraStatusUsuario;
    private javax.swing.JLabel rotuloCadastroProdutosCodigo;
    private javax.swing.JLabel rotuloCadastroProdutosDescricao;
    private javax.swing.JLabel rotuloCadastroProdutosObs;
    private javax.swing.JLabel rotuloCadastroProdutosUnidade;
    private javax.swing.JLabel rotuloCadastroProdutosValorUnitario;
    private javax.swing.JLabel rotuloCadastroUsuariosCodigo;
    private javax.swing.JLabel rotuloCadastroUsuariosLogin;
    private javax.swing.JLabel rotuloJanelaPrincipalLogo;
    private javax.swing.JLabel rotuloLoginLogin;
    private javax.swing.JLabel rotuloLoginLogo;
    private javax.swing.JLabel rotuloLoginSenha;
    private javax.swing.JLabel rotuloMovimentoEstoqueDescricao;
    private javax.swing.JLabel rotuloMovimentoEstoquePesquisa;
    private javax.swing.JLabel rotuloMovimentoEstoqueQuantidade;
    private javax.swing.JLabel rotuloMovimentoEstoqueUnid;
    private javax.swing.JLabel rotuloMovimentoEstoqueValorUnitario;
    private javax.swing.JLabel rotuloSenhaCadastroUsuariosNovaSenha;
    private javax.swing.JLabel rotuloSenhaCadastroUsuariosRepetirSenha;
    private javax.swing.JLabel rotuloSobreAutor;
    private javax.swing.JLabel rotuloSobreLogo;
    private javax.swing.JLabel rotuloSobrePrograma;
    private javax.swing.JTable tabelaMovimentaEstoqueSelecionaProduto;
    private javax.swing.JTable tabelaRelatorioEstoque;
    // End of variables declaration//GEN-END:variables
}
