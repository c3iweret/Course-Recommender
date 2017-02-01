package CEA_DB;

public class CourseInfo {
	  public String dept_code;
	  public int course_number;
	  public int course_id;
	  public int edition_id;
	
	  
	  
	  public CourseInfo() {
	    // TODO Auto-generated constructor stub
	  }
	  
	  //setters and getters
	  public String getDept_code(){
			return this.dept_code;
		}
	  
	  public int getCourse_number(){
		  return this.course_number;
	  }
		
	  public void setDept_code(String dept){
			this.dept_code = dept;
		}
	  
	  public void setCourse_number(int course){
		  this.course_number = course;
	  }
		
		
	  
}
