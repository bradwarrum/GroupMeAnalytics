package network.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Locale;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import network.models.FTLMessages;
import network.models.FTLRoot;
import persistence.sql.HistoryDatabase;
public class WebPageHandler implements HttpHandler {
	
	private Configuration ftlconfig;
	private final String botname;
	private final HistoryDatabase history;
	public WebPageHandler(String botname, HistoryDatabase history) throws IOException {
		ftlconfig = new Configuration();
		ftlconfig.setDirectoryForTemplateLoading(new File("./web/templates/"));
		ftlconfig.setDefaultEncoding("UTF-8");
		ftlconfig.setLocale(Locale.US);
		ftlconfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);	
		
		this.history = history;
		this.botname = botname;
	}
	@Override
	public void handle(HttpExchange xchg) throws IOException {
		Template template = null;		
		try {
			template = ftlconfig.getTemplate("main.ftl");
		}catch (IOException e) {
			e.printStackTrace();
			return;
		}
		FTLMessages messages = history.getMessages(60, 30);
		if (messages == null) {xchg.close(); return;}
		FTLRoot root = new FTLRoot(botname, "Title goes here", messages, 60);
		try {
			template.process(root, new OutputStreamWriter(System.out));
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			xchg.close();
		}
	}

}
