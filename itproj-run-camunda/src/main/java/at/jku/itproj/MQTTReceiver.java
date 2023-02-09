package at.jku.itproj;

import camundajar.impl.com.google.gson.Gson;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;
import java.util.Properties;


/** The MQTTReceiver class is used to receive messages from the MQTT broker.
 * The name of the message that calls this class is used to set the topic and
 * the name of the variable the received message is set to.
 * */
@Service("MQTTReceiver")
public class MQTTReceiver extends MQTTDelegator implements JavaDelegate  {
    RuntimeService runtimeService;

    /**The execute method is called by the Camunda engine through an execution listener when the message catch event is reached in the process.
     * The method creates a client for the MQTT broker and subscribes to the topic of the event.
     * In the callback on messageArrived, the message is processed.
     * @param execution DelegateExecution passed by the Camunda engine
     * */
    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        System.out.println("Executing MQTTReceiver");
        MqttClient client = getClient(execution.getCurrentActivityId());
        runtimeService = execution.getProcessEngineServices().getRuntimeService();
        client.setCallback(new MqttCallback() {
            public void messageArrived(String topic, MqttMessage message) {
                String messageReceived = new String(message.getPayload());
                processMessage(messageReceived, topic, execution);
            }
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("Delivery complete - Token: "+token.toString());
            }
            public void connectionLost(Throwable cause) {
                System.out.println("Connection lost - Cause: "+cause.getCause());
            }
        });
        String topic = execution.getCurrentActivityName();
        System.out.println("Name of topic to subscribe: "+topic);
        client.subscribe(execution.getCurrentActivityName()); //auch möglich: # für alle topics
    }

    /**The processMessage method is called by the callback on messageArrived.
     * The method checks for a message with the processID, correlates the message and sets the variables in the process.
     * @param message String containing the message received from the MQTT broker
     * @param topic String containing the topic of the message
     * */
    public synchronized void processMessage(String message, String topic, DelegateExecution execution) { //nach einer Message terminieren funktioniert nicht - was wenn anderer Input?
        //get variables from message
        Gson g = new Gson();
        Properties messageContent = g.fromJson(message, Properties.class);
        String processID = messageContent.get("processID").toString();
        String status = messageContent.get("status").toString();
        if(!processID.equals(execution.getProcessInstanceId())) {
            System.out.println("Message not for this process");
            return;
        }
        //check if message event with processID exists
        EventSubscription event = runtimeService
                .createEventSubscriptionQuery()
                .processInstanceId(processID)
                .eventName(topic).singleResult();
        if(event==null){
            System.out.println("No subscription found with ProcessID: "+processID+"and messageName: "+topic);
            return;
        }

        //correlate message
        System.out.println("Received on Topic: "+topic+"\nContent: "+messageContent);
        //System.out.println("Events in Queue: "+runtimeService.createEventSubscriptionQuery().list());
        System.out.println("Event to correlate: "+event);
        runtimeService.
                createMessageCorrelation(topic).
                processInstanceId(processID)
                .correlate();
        System.out.println("Creating variable "+createVariableName(topic+"_rec")+" with content: "+status+"\n");
        runtimeService.setVariable(processID, createVariableName(topic+"_rec"), status); //Struktur für Variable bei Receiver: topic und _rec
        insertMessage(processID, topic, status);
    }

    public void insertMessage(String processID, String topic, String content){
        DatabaseConnector.insertMessage("received_messages", processID, topic, content);
    }
}
