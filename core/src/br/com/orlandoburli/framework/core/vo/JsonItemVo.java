package br.com.orlandoburli.framework.core.vo;

public class JsonItemVo {

	private String id;
	private String label;
	private String value;
	private Object original;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Object getOriginal() {
		return original;
	}

	public void setOriginal(Object original) {
		this.original = original;
	}
}
