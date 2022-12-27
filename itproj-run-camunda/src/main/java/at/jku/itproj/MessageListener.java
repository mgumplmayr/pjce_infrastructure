package at.jku.itproj;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;

public class MessageListener implements ExecutionListener {
    @Autowired
    RuntimeService runtimeService; //is null
    /*To get a working Spring/CDI injection, the JavaDelegate must be referenced in the bpmn.xml through camunda:expression=#{MyBeanName}.
    It wont work with camunda:class="my.class.Delegate".*/

    @Override
    public void notify(DelegateExecution delegateExecution){
        /*List eventList = runtimeService
                .createEventSubscriptionQuery()
                .list();
        System.out.println("Events in Queue: "+eventList);*/
        System.out.println("MessageListener was called");
    }
}

