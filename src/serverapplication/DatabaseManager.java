package serverapplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author hardworker
 */
public class DatabaseManager {

    private final String databasePath;
    private final String databaseName;
    private final String databaseUsername;
    private final String databaseUserPassword;
    private Connection connection;

    /**
     *
     * @param databasePath
     * @param databaseName
     * @param databaseUsername
     * @param databaseUserPassword
     */
    public DatabaseManager(String databasePath, String databaseName, String databaseUsername, String databaseUserPassword) {
        this.databasePath = databasePath;
        this.databaseName = databaseName;
        this.databaseUsername = databaseUsername;
        this.databaseUserPassword = databaseUserPassword;
    }

    private boolean getDatabaseConnection() {
        boolean state = true;
        try {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
            connection = DriverManager.getConnection(databasePath + "/" + databaseName, databaseUsername, databaseUserPassword);
            return true;
        } catch (SQLException exception) {
            System.out.println("databasemanagerapp.DatabaseManager.databaseConnection().error: " + exception.getMessage());
            return false;
        }
    }

    /**
     *
     * @param tableName
     * @param idValue
     * @param autoIncrement
     * @param tableData
     * @return
     * @throws SQLException
     */
    public boolean insertDatabase(String tableName, int idValue, boolean autoIncrement, LinkedHashMap<String, String> tableData) throws SQLException {
        if (getDatabaseConnection()) {
            String columnsNames = "";
            String queryComplete = "";
            if (autoIncrement != true) {
                queryComplete += "?,";
            }
            int counter = 1;
            for (Map.Entry<String, String> entry : tableData.entrySet()) {
                String key = entry.getKey();
                if (counter == tableData.size()) {
                    columnsNames += key;
                    queryComplete += "?) ";
                    counter++;
                } else {
                    columnsNames += key + ",";
                    queryComplete += "?,";
                    counter++;
                }
            }
            String insertQuery = "INSERT INTO "+ tableName + "(" + columnsNames + ") VALUES(" + queryComplete;
            System.out.println(insertQuery);
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            int statementCounter = 1;
            if (autoIncrement != true) {
                preparedStatement.setInt(statementCounter, idValue);
                statementCounter++;
            }
            for (Map.Entry<String, String> entry : tableData.entrySet()) {
                String value = entry.getValue();
                preparedStatement.setString(statementCounter, value);
                statementCounter++;
            }
            return (preparedStatement.executeUpdate() == 1);
        } else {
            return false;
        }
    }

    /**
     *
     * @param tableName
     * @param idValue
     * @param idColumnName
     * @param idBased
     * @param updatedValues
     * @return
     * @throws SQLException
     */
    public boolean updateDatabase(String tableName, int idValue, String idColumnName, boolean idBased, LinkedHashMap<String, String> updatedValues) throws SQLException {
        // update table (values) where (condition)
        if (getDatabaseConnection()) {
            int statementCounter = 1;
            String updateQuery = "UPDATE " + tableName + " SET ";
            for (Map.Entry<String, String> entry : updatedValues.entrySet()) {
                String key = entry.getKey();
                if (statementCounter == updatedValues.size()) {
                    updateQuery += key + "=?";
                } else {
                    updateQuery += key + "=?,";
                    ++statementCounter;
                }
            }
            statementCounter = 1;
            if (idBased) {
                updateQuery += " WHERE " + idColumnName + "=?";
            }
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
            for (Map.Entry<String, String> entry : updatedValues.entrySet()) {
                String value = entry.getValue();
                preparedStatement.setString(statementCounter, value);
                statementCounter++;
            }
            if (idBased) {
                preparedStatement.setInt(statementCounter, idValue);
            }
            return (preparedStatement.executeUpdate() == 1);
        } else {
            return false;
        }
    }

