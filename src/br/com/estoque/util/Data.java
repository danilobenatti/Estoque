package br.com.estoque.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Data.java
 *
 * @author Romuel Dias de Oliveira - Java Full Teoria e Prática - pg.222
 */
public class Data {

	public static String mes, mesExtenso, dia, ano, diasemana, dataNormal, dataEspecial, hora, hora12, hora24;
	private final Timestamp data;

	public Data() {
		Calendar dataHora = Calendar.getInstance();
		mes = String.format("%tm", dataHora);
		dia = String.format("%td", dataHora);
		ano = String.format("%tY", dataHora);
		dataEspecial = String.format("%s/%s/%s", dia, mes, ano);
		dataNormal = String.format("%tD", dataHora);
		mesExtenso = String.format("%1$tB", dataHora);
		diasemana = String.format("%1$tA", dataHora);
		hora = String.format("%1$tH:%1$tM:%1$S", dataHora);
		hora12 = String.format("%tr", dataHora);
		hora24 = String.format("%tT", dataHora);

		Timestamp timestamp = null;
		try {
			Timestamp agora = new Timestamp(System.currentTimeMillis());
			SimpleDateFormat formatoData = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Calendar calendario = Calendar.getInstance();
			calendario.setTime(formatoData.parse(agora.toString()));
			timestamp = new Timestamp(calendario.getTimeInMillis());
		} catch (ParseException e) {
			System.out.println(e.getLocalizedMessage() + e.getMessage());
		}
		this.data = timestamp;
	}

	public String getData() {
		String dataS = this.data.toString().split(" ")[0];
		return dataS.split("-")[2] + "/" + dataS.split("-")[1] + "/" + dataS.split("-")[0];
	}

	public int getDia() {
		return Integer.parseInt(this.data.toString().split(" ")[0].split("-")[2]);
	}

	public int getMes() {
		return Integer.parseInt(this.data.toString().split(" ")[0].split("-")[1]);
	}

	public int getAno() {
		return Integer.parseInt(this.data.toString().split(" ")[0].split("-")[0]);
	}

	public String getHora() {
		String horaS = this.data.toString().split(" ")[1];
		return horaS.split(":")[0] + ":" + horaS.split(":")[1];
	}

	public String getMesDescricao() {
		String[] meses = {"Janeiro", "Fevereiro", "Março", "Abril",
			"Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
		return meses[this.getMes() - 1];
	}
}
