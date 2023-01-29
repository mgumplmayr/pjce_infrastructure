package at.jku.itproj;


import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Service;


@Service
public class MQTTReceiver extends MQTTDelegator implements JavaDelegate  {
    private MqttClient client;
    private String topic;
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
                System.out.println("Message received: " + messageReceived+ " on topic: " + topic);
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


    public void processMessage(String message, String messageName) {
        String processID = message.substring(0,message.indexOf(';')); //bei den MQTT Nachrichten muss man die ProcessID des Vorgangs mitschicken, Format: processid und ';' dann Nachricht

            System.out.println("ProcessID of message: "+processID);
            System.out.println("Events in Queue: "+runtimeService.createEventSubscriptionQuery().list());

            EventSubscription event = runtimeService
                    .createEventSubscriptionQuery()
                    .processInstanceId(processID)
                    .eventName(messageName).singleResult();
            System.out.println("Event to correlate: "+event);

            if(event==null){
                System.out.println("No subscription found with ProcessID: "+processID+"and messageName: "+messageName);

            } else{
            runtimeService.createMessageCorrelation(messageName)
                    .processInstanceId(processID)
                    .correlate();
            execution.setVariable(messageName, message);
            insertMessage(processID, messageName, message);
            }
    }

    public void insertMessage(String processID, String topic, String content){
        DatabaseConnector.insertMessage("received Messages", processID, topic, content);
    }
}
