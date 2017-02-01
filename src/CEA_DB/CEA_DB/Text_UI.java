package CEA_DB;


import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;





public class Text_UI {
  public Text_UI() {
    // TODO Auto-generated constructor stub
  }

  // method to check if a string contains alphabets
  public static boolean isAlpha(String name) {
    return name.matches("[a-zA-Z]+");
  }

  // method to check if a string contains digits
  public static boolean isDigit(String name) {
    return name.matches("[0-9]+");
  }

  // method to display all topics
  public static void displayTopics(Connection conn) throws SQLException {
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(
        "select distinct dept_name, topic from topics A, topic_interests B, courses C, departments "
            + "D where A.topic_id=B.topic_id and B.course_id=C.course_id and D.dept_code=C.dept_code order by 1");
    while (rs.next()) {
      System.out.format("%-40s%-40s\n", rs.getString(1), rs.getString(2));
    }
  }

  // method to display all skills
  public static void displaySkills(Connection conn) throws SQLException {
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(
        "select distinct dept_name, skill from skills A, skill_rankings B, courses C, departments "
            + "D where A.skill_id=B.skill_id and B.course_id=C.course_id and D.dept_code=C.dept_code order by 1");
    while (rs.next()) {
      System.out.format("%-40s%-40s\n", rs.getString(1), rs.getString(2));
    }
  }

  public static void contributeToDatabase(Connection conn, Student s) {
    Statement stmt;
    ResultSet rs;
    
    for (CourseInfo c : s.cInfo) {
      Scanner scanner = new Scanner(System.in);
      try {
        stmt = conn.createStatement();
        rs = stmt.executeQuery(String.format(
            "select * from courses where dept_code='%s' and course_number='%i'",
            c.dept_code, c.course_number));
        if (!(rs.isBeforeFirst())) {
          System.out.println(String.format("What is the course name of %s%i?",
              c.dept_code, c.course_number)); 
          String course_name = scanner.nextLine();
          stmt = conn.createStatement();
          stmt.executeUpdate(String.format(
              "insert into courses(dept_code, course_number, course_name) values ('%s', '%i','%s')",
              c.dept_code, c.course_number, course_name));
          scanner.close();
        }
        stmt = conn.createStatement();
        rs = stmt.executeQuery(String.format(
            "select course_id from courses where course_number='%i' and course_name='%s'",
            c.course_number, c.dept_code));
        while (rs.next()) {
          c.course_id = rs.getInt(1);
        }

      } catch (SQLException e) {
        continue;
      }
      System.out.println(String.format("Would you like to provide information about %s%i? (y/n)", c.dept_code, c.course_number));
      String ans = scanner.nextLine();
      if(ans.equals("y")){
        addEditionsToDatabase(conn, c);
        addEnrollmentsToDatabase(conn, s, c);
        addSkillsToDatabase(conn,s,c);
        addInterestsToDatabase(conn,s,c);
      }
      
      scanner.close();
    }
    
  }


  public static void addEditionsToDatabase(Connection conn, CourseInfo c) {
    Scanner scanner = new Scanner(System.in);
    System.out.println(
        "In what semester and year did you take this course? (e.g. Fall 2007)");
      System.out.println(
          "In what semester and year did you take this course? (e.g. Fall 2007)");
      String semyear = scanner.nextLine();
      System.out.println("How many students took the course?");
      String num_students = scanner.nextLine();
      System.out.println(
          "What time of day did the course take place (e.g. morning, afternoon, or evening)");
      String timeday = scanner.nextLine();
      String semester = "";
      String year = "";
      if (semyear != null) {
        String[] semesterparts = semyear.split(" ");
        if (semesterparts.length == 2) {
          semester = semesterparts[0].trim();
          year = semesterparts[1].trim();
          if (semester.equalsIgnoreCase("Fall") && isDigit(year)) {
            semester = "f";
          } else if (semester.equalsIgnoreCase("Winter") && isDigit(year)) {
            semester = "w";
          } else if (semester.equalsIgnoreCase("Summer") && isDigit(year)) {
            semester = "s";
          }
        }
      }
      if (timeday != null) {
        if (timeday.equalsIgnoreCase("morning")) {
          timeday = "m";
        } else if (timeday.equalsIgnoreCase("afternoon")) {
          timeday = "a";
        } else if (timeday.equalsIgnoreCase("evening")) {
          timeday = "e";
        }
      }
      String course_insert = "";
      try {
        course_insert = String.format(
            "insert into course_editions(course_id, semester, year, total_students, time_day) values (%i, '%s', %s, %s, '%s')",
            c.course_id, semester, year, num_students, timeday);
        Statement stmt = conn.prepareStatement(course_insert,
            Statement.RETURN_GENERATED_KEYS);
        stmt.executeUpdate(course_insert);
        ResultSet rs = stmt.getGeneratedKeys();
        if(rs.next()){
          c.edition_id = rs.getInt(1);
        }
      } catch (SQLException e) {
        System.out.println(course_insert);
      }

    scanner.close();
    return;
  }

  
  public static void addEnrollmentsToDatabase(Connection conn, Student s, CourseInfo c) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Please enter your grade (e.g. A+) (or press enter if you would prefer not to)");
    String grade = scanner.nextLine();
    if(grade == null || grade.equals("")){
      grade = "null";
    }
    System.out.println("Please rank the course from 1 to 5 (or press enter if you would prefer not to)");
    String ceval = scanner.nextLine();
    if(ceval == null || ceval.equals("")){
      ceval = "null";
    }else if (isDigit(ceval)){    
      int cevalint = Integer.parseInt(ceval);
      if(cevalint < 0 || cevalint > 5){
        ceval = "null";
      }
    }
    
