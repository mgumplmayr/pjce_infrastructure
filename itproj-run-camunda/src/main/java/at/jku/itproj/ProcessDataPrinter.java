package at.jku.itproj;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

@Service("ProcessDataPrinter")
public class ProcessDataPrinter implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        System.out.println("Printing data of process:");
        DatabaseConnector.getContent("sent_messages", execution.getProcessInstanceId());
        DatabaseConnector.getContent("received_messages", execution.getProcessInstanceId());
    }
}
