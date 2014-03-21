package br.com.orlandoburli.framework.core.be.validation.annotations.transformation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation que define transformacao das letras para uppercase.
 * 
 * @author orlandoburli
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Upper {

	/**
	 * Indica quando a transformacao deve ocorrer. O default e AFTER_VALIDATE
	 * (apos a validacao).
	 * 
	 * @return Momento da transformacao.
	 */
	TransformateWhen when() default TransformateWhen.AFTER_VALIDATE;
}
