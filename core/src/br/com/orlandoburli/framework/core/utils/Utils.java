package br.com.orlandoburli.framework.core.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public final class Utils {

	public static final String WEBINF_CLASSES_DIRECTORY = "/WEB-INF/classes";
	public static final String DOT_CLASS = ".class";
	static final int TAMANHO_BUFFER = 2048; // 2kb

	public static String parseClassName(String directory, String name) {
		name = name.substring(directory.length(), name.length() - Utils.DOT_CLASS.length());
		name = name.replaceAll("/|\\\\", ".");
		name = name.replaceAll("\\.\\.", ".");
		return name;
	}

	public static String getFacadeName(String appdir, String facadeName, ServletContext context) {
		String webinfdir = appdir + Utils.WEBINF_CLASSES_DIRECTORY;

		List<File> files = Utils.findFiles(new File(webinfdir), facadeName);

		if (files.size() > 0) {
			return Utils.parseClassName(webinfdir, files.get(0).getAbsolutePath());
		}
		return "";
	}

	public static List<File> findFiles(File startingDirectory, final String pattern) {
		List<File> files = new ArrayList<File>();
		if (startingDirectory.isDirectory()) {
			File[] sub = startingDirectory.listFiles((FileFilter) pathname -> {
				String finalString = pathname.getName().substring(pathname.getName().lastIndexOf("/") + 1);
				return pathname.isDirectory() || finalString.equalsIgnoreCase(pattern);
			});
			for (File fileDir : sub) {
				if (fileDir.isDirectory()) {
					files.addAll(Utils.findFiles(fileDir, pattern));
				} else {
					files.add(fileDir);
				}
			}
		}
		return files;
	}

	public static String removeAcentos(String str) {
		str = Normalizer.normalize(str, Normalizer.Form.NFD);
		str = str.replaceAll("[^\\p{ASCII}]", "");
		return str;
	}

	public static String voToJson(List<?> list) {
		if (list == null || list.size() <= 0) {
			return "[]";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("[");

		for (Object object : list) {
			sb.append(voToJson(object) + ",");
		}

		sb.delete(sb.length() - 1, sb.length());

		sb.append("]");

		return sb.toString();
	}

	public static String listToJson(List<?> list) {
		StringBuilder sb = new StringBuilder();

		sb.append("{");
		// sb.append("\"total_count\": " + list.size() + ", ");
		// sb.append("\"incomplete_results\": false,");
		// sb.append("\"items\": ");

		sb.append("\"results\":");

		sb.append(voToJson(list));
		sb.append(",\"more\": false");
		sb.append("}");

		return sb.toString();
	}

	public static String voToJson(Object obj) {

		if (obj == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder("{");

		for (Method m : obj.getClass().getDeclaredMethods()) {
			if (m.getName().startsWith("get") || m.getName().startsWith("is")) {
				Object value = DaoUtils.getValue(m, obj);

				String fieldName = m.getName();

				if (fieldName.startsWith("get")) {
					fieldName = fieldName.substring(3, 4).toLowerCase() + fieldName.substring(4);
				} else if (fieldName.startsWith("is")) {
					fieldName = fieldName.substring(2, 3).toLowerCase() + fieldName.substring(3);
				}

				if (value instanceof BaseVo && value != null) {
					sb.append("\"" + fieldName + "\":" + Utils.voToJson(value) + ",");
				} else if (value instanceof Boolean) {
					sb.append("\"" + fieldName + "\":" + (value == null ? "" : value) + ",");
				} else if (value instanceof Calendar) {
					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
					Calendar cal = (Calendar) value;
					sb.append("\"" + fieldName + "\":\"" + (cal == null ? "" : sdf.format(Utils.calendarToDate(cal))) + "\",");
				} else {
					sb.append("\"" + fieldName + "\":\"" + (value == null ? "" : value) + "\",");
				}
			}
		}

		sb.delete(sb.length() - 1, sb.length());

		// return new Gson().toJson(obj);

		sb.append("}");

		return sb.toString();
	}

	public static String voToXml(Object vo) {
		return Utils.voToXml(vo, true);
	}

	public static String voToXml(List<?> list, int count) {
		String xmlList = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><list>";
		for (Object _vo : list) {
			xmlList += Utils.voToXml(_vo, false);
		}
		xmlList += "<count>" + count + "</count>";
		xmlList += "</list>";
		return xmlList;
	}

	public static String voContentToXml(Object vo) {
		if (vo == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();

		Field[] fields = vo.getClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				Object value = field.get(vo);
				sb.append("<" + field.getName() + "><![CDATA[" + (value == null ? "" : value.toString()) + "]]></" + field.getName() + ">");
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}

		return sb.toString();
	}

	public static String voToXml(Object vo, boolean appendXmlHeader) {
		if (vo == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		if (appendXmlHeader) {
			sb.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
		}
		sb.append("<" + vo.getClass().getSimpleName().toLowerCase() + "> ");

		Field[] fields = vo.getClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				Object value = field.get(vo);

				if (value instanceof BaseVo) {
					sb.append("<" + field.getName() + ">" + Utils.voToXml(value, false) + "</" + field.getName() + ">");
					// } else if ((value instanceof List) && (value != null)) {
					// sb.append("<" + field.getName() + ">");
					// for(Object object : (List) value) {
					// if (object instanceof IValueObject) {
					// sb.append(voToXml(object, false));
					// }
					// }
					// sb.append("</"+ field.getName() + ">");
				} else {
					sb.append("<" + field.getName() + "><![CDATA[" + (value == null ? "" : value.toString()) + "]]></" + field.getName() + ">");
				}
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}
		sb.append("</" + vo.getClass().getSimpleName().toLowerCase() + ">");

		return sb.toString();
	}

	public static String voToXml(List<?> list) {
		return Utils.voToXml(list, true);
	}

	public static String voToXml(List<?> list, boolean headerXml) {
		StringBuilder sb = new StringBuilder();
		if (headerXml) {
			sb.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><list>");
		}
		for (Object vo : list) {
			sb.append(Utils.voToXml(vo, false));
		}
		if (headerXml) {
			sb.append("</list>");
		}
		return sb.toString();
	}

	/**
	 * Monta XML so dos itens
	 *
	 * @param vo
	 * @return
	 */
	public static String fieldsToXml(BaseVo vo) {
		StringBuilder sb = new StringBuilder();
		Field[] fields = vo.getClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				Object value = field.get(vo);

				if (value instanceof BaseVo) {
					sb.append("<" + field.getName() + ">" + Utils.voToXml(value, false) + "</" + field.getName() + ">");
				} else {
					sb.append("<" + field.getName() + "><![CDATA[" + (value == null ? "" : value.toString()) + "]]></" + field.getName() + ">");
				}
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}
		return sb.toString();
	}

	// public static String decode64(String valor) {
	// BASE64Decoder decoder = new BASE64Decoder();
	// try {
	// return new String(decoder.decodeBuffer(valor));
	// } catch (IOException e) {
	// return valor;
	// }
	// }
	//
	// public static byte[] decode64Bytes(String valor) {
	// BASE64Decoder decoder = new BASE64Decoder();
	// try {
	// return decoder.decodeBuffer(valor);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return null;
	// }
	//
	// public static String encode64(byte[] valor) {
	// BASE64Encoder encoder = new BASE64Encoder();
	// return encoder.encode(valor);
	// }

	public static String toUtf8(String valor) {
		Charset charset = Charset.forName("UTF-8");
		ByteBuffer bb = charset.encode(valor);
		return bb.toString();
	}

	public static Date getToday() {
		Calendar cal = Calendar.getInstance();
		Date date = Date.valueOf(cal.get(Calendar.YEAR) + "-" + Utils.fillString(cal.get(Calendar.MONTH) + 1, "0", 2, 1) + "-" + Utils.fillString(cal.get(Calendar.DAY_OF_MONTH), "0", 2, 1));
		return date;
	}

	public static Timestamp getNow() {
		Calendar cal = Calendar.getInstance();
		Timestamp time = new Timestamp(cal.getTimeInMillis());
		return time;
	}

	public static Calendar toCalendar(String date) {
		return Utils.toCalendar(date, "dd/MM/yyyy");
	}

	public static Calendar toCalendar(String date, String format) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();

		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			java.util.Date parse = sdf.parse(date);
			cal.setTime(parse);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}

		return cal;
	}

	public static Double getDiffDays(Date data1, Date data2) {
		Double milis1 = new Double(data1.getTime());
		Double milis2 = new Double(data2.getTime());
		Double dias = (milis2 - milis1) / 86400000;
		return dias;
	}

	public static Object getproperty(Object objeto, String property) {
		if (objeto == null) {
			return null;
		}
		String fieldname = property;
		Object objetoaux = objeto;
		int index = property.indexOf('.');
		if (index >= 0) {
			fieldname = property.substring(0, index);
		}
		Object retorno = null;
		try {
			Field[] fields = objetoaux.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (field.getName().equals(fieldname)) {
					field.setAccessible(true);
					retorno = field.get(objetoaux);

					if (index >= 0) {
						retorno = Utils.getproperty(retorno, property.substring(index + 1));
					}
					return retorno;
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return retorno;
	}

	public static String mapValues(Object object, String property) {
		String _retorno = property;
		String fieldname_old = "";
		if (object == null) {
			// return property;
		}
		if (_retorno != null) {
			while (_retorno.indexOf('{') >= 0) {
				int inicio = _retorno.indexOf('{') + 1;
				int fim = _retorno.indexOf('}');
				String fieldname = _retorno.substring(inicio, fim);
				if (fieldname.equals(fieldname_old)) {
					_retorno = _retorno.replace("{" + fieldname + "}", "");
				} else {
					Object retorno = Utils.getproperty(object, fieldname);
					if (retorno != null) {
						String newstring = retorno.toString();
						_retorno = _retorno.replace("{" + fieldname + "}", newstring);
					} else {
						_retorno = _retorno.replace("{" + fieldname + "}", "");
					}
				}
				fieldname_old = fieldname;
			}
		}
		return _retorno;
	}

	public static double round(double value, int decimals) {
		double aux = value * Math.pow(10, decimals);
		aux = Math.round(aux);
		return aux / Math.pow(10, decimals);
	}

	public static String getDoubleToString(Double valor, int decimais) {
		BigDecimal valorCerto = new BigDecimal(valor.toString());
		int multiplicador = Double.valueOf((Math.pow(10, decimais))).intValue();
		valorCerto = valorCerto.multiply(new BigDecimal(multiplicador));
		return Utils.fillString(Integer.toString(valorCerto.intValue()), "0", decimais, 1);
	}

	/**
	 *
	 * @param value
	 * @param fillWith
	 * @param size
	 * @param direction
	 *            1=Esquerda 2=Direita
	 * @return
	 */
	public static String fillString(Object ovalue, String fillWith, int size, int direction) {
		String value = ovalue.toString();
		// Checa se Linha a preencher ??? nula ou branco
		if (value == null || value.trim() == "") {
			value = "";
		}
		// if (value.length() > size) {
		// value = value.substring(0, size);
		// return value;
		// }
		// Enquanto Linha a preencher possuir 2 espa???os em branco seguidos,
		// substitui por 1 espa???o apenas
		while (value.contains("  ")) {
			value = value.replaceAll("  ", " ").trim();
		}
		// Retira caracteres estranhos
		value = value.replaceAll("[./-]", "");
		StringBuffer sb = new StringBuffer(value);
		if (direction == 1) { // a Esquerda
			for (int i = sb.length(); i < size; i++) {
				sb.insert(0, fillWith);
			}
		} else if (direction == 2) {// a Direita
			for (int i = sb.length(); i < size; i++) {
				sb.append(fillWith);
			}
		}
		return sb.toString();
	}

	public static void setNullAsEmpty(BaseVo vo) {
		Field[] fields = vo.getClass().getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			try {
				if (field.getType().equals(String.class)) {
					if (field.get(vo) == null) {
						field.set(vo, "");
					}
				} else if (field.getType().equals(Integer.class)) {
					// if (field.get(vo) == null) {
					// field.set(vo, 0);
					// }
				}
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}
	}

	public static void setNullAsZero(BaseVo vo) {
		Field[] fields = vo.getClass().getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			try {
				if (field.getType().equals(Integer.class)) {
					if (field.get(vo) == null) {
						field.set(vo, 0);
					}
				} else if (field.getType().equals(Double.class)) {
					if (field.get(vo) == null) {
						field.set(vo, 0.0);
					}
				}
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}
	}

	public static void compactar(String arqSaida, List<String> arquivos) {
		int i, cont;
		byte[] dados = new byte[Utils.TAMANHO_BUFFER];
		BufferedInputStream origem = null;
		FileInputStream streamDeEntrada = null;
		FileOutputStream destino = null;
		ZipOutputStream saida = null;
		ZipEntry entry = null;
		try {
			destino = new FileOutputStream(arqSaida);
			saida = new ZipOutputStream(new BufferedOutputStream(destino));
			for (i = 0; i < arquivos.size(); i++) {
				File arquivo = new File(arquivos.get(i));
				if (arquivo.isFile() && !(arquivo.getName()).equals(arqSaida)) {
					streamDeEntrada = new FileInputStream(arquivo);
					origem = new BufferedInputStream(streamDeEntrada, Utils.TAMANHO_BUFFER);
					entry = new ZipEntry(arquivos.get(i).substring(arquivos.get(i).lastIndexOf(File.separator) + 1));
					saida.putNextEntry(entry);

					while ((cont = origem.read(dados, 0, Utils.TAMANHO_BUFFER)) != -1) {
						saida.write(dados, 0, cont);
					}
					origem.close();
				}
			}
			saida.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}// fim compactar()

	public static void deletarArquivos(List<String> arquivos) {
		for (String arq : arquivos) {
			File f = new File(arq);
			if (f.exists()) {
				f.delete();
			}
		}
	}

	public static Calendar dateToCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	public static Calendar dateToCalendar(java.util.Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	public static Date calendarToDate(Calendar calendar) {
		Date date = new Date(calendar.getTime().getTime());
		return date;
	}

	public static String geraCadeiaString(int tamanho) {
		String[] carct = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
		String cadeia = "";

		for (int x = 0; x < tamanho; x++) {
			int j = (int) (Math.random() * carct.length);
			cadeia += carct[j];
		}

		return cadeia;
	}

	public static String getUrl2(HttpServletRequest req) {
		String url = req.getRequestURI();
		String path = req.getServletContext().getContextPath();

		url = url.replace(path, "");

		if (url.startsWith("/")) {
			url = url.substring(1);
		}
		return url;
	}

	public static String md5(String value) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(value.getBytes());
			return new String(thedigest).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String toSHA1(String valor) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(valor.getBytes());
			BigInteger hash = new BigInteger(1, md.digest());
			String novoValor = hash.toString(16);

			return novoValor;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String toBase64(String valor) {
		if (valor == null) {
			valor = "";
		}
		return DatatypeConverter.printBase64Binary(valor.getBytes());
	}

	public static String fromBase64(String valor) {
		byte[] parseBase64Binary = DatatypeConverter.parseBase64Binary(valor);

		return new String(parseBase64Binary);
	}

	public static String splitAndWhereIn(String source, String separator, boolean quote) {

		String result = "";

		String[] itens = source.split(separator);

		for (String s : itens) {
			if (quote) {
				result += "'" + s + "', ";
			} else {
				result += s + ", ";
			}
		}

		result = result.substring(0, result.length() - 2);

		return result;
	}

}