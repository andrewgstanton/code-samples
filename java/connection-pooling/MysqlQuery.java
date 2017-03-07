package *****

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Andy Stanton
 * handles all queries related to the db specified
 */
public class MysqlQuery {

	Logger logger = Logger.getLogger(MysqlQuery.class);

private Connection conn = null;
private String db_name = "";
private MysqlPool mysqlpool = null;

public MysqlQuery(String curr_db_name) {
    mysqlpool = MysqlPool.getInstance();
}

public MysqlQuery() {
    mysqlpool = MysqlPool.getInstance();
}

private void mysqlConnect() {
    try {
        conn = _mysqlpool.checkout();
        // setting to debug because we may want to turn off debug by setting log4j.properties to info as the 
        // the minimum level (instead of debug)
    	logger.debug("connection for MysqlPool = " + conn + "\n");

    } catch (Exception e) {
        conn = null;
        logger.fatal("Connector for pool failed.",e);
    }
}

private void mysqlClose() {
  if (conn != null) {
     try {
        mysqlpool.checkin(conn);
        conn = null;
     } catch (Exception e) {
         logger.fatal("mysqlClose failed.",e);
     }
 }
}

	/*
	 *  runs a sql update (UPDATE, INSERT OR DELETE)
	 */
	public void runUpdate(String sql) {
		mysqlConnect();
		Statement st = null;
		try {
			st = conn.createStatement();
			st.executeUpdate(sql);
		} catch (Exception ex) {
			System.out.println("error in runUpdate("+sql+") [MysqlQuery.java]\n");
			System.out.println("SQL:"+sql+"\n");
			System.out.println("Exception:"+ex);
		} finally {
			if (st != null) this.freeResources(st);
			st = null;
			mysqlClose();
		}
	}

	/* runs an insert, returns an int (for the last inserted id if the
	 * key is autoincrement
	 *
	 */
	public int runInsert(String sql) {
		mysqlConnect();
		int lastInsertedId = 0;
		Statement st = null;
		try {
			st = conn.createStatement();
			st.executeUpdate(sql);
		} catch (Exception ex) {
			System.out.println("error in runInsert("+sql+") [MysqlQuery.java]\n");
			System.out.println("SQL:"+sql+"\n");
			System.out.println("Exception:"+ex);
		} finally {
			lastInsertedId = getMySQLInsertId(st);
			if (st != null) this.freeResources(st);
			st = null;
			mysqlClose();
		}
		return lastInsertedId;
	}


/*
 * runs a sql select (SELECT)
 */

public ResultSet runQuery(String sql) {
        mysqlConnect();
        ResultSet rs = null;
        Statement st = null;
        try {
            logger.debug("starting execute sql:" + sql);

            st = conn.createStatement();
            logger.debug("statement created ...");

            rs = st.executeQuery(sql);

            logger.debug("finished executing sql");

        } catch (Exception ex) {
            logger.error("Error in runQuery!",ex);
        } finally {
            mysqlClose();
        }

        return rs;

}

public String mySQLNumRows(ResultSet rs) {

   int count = 0;
   String num = "0";
   if (rs != null) {
      try {
         rs.beforeFirst();
         count = 0;
         while (rs.next()) {
            count++;
         }
         num = Integer.toString(count);
       } catch (Exception ex) {
    	   logger.error("Exception in mySQLNumRows(): ",ex);
       }
   }
   return num;
}

public void freeResources(ResultSet rs) {
	if(rs != null){
       try {
          if( rs != null ) {
             Statement stmt = rs.getStatement();
             logger.debug("MysqlQuery.freeResources(ResultSet): getting Statement from ResultSet");
             if (stmt != null) stmt.close();
             logger.debug("MysqlQuery.freeResources(ResultSet): closing Statement");
             if( rs != null ) {
                rs.close();
                logger.debug("MysqlQuery.freeResources(ResultSet): closing ResultSet");
             }
          }
       } catch(Exception e){
           logger.error("Exception in MysqlQuery:freeResources: ",e);
       }

   }
}

public void freeResources(Statement st) {
       try {
          if( st != null ) {
             ResultSet rs = st.getResultSet();
             logger.debug("MysqlQuery.freeResources(Statement): getting ResultSet from Statement");
             if (rs != null) {
                rs.close();
                logger.debug("MysqlQuery.freeResources(Statement): closing ResultSet");
             }
             if (st!= null) {
                st.close();
                logger.debug("MysqlQuery.freeResources(Statement): closing Statement");
             } 
          }
       } catch(Exception e){
           logger.error("Exception in MysqlQuery:freeResources: ",e);
       }

   }

public int getMySQLInsertId(Statement st) {
    ResultSet rs = null;
    int newId = 0;
    if (st != null) {
       try {
          rs = st.getGeneratedKeys();
          if ( rs  != null &&  rs.next()) {
             newId = rs.getInt(1);
          }
        } catch (Exception ex) {
        	logger.error("Exception in MysqlQuery:getMYSQLInsertId: ",ex);
        }
    }
    return newId;
}

}
