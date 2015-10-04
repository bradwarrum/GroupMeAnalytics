package network.models;

import java.util.List;

public class JSONUrbanResponse {
	public List<String> tags;
	public String result_type;
	public List<Definition> list;
	
	public class Definition {
		public String word;
		public String permalink;
		public String definition;
		public String example;
	}
}
