package at.jku.itproj;


import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MQTTReceiver implements JavaDelegate {
    static MqttClient mqttClient;
    String broker = "tcp://localhost:1883";
    String clientId;
    RuntimeService runtimeService;

    @Override
    public void execute(final DelegateExecution execution) throws Exception { //topic name = message name = variable name, topic names in yaml festhalten
        System.out.println("Executing MQTTReceiver");
        MemoryPersistence persistence = new MemoryPersistence();
        System.out.println("members");
        clientId=execution.getCurrentActivityId();
        System.out.println("ClientID: "+clientId);
        mqttClient = new MqttClient(broker, clientId, persistence);
        System.out.println("client");
        MqttConnectOptions connOpts = new MqttConnectOptions();
        System.out.println("options");
        mqttClient.connect(connOpts);
        System.out.println("connection");
        runtimeService = execution.getProcessEngineServices().getRuntimeService();
        mqttClient.setCallback(new MqttCallback() {
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String messageReceived = new String(message.getPayload());
                System.out.println("Message received: " + messageReceived+ " on topic: " + topic);
                processMessage(messageReceived, topic);
            }
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
            public void connectionLost(Throwable cause) {
            }
        });
        System.out.println("callback");
        mqttClient.subscribe(execution.getEventName()); //auch möglich: # für alle topics
        System.out.println("subs");
    }


    public void processMessage(String message, String messageName) {
        String processID = message.substring(0,message.indexOf(';')); //bei den MQTT Nachrichten muss man die ProcessID des Vorgangs mitschicken, Format: processid und ';' dann Nachricht
            System.out.println("Received the following message: \n"+message+ " on topic: "+messageName);
            System.out.println("ProcessID: "+processID);
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
            }

    }
}