    /**
     *
     * @param tableName
     * @param idValue
     * @param idColumnName
     * @param idBased
     * @return
     * @throws SQLException
     */
    public boolean deleteDatabase(String tableName, int idValue, String idColumnName, boolean idBased) throws SQLException {
        if (getDatabaseConnection()) {
            String deleteQuery = "DELETE FROM " + tableName + " ";
            if (idBased) {
                deleteQuery += "WHERE " + idColumnName + "=?";
            }
            System.out.println(deleteQuery);
            PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
            preparedStatement.setInt(1, idValue);
            return (preparedStatement.executeUpdate() == 1);
        } else {
            return false;
        }
    }

    /**
     *
     * @param tableName
     * @param conditionData
     * @return
     * @throws SQLException
     */
    public LinkedHashMap<String, String> selectOneDatabase(String tableName, LinkedHashMap<String, String> conditionData) throws SQLException {
        ResultSet resultSet = null;
        LinkedHashMap<String, String> resultData = new LinkedHashMap<>();
        if (getDatabaseConnection()) {
            String selectOneQuery = "SELECT * FROM " + tableName + " WHERE ";
            int statementCounter = 1;
            for (Map.Entry<String, String> entry : conditionData.entrySet()) {
                String key = entry.getKey();
                if (statementCounter == conditionData.size()) {
                    selectOneQuery += key + "=?";
                } else {
                    selectOneQuery += key + "=? AND ";
                }
                statementCounter++;
            }
            statementCounter = 1;
            PreparedStatement preparedStatement = connection.prepareStatement(selectOneQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            for (Map.Entry<String, String> entry : conditionData.entrySet()) {
                String value = entry.getValue();
                preparedStatement.setString(statementCounter, value);
                statementCounter++;
            }
            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnLength = resultSetMetaData.getColumnCount();
            // Many Rows
            if (resultSet.first() == true && resultSet.next() == true) {
                return null;
            } // One Row
            else if (resultSet.first() == true && resultSet.next() == false) {
                resultSet.previous();
                for (int i = 1; i <= columnLength; i++) {
                    String columnName = resultSetMetaData.getColumnName(i);
                    String columnValue = resultSet.getString(columnName);
                    resultData.put(columnName, columnValue);
                }
                return resultData;
            } // None
            else {
                return null;
            }
        } else {
            return null;
        }
    }

    // Get All Function
    public LinkedHashMap<String, LinkedHashMap<String, String>> selectTable(String tableName, LinkedHashMap<String, String> conditionData) throws SQLException {
        ResultSet resultSet = null;
        ResultSetMetaData resultSetMetaData = null;
        LinkedHashMap<String, LinkedHashMap<String, String>> tableData = new LinkedHashMap<>();
        if (getDatabaseConnection()) {
            String query = "SELECT * FROM " + tableName + "";
            if (conditionData != null) {
                query += " WHERE ";
                int counter = 1;
                for (Map.Entry<String, String> entry : conditionData.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (counter == conditionData.size()) {
                        query += key + "=? ";
                    } else {
                        query += key + "=? AND";
                    }
                }
            } else {
                PreparedStatement preparedStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                resultSet = preparedStatement.executeQuery();
                resultSetMetaData = resultSet.getMetaData();
                int columnLength = resultSetMetaData.getColumnCount();
                while (resultSet.next()) {
                    String keyValue = "";
                    LinkedHashMap<String, String> columns = new LinkedHashMap<>();
                    for (int i = 1; i < columnLength; i++) {
                        String columnName = resultSetMetaData.getColumnName(i);
                        String columnValue = resultSet.getString(i);
                        if (i == 1) {
                            keyValue = columnValue;
                        } else {
                            columns.put(columnName, columnValue);
                        }
                    }
                    tableData.put(keyValue, columns);
                }
                return tableData;
            }
        } else {
            return null;
        }
        return null;
    }

    /**
     *
     * @throws SQLException
     */
    public void closeConnection() throws SQLException {
        connection.close();
    }

}

// Database Class - Database Connection
// Server - Functions
