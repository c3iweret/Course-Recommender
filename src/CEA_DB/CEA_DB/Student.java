package CEA_DB;

import java.sql.*;
import java.util.ArrayList;

public class Student {
	  
	  
	public String username;
	public int age = 0;
	public String gender = null;
	public String native_country = null;
	public int permission;
	public float distance;
	public ArrayList<CourseInfo> cInfo = new ArrayList<CourseInfo>();
	public ArrayList<Interests> interests = new  ArrayList<Interests>();
	public ArrayList<Skills> skills = new ArrayList<Skills>();
	public int recommendation_preference = 0;
	  
	private String insertSQL = "INSERT INTO students (username, permission, age, gender, native_country)"
	        + " VALUES (?,?,?,?,?)";
	private String querySQL = "SELECT * from students WHERE username=?";
	  //private String updateSQL = "UPDATE students SET age=?, gender=? WHERE name=?";

	PreparedStatement stmt = null;
	  
	public Student() {
	    // TODO Auto-generated constructor stub
	  }
	  
	public void addToDatabase (Connection conn) throws SQLException{
	    if (!this.validate()){
	        System.out.println("Student fields not set");
	        System.exit(1);
	    }       
	    
	    try {           
	        stmt = conn.prepareStatement(insertSQL);
	        stmt.setString(1,this.username);    
	        stmt.setInt(2, this.permission);               
	        stmt.setInt(3, this.age);
	        stmt.setString(4, this.gender);
	        stmt.setString(5, this.native_country);
	        stmt.execute();
	    } catch (SQLException e) {
	        e.printStackTrace();;
	    } finally {
	        if (stmt != null) {
	            stmt.close();
	        }
	    }
	  }
	  
	  public boolean queryDatabase(Connection conn) throws SQLException{
	    try{
	      
	      stmt = conn.prepareStatement(querySQL);
	      stmt.setString(1,this.username);
	      ResultSet rs = stmt.executeQuery();
	      if (!rs.isBeforeFirst() ) {    
	          return false;
	        }
  			else {
	        while (rs.next()) {
	          this.username = rs.getString(1);
	          this.age = rs.getInt(3);
	          this.gender = rs.getString(4);
              System.out.format("Name: %s Age %d Gender %s \n", username, age, gender);
            }
          return true;
	      }
	    }catch (SQLException e) {
	      System.out.println("oops, there was an error accessing the database");
	  } finally {
	      if (stmt != null) {
	          stmt.close();
	      }
	  }
	    return true;
	  }
	  

	//  public void updateInDatabase (Connection conn) throws SQLException{
//	    if (!this.validate()){
//	        System.out.println("Person fields not set");
//	        System.exit(1);
//	    }       
	//    
//	    try {           
//	        stmt = conn.prepareStatement(updateSQL);
//	        stmt.setInt(1,this.age);    
//	        stmt.setString(2, this.gender);             
//	        stmt.setString(3, this.name);
//	        stmt.execute();
//	    } catch (SQLException e) {
//	        SQLError.show(e);
//	    } finally {
//	        if (stmt != null) {
//	            stmt.close();
//	        }
//	    }

	    
	    
	    public boolean validate () {
	      if (this.username == null)
	          return false;
	      if (this.age <=15 || this.age >=100)
	          return false;
	      if (this.gender == null)
	    	  return true;
	      
	      if ((!this.gender.equals("m") && (!this.gender.equals("f"))))
	          return false;
	      return true;
	  }
	    
	  
	    //getters and setters
		public String getUsername() {
			return this.username;
		}

		public int getAge () {
			return this.age;
		}
		
		public String getGender () {
			return this.gender;
		}
		
		public String getCountry(){
			return this.native_country;
		}
		
		public int getPermission(){
			return this.permission;
		}
		
		public void setUsername (String name){
			this.username = name;
		}
		
		public void setAge (int age){
			this.age = age;
		}
		
		public void setGender (String gender){
			this.gender = gender;
		}
		
		public void setCountry(String country){
			this.native_country = country;
		}
		
		public void setPermission(int permission){
			this.permission = permission;
		}
		
		
			
	  
	    
	}


