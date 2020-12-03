package HW2;

import HW2.business.*;
import HW2.data.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import HW2.data.PostgreSQLErrorCodes;

import java.util.ArrayList;

import static HW2.business.ReturnValue.*;

public class Solution {

    private final static String STUDENT_TABLE = "Student";

    public static void createTables() {
       // InitialState.createInitialState();
        String studAttributes = "ID INTEGER NOT NULL,\n" + "Name TEXT NOT NULL,\n" + "Faculty TEXT NOT NULL,\n"
                + "Points INTEGER NOT NULL";
        String studConfigs = "PRIMARY KEY (ID),\n" + "CHECK (ID > 0)";
        createTable(STUDENT_TABLE, studAttributes, studConfigs);


    }

    public static void clearTables() {
        // clear your tables here
    }

    public static void dropTables() {
        InitialState.dropInitialState();
        // drop your tables here
    }

    public static ReturnValue addTest(Test test) {
        return OK;
    }

    public static Test getTestProfile(Integer testID, Integer semester) {
        return new Test();
    }

    public static ReturnValue deleteTest(Integer testID, Integer semester) {
        return OK;
    }

    public static ReturnValue addStudent(Student student) {
        return OK;
    }

    public static Student getStudentProfile(Integer studentID) {
        return new Student();
    }

    public static ReturnValue deleteStudent(Integer studentID) {
        return OK;
    }

    public static ReturnValue addSupervisor(Supervisor supervisor) {
        return OK;
    }

    public static Supervisor getSupervisorProfile(Integer supervisorID) {
        return new Supervisor();
    }

    public static ReturnValue deleteSupervisor(Integer supervisorID) {
        return OK;
    }

    public static ReturnValue studentAttendTest(Integer studentID, Integer testID, Integer semester) {
        return OK;
    }

    public static ReturnValue studentWaiveTest(Integer studentID, Integer testID, Integer semester) {
        return OK;
    }

    public static ReturnValue supervisorOverseeTest(Integer supervisorID, Integer testID, Integer semester) {
        return OK;
    }

    public static ReturnValue supervisorStopsOverseeTest(Integer supervisorID, Integer testID, Integer semester) {
        return OK;
    }

    public static Float averageTestCost() {
        return 0.0f;
    }

    public static Integer getWage(Integer supervisorID) {
        return 0;
    }

    public static ArrayList<Integer> supervisorOverseeStudent() {
        return new ArrayList<Integer>();
    }

    public static ArrayList<Integer> testsThisSemester(Integer semester) {
        return new ArrayList<Integer>();
    }

    public static Boolean studentHalfWayThere(Integer studentID) {
        return true;
    }

    public static Integer studentCreditPoints(Integer studentID) {
        return 0;
    }

    public static Integer getMostPopularTest(String faculty) {
        return 0;
    }

    public static ArrayList<Integer> getConflictingTests() {
        return new ArrayList<Integer>();
    }

    public static ArrayList<Integer> graduateStudents() {
        return new ArrayList<Integer>();
    }

    public static ArrayList<Integer> getCloseStudents(Integer studentID) {
        return new ArrayList<Integer>();
    }

    private static void createTable(String tableName, String attributes, String configs) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection
                    .prepareStatement("CREATE TABLE " + tableName + " (" + attributes + ",\n" + configs + ")");
            pstmt.execute();
        } catch (SQLException e) {
            // e.printStackTrace()();
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                // e.printStackTrace()();
            }
        }
    }
}
