package CEA_DB;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Recommender {
  
  private boolean is_ranked = false;
  private boolean grades_view_set = false;
  
  // DEMOGRAPHIC NEIGHBOURS QUERIES
  private String drop_neighbours = "drop view if exists neighbours";
  private String get_neighbours = "select * from neighbours";
  private String neighbour_query =
      "create view neighbours as select * from (SELECT neighbours.username, neighbours.distance FROM"
          + " (SELECT username, (";
  private String male_query = "select count(*) from students where gender='m'";
  private String female_query =
      "select count(*) from students where gender='f'";
  private String age_query = "select avg(age) from students";
  float gender_ratio;
  float avg_age;
  Statement stmt = null;
  private Map<String, Student> neighbours = new HashMap<String, Student>();
  private ArrayList<String> ranked_neighbours = new ArrayList<String>();


  // INTEREST NEIGHBOURS QUERIES
  private String topic_interest_query_p2 = "distance) distance2 from ("
      + "select username, distance, avg(k1) ak1, avg(k2) ak2, avg(k3) ak3, avg(k4) ak4, avg(k5) ak5"
      + " from (((((neighbours ";
  private String topic_interest_query_p1 =
      "CREATE VIEW neighbours2 as select username, (";
  private String drop_neighbours2 = "DROP VIEW IF EXISTS neighbours2";
  private String drop_neighbours3 = "DROP VIEW IF EXISTS neighbours3";


  // SKILL NEIGHBOURS QUERIES
  private String skill_query_p1 =
      "create view neighbours3 as select username, (";
  private String skill_query_p2 = "distance2) distance3 from ("
      + "select username, distance2, avg(k1) ak1, avg(k2) ak2, avg(k3) ak3, avg(k4) ak4, avg(k5) ak5"
      + " from (((((neighbours2 ";



  // RANK NEIGHBOURS AND COURSE RECOMMENDATION QUERIES
  private String ranked_neighbours_view =
      "create view ranked_neighbours as select * from neighbours3 order by 2 asc limit 15";
  private String grades_view =
      "create view course_grades as select username, max_grade, course_ranking, dept_code, course_number from letter_grades right outer join "
          + "(courses c1 right outer join (course_editions eds right outer join (select * from enrollments where username in (";
  private String grades_view2 =
      ")) a1 on a1.edition_id=eds.edition_id) c2 on c1.course_id = c2.course_id) cos on letter_grades.letter_grade=cos.letter_grade ";

  private String interest_view =
      "create view course_interests as select D.username, interest_before, interest_after, dept_code, course_number "
          + "from (select * from topic_interests where topic_id in ( ";
  private String interest_view2 =
      ")) B, courses C, (select * from enrollments where username in (";
  private String interest_view3 =
      ")) D where B.course_id=C.course_id and D.username=B.username";

  private String skill_view =
      "create view course_skill_ranks as select D.username, rank_before, rank_after, dept_code, course_number "
          + "from (select * from skill_rankings where skill_id in ( ";
  private String skill_view2 =
      ")) B, courses C, (select * from enrollments where username in (";
  private String skill_view3 =
      ")) D where B.course_id=C.course_id and D.username=B.username";


  public Recommender() {
    // TODO Auto-generated constructor stub
  }

  public void getRecommendations(Connection conn, Student s) {
    // calculate gender ratio and average age
    try {
      getAverages(conn);
      getDemoDistances(conn, s);
      getInterestDistance(conn, s);
      getSkillDistance(conn, s);
      getCourseRecommendations(conn, s);
    } catch (SQLException e) {
      System.out.println("Oops, there was a database error, please try again");
      SQLError.show(e);
    }

  }

  public void getCourseRecommendations(Connection conn, Student s)
      throws SQLException {
    if (!is_ranked) {
      stmt = conn.createStatement();
      stmt.executeUpdate("drop view if exists ranked_neighbours");
      stmt = conn.createStatement();
      stmt.executeUpdate(ranked_neighbours_view);
      stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("select * from ranked_neighbours");
      String uname;
      while (rs.next()) {
        uname = rs.getString(1);
        if (!(uname.equals(s.username))) {
          ranked_neighbours.add(uname);
        }
        //System.out.println(uname);
      }
      is_ranked = true;
    }

    if (!grades_view_set) {
      for (String username : ranked_neighbours) {
        grades_view += String.format("'%s',", username);
      }
      grades_view = grades_view.substring(0, grades_view.length() - 1);
      grades_view += grades_view2;
      //System.out.println(grades_view);


      stmt = conn.createStatement();
      stmt.executeUpdate("drop view if exists course_grades");
      stmt = conn.createStatement();
      stmt.executeUpdate(grades_view);
      grades_view_set = true;
    }

    if (s.recommendation_preference == 0) {
      rankByGrade(conn, s);
    } else if (s.recommendation_preference == 1) {
      rankByCourseEval(conn, s);
    } else if (s.recommendation_preference == 2) {
      rankByInterest(conn, s);
    } else if (s.recommendation_preference == 3) {
      rankBySkills(conn, s);
    }

  }

  public void rankByGrade(Connection conn, Student s) throws SQLException {
    stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(
        "select avg(max_grade), avg(course_ranking), dept_code, course_number from course_grades group by dept_code, course_number order by 1 desc limit 5");
    while (rs.next()) {
      System.out.println(rs.getString(3) + " : " + rs.getInt(4));
    }

  }

  public void rankByCourseEval(Connection conn, Student s) throws SQLException {

    stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(
        "select avg(max_grade), avg(course_ranking), dept_code, course_number from course_grades group by dept_code, course_number order by 2 desc limit 5");
    while (rs.next()) {
      System.out.println(rs.getString(3) + " : " + rs.getInt(4));
    }

  }

  // RANK BY INTERESTS
  public void rankByInterest(Connection conn, Student s) throws SQLException {

    for (Interests i : s.interests) {
      interest_view += String.format("'%s',", i.topic_id);
    }
    interest_view = interest_view.substring(0, interest_view.length() - 1);
    interest_view += interest_view2;

    for (String username : ranked_neighbours) {
      interest_view += String.format("'%s',", username);
    }
    interest_view = interest_view.substring(0, interest_view.length() - 1);
    interest_view += interest_view3;
    //System.out.println(interest_view);

    stmt = conn.createStatement();
    stmt.executeUpdate("drop view if exists course_interests");
    stmt = conn.createStatement();
    stmt.executeUpdate(interest_view);
    stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(
        "select avg(interest_after - interest_before), dept_code, course_number from course_interests group by dept_code, course_number order by 1 desc limit 5");
    while (rs.next()) {
      System.out.println(
          rs.getString(2) + " : " + rs.getInt(3));
    }

  }

  // RANK BY INTERESTS
  public void rankBySkills(Connection conn, Student s) throws SQLException {

    for (Skills i : s.skills) {
      skill_view += String.format("'%s',", i.skill_id);
    }
    skill_view = skill_view.substring(0, skill_view.length() - 1);
    skill_view += skill_view2;

    for (String username : ranked_neighbours) {
      skill_view += String.format("'%s',", username);
    }
    skill_view = skill_view.substring(0, skill_view.length() - 1);
    skill_view += skill_view3;
    System.out.println(skill_view);

    stmt = conn.createStatement();
    stmt.executeUpdate("drop view if exists course_skill_ranks");
    stmt = conn.createStatement();
    stmt.executeUpdate(skill_view);
    stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(
        "select (case when avg(rank_after - rank_before) is null then 0 else avg(rank_after - rank_before) end),"+
        " dept_code, course_number from course_skill_ranks group by dept_code, course_number order by 1 desc limit 5");
    while (rs.next()) {
      System.out.println(
          rs.getString(2) + " : " + rs.getInt(3));
    }

  }



  public void getInterestDistance(Connection conn, Student s)
      throws SQLException {
    int i = 1;
    for (Interests in : s.interests) {
      topic_interest_query_p1 += String.format(
          "(CASE WHEN ak%d is NULL THEN 25 else (ak%d - %d)*(ak%d - %d) END) +",
          i, i, in.rating, i, in.rating);
      topic_interest_query_p2 += String.format(
          "full join (select interest_before k%d, username u from "
              + "topic_interests where topic_id=%d) op%d on username=op%d.u) t%d ",
          i, in.topic_id, i, i, i);
      i += 1;
    }
    topic_interest_query_p1 += topic_interest_query_p2;
    topic_interest_query_p1 += "group by username, distance) as fl";
    //System.out.println(topic_interest_query_p1);
    stmt = conn.createStatement();
    stmt.executeUpdate(drop_neighbours2);
    stmt = conn.createStatement();
    stmt.executeUpdate(topic_interest_query_p1);
    stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("select * from neighbours2");
    Student st;
    while (rs.next()) {
      st = neighbours.get(rs.getString(1));
      st.distance = rs.getFloat(2);
      // System.out.println(String.format("user %s, dist %f", st.username,
      // st.distance));
    }
  }

  public void getSkillDistance(Connection conn, Student s) throws SQLException {
    int i = 1;
    for (Skills in : s.skills) {
      skill_query_p1 += String.format(
          "(CASE WHEN ak%d is NULL THEN 25 else (ak%d - %d)*(ak%d - %d) END) +",
          i, i, in.ranking, i, in.ranking);
      skill_query_p2 += String.format(
          "full join (select rank_before k%d, username u from "
              + "skill_rankings where skill_id=%d) op%d on username=op%d.u) t%d ",
          i, in.skill_id, i, i, i);
      i += 1;
    }
    skill_query_p1 += skill_query_p2;
    skill_query_p1 += "group by username, distance2) as fl";
    //System.out.println(skill_query_p1);

    stmt = conn.createStatement();
    stmt.executeUpdate(drop_neighbours3);
    stmt = conn.createStatement();
    stmt.executeUpdate(skill_query_p1);
    stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("select * from neighbours3");
    Student st;
    while (rs.next()) {
      st = neighbours.get(rs.getString(1));
      st.distance = rs.getFloat(2);
      // System.out.println(String.format("user %s, dist %f", st.username,
      // st.distance));
    }
  }

  public void getAverages(Connection conn) throws SQLException {
    stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(male_query);
    float males = 1;
    while (rs.next()) {
      males = rs.getInt(1);
    }
    stmt = conn.createStatement();
    rs = stmt.executeQuery(female_query);
    float females = 1;
    while (rs.next()) {
      females = rs.getInt(1);
    }
    gender_ratio = males / (males + females);
    stmt = conn.createStatement();
    rs = stmt.executeQuery(age_query);
    while (rs.next()) {
      avg_age = rs.getFloat(1);
    }
    // System.out.println(String.format("avg age = %f, gender rat = %f",
    // avg_age, gender_ratio));
  }

  public void getDemoDistances(Connection conn, Student s) throws SQLException {
    // build demographic query

    neighbour_query += String.format(
        "(CASE WHEN age is NULL THEN (%d - %s)*(%d - %s)  ELSE (age - %d)*(age - %d) END)",
        s.age, (new DecimalFormat("#.##").format(avg_age)), s.age,
        (new DecimalFormat("#.##").format(avg_age)), s.age, s.age);
    
    if (s.gender.equals("m")) {
      String male = "(CASE WHEN gender='m' THEN 0 ELSE 1 END)";
      neighbour_query +=
          String.format(" + (CASE WHEN gender is NULL THEN %s ELSE %s END)",
              (new DecimalFormat("#.##").format(1 - gender_ratio)), male);
    } else {
      String female = "(CASE WHEN gender='f' THEN 0 ELSE 1 END)";
      neighbour_query +=
          String.format(" + (CASE WHEN gender is NULL THEN %s ELSE %s END)",
              (new DecimalFormat("#.##").format(gender_ratio)), female);
    }
    
    String country = String.format(
        "(CASE WHEN native_country='%s' THEN 0 ELSE 1 END)", s.native_country);
    neighbour_query += String.format(
        "+(CASE WHEN native_country is null THEN 1 ELSE %s END))", country);
    neighbour_query +=
        "as distance FROM students ORDER BY 1 LIMIT 35) neighbours GROUP BY neighbours.username, neighbours.distance) sddd order by 2  ";


    stmt = conn.createStatement();
    stmt.executeUpdate(drop_neighbours);
    stmt = conn.createStatement();
    // System.out.println(neighbour_query);
    stmt.executeUpdate(neighbour_query);
    stmt = conn.createStatement();

    ResultSet rs = stmt.executeQuery(get_neighbours);
    while (rs.next()) {
      Student n = new Student();
      n.username = rs.getString(1);
      n.distance = rs.getFloat(2);
      // System.out.println(String.format("user %s, dist %f", n.username,
      // n.distance));
      neighbours.put(n.username, n);
    }
  }

  public static void main(String[] args) throws SQLException, IOException {
    Recommender r = new Recommender();
    Student s = new Student();
    s.age = 25;
    s.gender = "f";
    s.native_country = "China";


    Skills sk = new Skills();
    sk.skill_id = 57;
    sk.ranking = 2;
    s.skills.add(sk);
    sk = new Skills();
    sk.skill_id = 77;
    sk.ranking = 2;
    s.skills.add(sk);
    sk = new Skills();
    sk.skill_id = 40;
    sk.ranking = 3;
    s.skills.add(sk);
    sk = new Skills();
    sk.skill_id = 35;
    sk.ranking = 2;
    s.skills.add(sk);
    sk = new Skills();
    sk.skill_id = 7;
    sk.ranking = 1;
    s.skills.add(sk);



    Interests in = new Interests();
    in.topic_id = 72;
    in.rating = 3;
    s.interests.add(in);
    in = new Interests();
    in.topic_id = 52;
    in.rating = 4;
    s.interests.add(in);
    in = new Interests();
    in.topic_id = 5;
    in.rating = 4;
    s.interests.add(in);
    in = new Interests();
    in.topic_id = 22;
    in.rating = 4;
    s.interests.add(in);
    in = new Interests();
    in.topic_id = 82;
    in.rating = 2;
    s.interests.add(in);

    Properties props = new Properties();
    FileInputStream input = new FileInputStream(args[0]);
    props.load(input);
    input.close();
    java.sql.Connection conn = ConnectDB.getConnection(props);
    r.getAverages(conn);
    r.getDemoDistances(conn, s);
    r.getInterestDistance(conn, s);
    r.getSkillDistance(conn, s);

    s.recommendation_preference = 0;
    r.getCourseRecommendations(conn, s);

    s.recommendation_preference = 1;
    r.getCourseRecommendations(conn, s);

    s.recommendation_preference = 2;
    r.getCourseRecommendations(conn, s);

    s.recommendation_preference = 3;
    r.getCourseRecommendations(conn, s);



  }

}
