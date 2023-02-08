package at.jku.itproj.deprecated;


import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class ReceiveMessageREST {
    @Autowired
    RuntimeService runtimeService;

    @PostMapping("/sendMessage")
    public ResponseEntity<?> publish(@RequestBody String message){
        String processID = message; //bei den MQTT Nachrichten muss man die ProcessID des Vorgangs mitschicken
        String messageName = "message_received"; //der Name der Message muss mit den Topics abgestimmt werden
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
                return ResponseEntity.badRequest().body("No subscription found with ProcessID: "+processID+" and messageName: "+messageName);
            }

            runtimeService.createMessageCorrelation(messageName)
                    .processInstanceId(processID)
                    .correlate();
            return ResponseEntity.ok("Success");

        }catch(Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.ok("Fail");
        }
    }
}
