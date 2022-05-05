package br.com.estoque.util;

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Ferramentas.class
 *
 * @author Romuel Dias de Oliveira - Java Full Teoria e Prática - pg.225
 */
public class Ferramentas {

	public Ferramentas() {
	}

	public void criarJanelaDialogo(String titulo, JDialog jDialog, Dimension size, Point localizacao) {
		Dimension tamanhoDialogo = jDialog.getPreferredSize();
		Dimension tamanhoFrame = size;
		Point loc = localizacao;
		jDialog.setLocation((tamanhoFrame.width - tamanhoDialogo.width) / 2 + loc.x,
				(tamanhoFrame.height - tamanhoDialogo.height) / 2 + loc.y);
		jDialog.setTitle(titulo);
		jDialog.setModal(true);
		jDialog.pack();
		jDialog.setResizable(false);
		jDialog.setVisible(true);
	}

	public static JPasswordField[] limparComponentes(JPasswordField[] jpf) {
		for (JPasswordField jpf1 : jpf) {
			jpf1.setText("");
		}
		return jpf;
	}

	public static JTextField[] limparComponentes(JTextField[] jtf) {
		for (JTextField jtf1 : jtf) {
			jtf1.setText("");
		}
		return jtf;
	}

	public static JComboBox[] limparComponentes(JComboBox[] jcb) {
		for (JComboBox jcb1 : jcb) {
			jcb1.setSelectedIndex(0);
		}
		return jcb;
	}

	public String primeiraLetraMaiuscula(String valor) {
		char[] charArray = valor.toCharArray();
		charArray[0] = Character.toUpperCase(charArray[0]);
		return String.valueOf(charArray);
	}

	public JTextField verificaQuantidadeCaracteres(JTextField objeto,
			int limiteCaracteres, String campo) {
		if (objeto.getText().length() > limiteCaracteres) {
			JOptionPane.showMessageDialog(null, " Não é permitido mais que "
					+ limiteCaracteres + " caracteres para " + campo + "!",
					"EXCESSO DE CARACTERES", JOptionPane.ERROR_MESSAGE);
			objeto.grabFocus();
			objeto.selectAll();
		}
		return objeto;
	}

	public JPasswordField verificaQuantidadeCaracteres(JPasswordField objeto,
			int limiteCaracteres, String campo) {
		if (objeto.getPassword().length > limiteCaracteres) {
			JOptionPane.showMessageDialog(null, " Não é permitido mais que "
					+ limiteCaracteres + " caracteres para " + campo + "!",
					"EXCESSO DE CARACTERES", JOptionPane.ERROR_MESSAGE);
			objeto.grabFocus();
			objeto.selectAll();
		}
		return objeto;
	}

	public JFormattedTextField verificaValorMaximoNumerico(JFormattedTextField objeto,
			double valorMaximo, String campo) {
		String valor = objeto.getText().replace(".", "");
		if (isNumero(valor)) {
			if ((Double.parseDouble(valor.replace(",", ".")) < 0)
					|| (Double.parseDouble(valor.replace(",", ".")) > valorMaximo)) {
				JOptionPane.showMessageDialog(null,
						"Não é permitido um valor menor que 0,00 \nou maior que "
						+ valorMaximo + " para " + campo + "!",
						"VALOR NUMÈRICO INVÁLIDO", JOptionPane.ERROR_MESSAGE);
				objeto.grabFocus();
				objeto.selectAll();
			}
		} else {
			JOptionPane.showMessageDialog(null, "Valor inválido para " + campo + "!");
			objeto.setText("0,00");
			objeto.grabFocus();
			objeto.selectAll();
		}
		return objeto;
	}

	public JTextField verificaValorMaximoNumerico(JTextField objeto,
			int valorMaximo, String campo) {
		String valor = objeto.getText().replace(".", "");
		if (isNumero(valor)) {
			if ((Integer.parseInt(valor.replace(",", ".")) < 0)
					|| (Integer.parseInt(valor.replace(",", ".")) > valorMaximo)) {
				JOptionPane.showMessageDialog(null,
						"Não é permitido um valor menor que 0 \nou maior que "
						+ valorMaximo + " para " + campo + "!",
						"VALOR NUMÈRICO INVÁLIDO", JOptionPane.ERROR_MESSAGE);
				objeto.grabFocus();
				objeto.selectAll();
			}
		} else {
			JOptionPane.showMessageDialog(null, "Valor inválido para " + campo + "!");
			objeto.setText("0,00");
			objeto.grabFocus();
			objeto.selectAll();
		}
		return objeto;
	}

	public boolean isNumero(String numero) {
		return numero.chars().allMatch(Character::isDigit);
	}

	@SuppressWarnings("CallToPrintStackTrace")
	public int BackupEfetuar(String path) {
		String user = "root", password = "123456", database = "estoque";
		path = String.format("mysqldump --user=%s --password=%s %s --result-file=%s",
				user, password, database, path);
		try {
			Process process = Runtime.getRuntime().exec(path);
			return process.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return 1;
	}

	@SuppressWarnings("CallToPrintStackTrace")
	public int BackupRestaurar(String path) {
		String user = "root", password = "123456", database = "estoque";
		path = String.format("cmd.exe /c mysql.exe --user=%s --password=%s -D %s < $s",
				user, password, database, path);
		try {
			Process process = Runtime.getRuntime().exec(path);
			return process.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return 1;
	}
}
