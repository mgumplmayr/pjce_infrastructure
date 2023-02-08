package at.jku.itproj;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

/** The MQTTPublisher class is used to publish messages to the MQTT broker.
 * The name of the message that calls this class is used to set the topic and
 * the name of the variable the message to be sent was received from.
 * */
@Service("MQTTPublisher")
public class MQTTPublisher extends MQTTDelegator implements JavaDelegate {

    /**The execute method is called by the Camunda engine through an execution listener when the message throw event is reached in the process.
     * The method creates a client for the MQTT broker and subscribes to the topic of the event.
     * A JSON String is generated from the variables in the process and published to the broker.
     * @param execution DelegateExecution passed by the Camunda engine
     * */
    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        //set variables
        System.out.println("Executing MQTTPublisher");
        String topic = execution.getCurrentActivityName();
        String processID = execution.getProcessInstanceId();
        MqttClient client = getClient(execution.getCurrentActivityId());
        String status = execution.getVariable(createVariableName(topic+"_pub"))==null? "null": execution.getVariable(createVariableName(topic+"_pub")).toString(); //Struktur für Variable bei Publisher: topic und _pub
        String message = "{\n\"processID\":"+"\""+processID+"\""+",\n\"status\":"+"\""+status+"\""+"\n}";

        //publish message
        System.out.println("Publishing on topic "+topic+": \n"+message);
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        client.publish(topic, mqttMessage);
        insertMessage(execution.getProcessInstanceId(), topic, status);
        client.disconnect();
    }

    public void insertMessage(String processID, String topic, String content){
        DatabaseConnector.insertMessage("sent_messages", processID, topic, content);
    }
}
