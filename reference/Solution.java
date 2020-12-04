package corona;


import corona.business.Employee;
import corona.business.Lab;
import corona.business.ReturnValue;
import corona.business.Vaccine;
import corona.data.DBConnector;
import corona.data.PostgreSQLErrorCodes;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static corona.business.ReturnValue.*;


public class Solution {
    private final static String EMPLOYEE_TABLE_NAME = "Employee";
    private final static String LAB_TABLE_NAME = "Lab";
    private final static String VACCINE_TABLE_NAME = "Vaccine";
    private final static String EMPLOYED_TABLE_NAME = "Employed";
    private final static String PRODUCTION_TABLE_NAME = "Production";
    private final static String LAB_VACCINE_VIEW_NAME = "LabVaccine";
    private final static String EMPLOYEE_LAB_VIEW_NAME = "EmployeeLab";

    /**
     * Creates the tables and views for the solution.
     */
    public static void createTables() {
        ArrayList<Pair<String, String>> typedSchema = new ArrayList<>();
        typedSchema.add(new Pair("Employee_Id","integer"));
        typedSchema.add(new Pair("Employee_Name","text NOT NULL"));
        typedSchema.add(new Pair("City_of_Birth","text NOT NULL"));
        typedSchema.add(new Pair("PRIMARY KEY","(Employee_Id)"));
        typedSchema.add(new Pair("CHECK","(Employee_Id > 0)"));
        createTable(EMPLOYEE_TABLE_NAME, typedSchema);
        typedSchema.clear();

        typedSchema.add(new Pair("Lab_Id","integer"));
        typedSchema.add(new Pair("Lab_Name","text NOT NULL"));
        typedSchema.add(new Pair("City","text NOT NULL"));
        typedSchema.add(new Pair("Active","boolean NOT NULL"));
        typedSchema.add(new Pair("PRIMARY KEY","(Lab_Id)"));
        typedSchema.add(new Pair("CHECK","(Lab_Id > 0)"));
        createTable(LAB_TABLE_NAME, typedSchema);
        typedSchema.clear();

        typedSchema.add(new Pair("Vaccine_Id","integer"));
        typedSchema.add(new Pair("Vaccine_Name","text NOT NULL"));
        typedSchema.add(new Pair("Cost","integer NOT NULL"));
        typedSchema.add(new Pair("Units_in_Stock","integer NOT NULL"));
        typedSchema.add(new Pair("Productivity","integer NOT NULL"));
        typedSchema.add(new Pair("Income","integer NOT NULL DEFAULT 0"));
        typedSchema.add(new Pair("PRIMARY KEY","(Vaccine_Id)"));
        typedSchema.add(new Pair("CHECK","(Vaccine_Id > 0)"));
        typedSchema.add(new Pair("CHECK","(Cost >= 0)"));
        typedSchema.add(new Pair("CHECK","(Units_in_Stock >= 0)"));
        typedSchema.add(new Pair("CHECK","(Productivity BETWEEN 0 AND 100)"));
        typedSchema.add(new Pair("CHECK","(Income >= 0)"));
        createTable(VACCINE_TABLE_NAME, typedSchema);
        typedSchema.clear();

        typedSchema.add(new Pair("Employee_Id","integer"));
        typedSchema.add(new Pair("Lab_Id","integer"));
        typedSchema.add(new Pair("Salary","integer NOT NULL"));
        typedSchema.add(new Pair("PRIMARY KEY","(Lab_Id, Employee_Id)"));
        typedSchema.add(new Pair("FOREIGN KEY","(Employee_Id) REFERENCES " + EMPLOYEE_TABLE_NAME + "(Employee_Id) ON DELETE CASCADE ON UPDATE CASCADE"));
        typedSchema.add(new Pair("FOREIGN KEY","(Lab_Id) REFERENCES " + LAB_TABLE_NAME +"(Lab_Id) ON DELETE CASCADE ON UPDATE CASCADE"));
        typedSchema.add(new Pair("CHECK","(Salary >= 0)"));
        createTable(EMPLOYED_TABLE_NAME, typedSchema);
        typedSchema.clear();

        typedSchema.add(new Pair("Lab_Id","integer"));
        typedSchema.add(new Pair("Vaccine_Id","integer"));
        typedSchema.add(new Pair("PRIMARY KEY","(Vaccine_Id, Lab_Id)"));
        typedSchema.add(new Pair("FOREIGN KEY","(Lab_Id) REFERENCES " + LAB_TABLE_NAME + "(Lab_Id) ON DELETE CASCADE ON UPDATE CASCADE"));
        typedSchema.add(new Pair("FOREIGN KEY","(Vaccine_Id) REFERENCES " + VACCINE_TABLE_NAME + "(Vaccine_Id) ON DELETE CASCADE ON UPDATE CASCADE"));
        createTable(PRODUCTION_TABLE_NAME, typedSchema);
        typedSchema.clear();

        createLabVaccineView();
        createEmployeeLabView();
    }

