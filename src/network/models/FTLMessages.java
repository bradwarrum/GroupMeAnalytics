package network.models;

import java.util.ArrayList;
import java.util.List;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;

public class FTLMessages implements TemplateSequenceModel {

	private List<FTLMessage> messages = new ArrayList<FTLMessage>();
	
	public FTLMessages() {
	}
	
	public void addMessage(FTLMessage message) {
		messages.add(message);
	}
	@Override
	public TemplateModel get(int index) throws TemplateModelException {
		return messages.get(index);
	}

	@Override
	public int size() throws TemplateModelException {
		return messages.size();
	}

}
