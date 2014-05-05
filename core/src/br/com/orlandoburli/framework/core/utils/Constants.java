package br.com.orlandoburli.framework.core.utils;

public final class Constants {
	
	public static final String POST = "POST";
	public static final String GET = "GET";
	
	public static final String ALTERAR = "alterar";
	public static final String EXCLUIR = "excluir";
	public static final String INSERIR = "inserir";
	public static final String CONSULTAR = "consultar";
	public static final String VISUALIZAR = "visualizar";
	public static final String EXECUTE = "execute";
	public static final String ERROR = "error";
	
	public static final String DB_USER = "db.user";
	public static final String DB_PASS = "db.pass";
	public static final String DB_DATABASE = "db.database";
	public static final String DB_PORT = "db.port";
	public static final String DB_HOST = "db.host";
	public static final String DB_CLASS_DRIVER = "db.classdriver";
	
	public static final String USER_AGENT = "user-agent";
	public static final String BUTTON_DRAWTEXT = "button.drawtext";
	public static final String VO_SESSION	= "vo_session";
	public static final String TRUE	= "true";
	
	public static final String PORTA_LPT1 = "LPT1";
	public static final String PORTA_USB001 = "USB001";

	public final class Saida {
		public static final String SAIDA_HTML = "html";
		public static final String SAIDA_PDF = "pdf";
		public static final String SAIDA_XLS = "xls";
		public static final String SAIDA_RTF = "rtf";
		public static final String SAIDA_TXT = "txt";
	}
	
	/**
	 * Constantes de Variaveis de sessao
	 */
	public final class Session {
		public static final String SESSION_USUARIO = "usuario";
        public static final String SESSION_USUARIO_TEMP = "usuariotmp";

        public static final String SESSION_PERFIL = "perfilsessao";
        public static final String SESSION_MENU_USUARIO = "menuusuariosession";
        public static final String SESSION_LOJA = "lojasessao";
        public static final String SESSION_EMPRESA = "empresasessao";
        
		public static final String SESSION_PERMISSAO_OBJETOS = "permissoesobjetos";
		public static final String SESSION_PERMISSAO_ACAO_OBJETOS = "permissoesacoesobjetos";
		public static final String CARRINHO = "carrinho";
		public static final String CLIENTE = "cliente";
	}
	
	/**
	 * Constantes de parametros do sistema
	 */
	public final class Parameters {
		public static final String PATH_ARQUIVOS = "PATH_ARQUIVOS";
		public static final String APP_ID_FACEBOOK = "APP_ID_FACEBOOK";
		public static final String SECRET_ID_FACEBOOK = "SECRET_ID_FACEBOOK";
		public static final String EMAIL_PAGSEGURO = "EMAIL_PAGSEGURO";
		public static final String CHAVE_PAGSEGURO = "CHAVE_PAGSEGURO";
		
		
		public static final String SMTP_HOST = "SMTP_HOST";
		public static final String SMTP_PORT = "SMTP_PORT";
		public static final String SMTP_USER = "SMTP_USER";
		public static final String SMTP_PASSWORD = "SMTP_PASSWORD";
		public static final String SMTP_SSL = "SMTP_SSL";
	}
}