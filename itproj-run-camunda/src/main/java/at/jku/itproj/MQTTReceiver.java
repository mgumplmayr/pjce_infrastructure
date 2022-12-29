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
    MqttClient mqttClient;
    String topic = "mytopic";
    String broker = "tcp://localhost:1883";
    String clientId = "JavaSubscribeExample";
    RuntimeService runtimeService;
    String messageName = "message_received"; //der Name der Message muss mit den Topics abgestimmt werden


    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        System.out.println("Executing MQTTReceiver");
        MemoryPersistence persistence = new MemoryPersistence();
        mqttClient = new MqttClient(broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        mqttClient.connect(connOpts);
        runtimeService = execution.getProcessEngineServices().getRuntimeService();
        mqttClient.setCallback(new MqttCallback() {
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String messageReceived = new String(message.getPayload());
                System.out.println("Message received: " + messageReceived);
                processMessage(messageReceived);
            }
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
            public void connectionLost(Throwable cause) {
            }

        });
        mqttClient.subscribe(topic);
    }


    public void processMessage(String message){
        String processID = message; //bei den MQTT Nachrichten muss man die ProcessID des Vorgangs mitschicken

        try {
            System.out.println("Received the following message: \n"+message);
            System.out.println("Events in Queue: "+runtimeService.createEventSubscriptionQuery().list());

            EventSubscription event = runtimeService
                    .createEventSubscriptionQuery()
                    .processInstanceId(processID)
                    .eventName(messageName).singleResult();
            System.out.println("Event to correlate: "+event);

            if(event==null){
                System.out.println("No subscription found with ProcessID: "+processID+"and messageName: "+messageName);

            }
            runtimeService.createMessageCorrelation(messageName)
                    .processInstanceId(processID)
                    .correlate();
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
