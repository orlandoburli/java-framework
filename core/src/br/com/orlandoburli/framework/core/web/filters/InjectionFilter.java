package br.com.orlandoburli.framework.core.web.filters;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Enumeration;
import java.util.Locale;

import br.com.orlandoburli.framework.core.be.validation.annotations.transformation.Precision;
import br.com.orlandoburli.framework.core.utils.Utils;

/**
 * Filtro para Injecao de atributos
 * 
 * @author orlandoburli
 */
public class InjectionFilter extends BaseFilter {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean doFilter(Object vo) throws IllegalArgumentException, IllegalAccessException {

		// Injecao de InstantiateObject
		Field[] fields = vo.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.getAnnotation(InstantiateObject.class) != null) {
				try {
					field.setAccessible(true);
					field.set(vo, field.getType().newInstance());
				} catch (InstantiationException e) {
					// Se der excecao, e porque nao possui construtor sem
					// parametros
					e.printStackTrace();
				}
			}
		}

		// Injecao pelos dados de sessao
		Enumeration<?> enumSession = getRequest().getSession().getAttributeNames();
		while (enumSession.hasMoreElements()) {
			String campo = enumSession.nextElement().toString();
			Object value = getRequest().getSession().getAttribute(campo);
			setproperty(vo, campo, value); // Seta a propriedade
											// recursivamente
		}

		// Injecao pelos dados de parametros de request
		Enumeration<?> enumParm = getRequest().getParameterNames();
		while (enumParm.hasMoreElements()) {
			String campo = enumParm.nextElement().toString();
			String[] value = getRequest().getParameterValues(campo);
			if (value.length > 1) {
				setproperty(vo, campo, value); // Seta a propriedade como
												// array de strings
			} else if (value.length == 1) {
				setproperty(vo, campo, value[0]);
			}
		}

		// Injecao pelos dados de atributos
		Enumeration<?> enumAtt = getRequest().getAttributeNames();
		while (enumAtt.hasMoreElements()) {
			String campo = enumAtt.nextElement().toString();
			Object value = getRequest().getAttribute(campo);
			setproperty(vo, campo, value); // Seta a propriedade
											// recursivamente
		}

		return true;
	}

	public static void setproperty(Object objeto, String property, Object value) {
		if (objeto == null) {
			return;
		}
		String fieldname = property;

		int index = property.indexOf('.');
		if (index >= 0) {
			fieldname = property.substring(0, index);
		}
		try {
			Field[] fields = objeto.getClass().getDeclaredFields();
			boolean found = false;
			for (Field field : fields) {
				if (field.getName().equalsIgnoreCase(fieldname)) {
					found = true;
					field.setAccessible(true);
					if (index >= 0) {
						setproperty(field.get(objeto), property.substring(index + 1), value);
					} else {
						// Metodo para setar o valor de acordo com o atributo
						setField(objeto, field, value);
					}
				}
			}
			if (!found) { // tenta na superclasse
				fields = objeto.getClass().getSuperclass().getDeclaredFields();
				for (Field field : fields) {
					if (field.getName().equalsIgnoreCase(fieldname)) {
						found = true;
						field.setAccessible(true);
						if (index >= 0) {
							setproperty(field.get(objeto), property.substring(index + 1), value);
						} else {
							// Metodo para setar o valor de acordo com o
							// tipo
							// atributo
							setField(objeto, field, value);
						}
					}
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Realiza os setters corretamente tipados
	 * 
	 * @param container
	 * @param field
	 * @param value
	 */
	protected static void setField(Object container, Field field, Object value) {

		try {
			if (field.getType().equals(String.class)) {
				field.set(container, value.toString());
			} else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
				try {
					Integer intval = Integer.parseInt(value.toString());
					field.set(container, intval);
				} catch (Exception ex) {
				}
			} else if (field.getType().equals(double.class) || field.getType().equals(Double.class) || field.getType().equals(BigDecimal.class)) {
				try {
					String valorString = value.toString().replace("R$", "").trim();
					
					// Elimina os pontos (separador de decimais)
					valorString = valorString.replace(".", "");
					// Substitui as virgulas por pontos
					valorString = valorString.replace(",", ".");
					
					if (value != null && valorString.length() > 0) {
						Precision precision = field.getAnnotation(Precision.class);
						String mascara = "#,";
						if (precision != null) {
							for (int i = 0; i < precision.value(); i++) {
								mascara += "0";
							}
						} else {
							mascara += "00";
						}

						DecimalFormat formater = new DecimalFormat(mascara);
						formater.setCurrency(Currency.getInstance(new Locale("pt", "BR")));

						if (value instanceof BigDecimal) {
							field.set(container, value);
						} else {
							if (field.getType().equals(BigDecimal.class)) {
								formater.setParseBigDecimal(true);
								// new
								// BigDecimal(formater.parse(value.toString(),
								// new ParsePosition(0)));
								BigDecimal valor = (BigDecimal) formater.parse(valorString);
								if (precision != null) {
									valor = valor.setScale(precision.value(), BigDecimal.ROUND_CEILING);
								}
								field.set(container, valor);
							} else {
								Double doubleval = formater.parse(valorString).doubleValue();
								field.set(container, doubleval);
							}
						}
					} else {
						field.set(container, null);
					}
				} catch (Exception ex) {
					// Nao conseguiu setar, seta nulo
					// System.out.println("Erro ao converter double! " + value);
					ex.printStackTrace();
				}
			} else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
				try {
					Boolean bolval = Boolean.parseBoolean(value.toString());
					field.set(container, bolval);
				} catch (Exception ex) {
				}
			} else if (field.getType().equals(Calendar.class)) {
				try {
					if (value != null && value.toString().trim().length() == 10) {
						SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
						Calendar cal = Calendar.getInstance();
						cal.setTimeInMillis(sdf.parse(value.toString()).getTime());
						field.set(container, cal);
					} else {
						field.set(container, null);
					}
				} catch (Exception ex) {
					// Nao conseguiu setar... passa reto
					// ex.printStackTrace();
				}
			} else if (field.getType().equals(Date.class)) {
				try {
					if (value != null && value.toString().trim().length() == 10) {
						SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
						// System.out.println(value);
						// String valueof = value.toString().substring(6, 10) +
						// "-" + value.toString().substring(3, 5) + "-" +
						// value.toString().substring(0, 2);
						Date date = new Date(sdf.parse(value.toString()).getTime());
						field.set(container, date);
					} else {
						field.set(container, null);
					}
				} catch (Exception ex) {
					// Nao conseguiu setar... passa reto
					// ex.printStackTrace();
				}
			} else if (field.getType().equals(Timestamp.class)) {
				Timestamp time = null;
				if (value != null && value.toString().length() <= 5) {
					time = Timestamp.valueOf("0001-01-01 " + value.toString() + ":00");
				} else if (value != null && value.toString().trim().length() == 10) {
					String valueof = value.toString().substring(6, 10) + "-" + value.toString().substring(3, 5) + "-" + value.toString().substring(0, 2) + " 00:00:00";
					time = Timestamp.valueOf(valueof);
					field.set(container, time);
				} else {
					time = Timestamp.valueOf(value.toString());
				}
				field.set(container, time);
			} else if (field.getType().equals(String[].class) && value.getClass().equals(String.class)) { // Para
																											// arrays
				field.set(container, new String[] { value.toString() });
			} else {
				try {
					field.set(container, value); // Tenta setar como object
				} catch (Exception ex) {

				}
			}
		} catch (IllegalArgumentException e) {
			// e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static String mapValues(Object object, String property) {
		String _retorno = property;
		String fieldname_old = "";
		if (object == null) {
			return property;
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
}