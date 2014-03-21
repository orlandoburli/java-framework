package br.com.orlandoburli.framework.core.be.validation.annotations.transformation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation que define que o campo tera os espacos do comeco e fim removidos,
 * e todos os espacos duplicados tambem.
 * 
 * @author orlandoburli
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface FullTrim {
	/**
	 * Indica quando a transformacao deve ocorrer. O default e BEFORE_VALIDATE
	 * (antes da validacao).
	 * 
	 * @return Momento da transformacao.
	 */
	TransformateWhen when() default TransformateWhen.BEFORE_VALIDATE;
}
