package HW2;

import HW2.business.*;
import HW2.data.DBConnector;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import HW2.data.PostgreSQLErrorCodes;

import java.util.ArrayList;

import static HW2.business.ReturnValue.*;

public class Solution {

    private final static String STUDENT_TABLE = "Student";
    private final static String SUPERVISOR_TABLE = "Supervisor";
    private final static String TEST_TABLE = "Test";
    private final static String ATTEND_TABLE = "Attend";
    private final static String OVERSEE_TABLE = "Oversee";

    public static void createTables() {
        // create credit points fore each faculty table
        InitialState.createInitialState();
        // create student table
        String studAttributes = "Student_ID INTEGER NOT NULL,\n" + "Name TEXT NOT NULL,\n" + "Faculty TEXT NOT NULL,\n"
                + "Points INTEGER NOT NULL";
        String studConfigs = "PRIMARY KEY (Student_ID),\n" + "CHECK (Student_ID > 0),\n" + "CHECK (Points >= 0)";
        createTable(STUDENT_TABLE, studAttributes, studConfigs);
        // create supervisor table
        String supervAttributes = "Supervisor_ID INTEGER NOT NULL,\n" + "Name TEXT NOT NULL,\n"
                + "Salary INTEGER NOT NULL";
        String supervConfigs = "PRIMARY KEY (Supervisor_ID),\n" + "CHECK (Supervisor_ID > 0),\n"
                + "CHECK (Salary >= 0)";
        createTable(SUPERVISOR_TABLE, supervAttributes, supervConfigs);
        // create test table
        String testAttributes = "Course_ID INTEGER NOT NULL,\n" + "Semester INTEGER NOT NULL,\n"
                + "Time INTEGER NOT NULL,\n" + "Room INTEGER NOT NULL,\n" + "Day INTEGER NOT NULL,\n"
                + "Points INTEGER NOT NULL";
        String testConfigs = "PRIMARY KEY (Course_ID, Semester),\n" + "CHECK (Course_ID > 0),\n" + "CHECK (Room > 0),\n"
                + "CHECK (Points > 0)";
        createTable(TEST_TABLE, testAttributes, testConfigs);
        // create attend table
        String attendAttributes = "Student_ID INTEGER NOT NULL,\n" + "Course_ID INTEGER NOT NULL,\n"
                + "SEMESTER INTEGER NOT NULL";
        String attendConfigs = "FOREIGN KEY (Student_ID) REFERENCES " + STUDENT_TABLE + " ON DELETE CASCADE" + ",\n"
                + "FOREIGN KEY (Course_ID, Semester) REFERENCES " + TEST_TABLE + " ON DELETE CASCADE";
        createTable(ATTEND_TABLE, attendAttributes, attendConfigs);
        // create oversee table
        String overseeAttributes = "Supervisor_ID INTEGER NOT NULL,\n" + "Course_ID INTEGER NOT NULL,\n"
                + "SEMESTER INTEGER NOT NULL";
        String overseeConfigs = "FOREIGN KEY (Supervisor_ID) REFERENCES " + SUPERVISOR_TABLE + " ON DELETE CASCADE"
                + ",\n" + "FOREIGN KEY (Course_ID, Semester) REFERENCES " + TEST_TABLE + " ON DELETE CASCADE";
        createTable(OVERSEE_TABLE, overseeAttributes, overseeConfigs);
        // TODO: add views creation
    }

    public static void clearTables() {
        InitialState.clearInitialState();
        // drop student table
        clearTable(STUDENT_TABLE);
        // drop supervisor table
        clearTable(SUPERVISOR_TABLE);
        // drop test table
        clearTable(TEST_TABLE);
        // drop attend table
        clearTable(ATTEND_TABLE);
        // drop oversee table
        clearTable(OVERSEE_TABLE);
    }

    public static void dropTables() {
        // drop credit points fore each faculty table
        InitialState.dropInitialState();
        // drop student table
        dropTable(STUDENT_TABLE);
        // drop supervisor table
        dropTable(SUPERVISOR_TABLE);
        // drop test table
        dropTable(TEST_TABLE);
        // drop attend table
        dropTable(ATTEND_TABLE);
        // drop oversee table
        dropTable(OVERSEE_TABLE);
        // TODO: add views droping

    }

    public static ReturnValue addTest(Test test) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("INSERT INTO " + TEST_TABLE + " VALUES (?, ?, ?, ?, ?, ?)");
            pstmt.setInt(1, test.getId());
            pstmt.setInt(2, test.getSemester());
            pstmt.setInt(3, test.getTime());
            pstmt.setInt(4, test.getRoom());
            pstmt.setInt(5, test.getDay());
            pstmt.setInt(6, test.getCreditPoints());

