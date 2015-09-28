package sql;

import java.sql.*;

public class SQLTest {
	public static void main (String[] args) throws SQLException{
		Connection c = DriverManager.getConnection("jdbc:sqlite:test.db");
		Statement s = c.createStatement();
		s.executeUpdate("DROP TABLE IF EXISTS Messages");
		s.executeUpdate("CREATE TABLE Messages " +
						"(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
						"GMID INTEGER UNIQUE NOT NULL,"+
						"Sender INTEGER NOT NULL,"+
						"Payload VARCHAR(1000) NOT NULL,"+
						"Timestamp INTEGER NOT NULL);");
		s.close();
		c.close();
	
	}
}
