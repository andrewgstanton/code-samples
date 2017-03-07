/*
 * Connection Pooling Algorithm for the DB
 * Written by Andy Stanton
 *
 */

package ***;

import java.util.*;
import java.sql.*;

import java.util.logging.Level;
import java.io.IOException;

//public class MysqlPool extends LogToFile {
public class MysqlPool {

//private Statement stmt = null;
//private String out = "";
private static MysqlPool thePool = new MysqlPool();

private int connectionCount = Env.MYSQL_POOL_CONNECTION_COUNT;
private Vector availableConnections = new Vector();
private Vector usedConnections = new Vector();
private String dburl = Env.MYSQL_POOL_CONNECTION_URL;
private String dbuser = Env.MYSQL_POOL_USER;
private String dbpass = Env.MYSQL_POOL_PASS;

private MysqlPool() { 
//   setLogFilePath("/var/log/apps/mysqllog.log");
   for (int x = 0; x < connectionCount; x++) {
      availableConnections.addElement(getConnection());
   }
//   logToFile("Mysql Pool Created with " + connectionCount + " connections \n");
}

public static MysqlPool getInstance() {

//   LogToFile.logToFile("/var/log/apps/mysqllog.log",Level.INFO,"MysqlPool.getInstance Called\n");
   return thePool;
}

private Connection getConnection() {
   Connection conn = null;
   try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      conn = DriverManager.getConnection(dburl, dbuser, dbpass);
   } catch (Exception ex) {
 //     logToFile(Level.SEVERE,"Error: " + ex.getMessage() + "\n");
      ex.printStackTrace();
      conn = null;
   }
   return conn;
}

public int availableCount() {
   return availableConnections.size();
}

public int usedCount() {
   return usedConnections.size();
}

public synchronized Connection checkout() {
   Connection newConn = null;
   if (availableConnections.size() ==0) {
      // Im out of connections. Create one more.
      newConn = getConnection();
      // Add this connection to the "Used" list.
      usedConnections.addElement(newConn);
      //Nothing else to do
   } else {
      // Connections exist!
      // Get a connection object
      newConn = (Connection)availableConnections.lastElement();
      //Remove it from the available list
      availableConnections.removeElement(newConn);
      //Add it to used list	
      usedConnections.addElement(newConn);
   }
   //Either way, we should have a connection object now
  // logToFile("Checkout: Available MysqlPool Connections: " + availableCount() + "\n");
  // logToFile("Checkout: Used MysqlPool Connections: " + usedCount() + "\n");

   return newConn;
}

public synchronized void checkin(Connection conn) {
   if (conn != null) {
      //remove from used list
      usedConnections.removeElement(conn);
      //add to available list
      availableConnections.addElement(conn);
   }
  // logToFile("Checkin: Available MysqlPool Connections: " + availableCount() + "\n");
  // logToFile("Checkin: Used MysqlPool Connections: " + usedCount() + "\n");
}

}