    /**
     * Clears the tables for your solution (leaves tables in place but without any data).
     */
    public static void clearTables() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("SELECT table_name\n" +
                    "  FROM information_schema.tables\n" +
                    "  WHERE table_schema='public'\n" +
                    "   AND table_type='BASE TABLE';");
            ResultSet resultSet = pstmt.executeQuery();

            while (resultSet.next())
            {
                String tableName = resultSet.getString("table_name");
                pstmt = connection.prepareStatement("DELETE FROM  " + tableName + " CASCADE");
                pstmt.execute();
            }
            resultSet.close();
        } catch (SQLException e) {
            //e.printStackTrace()();
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
    }

    /**
     * Drops the tables and views from the DB.
     */
    public static void dropTables() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            // drop views;
            pstmt = connection.prepareStatement("SELECT table_name\n" +
                    "  FROM information_schema.tables\n" +
                    "  WHERE table_schema='public'\n" +
                    "   AND table_type='VIEW';");
            ResultSet resultSet1 = pstmt.executeQuery();

            while (resultSet1.next())
            {
                String viewName = resultSet1.getString("table_name");
                pstmt = connection.prepareStatement("DROP VIEW IF EXISTS " + viewName + " CASCADE");
                pstmt.execute();
            }
            resultSet1.close();

            //drop tables;
            pstmt = connection.prepareStatement("SELECT table_name\n" +
                    "  FROM information_schema.tables\n" +
                    "  WHERE table_schema='public'\n" +
                    "   AND table_type='BASE TABLE';");
            ResultSet resultSet2 = pstmt.executeQuery();

            while (resultSet2.next())
            {
                String tableName = resultSet2.getString("table_name");
                pstmt = connection.prepareStatement("DROP TABLE IF EXISTS " + tableName + " CASCADE");
                pstmt.execute();
            }
            resultSet2.close();
        } catch (SQLException e) {
            //e.printStackTrace()();
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
    }

    public static ReturnValue addLab(Lab lab) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("INSERT INTO " + LAB_TABLE_NAME +
                    " VALUES (?, ?, ?, ?)");
            pstmt.setInt(1,lab.getId());
            pstmt.setString(2, lab.getName());
            pstmt.setString(3, lab.getCity());
            pstmt.setBoolean(4, lab.getIsActive());
            pstmt.execute();
            return OK;
        } catch (SQLException e) {
            //e.printStackTrace();
            Integer errorCode = Integer.valueOf(e.getSQLState());
            if(errorCode == PostgreSQLErrorCodes.CHECK_VIOLATION.getValue()
                || errorCode == PostgreSQLErrorCodes.NOT_NULL_VIOLATION.getValue()
                || errorCode == PostgreSQLErrorCodes.FOREIGN_KEY_VIOLATION.getValue())
                return BAD_PARAMS;
            if(errorCode == PostgreSQLErrorCodes.UNIQUE_VIOLATION.getValue())
                return ALREADY_EXISTS;
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
        }
    }

    public static Lab getLabProfile(Integer labID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("SELECT * FROM " + LAB_TABLE_NAME + " WHERE Lab_Id = ?");
            pstmt.setInt(1,labID);

            ResultSet results = pstmt.executeQuery();
            if(results.next()) {
                Lab l = new Lab();
                l.setId(results.getInt(1));
                l.setName(results.getString(2));
                l.setCity(results.getString(3));
                l.setIsActive(results.getBoolean(4));
                results.close();
                return l;
            }

            results.close();
            return Lab.badLab();

        } catch (SQLException e) {
            //e.printStackTrace();
            return Lab.badLab();
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
    }

    public static ReturnValue deleteLab(Lab lab) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(
                    "DELETE FROM " + LAB_TABLE_NAME +
                            " WHERE Lab_Id = ?");
            pstmt.setInt(1,lab.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 1)
                return OK;
            else if(affectedRows == 0)
                return NOT_EXISTS;
            return ERROR;
        } catch (SQLException e) {
            //e.printStackTrace()();
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
        }
    }

    public static ReturnValue addEmployee(Employee employee) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("INSERT INTO " + EMPLOYEE_TABLE_NAME +
                    " VALUES (?, ?, ?)");
            pstmt.setInt(1,employee.getId());
            pstmt.setString(2, employee.getName());
            pstmt.setString(3, employee.getCity());
            pstmt.execute();
            return OK;
        } catch (SQLException e) {
            //e.printStackTrace();
            Integer errorCode = Integer.valueOf(e.getSQLState());
            if(errorCode == PostgreSQLErrorCodes.CHECK_VIOLATION.getValue()
                    || errorCode == PostgreSQLErrorCodes.NOT_NULL_VIOLATION.getValue()
                    || errorCode == PostgreSQLErrorCodes.FOREIGN_KEY_VIOLATION.getValue())
                return BAD_PARAMS;
            if(errorCode == PostgreSQLErrorCodes.UNIQUE_VIOLATION.getValue())
                return ALREADY_EXISTS;
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
        }
    }

    public static Employee getEmployeeProfile(Integer employeeID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("SELECT * FROM " + EMPLOYEE_TABLE_NAME +
                    " WHERE Employee_Id = ?");
            pstmt.setInt(1,employeeID);

            ResultSet results = pstmt.executeQuery();
            if(results.next()) {
                Employee emp = new Employee();
                emp.setId(results.getInt(1));
                emp.setName(results.getString(2));
                emp.setCity(results.getString(3));
                results.close();
                return emp;
            }

            results.close();
            return Employee.badEmployee();

        } catch (SQLException e) {
            //e.printStackTrace();
            return Employee.badEmployee();
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
    }

    public static ReturnValue deleteEmployee(Employee employee) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(
                    "DELETE FROM " + EMPLOYEE_TABLE_NAME +
                            " WHERE Employee_Id = ?");
            pstmt.setInt(1,employee.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 1)
                return OK;
            else if(affectedRows == 0)
                return NOT_EXISTS;
            return ERROR;
        } catch (SQLException e) {
            //e.printStackTrace()();
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
        }
    }

    public static ReturnValue addVaccine(Vaccine vaccine) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("INSERT INTO " + VACCINE_TABLE_NAME +
                    " VALUES (?, ?, ?, ?, ?, ?)");
            pstmt.setInt(1,vaccine.getId());
            pstmt.setString(2, vaccine.getName());
            pstmt.setInt(3,vaccine.getCost());
            pstmt.setInt(4,vaccine.getUnits());
            pstmt.setInt(5,vaccine.getProductivity());
            pstmt.setInt(6,0);      // initial income is 0.
            pstmt.execute();
            return OK;
        } catch (SQLException e) {
            //e.printStackTrace();
            Integer errorCode = Integer.valueOf(e.getSQLState());
            if(errorCode == PostgreSQLErrorCodes.CHECK_VIOLATION.getValue()
                    || errorCode == PostgreSQLErrorCodes.NOT_NULL_VIOLATION.getValue()
                    || errorCode == PostgreSQLErrorCodes.FOREIGN_KEY_VIOLATION.getValue())
                return BAD_PARAMS;
            if(errorCode == PostgreSQLErrorCodes.UNIQUE_VIOLATION.getValue())
                return ALREADY_EXISTS;
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
        }
    }

    public static Vaccine getVaccineProfile(Integer vaccineID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("SELECT * FROM " + VACCINE_TABLE_NAME + " WHERE Vaccine_Id = ?");
            pstmt.setInt(1,vaccineID);

            ResultSet results = pstmt.executeQuery();
            if(results.next()) {
                Vaccine v = new Vaccine();
                v.setId(results.getInt(1));
                v.setName(results.getString(2));
                v.setCost(results.getInt(3));
                v.setUnits(results.getInt(4));
                v.setProductivity(results.getInt(5));
                results.close();
                return v;
            }

            results.close();
            return Vaccine.badVaccine();

        } catch (SQLException e) {
            //e.printStackTrace();
            return Vaccine.badVaccine();
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
    }

    public static ReturnValue deleteVaccine (Vaccine vaccine) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(
                    "DELETE FROM " + VACCINE_TABLE_NAME +
                            " WHERE Vaccine_Id = ?");
            pstmt.setInt(1,vaccine.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 1)
                return OK;
            else if(affectedRows == 0)
                return NOT_EXISTS;
            return ERROR;
        } catch (SQLException e) {
            //e.printStackTrace()();
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
        }
    }


    public static ReturnValue employeeJoinLab(Integer employeeID, Integer labID, Integer salary) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("INSERT INTO " + EMPLOYED_TABLE_NAME +
                    " VALUES (?, ?, ?)");
            pstmt.setInt(1,employeeID);
            pstmt.setInt(2, labID);
            pstmt.setInt(3, salary);
            pstmt.execute();
            return OK;
        } catch (SQLException e) {
            //e.printStackTrace();
            Integer errorCode = Integer.valueOf(e.getSQLState());
            if(errorCode == PostgreSQLErrorCodes.CHECK_VIOLATION.getValue()
                    || errorCode == PostgreSQLErrorCodes.NOT_NULL_VIOLATION.getValue())
                return BAD_PARAMS;
            if(errorCode == PostgreSQLErrorCodes.FOREIGN_KEY_VIOLATION.getValue())
                return NOT_EXISTS;
            if(errorCode == PostgreSQLErrorCodes.UNIQUE_VIOLATION.getValue())
                return ALREADY_EXISTS;
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
        }
    }

    public static ReturnValue employeeLeftLab(Integer labID, Integer employeeID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(
                    "DELETE FROM " + EMPLOYED_TABLE_NAME +
                            " WHERE Employee_Id = ? AND Lab_Id = ?");
            pstmt.setInt(1,employeeID);
            pstmt.setInt(2,labID);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 1)
                return OK;
            else if(affectedRows == 0)
                return NOT_EXISTS;
            return ERROR;
        } catch (SQLException e) {
            //e.printStackTrace();
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
        }
    }

    /**
     * The vaccine with vaccineID is now produced at the lab with labID.
     */
    public static ReturnValue labProduceVaccine(Integer vaccineID, Integer labID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("INSERT INTO " + PRODUCTION_TABLE_NAME + "(Lab_Id,Vaccine_Id) " +
                    " VALUES (?, ?)");
            pstmt.setInt(1,labID);
            pstmt.setInt(2, vaccineID);
            pstmt.execute();
            return OK;
        } catch (SQLException e) {
            //e.printStackTrace();
            Integer errorCode = Integer.valueOf(e.getSQLState());
            if(errorCode == PostgreSQLErrorCodes.FOREIGN_KEY_VIOLATION.getValue())
                return NOT_EXISTS;
            if(errorCode == PostgreSQLErrorCodes.UNIQUE_VIOLATION.getValue())
                return ALREADY_EXISTS;
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
        }
    }

    /**
     * The vaccine with vaccineID is no longer produced in the lab with labID.
     */
    public static ReturnValue labStoppedProducingVaccine(Integer labID, Integer vaccineID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(
                    "DELETE FROM " + PRODUCTION_TABLE_NAME +
                            " WHERE Lab_Id = ? AND Vaccine_Id = ?");
            pstmt.setInt(1,labID);
            pstmt.setInt(2,vaccineID);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 1)
                return OK;
            else if(affectedRows == 0)
                return NOT_EXISTS;
            return ERROR;
        } catch (SQLException e) {
            //e.printStackTrace();
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
//                return ERROR;
            }
        }
    }

    /**
     * Confirms that amount units of the vaccine with id vaccineID were sold.
     * As a result of this sell, the amount in stock is reduced by amount,
     * the cost of the vaccine is now doubled
     * and the productivity is now productivity+15 up to a maximum of 100%.
     * @param vaccineID ID of the vaccine
     * @param amount that amount (assume >=0) units of it were sold.
     */
    public static ReturnValue vaccineSold(Integer vaccineID, Integer amount) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(
                    "UPDATE " + VACCINE_TABLE_NAME +
                            " SET (Units_in_Stock, Cost, Income, Productivity) = \n" +
                            " (Units_in_Stock-?, Cost*2, Income + Cost*?, LEAST(Productivity+15,100))\n" +
                            " WHERE Vaccine_Id = ?");
            pstmt.setInt(1,amount);
            pstmt.setInt(2,amount);
            pstmt.setInt(3,vaccineID);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 1)        // amount is less than or equal to the number of units in stock
                return OK;                    // and vaccineID exists.
            else if(affectedRows == 0)    // vaccineID doesn't exist.
                return NOT_EXISTS;
            return ERROR;
        } catch (SQLException e) {
            //e.printStackTrace()();
            Integer errorCode = Integer.valueOf(e.getSQLState());
            if(errorCode == PostgreSQLErrorCodes.CHECK_VIOLATION.getValue())
                return BAD_PARAMS;              //amount is greater than the number of units in stock
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
    }

    /**
     * Confirms that amount units of the vaccine with id vaccineID were produced.
     * As a result of this production, amount is added to the amount in stock,
     * the cost of the vaccine is now halved (7/2=3)
     * and the productivity is now productivity-15 until a minimum of 0%.
     * @param vaccineID of the vaccine that amount units of it were produced.
     * @return ReturnValue with the following conditions:
     *          OK in case of success
     *          BAD_PARAMS if amount is negative.
     *          NOT_EXISTS if this vaccine does not exist.
     *          ERROR in case of a database error.
     */
    public static ReturnValue vaccineProduced(Integer vaccineID, Integer amount) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt=connection.prepareStatement("SELECT 1 WHERE ?<0");
            pstmt.setInt(1,amount);
            ResultSet results = pstmt.executeQuery();
            if(results.next()) {
                results.close();
                return BAD_PARAMS;              //amount is negative
            }
            results.close();

            pstmt = connection.prepareStatement(
                    "UPDATE " + VACCINE_TABLE_NAME +
                            " SET (Units_in_Stock, Cost, Productivity) = \n" +
                            " (Units_in_Stock+?, Cost/2, GREATEST(Productivity - 15, 0))\n" +           //integer division truncates the result
                            " WHERE Vaccine_Id = ?");
            pstmt.setInt(1,amount);
            pstmt.setInt(2,vaccineID);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 1)                          // vaccineID exists.
                return OK;                                  //successfully finished updating.
            else if(affectedRows == 0)                      // vaccineID doesn't exist.
                return NOT_EXISTS;
            return ERROR;
        } catch (SQLException e) {
            //e.printStackTrace()();
            if(Integer.valueOf(e.getSQLState()) == PostgreSQLErrorCodes.CHECK_VIOLATION.getValue())
                return BAD_PARAMS;                          // (Units_in_Stock + amount) is negative
            return ERROR;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
    }

    /**
     * Returns whether all of the labs vaccines have a productivity of over 20%.
     * If a lab has 0 vaccines then in an empty way, it satisfies the condition.
     * @return The Boolean result of the above condition. On any error (such as lab does not exist) return false.
     */
    public static Boolean isLabPopular(Integer labID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            //lab exists;
            pstmt = connection.prepareStatement(" SELECT Lab_Id FROM " + LAB_TABLE_NAME +
                    " WHERE Lab_Id = ?");
            pstmt.setInt(1,labID);

            ResultSet results1 = pstmt.executeQuery();
            if(!results1.next()) {            //no lab with labID found
                results1.close();
                return false;
            }
            results1.close();

            //lab is popular;
            pstmt = connection.prepareStatement(" SELECT Vaccine_Id FROM " + LAB_VACCINE_VIEW_NAME +
                                                    " WHERE Lab_Id = ? AND Productivity <= 20");
            pstmt.setInt(1,labID);

            ResultSet results2 = pstmt.executeQuery();
            if(results2.next()) {            //found vaccines with productivity<=20
                results2.close();
                return false;
            }
            results2.close();
            return true;
        } catch (SQLException e) {
            //e.printStackTrace();
            return false;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
    }

    /**
     * Returns the amount of money payed for buying the vaccine with vaccineID.
     * The output should be the amount of money payed for vaccineID from the moment it entered the system up until now.
     * @return the amount of money payed on vaccineID, 0 in failure such as vaccine does not exist.
     */
    public static Integer getIncomeFromVaccine(Integer vaccineID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(" SELECT Income FROM " + VACCINE_TABLE_NAME +
                                                    " WHERE Vaccine_Id = ?");
            pstmt.setInt(1,vaccineID);

            ResultSet results = pstmt.executeQuery();
            if(results.next()) {
                Integer income = results.getInt(1);
                results.close();
                return income;
            }
            results.close();
            return 0;
        } catch (SQLException e) {
            //e.printStackTrace();
            return 0;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
    }

    /**
     * Returns the total number of vaccines (units) that work (over 20% productivity).
     */
    public static Integer getTotalNumberOfWorkingVaccines() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(" SELECT SUM(Units_in_Stock) FROM " + VACCINE_TABLE_NAME +
                    " WHERE Productivity > 20");

            ResultSet results = pstmt.executeQuery();
            if(results.next()) {
                Integer s = results.getInt(1);
                results.close();
                return s;
            }
            results.close();
            return 0;
        } catch (SQLException e) {
            //e.printStackTrace();
            return 0;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
    }

    /**
     * Return the lab's wages (employees' salaries) expense,
     * only for a lab with more than one employee and active. Otherwise 0.
     */
    public static Integer getTotalWages(Integer labID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(" SELECT Active FROM " + LAB_TABLE_NAME +
                    " WHERE Lab_Id = ?");
            pstmt.setInt(1,labID);

            ResultSet results = pstmt.executeQuery();
            if(results.next()) {                                    //lab exists
                if(results.getBoolean(1)) {              //lab active
                    pstmt = connection.prepareStatement("SELECT total_salary\n" +
                            "FROM\n" +
                            "   (SELECT COUNT(*) AS Number_of_Employees, SUM(Salary) AS Total_Salary FROM Employed\n" +
                            "    WHERE Lab_Id = ?) AS T1\n" +
                            "WHERE Number_of_Employees>1");
                    pstmt.setInt(1,labID);

                    ResultSet results2 = pstmt.executeQuery();
                    if(results2.next()) {
                        Integer totalSalary = results2.getInt(1);
                        results.close();
                        results2.close();
                        return totalSalary;          //more than one employee in the lab
                    }
                    results2.close();
                }
            }
            results.close();
            return 0;                   //lab doesn't exist or not active or one or less employees
        } catch (SQLException e) {
            //e.printStackTrace();
            return 0;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
    }

    /**
     * Returns the name of the lab with the most employees working at it,
     * that their birth city is the same as lab's location (this number must be above 0,
     * otherwise it counts as a failure).
     * In case of equality, return the lab with the smallest ID (between those which are best).
     * @return ID of the lab;
     *         0 in any case of failure or if all the employees are not working.
     */
    public static Integer getBestLab() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("SELECT Lab_Id\n" +
                    "FROM\n" +
                    "   (SELECT Lab_Id, count(*) AS Local_Employees\n" +
                    "    FROM " + EMPLOYEE_LAB_VIEW_NAME + "\n" +
                    "    WHERE City_of_Birth = City\n" +
                    "    GROUP BY Lab_Id) as T1\n" +
                    "WHERE Local_Employees > 0\n" +
                    "ORDER BY Local_Employees DESC,Lab_Id\n" +
                    "LIMIT 1");

            ResultSet results = pstmt.executeQuery();
            if(results.next()) {
                Integer bestLab = results.getInt(1);
                results.close();
                return bestLab;
            }
            results.close();
            return 0;                       //no local employees in any lab
        } catch (SQLException e) {
            //e.printStackTrace();
            return 0;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
    }

    /**
     * Returns the birth city that has the highest number of employees working *that are FROM* it.
     * Note that if an employee works at two labs, he will be count twice.
     * In case of equality, return the last by lexicographical order (between those which are most popular).
     * @return String of the city that has the highest number of employees working *that are FROM* that city.
     *         The empty string "" if there are no working employees.
     *         null in any other case.
     */
    public static String getMostPopularCity() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("SELECT City_of_Birth\n" +
                    "FROM\n" +
                    "(SELECT City_of_Birth, COUNT(*) AS Employees_From_City\n" +
                    "FROM " + EMPLOYEE_LAB_VIEW_NAME + "\n" +
                    "GROUP BY City_of_Birth) AS T1\n" +
                    "ORDER BY Employees_From_City DESC,City_of_Birth DESC\n" +
                    "LIMIT 1");

            ResultSet results = pstmt.executeQuery();
            if(results.next()) {
                String mostPopularCity = results.getString(1);
                results.close();
                return mostPopularCity;
            }
            results.close();
            return "";                       //no employees in any lab
        } catch (SQLException e) {
            //e.printStackTrace();
            return null;
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
    }

    /**
     * Returns a list with 3 items containing the lab *IDs*,
     * where all of the lab's vaccines have a productivity of over 20%.
     * In case of equality, order by ID in ascending order.
     * Note that in order to appear in this list, a lab must produce at least one vaccine.
     * @return ArrayList with the labs' ids (if there are less than 3 labs, return an Arraylist with the <3 labs).
     *         Empty ArrayList in any other case.
     */
    public static ArrayList<Integer> getPopularLabs() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("SELECT DISTINCT Lab_Id\n" +
                    "FROM " + LAB_VACCINE_VIEW_NAME + "\n" +
                    "EXCEPT\n" +
                    "(SELECT DISTINCT Lab_Id\n" +
                    "FROM " + LAB_VACCINE_VIEW_NAME + "\n" +
                    "WHERE Productivity<=20)\n" +
                    "ORDER BY Lab_Id\n" +
                    "LIMIT 3");

            ResultSet results = pstmt.executeQuery();
            ArrayList<Integer> popularLabs = new ArrayList<>();
            while(results.next())
                popularLabs.add(results.getInt(1));

            results.close();
            return popularLabs;
        } catch (SQLException e) {
            //e.printStackTrace();
            return new ArrayList<>();
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
    }

    /**
     * Returns a list of the top 10 vaccines' ids according to the following rating calculation in descending order.
     * @return ArrayList with the vaccine's ids that satisfy the conditions above
     *         (if there are less than 10 vaccines, return an Arraylist with the <10 vaccines).
     *         Empty ArrayList in any other case.
     */
    public static ArrayList<Integer> getMostRatedVaccines() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement("SELECT Vaccine_Id\n" +
                    "FROM\n" +
                    "(SELECT Vaccine_Id, Productivity + Units_in_Stock - Cost as Rating\n" +
                    "FROM " + VACCINE_TABLE_NAME + "\n" +
                    "ORDER BY Rating DESC, Vaccine_Id\n" +
                    "LIMIT 10) AS T1");

