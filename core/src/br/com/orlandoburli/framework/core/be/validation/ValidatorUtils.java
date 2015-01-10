package br.com.orlandoburli.framework.core.be.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import br.com.orlandoburli.framework.core.be.exceptions.BeException;
import br.com.orlandoburli.framework.core.be.validation.annotations.transformation.FilterOnly;
import br.com.orlandoburli.framework.core.be.validation.annotations.transformation.FullTrim;
import br.com.orlandoburli.framework.core.be.validation.annotations.transformation.Lower;
import br.com.orlandoburli.framework.core.be.validation.annotations.transformation.MD5;
import br.com.orlandoburli.framework.core.be.validation.annotations.transformation.NoAccents;
import br.com.orlandoburli.framework.core.be.validation.annotations.transformation.NoSpace;
import br.com.orlandoburli.framework.core.be.validation.annotations.transformation.Precision;
import br.com.orlandoburli.framework.core.be.validation.annotations.transformation.SpaceToUnderline;
import br.com.orlandoburli.framework.core.be.validation.annotations.transformation.TransformateWhen;
import br.com.orlandoburli.framework.core.be.validation.annotations.transformation.Upper;
import br.com.orlandoburli.framework.core.be.validation.annotations.transformation.ZeroIfNull;
import br.com.orlandoburli.framework.core.be.validation.annotations.validators.Cnpj;
import br.com.orlandoburli.framework.core.be.validation.annotations.validators.Cpf;
import br.com.orlandoburli.framework.core.be.validation.annotations.validators.Domain;
import br.com.orlandoburli.framework.core.be.validation.annotations.validators.Email;
import br.com.orlandoburli.framework.core.be.validation.annotations.validators.MaxSize;
import br.com.orlandoburli.framework.core.be.validation.annotations.validators.MinSize;
import br.com.orlandoburli.framework.core.be.validation.annotations.validators.NotEmpty;
import br.com.orlandoburli.framework.core.be.validation.annotations.validators.NotNegative;
import br.com.orlandoburli.framework.core.be.validation.annotations.validators.NotNull;
import br.com.orlandoburli.framework.core.be.validation.annotations.validators.NotZero;
import br.com.orlandoburli.framework.core.be.validation.implementation.transformation.FilterOnlyTransformation;
import br.com.orlandoburli.framework.core.be.validation.implementation.transformation.FullTrimTransformation;
import br.com.orlandoburli.framework.core.be.validation.implementation.transformation.LowerTransformation;
import br.com.orlandoburli.framework.core.be.validation.implementation.transformation.MD5Transformation;
import br.com.orlandoburli.framework.core.be.validation.implementation.transformation.NoAccentsTransformation;
import br.com.orlandoburli.framework.core.be.validation.implementation.transformation.NoSpaceTransformation;
import br.com.orlandoburli.framework.core.be.validation.implementation.transformation.PrecisionTransformation;
import br.com.orlandoburli.framework.core.be.validation.implementation.transformation.SpaceToUnderlineTransformation;
import br.com.orlandoburli.framework.core.be.validation.implementation.transformation.UpperTransformation;
import br.com.orlandoburli.framework.core.be.validation.implementation.transformation.ZeroIfNullTransformation;
import br.com.orlandoburli.framework.core.be.validation.implementation.validators.CnpjValidator;
import br.com.orlandoburli.framework.core.be.validation.implementation.validators.CpfValidator;
import br.com.orlandoburli.framework.core.be.validation.implementation.validators.DomainValidator;
import br.com.orlandoburli.framework.core.be.validation.implementation.validators.EmailValidator;
import br.com.orlandoburli.framework.core.be.validation.implementation.validators.MaxSizeValidator;
import br.com.orlandoburli.framework.core.be.validation.implementation.validators.MinSizeValidator;
import br.com.orlandoburli.framework.core.be.validation.implementation.validators.NotEmptyValidator;
import br.com.orlandoburli.framework.core.be.validation.implementation.validators.NotNegativeValidator;
import br.com.orlandoburli.framework.core.be.validation.implementation.validators.NotNullValidator;
import br.com.orlandoburli.framework.core.be.validation.implementation.validators.NotZeroValidator;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.dao.annotations.Column;
import br.com.orlandoburli.framework.core.dao.annotations.Join;
import br.com.orlandoburli.framework.core.vo.BaseDomain;
import br.com.orlandoburli.framework.core.vo.BaseVo;
import br.com.orlandoburli.framework.core.vo.DomainVo;
import br.com.orlandoburli.framework.core.vo.annotations.Description;

