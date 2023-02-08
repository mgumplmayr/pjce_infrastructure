package at.jku.itproj;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class MQTTDelegator {
    private final String broker = "tcp://localhost:1883";
    private MqttClient client;

    /**The getClient method creates a client for the MQTT broker.
     * The client ID is created from the String passed to the method and the current timestamp.
     * @param clientID String to be used as part of the client ID
     *                 (e.g. the current activity ID of the event)
     * @return MqttClient client used to connect to the broker
     * */
    MqttClient getClient(String clientID) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new java.util.Date());
        System.out.println("Generating client with ID: " + clientID+" and "+timeStamp);
        clientID+="_"+timeStamp;
        MemoryPersistence persistence = new MemoryPersistence();
        System.out.println("ClientID for "+getClass()+": " + clientID);
        try {
            client = new MqttClient(broker, clientID, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            client.connect(connOpts);
        } catch (Exception e) {
            System.out.println("Error while creating Client for MQTT Broker for "+getClass());
            e.printStackTrace();
        }
        return client;
    }

    /**The createVariableName method creates the variable name from the topic name.
     * It replaces characters that are not allowed in variable names with underscores.
     * @param toTransform String to be transformed into a valid variable name
     *                 (e.g. the topic to be transformed into a variable name)
     * @return String variable name that can be used in Camunda
     * */
    String createVariableName(String toTransform){
        Pattern pattern = Pattern.compile("\\W");
        Matcher matcher = pattern.matcher(toTransform);
        return matcher.replaceAll("_");
    }

    /**The insertMessage method inserts the message into the database.
     * @param processID String to be inserted into the database in the processID column
     * @param topic String to be inserted into the database in the topic column
     * @param content String to be inserted into the database in the content column
     * */
    public abstract void insertMessage(String processID, String topic, String content);
}
