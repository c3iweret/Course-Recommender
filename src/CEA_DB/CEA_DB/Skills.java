package CEA_DB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Skills {
	public String skill;
	public int ranking;
  public int skill_id;
	  
	  
	public Skills() {
	    // TODO Auto-generated constructor stub
	}
	
	//setters and getters
	public String getSkill(){
		return this.skill;
	}
	
	public int getRanking(){
		return this.ranking;
	}
		
	public void setSkill(String skill){
		this.skill = skill;
	}
	
	public void setRanking(int rank){
		this.ranking = rank;
	}
	
    public int getId(Connection conn){
      try {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(String.format("select skill_id from skills where skill='%s'", this.skill));
        while(rs.next()){
            this.skill_id = rs.getInt(1);   
        }
        return this.skill_id;
      } catch (SQLException e) {
        return 0;
      }
  }

	    public boolean validateSkill(Connection conn){
	       Statement stmt;
	      try {
	        stmt = conn.createStatement();
	        ResultSet rs = stmt.executeQuery(String.format("select skill_id from skills where skill='%s'", skill));
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

