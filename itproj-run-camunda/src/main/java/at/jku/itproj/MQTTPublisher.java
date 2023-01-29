package at.jku.itproj;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.springframework.stereotype.Service;

@Service("MQTTPublisher")
public class MQTTPublisher extends MQTTDelegator implements JavaDelegate {
    private MqttClient client;
    private String topic;

    private DelegateExecution execution;
            @Override
            public void execute(final DelegateExecution execution) throws Exception {
                System.out.println("Executing MQTTPublisher");
                topic = execution.getCurrentActivityName();
                client = getClient(execution.getCurrentActivityId());
                String username = execution.getVariable("username").toString();
                String message = "Hello, my name is: " + username +" and my process ID is: "+execution.getProcessInstanceId();
                System.out.println(message +" will be sent via MQTT");
                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                client.publish(topic, mqttMessage);
                //SQL Statment: Process ID; Topic; Content; Timestamp
                insertMessage(execution.getProcessInstanceId(), topic, message);
                client.disconnect();
            }

    public void insertMessage(String processID, String topic, String content){
        DatabaseConnector.insertMessage("sentMessages", processID, topic, content);
    }
}
