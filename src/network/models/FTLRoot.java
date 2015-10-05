package network.models;

import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class FTLRoot implements TemplateHashModel {
	private SimpleScalar botname;
	private SimpleScalar title;
	private FTLMessages messages;
	private SimpleNumber targetElem;
	public FTLRoot(String botname, String title, FTLMessages messages, int targetElem) {
		this.botname = new SimpleScalar(botname);
		this.title = new SimpleScalar(title);
		this.messages = messages;
		this.targetElem = new SimpleNumber(targetElem);
	}
	@Override
	public TemplateModel get(String key) throws TemplateModelException {
		switch(key){
		case "botname": return botname;
		case "title": return title;
		case "messages":return messages;
		case "activeElem": return targetElem;
		}
		return null;
	}
	@Override
	public boolean isEmpty() throws TemplateModelException {
		return false;
	}
	
	
}
