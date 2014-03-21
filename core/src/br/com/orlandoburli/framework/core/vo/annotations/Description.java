package br.com.orlandoburli.framework.core.vo.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation para identificar o campo
 * 
 * @author orlandoburli
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Description {

	/**
	 * Descricao do campo.
	 * @return Descricao.
	 */
	String value();
}
