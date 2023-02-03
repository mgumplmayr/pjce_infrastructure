package at.jku.itproj;

import java.sql.*;

public class DatabaseConnector {



    //connect
    public static void insertMessage(String tableName, String processID, String topic, String content){

        String url = "jdbc:mariadb://localhost:3306/test";
        String user = "root";
        String password = "admin";

        try {
            // Verbindung zur Datenbank herstellen
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connection to Database successfull");

            /*
            // SELECT-Abfrage erstellen
            String query = "SELECT * FROM table_name";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);



            // Ergebnisse ausgeben
            while (resultSet.next()) {
                System.out.println(resultSet.getString("column_name"));
            }

            */
            String query = "INSERT INTO tableName (processID, topic) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, processID);
            statement.setString(2, topic);

            // Abfrage ausführen
            int rowsInserted = statement.executeUpdate();
            System.out.println(rowsInserted + " rows inserted.");

            // Verbindung schließen
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Something bad happaned !");
        }

    }
}
