package application.controllers.observer;

import application.dto.OrderDTO;
import application.entities.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class ConcreteObservable extends Observable {

    //private String courierName;

    private List<OrderDTO> orders = new ArrayList<>();

    //public ConcreteObservable(String courierName)
    //{
        //this.courierName=courierName;
    //}

    public void addOrder(OrderDTO order)
    {
        orders.add(order);
        this.setChanged();
        this.notifyObservers("Order with id "+order.getId()+" was added at "+order.getDatetime());
    }
}