    System.out.println("Please rank the instructor from 1 to 5 (or press enter if you would prefer not to)");
    String ineval = scanner.nextLine();
    if(ineval == null || ineval.equals("")){
      ineval = "null";
    }else if (isDigit(ineval)){    
      int cevalint = Integer.parseInt(ineval);
      if(cevalint < 0 || cevalint > 5){
        ineval = "null";
      }
    }
    String enroll_insert = "";
    try{
      Statement stmt = conn.createStatement();
      enroll_insert = String.format(
          "insert into enrollments(username, letter_grade, course_ranking, instr_ranking) values ('%s', '%s', %s, %s)",
          s.getUsername(), grade, ceval, ineval);
      stmt.executeUpdate(enroll_insert);
    }catch (SQLException e) {
      System.out.println(enroll_insert);
    }
    scanner.close();
    return;
  }


  //method to add skills data
  public static void addSkillsToDatabase(Connection conn, Student s, CourseInfo c){
        System.out.println("Type a new skill to add or an existing skill to rate. Type \'end\' when you're done");
        Scanner scanner = new Scanner(System.in);
        boolean rate_skills = true;
        while(!rate_skills){
            String skill = scanner.nextLine().trim();
            if(skill.equals("end")){
                rate_skills = true;
            }
            else{
                try {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(String.format("select skill_id from skills where skill='%s'", skill));
                    Skills skill1 = new Skills();
                    skill1.setSkill(skill);
                    if (!rs.isBeforeFirst() ) { 
                        String insertSQL = "INSERT INTO skills (skill)" + " VALUES (?)";
                        PreparedStatement stmt1 = conn.prepareStatement(insertSQL);
                        stmt1.setString(1,skill);    
                        stmt1.execute();
                        System.out.println("skills added!");
                    }
                    System.out.println("before rate for " + skill + ": ");
                    int before = Integer.parseInt(scanner.nextLine().trim());
                    System.out.println("after rate for " + skill + ": ");
                    int after = Integer.parseInt(scanner.nextLine().trim());
                    int id = skill1.getId(conn);
                    String insert = "INSERT INTO skill_rankings (course_id, edition_id, username, skill_id, rank_before, rank_after)" + " VALUES(?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt2 = conn.prepareStatement(insert);
                    stmt2.setInt(1, c.course_id);
                    stmt2.setInt(2, c.edition_id);
                    stmt2.setString(3, s.username);
                    stmt2.setInt(4, id);
                    stmt2.setInt(5, before);
                    stmt2.setInt(6, after);
                    stmt2.execute();
                    System.out.println("Information added!");
                } 
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
                
        }
        scanner.close();
    }
  
  
  //method to add topics data
  public static void addInterestsToDatabase(Connection conn, Student s, CourseInfo c){
        System.out.println("Type a new topic to add or an existing topic to rate. Type \'end\' when you're done");
        Scanner scanner = new Scanner(System.in);
        boolean rate_topics = true;
        while(!rate_topics){
            String topic = scanner.nextLine().trim();
            if(topic.equals("end")){
                rate_topics = true;
            }
            else{
                try {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(String.format("select topic_id from topics where topic='%s'", topic));
                    Interests interests = new Interests();
                    interests.setTopic(topic);
                    if (!rs.isBeforeFirst() ) { 
                        String insertSQL = "INSERT INTO topics (topic)" + " VALUES (?)";
                        PreparedStatement stmt1 = conn.prepareStatement(insertSQL);
                        stmt1.setString(1,topic);    
                        stmt1.execute();
                        System.out.println("topics added!");
                    }
                    System.out.println("before rate for " + topic + ": ");
                    int before = Integer.parseInt(scanner.nextLine().trim());
                    System.out.println("after rate for " + topic + ": ");
                    int after = Integer.parseInt(scanner.nextLine().trim());
                    int id = interests.getId(conn);
                    String insert = "INSERT INTO topic_interests (course_id, edition_id, username, topic_id, interest_before, interest_after)" + " VALUES(?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt2 = conn.prepareStatement(insert);
                    stmt2.setInt(1, c.course_id);
                    stmt2.setInt(2, c.edition_id);
                    stmt2.setString(3, s.username);
                    stmt2.setInt(4, id);
                    stmt2.setInt(5, before);
                    stmt2.setInt(6, after);
                    stmt2.execute();
                    System.out.println("Information added!");
                } 
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
                
        }
        scanner.close();
        
}

  public static void main(String[] args) throws IOException, SQLException {
    // read properties
    Properties props = new Properties();
    FileInputStream in = new FileInputStream(args[0]);
    props.load(in);
    in.close();

    // pre-set connection
    java.sql.Connection conn = ConnectDB.getConnection(props);
    System.out.println("===============================");
    System.out.println("Welcome to the CEA Database");
    System.out.println("===============================");
    Scanner scanner = new Scanner(System.in);
    Student s = new Student();
    boolean name_entered = false;
    while (!name_entered) {
      System.out.println("Please enter your name:");
      String stud_name = scanner.nextLine();
      if (stud_name != null && !(stud_name.equals(""))) {
        s.username = stud_name;
      }else{ 
        continue;
      }
      // if student doesn't exist, add student to database
      while (!s.queryDatabase(conn)) {
        try {
          s.setUsername(stud_name);
          System.out.println("Please enter your demographic info....:");
          System.out.println("Enter Age:");
          int age = Integer.parseInt(scanner.nextLine());
          s.setAge(age);
          System.out.println("Enter Gender(m/f):");
          String gender = scanner.nextLine();
          if (gender.isEmpty()) {
            s.setGender(null);
          } else {
            s.setGender(gender);
          }
          System.out.println("May we release your information anonymously (y/n):");
          String permission = scanner.nextLine();
          int p;
          if(permission.equals("y")){
            p = 1;
          }else{
            p = 0;
          }
          s.setPermission(p);
          System.out.println("Enter native country:");
          s.setCountry(scanner.nextLine());
          if (!s.validate()) {
            System.out.println("Information isnt valid");
          } else {
            s.addToDatabase(conn);
            System.out.println("Information added!");
            name_entered = true;
          }
        } catch (NullPointerException e) {
          System.out.println("Oops, there was an error processing your information, please re-enter it");
          continue;
        } catch (SQLException e2){
          System.out.println("Oops, there was an error processing your information, please re-enter it");
          continue;
        }
      }

      // if student exists in database display student info for confirmation
      // (need to properly implement the last else statement)
        System.out.println("Is the above info correct(y/n)?");
        String answer = scanner.nextLine();
        if (answer.equals("n")) {
          name_entered = false;
        } else if (answer.equals("y")) {
          name_entered = true;
        }
    }

    // Get courses student has taken
    System.out.println(
        "Please enter courses you've taken in this form(e.g CSC108)\nType \'end\' when done");
    boolean end = false;
    while (!end) {
      try {
        CourseInfo course = new CourseInfo();
        String courseinput = scanner.nextLine().trim();
        if (courseinput.equals("end")) {
          end = true;
        } else {
          if (courseinput.length() > 5 && courseinput.length() < 8) {
            String dept = courseinput.substring(0, 3);
            String number = courseinput.substring(3);
            
              int coursenumber = Integer.parseInt(number);
              dept = dept.toUpperCase();
              course.setDept_code(dept);
              course.setCourse_number(coursenumber);
              s.cInfo.add(course);
            } 
        }
      } catch (NullPointerException e) {
        System.out.println("Please re-enter the above course");
      } catch (NumberFormatException e2){
        System.out.println("Please re-enter the above course");
      }
      // check that the format is right (format check doesn't work..don't know
      // why)
      /*
       * if(!isAlpha(dept)){ System.out.println("WrongFormat! Enter Course:"); }
       * else if (!isDigit(number)){ System.out.println(
       * "WrongFormat! Enter Course:"); } else{
       */

      // }

    }

    // list all courses and confirm (not necessary)


    // display all available topics and ask to rate (need query to group topics
    // by department)
    System.out.println("Available Topics");
    displayTopics(conn);

    // Ask user to rate topics and store input (need to format check and verify
    // topic is in database)
    // check that : is in the string, if it is split string and check if first
    // part is in the database and check if last char is a digit between 1 and 5
    // need to implement null
    System.out.println(
        "Please pick out five topics and rate them in this format(e.g Programming:5). Ratings should fall between 1-5\nType \'end\' when done");
    int i = 0;
    boolean endtopic = false;
    while (!endtopic) {
      Interests interests = new Interests();
      String topic = scanner.nextLine();
      if (topic.equals("end")) {
        endtopic = true;
      } else {
        try {
          if(topic.matches("[A-Za-z ]+[ ]?\\:[ ]?[1-5]")){
            interests = new Interests();
            String[] parts = topic.split(":");
            String topicname = parts[0].trim();
            String topicrank = parts[1].trim();
            topicname = topicname.substring(0, 1).toUpperCase() + topicname.substring(1);
            int rank = Integer.parseInt(topicrank);
            interests.setTopic(topicname);
            interests.setRating(rank);
            interests.getId(conn);
            s.interests.add(interests);
            i++;
            if (i==5){
              break;
            }
          } else {
            System.out.println("Please re-enter the above topic and rating");
          }
        } catch (NullPointerException e) {
          System.out.println("Please re-enter the above topic and rating");
        }catch (NumberFormatException e2){
          System.out.println("Please re-enter the above topic and rating");
        }catch (ArrayIndexOutOfBoundsException e2){
          System.out.println("Please re-enter the above topic and rating");
        }
      }
    }


    // Display all available skills and ask to rate (need to display grouped by
    // department)
    System.out.println("Available Skillls");
    displaySkills(conn);


    // Ask user to rate topics and store input
    // need to check format and implement null
    System.out.println(
        "Please pick out five skills and rate them in this format (e.g calculus:4). Ratings should fall between 1-5\nType \'end\' when done");
    boolean endskills = false;
    i = 0;
    while (!endskills) {
      Skills skills;
      String skill = scanner.nextLine();
      if (skill.equals("end")) {
        endskills = true;
      } else {
        try {
          if(skill.matches("[A-Za-z ]+[ ]?\\:[ ]?[1-5]")){
              skills = new Skills();
              String[] skillparts = skill.split(":");
              String skillname = skillparts[0].trim();
              String skillrank = skillparts[1].trim();
              int srank = Integer.parseInt(skillrank);
              skillname = skillname.toLowerCase();
              skills.setRanking(srank);
              skills.setSkill(skillname);
              skills.getId(conn);
              s.skills.add(skills);
              i++;
              if (i==5){
                break;
              }
          }else {
            System.out.println("Please re-enter the above topic and rating");
          }
        } catch (NullPointerException e) {
          System.out.println("Please re-enter the above topic and rating");
        }catch (NumberFormatException e2){
          System.out.println("Please re-enter the above topic and rating");
        }catch (ArrayIndexOutOfBoundsException e2){
          System.out.println("Please re-enter the above topic and rating");
        }
      }
    }


    // User chooses how he would like his recommendations ranked
    boolean finished = false;
    while (!finished) {
      System.out.println("How would you like your recommendations ranked?");
      System.out.println("For ranking by expected grade, type 0");
      System.out.println("For ranking by expected course satisfcation, type 1");
      System.out.println(
          "For ranking by expected increase in topic interest, type 2");
      System.out.println(
          "For ranking by expected increase in skill proficiency, type 3");
      System.out.println("If you are finished, type 'end'");
      String input = scanner.nextLine();
      if (input.equals("end")) {
        finished = true;
      }else {
        try {
          s.recommendation_preference = Integer.parseInt(input);
          System.out.println("Type ");
          Recommender r = new Recommender();
          r.getRecommendations(conn, s);
        } catch (NumberFormatException e) {
          System.out.println("please enter a number");
          continue;
        }
      }
    }
    
    
    //Ask User if he/she would like to add more info to database
    System.out.println("Would you like to add some more data to your profile? (y/n):");
    boolean addition = false;
    String add_answer = scanner.nextLine();
    while(!addition){
    	if(add_answer.equals("n")){
    		addition = true;
    		System.out.println("You've been logged out!");
    		System.exit(0);
    	}
    	else if(add_answer.equals("y")){
    		contributeToDatabase(conn, s);
    		System.out.println("You've been logged out!");
    		System.exit(0);
    	}
    	else{
    		System.out.println("Wrong Format!");
    	}
    	
    }
   

    scanner.close();
  }
}

