package br.com.orlandoburli.framework.core.dao.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
/**
 * Annotation utilizada para indicar o nome da tabela
 * @author orlandoburli
 */
public @interface Table {
	
	/**
	 * Nome da tabela
	 * @return Nome da tabela
	 */
	String value();
	
	UniqueConstraint[] constraints() default {};
	
	/**
	 * Nome do schema a ser colocado na tabela, se houver
	 * @return nome do Schema
	 */
	String schema() default "";
}
