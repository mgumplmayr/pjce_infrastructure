package at.jku.itproj;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.springframework.stereotype.Service;

@Service("MQTTPublisher")
public class MQTTPublisher extends MQTTDelegator implements JavaDelegate {
    private MqttClient client;
    private String topic;
    private String processID;

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        System.out.println("Executing MQTTPublisher");
        topic = execution.getCurrentActivityName();
        processID = execution.getProcessInstanceId();
        client = getClient(execution.getCurrentActivityId());
        String status = execution.getVariable(createVariableName(topic+"_pub")).toString(); //Struktur für Variable bei Publisher: topic und _pub
        String message = "{\n\"processID\":"+"\""+processID+"\""+",\n\"status\":"+"\""+status+"\""+"\n}";
        System.out.println("Publishing on topic "+topic+": "+message);
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
