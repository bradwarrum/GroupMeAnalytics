package network.models;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JSONMessageResponse {
	public class Message {
		public class Attachment {
			public String type;
			public String url;
			
		}
		@SerializedName("created_at")
		public long timestamp;
		@SerializedName("id")
		public String messageID;
		@SerializedName("name")
		public String senderName;
		@SerializedName("sender_id")
		public String senderID;
		@SerializedName("sender_type")
		public String senderType;
		@SerializedName("system")
		public boolean system;
		@SerializedName("text")
		public String text;
		@SerializedName("user_id")
		public String userID;
		@SerializedName("avatar_url")
		public String userAvatar;
		public List<Attachment> attachments;
	}
	public class InternalWrapper {
		public List<Message> messages;
		public int count;
	}
	
	public class JSONMeta {
		@SerializedName("code")
		public int status;
		@Expose(serialize = false, deserialize = false)
		public int actualStatus;		
	}
	
	@SerializedName("meta")
	public JSONMeta meta;
	@SerializedName("response")
	public InternalWrapper data;
	
	public static void main (String[] args) {
		String test = "{   \"response\": {     \"count\": 52816,     \"messages\": [       {         \"attachments\": [],         \"avatar_url\": \"http://i.groupme.com/a74b9e105e7801306b5d12313d092470\",         \"created_at\": 1361476137,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136147613778295618\",         \"name\": \"GroupMe\",         \"sender_id\": \"9413210\",         \"sender_type\": \"user\",         \"source_guid\": \"a7a054c05e8d0130d036123138156909\",         \"system\": false,         \"text\": \"Tyler Wood added Eli Magers, Evan Rogers, Kevin Thang, Chris Markiewicz, and Brad Warrum to the group\",         \"user_id\": \"9413210\"       },       {         \"attachments\": [],         \"avatar_url\": \"http://i.groupme.com/a74b9e105e7801306b5d12313d092470\",         \"created_at\": 1361476169,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136147616093669152\",         \"name\": \"Tyler Wood\",         \"sender_id\": \"9413210\",         \"sender_type\": \"user\",         \"source_guid\": \"ios383168961.200735E2DD00CA-753C-4498-B3EB-919A202C49F9\",         \"system\": false,         \"text\": \"Someone suggested this a while back, didnt they?\",         \"user_id\": \"9413210\"       },       {         \"attachments\": [],         \"avatar_url\": null,         \"created_at\": 1361477362,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136147735668822516\",         \"name\": \"Brad Warrum\",         \"sender_id\": \"2305544\",         \"sender_type\": \"user\",         \"source_guid\": \"7e225050-5e90-0130-28bb-1231381d515c\",         \"system\": false,         \"text\": \"Yes, did you get the app?\",         \"user_id\": \"2305544\"       },       {         \"attachments\": [],         \"avatar_url\": \"http://i.groupme.com/445ddc004e080130d50f1231381d2930\",         \"created_at\": 1361477667,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136147766077405946\",         \"name\": \"Kevin Thang\",         \"sender_id\": \"8580227\",         \"sender_type\": \"user\",         \"source_guid\": \"ios383170559.738257487E85C3-75DB-42AA-82FC-D8C82E13F434\",         \"system\": false,         \"text\": \"Awesome, I am now in 5 different GroupMe groups...\",         \"user_id\": \"8580227\"       },       {         \"attachments\": [],         \"avatar_url\": null,         \"created_at\": 1361477793,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136147779396434431\",         \"name\": \"Chris Markiewicz\",         \"sender_id\": \"9416948\",         \"sender_type\": \"user\",         \"source_guid\": \"82c4bf00-5e91-0130-28bb-1231381d515c\",         \"system\": false,         \"text\": \"Does this cost me extra?\",         \"user_id\": \"9416948\"       },       {         \"attachments\": [],         \"avatar_url\": \"http://i.groupme.com/51a532e0552801307dce22000a1c45c8\",         \"created_at\": 1361478363,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136147836034373019\",         \"name\": \"Eli Magers\",         \"sender_id\": \"9168814\",         \"sender_type\": \"user\",         \"source_guid\": \"958ae604-db6d-4b00-986d-3b35172e5745\",         \"system\": false,         \"text\": \"What!? We gotta start over!?\",         \"user_id\": \"9168814\"       },       {         \"attachments\": [           {             \"type\": \"image\",             \"url\": \"http://i.groupme.com/e22ff2705e92013092871231392705e6\"           }         ],         \"avatar_url\": \"http://i.groupme.com/51a532e0552801307dce22000a1c45c8\",         \"created_at\": 1361478384,         \"favorited_by\": [           \"2305544\",           \"9168814\"         ],         \"group_id\": \"3689527\",         \"id\": \"136147838413036450\",         \"name\": \"Eli Magers\",         \"sender_id\": \"9168814\",         \"sender_type\": \"user\",         \"source_guid\": \"2a8247d3-89fa-47e0-9d5b-46cedfeeb607\",         \"system\": false,         \"text\": null,         \"user_id\": \"9168814\"       },       {         \"attachments\": [],         \"avatar_url\": null,         \"created_at\": 1361478411,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136147840923418623\",         \"name\": \"Chris Markiewicz\",         \"sender_id\": \"9416948\",         \"sender_type\": \"user\",         \"source_guid\": \"f17f5a30-5e92-0130-28bb-1231381d515c\",         \"system\": false,         \"text\": \"To mom.\",         \"user_id\": \"9416948\"       },       {         \"attachments\": [],         \"avatar_url\": \"http://i.groupme.com/51a532e0552801307dce22000a1c45c8\",         \"created_at\": 1361478435,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136147842968380511\",         \"name\": \"Eli Magers\",         \"sender_id\": \"9168814\",         \"sender_type\": \"user\",         \"source_guid\": \"43f0ba5f-95cb-4120-86e1-0d815b0ee70b\",         \"system\": false,         \"text\": \"This isn't your mom\",         \"user_id\": \"9168814\"       },       {         \"attachments\": [],         \"avatar_url\": null,         \"created_at\": 1361478464,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136147846221195336\",         \"name\": \"Chris Markiewicz\",         \"sender_id\": \"9416948\",         \"sender_type\": \"user\",         \"source_guid\": \"1114a150-5e93-0130-28bb-1231381d515c\",         \"system\": false,         \"text\": \"Ur mom.\",         \"user_id\": \"9416948\"       },       {         \"attachments\": [],         \"avatar_url\": \"http://i.groupme.com/51a532e0552801307dce22000a1c45c8\",         \"created_at\": 1361480196,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136148019608651722\",         \"name\": \"Eli Magers\",         \"sender_id\": \"9168814\",         \"sender_type\": \"user\",         \"source_guid\": \"95ff1495-816c-4bd8-9365-3106ae1b03c3\",         \"system\": false,         \"text\": \"Jolly good comeback, sir! I should of known getting in a debate with a scholar as fine as you would only end in my embarrassment! Well played, friend!\",         \"user_id\": \"9168814\"       },       {         \"attachments\": [],         \"avatar_url\": null,         \"created_at\": 1361480286,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136148027885581882\",         \"name\": \"Chris Markiewicz\",         \"sender_id\": \"9416948\",         \"sender_type\": \"user\",         \"source_guid\": \"4be08530-5e97-0130-0a9b-1231381a6831\",         \"system\": false,         \"text\": \"Would you care for a spot of tea good sir?\",         \"user_id\": \"9416948\"       },       {         \"attachments\": [],         \"avatar_url\": \"http://i.groupme.com/51a532e0552801307dce22000a1c45c8\",         \"created_at\": 1361480440,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136148043919750920\",         \"name\": \"Eli Magers\",         \"sender_id\": \"9168814\",         \"sender_type\": \"user\",         \"source_guid\": \"c82c9089-8f67-4572-af1f-0f77288ede19\",         \"system\": false,         \"text\": \"Can we kick Chris, please?\",         \"user_id\": \"9168814\"       },       {         \"attachments\": [],         \"avatar_url\": \"http://i.groupme.com/51a532e0552801307dce22000a1c45c8\",         \"created_at\": 1361480478,         \"favorited_by\": [           \"2305544\",           \"9168814\"         ],         \"group_id\": \"3689527\",         \"id\": \"136148047719099480\",         \"name\": \"Eli Magers\",         \"sender_id\": \"9168814\",         \"sender_type\": \"user\",         \"source_guid\": \"b8288396-baf5-4796-afc2-7d10a588835d\",         \"system\": false,         \"text\": \"Also, why isn't Derek in this?\",         \"user_id\": \"9168814\"       },       {         \"attachments\": [],         \"avatar_url\": \"http://i.groupme.com/51a532e0552801307dce22000a1c45c8\",         \"created_at\": 1361480544,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136148053595473842\",         \"name\": \"Eli Magers\",         \"sender_id\": \"9168814\",         \"sender_type\": \"user\",         \"source_guid\": \"31496c0a-93c9-4246-8095-c27924ab87ff\",         \"system\": false,         \"text\": \"?. <--- what the fuck is this?\",         \"user_id\": \"9168814\"       },       {         \"attachments\": [],         \"avatar_url\": \"http://i.groupme.com/51a532e0552801307dce22000a1c45c8\",         \"created_at\": 1361480563,         \"favorited_by\": [           \"9413210\"         ],         \"group_id\": \"3689527\",         \"id\": \"136148056368964893\",         \"name\": \"Eli Magers\",         \"sender_id\": \"9168814\",         \"sender_type\": \"user\",         \"source_guid\": \"18b9cd29-cbe8-4c96-b2ff-7906a9927943\",         \"system\": false,         \"text\": \"?\",         \"user_id\": \"9168814\"       },       {         \"attachments\": [],         \"avatar_url\": null,         \"created_at\": 1361483155,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136148314968966604\",         \"name\": \"Chris Markiewicz\",         \"sender_id\": \"9416948\",         \"sender_type\": \"user\",         \"source_guid\": \"fb092870-5e9d-0130-28bb-1231381d515c\",         \"system\": false,         \"text\": \"Playstation 4!\",         \"user_id\": \"9416948\"       },       {         \"attachments\": [],         \"avatar_url\": \"http://i.groupme.com/a74b9e105e7801306b5d12313d092470\",         \"created_at\": 1361483286,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136148328151411832\",         \"name\": \"GroupMe\",         \"sender_id\": \"9413210\",         \"sender_type\": \"user\",         \"source_guid\": \"499f32c05e9e0130946b123139183de4\",         \"system\": false,         \"text\": \"Tyler Wood added Derek Pearson to the group\",         \"user_id\": \"9413210\"       },       {         \"attachments\": [],         \"avatar_url\": \"http://i.groupme.com/a74b9e105e7801306b5d12313d092470\",         \"created_at\": 1361483347,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136148333894937920\",         \"name\": \"Tyler Wood\",         \"sender_id\": \"9413210\",         \"sender_type\": \"user\",         \"source_guid\": \"ios383176139.2146125B10408D-41A6-4DA6-953D-50CF89CED7DC\",         \"system\": false,         \"text\": \"I WANT A TOP HAT ELI HOW DO I DO A TOP HAT!!\",         \"user_id\": \"9413210\"       },       {         \"attachments\": [],         \"avatar_url\": \"http://i.groupme.com/51a532e0552801307dce22000a1c45c8\",         \"created_at\": 1361487804,         \"favorited_by\": [],         \"group_id\": \"3689527\",         \"id\": \"136148780319804646\",         \"name\": \"Eli Magers\",         \"sender_id\": \"9168814\",         \"sender_type\": \"user\",         \"source_guid\": \"d24cd228-8ed2-4068-80a0-d9fbe8518752\",         \"system\": false,         \"text\": \"You click on it\",         \"user_id\": \"9168814\"       }     ]   },   \"meta\": {     \"code\": 200   } }";
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
		JSONMessageResponse res = gson.fromJson(test, JSONMessageResponse.class);
		System.out.println("DONE");
	}
}