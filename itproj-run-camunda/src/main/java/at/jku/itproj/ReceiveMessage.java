package at.jku.itproj;


import camundajar.impl.com.google.gson.Gson;
import camundajar.impl.com.google.gson.JsonObject;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class ReceiveMessage {
    @Autowired
    RuntimeService runtimeService;

    @PostMapping("/sendMessage")
    public ResponseEntity<?> publish(@RequestBody String message){
        try {
            System.out.println("Received the following message: \n"+message);
            runtimeService.createMessageCorrelation("message_received")
                    .setVariable("received", message).correlate();

            return ResponseEntity.ok("Success");
        }catch(Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.ok("fail");
        }
    }
}
