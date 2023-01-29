package at.jku.itproj;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public abstract class MQTTDelegator {
    private String broker = "tcp://localhost:1883";

    private MqttClient client;

    MqttClient getClient(String clientID) {
        System.out.println("Generating client with ID"+clientID);
        MemoryPersistence persistence = new MemoryPersistence();
        System.out.println("ClientID for MQTT Receiver: "+clientID);
        try{
        client = new MqttClient(broker, clientID, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        client.connect(connOpts);
        }
        catch(Exception e) {
            System.out.println("Error while creating Client for MQTT Broker");
            e.printStackTrace();
        }
        return client;
    }
}
