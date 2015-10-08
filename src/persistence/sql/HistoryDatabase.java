package persistence.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import com.sun.corba.se.spi.orbutil.fsm.State;

import core.DateUtils;
import network.models.FTLMessage;
import network.models.FTLMessages;
import network.models.JSONMessageResponse.Message;
import network.models.JSONMessageResponse.Message.Attachment;

public class HistoryDatabase {

	public class UserMapEntry {
		public final String username;
		@SuppressWarnings("unused")
		public final int internalID;
		public UserMapEntry(String username, int internalID) {
			this.username = username;
			this.internalID = internalID;
		}
	}
	private final Connection conn;
	private HashMap<String, UserMapEntry> UserIDMap = new HashMap<String, UserMapEntry>();
	private HashMap<Integer, String> UsernameMap = new HashMap<Integer, String>(); 
	private int nextUserID = 0;
	private int nextMsgID = 0;
	public HistoryDatabase() {
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:./data/history.db");
			conn.setAutoCommit(false);
		}catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalStateException("Database is all messed up");
		}
		initialize();
		cacheHighFreqInfo();
	}

	private void initialize(){
		Statement s = null;
		try {		
			s = conn.createStatement();
			s.executeUpdate("CREATE TABLE IF NOT EXISTS Users " +
					"(ID INTEGER PRIMARY KEY NOT NULL," +
					"GMUserID VARCHAR(16) UNIQUE NOT NULL," +
					"CurrentAvatar VARCHAR(256)," +
					"CurrentNickname VARCHAR(64) NOT NULL);");
			s.executeUpdate("INSERT OR IGNORE INTO Users (ID, GMUserID, CurrentAvatar, CurrentNickname) VALUES(-1, 'system', NULL, 'GroupMe');");
			s.executeUpdate("CREATE TABLE IF NOT EXISTS Messages " +
					"(ID INTEGER PRIMARY KEY NOT NULL,"+
					"GMID INTEGER UNIQUE NOT NULL,"+
					"Sender INTEGER NOT NULL REFERENCES Users(ID),"+
					"IsSystem BOOLEAN NOT NULL," +
					"Payload VARCHAR(1000),"+
					"ImageURL VARCHAR(1000),"+
					"SendTime BIGINT NOT NULL);");
			s.executeUpdate("CREATE INDEX IF NOT EXISTS msgbytime ON Messages(SendTime);");
			s.executeUpdate("CREATE TABLE IF NOT EXISTS Nicknames " +
					"(ID INTEGER PRIMARY KEY NOT NULL," +
					"UserID INTEGER NOT NULL REFERENCES Users(ID)," +
					"FirstMessage INTEGER NOT NULL REFERENCES Messages(ID)," +
					"LastMessage INTEGER REFERENCES Messages(ID)," +
					"Nickname VARCHAR(64) NOT NULL);");
			conn.commit();
			s.close();	
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalStateException("Database is all messed up");
		}
	}

	private void cacheHighFreqInfo(){
		try {		
			Statement s = conn.createStatement();
			ResultSet results = s.executeQuery("SELECT ID, GMUserID, CurrentNickname FROM Users;");
			while(results.next()) {
				int internalID = results.getInt(1);
				String groupmeID = results.getString(2);
				String nickname = results.getString(3);
				UsernameMap.put(internalID, nickname);
				UserIDMap.put(groupmeID, new UserMapEntry(nickname, internalID));
			}
			results.close();
			results = s.executeQuery("SELECT MAX(ID) FROM Users;");
			if (results.next()) {
				nextUserID = results.getInt(1) + 1;
				if (nextUserID == 0) nextUserID = 1;
			} else {
				nextUserID = 1;
			}
			results.close();
			results = s.executeQuery("SELECT MAX(ID) FROM Messages;");
			if (results.next()) {
				nextMsgID = results.getInt(1) + 1;
			} else {
				nextMsgID = 1;
			}
			results.close();
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalStateException("Database is all messed up");
		}		

	}

	public void drop() {
		try {
			Statement s = conn.createStatement();
			s.executeUpdate("DROP TABLE IF EXISTS Nicknames;");
			s.executeUpdate("DROP TABLE IF EXISTS Users;");
			s.executeUpdate("DROP TABLE IF EXISTS Messages");
			UserIDMap.clear();
			nextMsgID = 0;
			nextUserID = 0;
			initialize();
			cacheHighFreqInfo();
			s.close();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalStateException("Database is all messed up");
		}
	}

	public String latestMessageID() {
		String messageID = "0";
		try {
			Statement s = conn.createStatement();
			ResultSet results = s.executeQuery("SELECT MAX(GMID) FROM Messages;");
			if (results.next()) {
				messageID = results.getString(1);
				if (messageID == null) messageID = "0";
			}
			results.close();
			s.close();
			return messageID;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalStateException("Database is all messed up");
		}
	}
	public int totalNumMessages() {
		int totalMessages = 0;
		try {
			Statement s = conn.createStatement();
			ResultSet results = s.executeQuery("SELECT MAX(ID) FROM Messages;");
			if (results.next()) {
				totalMessages = results.getInt(1);
			}
			results.close();
			s.close();
			return totalMessages;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalStateException("Database is all messed up");
		} 
	}
	
	public String translateToGMID (int messageID) {
		String translation = "0";
		try {
			Statement s = conn.createStatement();
			ResultSet results = s.executeQuery("SELECT (GMID) FROM Messages WHERE (ID=" + messageID + ");");
			if (results.next()) {
				translation = results.getString(1);
				if (translation == null) translation = "0";
			}
			
			results.close();
			s.close();
			return translation;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalStateException("Database is all messed up");
		}
	}

	public UserMapEntry userFromID(String userGMID) {
		return UserIDMap.get(userGMID);
	}

	public String usernameFromInternalID(int internalID) {
		return UsernameMap.get(internalID);
	}


	public MessageProcessingStats processMessage(Message respmsg) {
		byte userid = (byte) 0xFF;
		if (!respmsg.system) {
			UserMapEntry user = UserIDMap.get(respmsg.userID);
			if (user == null || !user.username.equals(respmsg.senderName)) {
				try {
					PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO Users (ID, GMUserID, CurrentAvatar, CurrentNickname) VALUES (?, ?, ?, ?);");
					ps.setInt(1, (user == null) ? nextUserID : user.internalID);
					ps.setString(2, respmsg.senderID);
					ps.setString(3, respmsg.userAvatar);
					ps.setString(4, respmsg.senderName);
					if (0 == ps.executeUpdate()) {
						throw new SQLException("Update failed for user insert");
					}
					ps.close();
					if (user != null) {
						ps = conn.prepareStatement("UPDATE Nicknames SET LastMessage=? WHERE (UserID=? AND LastMessage IS NULL);");
						ps.setInt(1, nextMsgID);
						ps.setInt(2, user.internalID);
						if (1 != ps.executeUpdate()) {
							throw new SQLException("Update failed for user insert");
						}
						ps.close();
					}					
					ps = conn.prepareStatement("INSERT INTO Nicknames (UserID, FirstMessage, Nickname) VALUES (?, ?, ?);");
					ps.setInt(1, (user == null) ? nextUserID : user.internalID);
					ps.setInt(2, nextMsgID);
					ps.setString(3, respmsg.senderName);
					if (0 == ps.executeUpdate()) {
						throw new SQLException("Update failed for user insert");
					}
					ps.close();
					conn.commit();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new IllegalStateException("Database is all messed up");
				}
				UsernameMap.put(user == null ? nextUserID : user.internalID, respmsg.senderName);
				user = new UserMapEntry(respmsg.senderName, user == null ? nextUserID++ : user.internalID);

				UserIDMap.put(respmsg.userID, user);				
			}
			userid = (byte) user.internalID;			
		}
		try {
			String imageURL = null;
			for (Attachment attachment : respmsg.attachments) {
				if (attachment.type.equals("image")) {
					imageURL = attachment.url;
					break;
				}
			}
			PreparedStatement ps = conn.prepareStatement("INSERT INTO Messages (ID, GMID, Sender, IsSystem, Payload, SendTime, ImageURL) " +
					"VALUES (?,?,?,?,?,?,?);");
			ps.setInt(1, nextMsgID);
			ps.setString(2, respmsg.messageID);
			ps.setByte(3, userid);
			ps.setBoolean(4, respmsg.system);
			ps.setString(5, respmsg.text);
			ps.setLong(6, respmsg.timestamp);
			ps.setString(7, imageURL);
			if (0 == ps.executeUpdate()) {
				throw new SQLException("Message update failed");
			}
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalStateException("Database is all messed up");
		}		
		return new MessageProcessingStats(1, nextMsgID++, respmsg.messageID, userid);

	}
	
	public void commit() throws SQLException{
		conn.commit();
	}

	public FTLMessages getMessages(int targetInternalID, int contextCount) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement("SELECT M.ID, M.Sender, M.IsSystem, M.Payload, M.ImageURL, M.SendTime, U.CurrentAvatar, U.CurrentNickname " +
					"FROM Messages AS M INNER JOIN Users AS U ON (M.Sender=U.ID) WHERE (M.ID > ? AND M.ID < ?) ORDER BY M.ID;");
			ps.setInt(1, targetInternalID - contextCount / 2);
			ps.setInt(2, targetInternalID + contextCount / 2);
			ResultSet results = ps.executeQuery();
			if (results == null) return null;
			FTLMessages messages = new FTLMessages();
			while (results.next()) {
				FTLMessage message = new FTLMessage(results.getString(8), 
													results.getString(7), 
													DateUtils.getChatTimestamp(results.getLong(6)), 
													results.getString(4).replace("\n", "<br/>"), 
													results.getString(5),
													results.getInt(1));
				messages.addMessage(message);
			}
			results.close();
			ps.close();
			return messages;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}		

	}
}