public final class ValidatorUtils {

	/**
	 * Valida um vo baseado nas regras definidas pelas annotations.
	 *
	 * @param vo
	 *            VO a ser validado.
	 * @throws BeException
	 */
	public static void validate(BaseVo vo) throws BeException {
		transformateBefore(vo);

		validateOnly(vo);

		transformateAfter(vo);
	}

	public static void validateOnly(BaseVo vo) throws BeException {
		if (vo == null) {
			return;
		}

		@SuppressWarnings("unchecked")
		Class<BaseVo> classe = (Class<BaseVo>) vo.getClass();

		Field[] fields = vo.getClass().getDeclaredFields();

		for (Field f : fields) {

			Annotation[] annotations = f.getAnnotations();

			for (Annotation a : annotations) {
				if (a instanceof Email) {
					new EmailValidator().validate(vo, f, classe);
				} else if (a instanceof MaxSize) {
					new MaxSizeValidator().validate(vo, f, classe);
				} else if (a instanceof MinSize) {
					new MinSizeValidator().validate(vo, f, classe);
				} else if (a instanceof NotEmpty) {
					new NotEmptyValidator().validate(vo, f, classe);
				} else if (a instanceof NotNull) {
					new NotNullValidator().validate(vo, f, classe);
				} else if (a instanceof NotZero) {
					new NotZeroValidator().validate(vo, f, classe);
				} else if (a instanceof NotNegative) {
					new NotNegativeValidator().validate(vo, f, classe);
				} else if (a instanceof Domain) {
					new DomainValidator().validate(vo, f, classe);
				} else if (a instanceof Cpf) {
					new CpfValidator().validate(vo, f, classe);
				} else if (a instanceof Cnpj) {
					new CnpjValidator().validate(vo, f, classe);
				}
			}
		}
	}

	/**
	 * Aplica as transformacoes que ocorrerem antes da validacao.
	 *
	 * @param vo
	 *            VO a ser transformado.
	 */
	static void transformateBefore(BaseVo vo) {
		transformate(vo, TransformateWhen.BEFORE_VALIDATE);
	}

	/**
	 * Aplica as transformacoes que ocorrerem apos a validacao.
	 *
	 * @param vo
	 *            VO a ser transformado.
	 */
	static void transformateAfter(BaseVo vo) {
		transformate(vo, TransformateWhen.AFTER_VALIDATE);
	}

