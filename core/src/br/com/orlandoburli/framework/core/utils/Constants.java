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
	}
	
	/**
	 * Constantes de parametros do sistema
	 */
	public final class Parameters {
		
		/**
		 * Parametros de folha
		 */
		public final class Folha {
			public static final String FORMA_CALCULO_PROPORCAO_FOLHA = "FORMA_CALCULO_PROPORCAO_FOLHA";
			public static final String NUMERO_HORAS_MES_FOLHA = "NUMERO_HORAS_MES_FOLHA";
			public static final String IMPACTA_FALTA_PARCIAL_DSR = "IMPACTA_FALTA_PARCIAL_DSR";
			public static final String ALIQUOTA_FGTS = "ALIQUOTA_FGTS";
			
			public static final String CODIGO_VERBA_FALTA = "CODIGO_VERBA_FALTA";
			public static final String CODIGO_VERBA_FALTA_DSR = "CODIGO_VERBA_FALTA_DSR";
			public static final String CODIGO_VERBA_FALTA_PARCIAL = "CODIGO_VERBA_FALTA_PARCIAL";
			public static final String CODIGO_VERBA_INSS = "CODIGO_VERBA_INSS";
			public static final String CODIGO_VERBA_IRRF = "CODIGO_VERBA_IRRF";
			public static final String CODIGO_VERBA_SALARIO_FAMILIA = "CODIGO_VERBA_SALARIO_FAMILIA";
			public static final String CODIGO_VERBA_VALE = "CODIGO_VERBA_VALE";
			public static final String CODIGO_VERBA_COMISSAO_DSR = "CODIGO_VERBA_COMISSAO_DSR";
		}
		
		/**
		 * Nota de saida
		 */
		public final class NFSaida {
			public static final String PROXIMA_NF_SAIDA = "PROXIMA_NF_SAIDA";
		}
		
		/**
		 * Parametros da importacao de XML da NF-e
		 */
		public final class ImportacaoNFeXML {
			public static final String IMPORTACAO_XML_NFE_TIPO_DOCUMENTO = "IMPORTACAO_XML_NFE_TIPO_DOCUMENTO";
			public static final String IMPORTACAO_XML_NFE_PORTADOR = "IMPORTACAO_XML_NFE_PORTADOR";
			public static final String IMPORTACAO_XML_NFE_PLANO_CONTA = "IMPORTACAO_XML_NFE_PLANO_CONTA";
			public static final String IMPORTACAO_XML_NFE_CENTRO_CUSTO = "IMPORTACAO_XML_NFE_CENTRO_CUSTO";
		}

		/**
		 * Parametros de licenca
		 */
		public final class Licenca {
			public static final String LICENCA_SISTEMA = "LICENCA_SISTEMA";
			public static final String DATA_SISTEMA = "DATA_SISTEMA";
			public static final String LICENCA_KEY = "licence.key";
		}
	
		public final class Geral {
			public static final String SITUACAO_INICIAL_TROCA_MERCADORIAS = "SITUACAO_INICIAL_TROCA_MERCADORIAS";
			public static final String LOGO_RELATORIO = "LOGO_RELATORIO";
			public static final String PROXIMO_CUPOM_FISCAL = "PROXIMO_CUPOM_FISCAL";
		}
		
		public final class Estoque {
			public static final String TABELA_PRECO_PADRAO = "TABELA_PRECO_PADRAO";
		}
		
		/**
		 * Parametros da NF-e
		 */
		public final class NFe {
			/**
			 * Certificado digital do cliente
			 */
			public static final String KEYSTORE_FILE = "KEYSTORE_FILE";
			/**
			 * Senha do certificado do cliente
			 */
			public static final String KEYSTORE_PASSWORD = "KEYSTORE_PASSWORD";
			/**
			 * Tipo do certificado do cliente
			 */
			public static final String KEYSTORE_TYPE = "KEYSTORE_TYPE";
			
			/**
			 * Cadeia de certificados
			 */
			public static final String TRUSTSTORE_FILE = "TRUSTSTORE_FILE";
			/**
			 * Senha da cadeia de certificados
			 */
			public static final String TRUSTSTORE_PASSWORD = "TRUSTSTORE_PASSWORD";
			/**
			 * Ambiente da NF-e
			 */
			public static final String AMBIENTE_NFE = "AMBIENTE_NFE";
		}
	}
	
	/**
	 * Endere���os dos servi���os da NF-e
	 */
	public final class NfeParameters {
		
		/**
		 * Ambiente de Homologacao
		 */
		public final class Homologacao {
			public static final String NFERECEPCAO = "homologacao.nferecepcao.address";	
			public static final String NFERETRECEPCAO = "homologacao.nferetrecepcao.address";
			public static final String NFECANCELAMENTO = "homologacao.nfecancelamento.address";
			public static final String NFEINUTILIZACAO = "homologacao.nfeinutilizacao.address";
			public static final String NFECONSULTANF = "homologacao.nfeconsultanf.address";
			public static final String NFESTATUSSERVICO = "homologacao.nfestatusservico.address";
		}
		
		/**
		 * Ambiente de Producao
		 */
		public final class Producao {
			public static final String NFERECEPCAO = "producao.nferecepcao.address";	
			public static final String NFERETRECEPCAO = "producao.nferetrecepcao.address";
			public static final String NFECANCELAMENTO = "producao.nfecancelamento.address";
			public static final String NFEINUTILIZACAO = "producao.nfeinutilizacao.address";
			public static final String NFECONSULTANF = "producao.nfeconsultanf.address";
			public static final String NFESTATUSSERVICO = "producao.nfestatusservico.address";
		}
		
		/**
		 * Constantes para propriedades do SSL
		 */
		@Deprecated
		public final class Ssl {
			/**
			 * Certificado digital do cliente
			 */
			public static final String KEYSTORE_FILE = "keystore.file";
			/**
			 * Senha do certificado do cliente
			 */
			public static final String KEYSTORE_PASSWORD = "keystore.password";
			/**
			 * Tipo do certificado do cliente
			 */
			public static final String KEYSTORE_TYPE = "keystore.type";
			
			/**
			 * Cadeia de certificados
			 */
			public static final String TRUSTSTORE_FILE = "truststore.file";
			/**
			 * Senha da cadeia de certificados
			 */
			public static final String TRUSTSTORE_PASSWORD = "truststore.password";
		}

		/**
		 * Constantes com os c���digos de retorno
		 */
		public final class Retornos {
			public static final String LOTE_RECEBIDO_SUCESSO = "103";
			public static final String LOTE_EM_PROCESSAMENTO = "105";
			public static final String LOTE_EVENTO_PROCESSADO = "128";
			public static final String EVENTO_REGISTRADO = "135";
			public static final String LOTE_PROCESSADO = "104";
			public static final String NFE_AUTORIZADA = "100";
			public static final String NFE_CANCELADA = "101";
			public static final String NFE_DUPLICADA = "204";
		}
	}
}