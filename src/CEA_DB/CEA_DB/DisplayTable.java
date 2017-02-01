package CEA_DB;

import java.sql.*;

public class DisplayTable {
  
  public DisplayTable() {
    // TODO Auto-generated constructor stub
  }
  public static void print (Connection conn, String tblName) throws SQLException {
    Statement stmt = null;
    String query = "select * FROM " + tblName;
   
    try {
        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        int cols = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            for (int i=0; i< cols; i++)
                System.out.print (rs.getObject(i+1) + "\t");
            System.out.print("\n");            
        }
    } catch (SQLException e ) {
        e.printStackTrace();
       // JDBCTutorialUtilities.printSQLException(e);
    } finally {
        if (stmt != null) { stmt.close(); }
    }
}



}
