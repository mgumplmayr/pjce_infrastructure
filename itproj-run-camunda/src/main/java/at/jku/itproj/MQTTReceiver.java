package at.jku.itproj;

import camundajar.impl.com.google.gson.Gson;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
        this.execution = execution;
        client = getClient(execution.getCurrentActivityId());
        runtimeService = execution.getProcessEngineServices().getRuntimeService();
        client.setCallback(new MqttCallback() {
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String messageReceived = new String(message.getPayload());
                //SQL Statment: Process ID; Topic; Content; Timestamp
                processMessage(messageReceived, topic);
            }
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("Delivery complete - Token: "+token.toString());
            }
            public void connectionLost(Throwable cause) {
                System.out.println("Connection lost - Cause: "+cause.getCause());
            }
        });
        topic = execution.getCurrentActivityName();
        System.out.println("Name of topic to subscribe: "+topic);
        client.subscribe(execution.getCurrentActivityName()); //auch möglich: # für alle topics
    }


    public synchronized void processMessage(String message, String topic) throws InterruptedException { //nach einer Message terminieren funktioniert nicht - was wenn anderer Input?
        Gson g = new Gson();
        Properties messageContent = g.fromJson(message, Properties.class);
        processID = messageContent.get("processID").toString();
        String status = messageContent.get("status").toString();
        //bei den MQTT Nachrichten muss man die ProcessID des Vorgangs im JSON mitsenden.
        EventSubscription event = runtimeService
                .createEventSubscriptionQuery()
                .processInstanceId(processID)
                .eventName(topic).singleResult();
        if(event==null){
            System.out.println("No subscription found with ProcessID: "+processID+"and messageName: "+topic);
            return;
        }
        System.out.println("Received on Topic: "+topic+"\nContent: "+messageContent);
        System.out.println("Events in Queue: "+runtimeService.createEventSubscriptionQuery().list());
        System.out.println("Event to correlate: "+event);
        insertMessage(processID, topic, status);
        runtimeService.
                createMessageCorrelation(topic).
                processInstanceId(processID)
                .correlate();
        System.out.println("Creating variable "+createVariableName(topic+"_rec")+"_response with content: "+status);
        runtimeService.setVariable(processID, createVariableName(topic+"_rec"), status); //Struktur für Variable bei Receiver: topic und _rec
    }


    public void insertMessage(String processID, String topic, String content){
        //DatabaseConnector.insertMessage("received Messages", processID, topic, content);
    }
}
