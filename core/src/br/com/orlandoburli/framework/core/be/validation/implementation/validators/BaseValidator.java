package br.com.orlandoburli.framework.core.be.validation.implementation.validators;

import java.lang.reflect.Field;

import br.com.orlandoburli.framework.core.be.exceptions.BeException;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public abstract class BaseValidator {

	public abstract void validate(BaseVo vo, Field f, Class<BaseVo> classe) throws BeException;

}