            pstmt.execute();
            return OK;

        } catch (SQLException e) {
            // e.printStackTrace()();
            Integer errorCode = Integer.valueOf(e.getSQLState());
            if (errorCode == PostgreSQLErrorCodes.CHECK_VIOLATION.getValue()
                    || errorCode == PostgreSQLErrorCodes.NOT_NULL_VIOLATION.getValue()
                    || errorCode == PostgreSQLErrorCodes.FOREIGN_KEY_VIOLATION.getValue())
                return BAD_PARAMS;
            if (errorCode == PostgreSQLErrorCodes.UNIQUE_VIOLATION.getValue())
                return ALREADY_EXISTS;
            return ERROR;
        } finally {
            try {
                pstmt.close();
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

    public static Test getTestProfile(Integer testID, Integer semester) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection
                    .prepareStatement("SELECT * FROM " + TEST_TABLE + " WHERE Course_ID = ? AND Semester = ?");
            pstmt.setInt(1, testID);
            pstmt.setInt(2, semester);

            ResultSet results = pstmt.executeQuery();
            if (results.next()) {
                Test t = new Test();
                t.setId(results.getInt(1));
                t.setSemester(results.getInt(2));
                t.setTime(results.getInt(3));
                t.setRoom(results.getInt(4));
                t.setDay(results.getInt(5));
                t.setCreditPoints(results.getInt(6));
                results.close();
                return t;
            }

            results.close();
            return Test.badTest();

        } catch (SQLException e) {
            // e.printStackTrace()();
            return Test.badTest();
        } finally {
            try {
                pstmt.close();
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

    public static ReturnValue deleteTest(Integer testID, Integer semester) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("DELETE FROM " + TEST_TABLE + " WHERE Course_ID = ? AND Semester = ?");
            pstmt.setInt(1, testID);
            pstmt.setInt(2, semester);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 1)
                return OK;
            else if (affectedRows == 0)
                return NOT_EXISTS;
            return ERROR;
        } catch (SQLException e) {
            // e.printStackTrace()();
            return ERROR;
        } finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                // e.printStackTrace()();
                // return ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                // e.printStackTrace()();
                // return ERROR;
            }
        }
    }

    public static ReturnValue addStudent(Student student) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("INSERT INTO " + STUDENT_TABLE + " VALUES (?, ?, ?, ?)");
            pstmt.setInt(1, student.getId());
            pstmt.setString(2, student.getName());
            pstmt.setString(3, student.getFaculty());
            pstmt.setInt(4, student.getCreditPoints());

            pstmt.execute();
            return OK;

        } catch (SQLException e) {
            // e.printStackTrace()();
            Integer errorCode = Integer.valueOf(e.getSQLState());
            if (errorCode == PostgreSQLErrorCodes.CHECK_VIOLATION.getValue()
                    || errorCode == PostgreSQLErrorCodes.NOT_NULL_VIOLATION.getValue()
                    || errorCode == PostgreSQLErrorCodes.FOREIGN_KEY_VIOLATION.getValue())
                return BAD_PARAMS;
            if (errorCode == PostgreSQLErrorCodes.UNIQUE_VIOLATION.getValue())
                return ALREADY_EXISTS;
            return ERROR;
        } finally {
            try {
                pstmt.close();
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

    public static Student getStudentProfile(Integer studentID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("SELECT * FROM " + STUDENT_TABLE + " WHERE Student_ID = ?");
            pstmt.setInt(1, studentID);

            ResultSet results = pstmt.executeQuery();
            if (results.next()) {
                Student s = new Student();
                s.setId(results.getInt(1));
                s.setName(results.getString(2));
                s.setFaculty(results.getString(3));
                s.setCreditPoints(results.getInt(4));
                results.close();
                return s;
            }

            results.close();
            return Student.badStudent();

        } catch (SQLException e) {
            // e.printStackTrace()();
            return Student.badStudent();
        } finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                // e.printStackTrace()();
                // return ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                // e.printStackTrace()();
                // return ERROR;
            }
        }
    }

    public static ReturnValue deleteStudent(Integer studentID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("DELETE FROM " + STUDENT_TABLE + " WHERE Student_ID = ? ");
            pstmt.setInt(1, studentID);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 1)
                return OK;
            else if (affectedRows == 0)
                return NOT_EXISTS;
            return ERROR;
        } catch (SQLException e) {
            // e.printStackTrace()();
            return ERROR;
        } finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                // e.printStackTrace()();
                // return ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                // e.printStackTrace()();
                // return ERROR;
            }
        }
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

    private static void dropTable(String tableName) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("DROP TABLE IF EXISTS " + tableName);
            pstmt.execute();

        } catch (SQLException e) {
            // e.printStackTrace();
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // e.printStackTrace();
            }
        }
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
        }
    }

    private static void clearTable(String tableName) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("DELETE FROM " + tableName + ";");
            pstmt.execute();

        } catch (SQLException e) {
            // e.printStackTrace();
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // e.printStackTrace();
            }
        }
    }

}
