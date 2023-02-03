package at.jku.itproj;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.SimpleDateFormat;

public abstract class MQTTDelegator {
    private final String broker = "tcp://localhost:1883";

    private MqttClient client;

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
            System.out.println("Error while creating Client for MQTT Broker");
            e.printStackTrace();
        }
        return client;
    }
}