//            SELECT Vaccine_Id
//            FROM Vaccine
//            ORDER BY Productivity + Units_in_Stock - Cost DESC, Vaccine_Id
//            LIMIT 10

            ResultSet results = pstmt.executeQuery();
            ArrayList<Integer> mostRatedVaccines = new ArrayList<>();
            while(results.next())
                mostRatedVaccines.add(results.getInt(1));

            results.close();
            return mostRatedVaccines;
        } catch (SQLException e) {
            //e.printStackTrace();
            return new ArrayList<>();
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
    }

    /**
     * Returns a list of the 10 "close employees" to the employee with id employeeID.
     * Close employees are defined as employees who work at least (>=) 50% of the labs the employee with id employeeID does.
     * Note that one cannot be a close employee of himself.
     * The list should be ordered by ids in ascending order.
     * @return ArrayList with the employees' ids that meet the conditions described above
     *         (if there are less than 10 employees, return an Arraylist with the <10 employees).
     *         Empty ArrayList in any other case.
     *         Note: employees are close can be vacuously true. (in an empty way) (employee in question doesn't work).
     */
    public static ArrayList<Integer> getCloseEmployees(Integer employeeID) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;
        try {
            ArrayList<Integer> closeEmployees = new ArrayList<>();


            pstmt = connection.prepareStatement("SELECT Employee_Id\n" +
                    "FROM " + EMPLOYEE_TABLE_NAME + "\n" +
                    "WHERE Employee_Id = ?");
            pstmt.setInt(1,employeeID);

            ResultSet results = pstmt.executeQuery();
            if(!results.next()) {
                results.close();
                return new ArrayList<>();                         //employee doesn't exist
            }


            pstmt = connection.prepareStatement("SELECT COUNT(Lab_Id)\n" +
                    "FROM " + EMPLOYED_TABLE_NAME + "\n" +
                    "WHERE Employee_Id = ?");
            pstmt.setInt(1,employeeID);

            results = pstmt.executeQuery();
            if(!results.next()) {
                results.close();
                return new ArrayList<>();                         //some DB error, didn't return count
            }
            Integer employeeJobs = results.getInt(1);
            if(employeeJobs == 0) {                               //employee doesn't work
                pstmt = connection.prepareStatement("SELECT Employee_Id\n" +
                        "FROM " + EMPLOYEE_TABLE_NAME + "\n" +
                        "WHERE Employee_ID != ?\n" +
                        "ORDER BY Employee_Id\n" +
                        "LIMIT 10");
                pstmt.setInt(1,employeeID);

                results = pstmt.executeQuery();
                while(results.next())
                    closeEmployees.add(results.getInt(1));
                results.close();
                return closeEmployees;
            }

            pstmt = connection.prepareStatement("SELECT Employee_ID\n" +
                    "FROM\n" +
                    "\t(SELECT Employee_ID, Count(Employee_ID) AS Shared_Labs\n" +
                    "\tFROM " + EMPLOYED_TABLE_NAME + "\n" +
                    "\tWHERE Lab_ID IN\n" +
                    "\t\t\t\t(SELECT Lab_ID\n" +
                    "\t\t\t\tFROM Employed\n" +
                    "\t\t\t\tWHERE Employee_ID = ?) AND Employee_ID != ?\n" +
                    "\tGROUP BY Employee_ID\n" +
                    "\tORDER BY Shared_Labs DESC, Employee_ID\n" +
                    "\t) AS T1\n" +
                    "WHERE Shared_Labs >= (SELECT Count(Employee_ID)\n" +
                    "\t\t\t\t\t\tFROM Employed\n" +
                    "\t\t\t\t\t\tWHERE Employee_ID = ?)/2.0\n" +
                    "ORDER BY Employee_ID\n" +
                    "LIMIT 10");
            pstmt.setInt(1,employeeID);
            pstmt.setInt(2,employeeID);
            pstmt.setInt(3,employeeID);

            results = pstmt.executeQuery();
            while(results.next())
                closeEmployees.add(results.getInt(1));
            results.close();
            return closeEmployees;                                //employee exists and works

        } catch (SQLException e) {
            //e.printStackTrace();
            return new ArrayList<>();
        }
        finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                //e.printStackTrace()();
            }
        }
    }



    private static void kcreateTable(String name, ArrayList<Pair<String, String>> typedSchema) {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE " + name + "\n" + "(\n");
            for (Pair field : typedSchema)
                sb.append("    " + field.getKey() + " " + field.getValue() + ",\n");
            sb.deleteCharAt(sb.length()-2);             //delete last ","
            sb.append(")");
            pstmt = connection.prepareStatement(sb.toString());

            pstmt.execute();

        } catch (SQLException e) {
//            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
//                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (SQLException e) {
//                e.printStackTrace();
            }
        }
    }

    private static void createLabVaccineView() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE OR REPLACE VIEW " + LAB_VACCINE_VIEW_NAME + " AS\n" +
                    "(\n" +
                    "    SELECT " + PRODUCTION_TABLE_NAME + ".Lab_Id, " + PRODUCTION_TABLE_NAME + ".Vaccine_Id," +
                            VACCINE_TABLE_NAME + ".Productivity\n" +
                    "    FROM " + PRODUCTION_TABLE_NAME + ", " + VACCINE_TABLE_NAME + "\n" +
                    "    WHERE " + PRODUCTION_TABLE_NAME + ".Vaccine_Id = " + VACCINE_TABLE_NAME + ".Vaccine_Id\n" +
                    ")");
            pstmt = connection.prepareStatement(sb.toString());
            pstmt.execute();
        } catch (SQLException e) {
//            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
//                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (SQLException e) {
//                e.printStackTrace();
            }
        }
    }

    private static void createEmployeeLabView() {
        Connection connection = DBConnector.getConnection();
        PreparedStatement pstmt = null;

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE OR REPLACE VIEW " + EMPLOYEE_LAB_VIEW_NAME + " AS\n" +
                    "(\n" +
                    "    SELECT " + EMPLOYEE_TABLE_NAME + ".Employee_Id, " + EMPLOYEE_TABLE_NAME + ".City_of_Birth," +
                    LAB_TABLE_NAME + ".Lab_Id, " + LAB_TABLE_NAME + ".City\n" +
                    "    FROM " + EMPLOYED_TABLE_NAME + ", " + EMPLOYEE_TABLE_NAME + ", " + LAB_TABLE_NAME + "\n" +
                    "    WHERE " + EMPLOYED_TABLE_NAME + ".Employee_Id = " + EMPLOYEE_TABLE_NAME + ".Employee_Id\n" +
                    "        AND " + EMPLOYED_TABLE_NAME + ".Lab_Id = " + LAB_TABLE_NAME + ".Lab_Id\n" +
                    ")");
            pstmt = connection.prepareStatement(sb.toString());
            pstmt.execute();
        } catch (SQLException e) {
//            e.printStackTrace();
        } finally {
            try {
                pstmt.close();
            } catch (SQLException e) {
//                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (SQLException e) {
//                e.printStackTrace();
            }
        }
    }
}

