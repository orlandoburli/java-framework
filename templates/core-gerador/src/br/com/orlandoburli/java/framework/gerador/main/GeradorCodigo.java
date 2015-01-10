package br.com.orlandoburli.java.framework.gerador.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import br.com.orlandoburli.framework.core.be.validation.ValidatorUtils;
import br.com.orlandoburli.framework.core.utils.Utils;
import br.com.orlandoburli.minhalanchonete.model.vo.cadastros.SubCategoriaVo;

public class GeradorCodigo {

	public static void main(String[] args) {
		// Source folder para o projeto Model
		// String sourceModelFolder =
		// "/Users/orlandoburli/Documents/Projetos/PMO-Manager/java/pmo-manager-model/src/";
		String sourceModelFolder = "/Users/orlandoburli/Documents/Projetos/minhalanchonete/web/java/minhalanchonete-model/src/";

		// Source folder para o projeto Web
		// String sourceWebFolder =
		// "/Users/orlandoburli/Documents/Projetos/PMO-Manager/java/pmo-manager-web/WEB-INF/src/";
		String sourceWebFolder = "/Users/orlandoburli/Documents/Projetos/minhalanchonete/web/java/minhalanchonete-web-caixa/WEB-INF/src/";

		// Folder das pastas web
		// String webFolder =
		// "/Users/orlandoburli/Documents/Projetos/PMO-Manager/java/pmo-manager-web/web/pages/";
		String webFolder = "/Users/orlandoburli/Documents/Projetos/minhalanchonete/web/java/minhalanchonete-web-caixa/web/pages/";

		//
		// String packageWeb = "br.com.orlandoburli.pmo.web.actions";
		String packageWeb = "br.com.orlandoburli.minhalanchonete.web.actions";
		// Classe Vo de base
		Class<?> voClass = SubCategoriaVo.class;

		for (int i = 0; i < 200; i++) {
			System.out.print("-");
		}

		System.out.println();

		System.out.println("Iniciando geração de código...");

		for (int i = 0; i < 200; i++) {
			System.out.print("-");
		}

		System.out.println();

		// Engine Velocity
		VelocityEngine ve = new VelocityEngine();
		ve.init();

		VelocityContext context = new VelocityContext();
		context.put("class", voClass);

		// Coloca a classe Utils no Contexto
		context.put("Utils", Utils.class);
		context.put("ValidatorUtils", ValidatorUtils.class);

		String fileDao = sourceModelFolder + voClass.getPackage().getName().replace(".vo.", ".dao.").replace(".", "/") + "/" + voClass.getSimpleName().replace("Vo", "Dao") + ".java";
		String fileBe = sourceModelFolder + voClass.getPackage().getName().replace(".vo.", ".be.").replace(".", "/") + "/" + voClass.getSimpleName().replace("Vo", "Be") + ".java";

		// Dao
		executeTemplate(ve, context, "templates/generate-dao.vm", fileDao);

		// Be
		executeTemplate(ve, context, "templates/generate-be.vm", fileBe);

		String grupo = voClass.getPackage().getName().substring(voClass.getPackage().getName().indexOf(".vo.")).replace(".vo.", "");

		String packageActions = packageWeb + "." + grupo + "." + voClass.getSimpleName().replace("Vo", "").toLowerCase();
		String fileCadastroAction = sourceWebFolder + packageActions.replace(".", "/") + "/" + voClass.getSimpleName().replace("Vo", "CadastroAction") + ".java";
		String fileConsultaAction = sourceWebFolder + packageActions.replace(".", "/") + "/" + voClass.getSimpleName().replace("Vo", "ConsultaAction") + ".java";

		context.put("packageActions", packageActions);
		context.put("grupo", grupo);

		// Cadastro Action

		executeTemplate(ve, context, "templates/generate-cadastro-action.vm", fileCadastroAction);

		// Consulta Action

		executeTemplate(ve, context, "templates/generate-consulta-action.vm", fileConsultaAction);

		String fileCadastroJsp = webFolder + grupo + "/" + voClass.getSimpleName().replace("Vo", "").toLowerCase() + "/" + voClass.getSimpleName().replace("Vo", "cadastro.jsp").toLowerCase();
		String fileConsultaJsp = webFolder + grupo + "/" + voClass.getSimpleName().replace("Vo", "").toLowerCase() + "/" + voClass.getSimpleName().replace("Vo", "consulta.jsp").toLowerCase();
		String fileConsultaJspGrid = webFolder + grupo + "/" + voClass.getSimpleName().replace("Vo", "").toLowerCase() + "/" + voClass.getSimpleName().replace("Vo", "consulta_grid.jsp").toLowerCase();

		// Cadastro Jsp
		executeTemplate(ve, context, "templates/generate-cadastro-jsp.vm", fileCadastroJsp);

		// Consulta Jsp

		executeTemplate(ve, context, "templates/generate-consulta-jsp.vm", fileConsultaJsp);
		executeTemplate(ve, context, "templates/generate-consulta-grid-jsp.vm", fileConsultaJspGrid);

		for (int i = 0; i < 200; i++) {
			System.out.print("-");
		}
		System.out.println();

		System.out.println("Finalizando geração de código.");

		for (int i = 0; i < 200; i++) {
			System.out.print("-");
		}
		System.out.println();
	}

	private static void executeTemplate(VelocityEngine ve, Context context, String templateName, String filePath) {

		Template t = ve.getTemplate(templateName);

		// variavel que sera acessada no template:

		StringWriter writer = new StringWriter();

		// mistura o contexto com o template
		t.merge(context, writer);

		try {
			// Verifica se a pasta existe
			String folderName = filePath.substring(0, filePath.lastIndexOf("/"));

			File folder = new File(folderName);
			if (!folder.exists()) {
				System.out.println("Diretório não existe, criando...");
				folder.mkdirs();
			}

			File f = new File(filePath);
			if (!f.exists()) {

				System.out.println("Escrevendo arquivo " + f.getName() + " na pasta " + f.getPath() + "...");

				FileOutputStream fos = new FileOutputStream(f);
				fos.write(writer.toString().getBytes("ISO-8859-1"));
				fos.close();
			} else {
				System.out.println("Arquivo " + f.getName() + " na pasta " + f.getPath() + " já existe!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
