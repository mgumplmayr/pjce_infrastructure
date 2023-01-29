package at.jku.itproj;

public class DatabaseConnector {

    String url = "mariadb";
    String user = "root";

    //connect
    public static void insertMessage(String tableName, String processID, String topic, String content){
        System.out.println("Connecting to Database");
    }
}
