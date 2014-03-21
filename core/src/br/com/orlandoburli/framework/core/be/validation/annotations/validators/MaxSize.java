package br.com.orlandoburli.framework.core.be.validation.annotations.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation que define um tamanho maximo para um campo.
 * @author orlandoburli
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface MaxSize {
	/**
	 * Tamanho maximo do campo.
	 * @return Tamanho maximo do campo.
	 */
	int value();
	
	/**
	 * Mensagem customizada para o atributo. Se nao for especificada, a mensagem padrao sera criada.
	 * @return Mensagem customizada.
	 */
	String customError() default "";
}
