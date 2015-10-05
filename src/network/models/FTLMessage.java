package network.models;

import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class FTLMessage implements TemplateHashModel {

	private SimpleScalar sender;
	private SimpleScalar avatarURL;
	private SimpleScalar date;
	private SimpleScalar text;
	private SimpleScalar imageURL;
	private SimpleNumber messageID;
	public FTLMessage (String sender, String avatarURL, String date, String text, String imageURL, int messageID) {
		this.sender = new SimpleScalar(sender);
		this.avatarURL = new SimpleScalar(avatarURL);
		this.date = new SimpleScalar(date);
		this.text = new SimpleScalar(text);
		this.imageURL = new SimpleScalar(imageURL);
		this.messageID = new SimpleNumber(messageID);
	}
	@Override
	public TemplateModel get(String key) throws TemplateModelException {
		switch (key) {
		case "sender": return sender;
		case "avatarURL":return avatarURL;
		case "date":return date;
		case "text":return text;
		case "imageURL": return imageURL;
		case "messageID": return messageID;
		}
		return null;
	}

	@Override
	public boolean isEmpty() throws TemplateModelException {
		return false;
	}

}
