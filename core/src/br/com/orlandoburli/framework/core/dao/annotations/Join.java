package br.com.orlandoburli.framework.core.dao.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation que serve para marcar o join remoto com outra tabela. <br/>
 *
 * Pode ser usada em um VO ou em um Field simples, apenas para trazer o seu
 * valor.
 *
 * @author orlandoburli
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Join {

	/**
	 * Array de colunas da tabela local para o Join. Deve ser especificada em
	 * ordem igual as colunas remotas.
	 *
	 * @return Array de String das colunas locais.
	 */
	String[] columnsLocal();

	/**
	 * Array de colunas da tabela remota para o Join. Deve ser especificada em
	 * ordem igual as colunas locais.
	 *
	 * Se nao for informado, sera utilizado o mesmo nome de coluna de
	 * columnsLocal()
	 *
	 * @return Array de String das colunas remotas.
	 */
	String[] columnsRemote();

	/**
	 * Nome da tabela remota. Se nao for especificado, o objeto deve ser do tipo
	 * VO ou ira disparar uma DAOException.
	 *
	 * @return Nome da tabela remota.
	 */
	String tableRemote() default "";

	/**
	 * Tipo de Join. O Default é LEFT JOIN.
	 *
	 * @return Tipo de Join
	 */
	JoinType joinType() default JoinType.LEFT;

	/**
	 * Momento do Join, se sempre ou se apenas quando requisitado
	 *
	 * @return Momento do Join.
	 */
	JoinWhen joinWhen() default JoinWhen.ALWAYS;

	/**
	 * Nome da coluna remota que se fara o Join. No caso de um objeto VO
	 * completo, nao especificar esse campo, o mesmo sera ignorado.
	 *
	 * @return Nome do Field remoto.
	 */
	String fieldRemote() default "";

	/**
	 * Alias do Join. Utilizado quando existe 2 Joins de um mesmo VO.
	 *
	 * @return Alias do Join.
	 */
	String tableAlias() default "";
}
