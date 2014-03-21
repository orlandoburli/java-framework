package br.com.orlandoburli.framework.core.be.validation.annotations.transformation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation que define que o atributo deve ser setado como zero caso esteja
 * nulo.
 * 
 * @author orlandoburli
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface ZeroIfNull {

	/**
	 * Indica quando a transformacao deve ocorrer. O default e BEFORE_VALIDATE
	 * (antes da validacao).
	 * 
	 * @return Momento da transformacao.
	 */
	TransformateWhen when() default TransformateWhen.BEFORE_VALIDATE;
}
