package at.jku.itproj;

import camundajar.impl.com.google.gson.Gson;
import org.apache.tomcat.util.json.JSONParser;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

import java.util.Properties;


@Service
public class MQTTReceiver extends MQTTDelegator implements JavaDelegate  {
    private MqttClient client;
    private String topic;
    private String processID;
    private RuntimeService runtimeService;
    private DelegateExecution execution;

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        //topic name = message name = variable name, topic names in yaml festhalten, client id = id vom element
        System.out.println("Executing MQTTReceiver");
        client = getClient(execution.getCurrentActivityId());
        this.execution = execution;
        runtimeService = execution.getProcessEngineServices().getRuntimeService();
        client.setCallback(new MqttCallback() {
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String messageReceived = new String(message.getPayload());
                System.out.println("Received on topic "+topic+": "+messageReceived);
                //SQL Statment: Process ID; Topic; Content; Timestamp
                processMessage(messageReceived, topic);
            }
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
            public void connectionLost(Throwable cause) {
            }
        });
        topic = execution.getCurrentActivityName();
        System.out.println("Name of topic to subscribe: "+topic);
        client.subscribe(execution.getCurrentActivityName()); //auch möglich: # für alle topics
    }


    public void processMessage(String message, String topic) {
        System.out.println("Processing");
        Gson g = new Gson();
        Properties messageContent = g.fromJson(message, Properties.class);
        processID = messageContent.get("processID").toString();
        String status = messageContent.get("status").toString();
        System.out.println(messageContent);
        //bei den MQTT Nachrichten muss man die ProcessID des Vorgangs im JSON mitsenden.
        System.out.println("ProcessID of message: "+processID);
        System.out.println("Events in Queue: "+runtimeService.createEventSubscriptionQuery().list());
        EventSubscription event = runtimeService
            .createEventSubscriptionQuery()
            .processInstanceId(processID)
            .eventName(topic).singleResult();
        if(event==null){
                System.out.println("No subscription found with ProcessID: "+processID+"and messageName: "+topic);
        } else{
            System.out.println("Event to correlate: "+event);
            insertMessage(processID, topic, status);
            runtimeService.
                    createMessageCorrelation(topic).
                    processInstanceId(processID)
                    .correlate();
            }
            System.out.println(topic);
            System.out.println(status);
            runtimeService.setVariable(processID, topic+"_response", status); //Struktur für Variable bei Receiver: topic und _response
    }

    public void insertMessage(String processID, String topic, String content){
        DatabaseConnector.insertMessage("received Messages", processID, topic, content);
    }
}
