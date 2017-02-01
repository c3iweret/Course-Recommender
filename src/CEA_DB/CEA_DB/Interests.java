package CEA_DB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Interests {
	public String topic;
	public int rating;
  public int topic_id;
	
	
	public Interests() {
	    // TODO Auto-generated constructor stub
	  }
	
	//getters and setters
	public String getTopic(){
		return this.topic;
	}
	
	public int getRating(){
		return this.rating;
	}
		
	public void setTopic(String topic){
		this.topic = topic;
	}
	
	public void setRating(int rank){
		this.rating = rank;
	}
	

	
	   public int getId(Connection conn){
         try {
          Statement stmt = conn.createStatement();
           ResultSet rs = stmt.executeQuery(String.format("select topic_id from topics where topic='%s'", this.topic));
           while(rs.next()){
             
             this.topic_id = rs.getInt(1);
           }
           return this.topic_id;
        } catch (SQLException e) {
        }
         return 0;
       }

	public boolean validateTopic(Connection conn){
	   Statement stmt;
      try {
        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(String.format("select topic_id from topics where topic='%s'", topic));
        if (!rs.isBeforeFirst() ) {  
          System.out.println("Sorry, it seems this topic is not in the database");
          return false;
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
      return true;

	      
	}
	
}
