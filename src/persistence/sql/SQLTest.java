package persistence.sql;

import java.sql.*;

public class SQLTest {
	public static void main (String[] args) throws SQLException{
		Connection c = DriverManager.getConnection("jdbc:sqlite:test.db");
		Statement s = c.createStatement();
		s.executeUpdate("DROP TABLE IF EXISTS Messages");
		s.executeUpdate("DROP TABLE IF EXISTS Users");
		s.executeUpdate("DROP TABLE IF EXISTS Nicknames");
		s.executeUpdate("CREATE TABLE Messages " +
						"(ID INTEGER PRIMARY KEY NOT NULL,"+
						"GMID INTEGER NOT NULL,"+
						"Sender INTEGER NOT NULL,"+
						"IsSystem BOOLEAN NOT NULL," +
						"Payload VARCHAR(1000) NOT NULL,"+
						"SendTime BIGINT NOT NULL);");
		s.executeUpdate("CREATE UNIQUE INDEX msgbyuser ON Messages(Sender);");
		s.executeUpdate("CREATE UNIQUE INDEX msgbytime ON Messages(SendTime);");
		s.executeUpdate("CREATE TABLE Users " +
						"(ID INTEGER PRIMARY KEY NOT NULL," +
						"GMUserID INTEGER UNIQUE NOT NULL," +
						"TotalMessages INTEGER NOT NULL," +
						"CurrentAvatar VARCHAR(256) NOT NULL," +
						"CurrentNickname VARCHAR(64) NOT NULL);");
		s.executeUpdate("CREATE TABLE Nicknames " +
						"(ID INTEGER PRIMARY KEY NOT NULL," +
						"UserID INTEGER NOT NULL REFERENCES Users(GMUserID)," +
						"FirstMessage INTEGER NOT NULL REFERENCES Messages(ID)," +
						"LastMessage INTEGER REFERENCES Messages(ID)," +
						"Nickname VARCHAR(64) NOT NULL);");
						
		s.close();
		c.close();
	
	}
}
