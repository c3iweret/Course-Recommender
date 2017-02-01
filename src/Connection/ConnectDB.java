package Connection;
import java.util.*;
import java.io.*;
import java.sql.*;
import CEA_DB.*;

public class ConnectDB {
    public static final String PROGRAM_NAME = "DBConnection";
    public static Connection getConnection() 
    {
        String url = "jdbc:postgresql://localhost:5432/csc343h-c5mcquil";
        Properties props = new Properties();
        props.setProperty("driver","org.postgresql.Driver");
        
        try {        
           System.out.println("connecting");
          return DriverManager.getConnection(url, props);
        }
        catch (SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(null,ex,"Failed to connect with database",javax.swing.JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    public static void closeConnection ( java.sql.Connection conn){
        if (conn !=null) {
            try {
                conn.close();
            }
            catch (SQLException e){
                SQLError.print(e);
            }
        }
    }
    
    /** Test connection to database - first thing
     */
    public static void main (String [] args) throws IOException, SQLException  {
//        if (args.length == 0){
//            System.out.println("Usage: "+PROGRAM_NAME+" <name of properties file>");
//            System.exit(1);
//        }
//        Properties props = new Properties();
//        FileInputStream in = new FileInputStream(args[0]);
//        props.load(in);
//        in.close();
        
        Connection conn = getConnection ();
        if (conn == null) {
            System.out.println("DB connection error");
        }
        else {
            System.out.println("Yes! connection works");
            conn.close();
        }
    }
}
