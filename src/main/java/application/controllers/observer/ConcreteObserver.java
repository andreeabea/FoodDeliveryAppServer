package application.controllers.observer;

import application.controllers.notification.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ConcreteObserver implements Observer {

    private PrintWriter out;

    public ConcreteObserver(PrintWriter out)
    {
        this.out=out;
    }

    @SneakyThrows
    @Override
    public void update(Observable o, Object arg) {
        ObjectMapper mapper = new ObjectMapper();
        Message message = new Message();
        message.setHeader("courierNotification");
        List<String> stringList = new ArrayList<>();
        stringList.add((String) arg);
        message.setObjectsJson(stringList);
        out.println(mapper.writeValueAsString(message));
    }
}
