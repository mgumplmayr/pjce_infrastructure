package at.jku.itproj.deprecated;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttSubscribeExample {
    public static void main(String[] args) {
        String topic = "mytopic";
        String broker = "tcp://localhost:1883";
        String clientId = "JavaSubscribeExample";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient mqttClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            mqttClient.connect(connOpts);
            mqttClient.setCallback(new MqttCallback() {
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("Message received: " + new String(message.getPayload()));
                }
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
                public void connectionLost(Throwable cause) {
                }
            });
            mqttClient.subscribe(topic);
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
}
