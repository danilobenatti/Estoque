package br.com.estoque.relatorios;

import br.com.estoque.util.JDBC;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

public class Relatorios {

	private Statement instrucao;
	private ResultSet resultado;
	private final Connection conexao;

	public Relatorios() {
		this.conexao = JDBC.receberConexao();
	}

	public void imprimirRelatorioEstoque() {
		try {
			String SQL = "SELECT id, descricao, unidade, quantidade, "
				+ "FORMAT(valorUnitario, 2, 'pt_BR') AS valorUnitario, "
				+ "FORMAT(valorUnitario * quantidade, 2, 'pt_BR') AS valorTotal "
				+ "FROM produtos";
			String relatorio = "src/br/com/estoque/relatorios/reportStorage.jasper";
			String relatorioPdf = "src/br/com/estoque/relatorios/reportStorage.pdf";
			instrucao = conexao.createStatement();
			resultado = instrucao.executeQuery(SQL);
			try {
				JRResultSetDataSource resultSetDataSource
					= new JRResultSetDataSource(resultado);
				JasperPrint impressao
					= JasperFillManager.fillReport(relatorio, null, resultSetDataSource);
				JasperExportManager.exportReportToPdfFile(impressao, relatorioPdf);
				JasperViewer.viewReport(impressao, false);
			} catch (JRException ex) {
				ex.printStackTrace();
			}
		} catch (SQLException ex) {
			ex.getMessage();
		} finally {
			JDBC.fecharConexao(conexao, instrucao, resultado);
		}
	}
}
