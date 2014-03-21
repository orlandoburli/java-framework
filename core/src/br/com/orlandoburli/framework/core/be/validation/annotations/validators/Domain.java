package br.com.orlandoburli.framework.core.be.validation.annotations.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
/**
 * Annotation que define uma validacao com base em um dominio.
 * @author orlandoburli
 *
 */
public @interface Domain {

	/**
	 * Classe que contem o dominio de validacao
	 * 
	 * @return Classe do dominio.
	 */
	Class<?> value();
}
