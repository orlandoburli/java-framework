package br.com.orlandoburli.framework.core.be.validation.implementation.transformation;

import java.lang.reflect.Field;

import br.com.orlandoburli.framework.core.vo.BaseVo;

public abstract class BaseTransformation {

	public abstract void transform(BaseVo vo, Field f, Class<BaseVo> classe);

}
