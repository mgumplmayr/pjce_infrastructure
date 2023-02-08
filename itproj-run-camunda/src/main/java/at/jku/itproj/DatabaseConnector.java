package at.jku.itproj;

import java.sql.*;

/**The DatabaseConnector class is used to connect to the mariaDB database.
 * */
public class DatabaseConnector {
    private static Connection connection;
    private static String url = "jdbc:mariadb://localhost:3306/ceprojekt";
    private static String user = "root";
    private static String password = "password";

    /**The insertMessage method inserts the message into the relational database.
     * @param tableName String to be used as the name of the table to insert the message into
     * @param processID String to be inserted into the database in the processID column
     * @param topic String to be inserted into the database in the topic column
     * @param content String to be inserted into the database in the content column
     * */
    public static void insertMessage(String tableName, String processID, String topic, String content){
        try {
            //get connection to database
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connection to Database successfull");

            //build statement
            String query = "INSERT INTO tableName (process_id, topic, content) VALUES (?, ?, ?)";
            query =query.replace("tableName",tableName);
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, processID);
            statement.setString(2, topic);
            statement.setString(3, content);

            //execute statement
            int rowsInserted = statement.executeUpdate();
            System.out.println(rowsInserted + " rows inserted.");
            connection.close();
        } catch (SQLException e) {
            System.out.println("Insertion into to Database failed");
            e.printStackTrace();
        }
    }

    /**The getContent method retrieves the content of a processID and prints out the results.
     * @param tableName String to be used as the name of the table to retrieve the content from
     * @param processID String to be used as the processID to retrieve the content from
     * */
    public static void getContent(String tableName, String processID){
        try {
            //build statement
            connection = DriverManager.getConnection(url, user, password);
            String query = "SELECT * FROM tableName WHERE process_id = ?";
            query =query.replace("tableName",tableName);
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, processID);

            //execute statement
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                String date = resultSet.getTimestamp("date").toString();
                String topic = resultSet.getString("topic");
                String content = resultSet.getString("content");
                System.out.println("date: " + date + "topic: " + topic + ", content: " + content);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeContent(String processID){}
}
