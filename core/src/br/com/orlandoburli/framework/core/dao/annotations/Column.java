package br.com.orlandoburli.framework.core.dao.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
/**
 * Definicoes da coluna.
 * @author orlandoburli
 */
public @interface Column {
	/**
	 * Nome da coluna. Se deixar o valor default ("" - vazio) sera utilizado o nome do atributo na classe.
	 * @return String com o nome da coluna.
	 */
	String name();
	
	/**
	 * Nome da coluna. Se deixar o valor default ("" - vazio) sera utilizado o nome do atributo na classe.
	 * @return String com o nome da coluna.
	 */
	String value() default "";
	
	/**
	 * Tamanho maximo da coluna. Se deixar o valor default (0) nenhuma restricao sera aplicada, e o tamanho sera presumido.
	 * @return Tamanho maximo da coluna.
	 */
	int maxSize() default 0;
	
	/**
	 * Precisao dos tipos numericos.
	 * @return Precisao numerica.
	 */
	int precision() default 0;
	
	/**
	 * Tipo de dados da coluna. Se deixar o valor default ("" - vazio) sera presumido pela seguinte regra:
	 * <br/><b>String</b> - DataType.VARCHAR
	 * <br/><b>Integer</b> - DataType.INT
	 * <br/><b>java.math.BigDecimal</b> - DataType.NUMERIC
	 * <br/><b>java.sql.Date</b> - DataType.DATE
	 * @return String com o tipo de dados.
	 */
	DataType dataType() default DataType.VARCHAR;
	
	/**
	 * Define se a coluna e campo chave.
	 * @return Coluna chave.
	 */
	boolean isKey() default false;
	
	/**
	 * Se a coluna for chave, define se e de auto incremento.
	 * @return Coluna autoincremento.
	 */
	boolean isAutoIncrement() default false;
	
	
	/**
	 * Define se a coluna aceita nulos ou nao. 
	 * <strong>ATENCAO: </strong> NULO e diferente de VAZIO.
	 * @return TRUE se nao aceitar nulo, FALSE se aceita nulo.
	 */
	boolean isNotNull() default false;
	
	/**
	 * Se a coluna for auto incremento, deve ser setado o nome da sequence no banco de dados.
	 * @return Nome da sequence
	 */
	String sequenceName() default "";
	
	/**
	 * Valor default para a coluna, caso ela seja not null.
	 * @return Valor default
	 */
	String defaultValue() default "";
}
