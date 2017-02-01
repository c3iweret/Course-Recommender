package CEA_DB;
import java.sql.*;

public class SQLError {
    public static void print(SQLException ex) {
        while (ex != null) {
              System.err.println("SQLState: " + ex.getSQLState());
              System.err.println("Error Code: " + ex.getErrorCode());
              System.err.println("Message: " + ex.getMessage());
              Throwable t = ex.getCause();
              while (t != null) {
                System.out.println("Cause: " + t);
                t = t.getCause();
              }
              ex = ex.getNextException();
        }
    }
    
    public static void show(SQLException ex){
         javax.swing.JOptionPane.showMessageDialog(null,ex.getMessage()+"\n"+ex.getSQLState(),"SQL error "+ex.getErrorCode(),javax.swing.JOptionPane.ERROR_MESSAGE);
    }
}
