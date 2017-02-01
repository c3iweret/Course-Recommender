package CEA_DB;
import java.util.*;
import java.io.*;
import java.sql.*;

public class ConnectDB {
	public static final String PROGRAM_NAME = "DBConnection";
    public static Connection getConnection(Properties props) 
    {
        String driver = props.getProperty("driver");
        String url = props.getProperty("url");
        if (driver == null || url == null) {
        	System.out.println ("No JDBC driver or db URL specified in properties");
        	return null;
        }
        String user = props.getProperty("user","");
        String password = props.getProperty("password","");
        
    	// load the JDBC driver using the current class loader and db URL
        try {        	
        	Class.forName(driver);         	
        	return DriverManager.getConnection(url, user, password);
        }
        catch (ClassNotFoundException ce){
        	System.out.println("JDBC Driver not found");
        	return null;
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
    			e.printStackTrace();;
    		}
    	}
    }
    
    /** Test connection to database - first thing
     */
    public static void main (String [] args) throws IOException, SQLException  {
    	if (args.length == 0){
    		System.out.println("Usage: "+PROGRAM_NAME+" <name of properties file>");
    		System.exit(1);
    	}
    	Properties props = new Properties();
    	FileInputStream in = new FileInputStream(args[0]);
    	props.load(in);
    	in.close();
    	
    	Connection conn = getConnection (props);
    	if (conn == null) {
    		System.out.println("DB connection error");
    	}
    	else {
    		System.out.println("Yes! connection works");
    		conn.close();
    	}
    }
}