	/**
	 * Aplica as transformacoes no VO, no momento definido.
	 *
	 * @param vo
	 *            VO a ser transformado.
	 * @param when
	 *            Momento em que sera validado.
	 */
	static void transformate(BaseVo vo, TransformateWhen when) {
		if (vo == null) {
			return;
		}

		@SuppressWarnings("unchecked")
		Class<BaseVo> classe = (Class<BaseVo>) vo.getClass();

		Field[] fields = vo.getClass().getDeclaredFields();

		for (Field f : fields) {

			Annotation[] annotations = f.getAnnotations();

			for (Annotation a : annotations) {

				// Log.debug("Transformate " + a + " when " + when);

				if (a instanceof FullTrim) {
					FullTrim fullTrim = (FullTrim) a;
					if (fullTrim != null && fullTrim.when() == when) {

						new FullTrimTransformation().transform(vo, f, classe);
					}
				} else if (a instanceof Lower) {
					Lower lower = (Lower) a;
					if (lower != null && lower.when() == when) {
						new LowerTransformation().transform(vo, f, classe);
					}
				} else if (a instanceof Upper) {
					Upper upper = (Upper) a;
					if (upper != null && upper.when() == when) {
						new UpperTransformation().transform(vo, f, classe);
					}
				} else if (a instanceof MD5) {
					MD5 md5 = (MD5) a;
					if (md5 != null && md5.when() == when) {
						if (vo.isNew() && md5.onInsert()) {
							new MD5Transformation().transform(vo, f, classe);
						} else if (!vo.isNew() && md5.onUpdate()) {
							new MD5Transformation().transform(vo, f, classe);
						}
					}
				} else if (a instanceof NoSpace) {
					NoSpace noSpace = (NoSpace) a;
					if (noSpace != null && noSpace.when() == when) {
						new NoSpaceTransformation().transform(vo, f, classe);
					}
				} else if (a instanceof Precision) {
					Precision precision = (Precision) a;
					if (precision != null && precision.when() == when) {
						new PrecisionTransformation().transform(vo, f, classe);
					}
				} else if (a instanceof ZeroIfNull) {
					ZeroIfNull zeroIfNull = (ZeroIfNull) a;
					if (zeroIfNull != null && zeroIfNull.when() == when) {
						new ZeroIfNullTransformation().transform(vo, f, classe);
					}
				} else if (a instanceof FilterOnly) {
					FilterOnly filterOnly = (FilterOnly) a;
					if (filterOnly != null && filterOnly.when() == when) {
						new FilterOnlyTransformation().transform(vo, f, classe);
					}
				} else if (a instanceof NoAccents) {
					NoAccents noAccents = (NoAccents) a;
					if (noAccents != null && noAccents.when() == when) {
						new NoAccentsTransformation().transform(vo, f, classe);
					}
				} else if (a instanceof SpaceToUnderline) {
					SpaceToUnderline spaceToUnderline = (SpaceToUnderline) a;
					if (spaceToUnderline != null && spaceToUnderline.when() == when) {
						new SpaceToUnderlineTransformation().transform(vo, f, classe);
					}
				}
			}
		}
	}

	public static boolean isField(Field f) {
		Column c = f.getAnnotation(Column.class);
		if (c == null) {
			return false;
		}

		return true;
	}

	public static boolean isKey(Field f) {
		Column c = f.getAnnotation(Column.class);

		if (c != null && c.isKey()) {
			return true;
		}

		return false;
	}

	public static boolean isDomain(Field f) {
		Column c = f.getAnnotation(Column.class);
		Domain d = f.getAnnotation(Domain.class);

		if (c != null && d != null) {
			return true;
		}

		return false;
	}

	public static List<DomainVo> getDomains(Field f) {
		if (!isDomain(f)) {
			return null;
		}

		Domain d = f.getAnnotation(Domain.class);

		BaseDomain domain = (BaseDomain) DaoUtils.getNewObject(d.value());

		return domain.getList();
	}

	public static String getDomainValue(Field f, Object value) {
		if (isDomain(f)) {
			Domain d = f.getAnnotation(Domain.class);

			BaseDomain domain = (BaseDomain) DaoUtils.getNewObject(d.value());

			return domain.getDescription(value);
		}

		return null;
	}

	public static boolean isJoin(Class<?> clazz, Field f) {
		if (!isField(f)) {
			return false;
		}
		Column c = f.getAnnotation(Column.class);
		String columnName = c.name();

		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			Join join = field.getAnnotation(Join.class);

			if (join != null) {
				if (join.columnsLocal()[0].equals(columnName)) {
					return true;
				}
			}
		}

		return false;
	}

	/*
	 * public static boolean isJoin(Class<?> clazz, Field f) { if (!isField(f))
	 * { return false; } Column c = f.getAnnotation(Column.class); String
	 * columnName = c.name();
	 * 
	 * Join j = null; Field fJoin = null;
	 * 
	 * Field[] fields = clazz.getDeclaredFields();
	 * 
	 * for (Field field : fields) { Join join = field.getAnnotation(Join.class);
	 * 
	 * if (join != null) { if (join.columnsLocal()[0].equals(columnName)) { j =
	 * join; fJoin = field; break; } } }
	 * 
	 * if (j != null) { return true; }
	 * 
	 * return false; }
	 */

	public static String getFieldDescription(Field f) {
		if (f != null) {
			Description desc = f.getAnnotation(Description.class);
			if (desc != null) {
				return desc.value();
			} else {
				return f.getName();
			}
		}

		return null;
	}
}
