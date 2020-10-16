package application.controllers.notification;

import application.controllers.observer.ConcreteObservable;
import application.controllers.observer.ConcreteObserver;
import application.dto.*;
import application.entities.Order;
import application.entities.Restaurant;
import application.entities.User;
import application.services.*;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.IncorrectPasswordException;
import application.services.exceptions.InvalidDataException;
import application.services.exceptions.UserNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runnable listening for incoming messages from the client
 */
class ClientSocketConnection {

    private final NotificationService notificationService;
    private BufferedReader in;
    private PrintWriter out;

    private UserService userService;
    private RestaurantService restaurantService;
    private OrderService orderService;
    private ItemService itemService;
    private RatingService ratingService;

    private static ConcreteObserver observer=null;
    private ConcreteObservable subject=null;

    ClientSocketConnection(Socket clientSocket, NotificationService notificationService) {
        this.notificationService = notificationService;
        this.userService = BeanUtil.getBean(UserService.class);
        this.restaurantService = BeanUtil.getBean(RestaurantService.class);
        this.orderService = BeanUtil.getBean(OrderService.class);
        this.itemService = BeanUtil.getBean(ItemService.class);
        this.ratingService = BeanUtil.getBean(RatingService.class);
        createReaderAndWriter(clientSocket);
        new Thread(this::listenAndRespond).start(); // starting a new thread which listens to client messages
    }

    private void createReaderAndWriter(Socket clientSocket) {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (Exception exception) {
            System.out.println("Client disconnected");
        }
    }

    /**
     * Listening for messages from the client, printing them to the console and responding to them
     */
    private void listenAndRespond(){

        ObjectMapper mapper = new ObjectMapper();
        String json;
        Message message = null;
        List<String> jsonList;
        try {
            while (true) {
                try {
                    String msg = in.readLine();
                    System.out.println("Received: " + msg);
                    Message received = mapper.readValue(msg, Message.class);

                    jsonList = new ArrayList<>();

                    switch (received.getHeader())
                    {
                        case "login":
                            UserDTO obj = mapper.readValue(received.getObjectsJson().get(0), UserDTO.class);
                            UserDTO userDTO = userService.loginUser(obj.getUsername(), obj.getPassword());
                            json = mapper.writeValueAsString(userDTO);
                            jsonList.add(json);
                            message = Message.builder().header("UserDTO").objectsJson(jsonList).build();
                            if(userDTO.getUsername().equals("mihai.curier"))
                            {
                                observer = new ConcreteObserver(out);
                            }
                            break;
                        case "getUser":
                            UserDTO userDTO1 = userService.getUser(received.getObjectsJson().get(0));
                            json = mapper.writeValueAsString(userDTO1);
                            jsonList.add(json);
                            message = Message.builder().header("UserDTO").objectsJson(jsonList).build();
                            break;
                        case "getRestaurants":
                            List<RestaurantDTO> restaurants = restaurantService.getAllRestaurants();
                            message = Message.builder().objectsJson(new ArrayList<>()).build();
                            for (RestaurantDTO r : restaurants) {
                                json = mapper.writeValueAsString(r);
                                jsonList.add(json);
                            }
                            message.setObjectsJson(jsonList);
                            break;
                        case "getSelRestaurant":
                            RestaurantDTO restaurant = restaurantService.findSelectedRestaurant(received.getObjectsJson().get(0));
                            json = mapper.writeValueAsString(restaurant);
                            jsonList.add(json);
                            message = Message.builder().header(RestaurantDTO.class.getName()).objectsJson(jsonList).build();
                            break;
                        case "orderItems":
                            UserDTO currentUser = mapper.readValue(received.getObjectsJson().get(0),UserDTO.class);
                            RestaurantDTO selectedRestaurant = mapper.readValue(received.getObjectsJson().get(1), RestaurantDTO.class);
                            Map<String,String> itemsToOrder = mapper.readValue(received.getObjectsJson().get(2),new TypeReference<HashMap<String, String>>() {});
                            OrderDTO orderDTO = orderService.createNewOrder(currentUser,selectedRestaurant,itemsToOrder);
                            jsonList.add(mapper.writeValueAsString(currentUser));
                            message = Message.builder().header("currentUser").objectsJson(jsonList).build();

                            if(observer!=null) {
                                subject = new ConcreteObservable();
                                subject.addObserver(observer);
                                subject.addOrder(orderDTO);
                            }
                            break;
                        case "getItems":
                            List<ItemDTO> items = restaurantService.getRestaurantItems(received.getObjectsJson().get(0));
                            for(ItemDTO item : items)
                            {
                                json = mapper.writeValueAsString(item);
                                jsonList.add(json);
                            }
                            message = Message.builder().header("ItemDTO").objectsJson(jsonList).build();
                            break;
                        case "register":
                            List<String> receivedStrings = received.getObjectsJson();
                            UserDTO registeredUser = userService.createNewUser(receivedStrings.get(0),receivedStrings.get(1),
                                    receivedStrings.get(2),receivedStrings.get(3),receivedStrings.get(4));
                            jsonList.add(mapper.writeValueAsString(registeredUser));
                            message = Message.builder().header("UserDTO").objectsJson(jsonList).build();
                            break;
                        case "getUsers":
                            List<UserDTO> users = userService.getAllRegularUsers();
                            message = Message.builder().objectsJson(new ArrayList<>()).build();
                            for (UserDTO u : users) {
                                json = mapper.writeValueAsString(u);
                                jsonList.add(json);
                            }
                            message.setObjectsJson(jsonList);
                            break;
                        case "editWallet":
                            userService.updateWalletAmount(received.getObjectsJson().get(0),received.getObjectsJson().get(1));
                            message=null;
                            break;
                        case "createRestaurant":
                            restaurantService.createRestaurant(received.getObjectsJson().get(0));
                            message=null;
                            break;
                        case "updateRestaurant":
                            restaurantService.updateRestaurant(received.getObjectsJson().get(0), received.getObjectsJson().get(1));
                            message=null;
                            break;
                        case "deleteRestaurant":
                            restaurantService.deleteRestaurant(received.getObjectsJson().get(0));
                            message=null;
                            break;
                        case "addItem":
                            RestaurantDTO restaurantDTO = mapper.readValue(received.getObjectsJson().get(0),RestaurantDTO.class);
                            itemService.addItem(restaurantDTO,received.getObjectsJson().get(1),
                                    received.getObjectsJson().get(2),received.getObjectsJson().get(3));
                            message=null;
                            break;
                        case "updateItem":
                            RestaurantDTO restaurantDTO1 = mapper.readValue(received.getObjectsJson().get(0),RestaurantDTO.class);
                            itemService.updateItem(restaurantDTO1,received.getObjectsJson().get(1),
                                    received.getObjectsJson().get(2),received.getObjectsJson().get(3),received.getObjectsJson().get(4));
                            message=null;
                            break;
                        case "deleteItem":
                            RestaurantDTO restaurantDTO2 = mapper.readValue(received.getObjectsJson().get(0),RestaurantDTO.class);
                            itemService.deleteItem(restaurantDTO2,received.getObjectsJson().get(1));
                            message=null;
                            break;
                        case "getItem":
                            RestaurantDTO r = mapper.readValue(received.getObjectsJson().get(0),RestaurantDTO.class);
                            ItemDTO i = itemService.findById(received.getObjectsJson().get(1),r);
                            json = mapper.writeValueAsString(i);
                            jsonList.add(json);
                            message.setHeader("ItemDTO");
                            message.setObjectsJson(jsonList);
                            break;
                        case "getOrders":
                            List<OrderDTO> orders = orderService.getAllOrders();
                            for(OrderDTO order : orders)
                            {
                                json = mapper.writeValueAsString(order);
                                jsonList.add(json);
                            }
                            message = Message.builder().header("OrderDTO").objectsJson(jsonList).build();
                            break;
                        case "changeOrderStatus":
                            orderService.changeOrderStatus(received.getObjectsJson().get(0),received.getObjectsJson().get(1));
                            message=null;
                            break;
                        case "getFavouriteRestaurants":
                            List<RestaurantDTO> favRestaurants = userService.getFavouriteRestaurants(received.getObjectsJson().get(0));
                            message = Message.builder().objectsJson(new ArrayList<>()).build();
                            for (RestaurantDTO rest : favRestaurants) {
                                json = mapper.writeValueAsString(rest);
                                jsonList.add(json);
                            }
                            message.setObjectsJson(jsonList);
                            break;
                        case "addFavouriteRestaurant":
                            UserDTO user = userService.addFavouriteRestaurant(received.getObjectsJson().get(0),received.getObjectsJson().get(1));
                            message.setHeader("UserDTO");
                            json = mapper.writeValueAsString(user);
                            jsonList.add(json);
                            message.setObjectsJson(jsonList);
                            break;
                        case "deleteFavouriteRestaurant":
                            UserDTO user1 = userService.deleteFavouriteRestaurant(received.getObjectsJson().get(0),received.getObjectsJson().get(1));
                            message.setHeader("UserDTO");
                            json = mapper.writeValueAsString(user1);
                            jsonList.add(json);
                            message.setObjectsJson(jsonList);
                            break;
                        case "rateRestaurant":
                            UserDTO user2 = userService.rateRestaurant(received.getObjectsJson().get(0),
                                    received.getObjectsJson().get(1),received.getObjectsJson().get(2));
                            message.setHeader("UserDTO");
                            json = mapper.writeValueAsString(user2);
                            jsonList.add(json);
                            message.setObjectsJson(jsonList);
                            break;
                        case "getRatings":
                            List<RatingDTO> ratings = ratingService.getRatings(received.getObjectsJson().get(0));
                            message = Message.builder().objectsJson(new ArrayList<>()).build();
                            for (RatingDTO rating : ratings) {
                                json = mapper.writeValueAsString(rating);
                                jsonList.add(json);
                            }
                            message.setObjectsJson(jsonList);
                            break;
                        case "deleteRating":
                            UserDTO receivedUser = mapper.readValue(received.getObjectsJson().get(1),UserDTO.class);
                            ratingService.deleteRating(received.getObjectsJson().get(0),receivedUser);
                            message=null;
                            break;
                        case "addRating":
                            UserDTO user3 = ratingService.addRating(received.getObjectsJson().get(0),
                                    received.getObjectsJson().get(1),received.getObjectsJson().get(2));
                            message.setHeader("UserDTO");
                            json = mapper.writeValueAsString(user3);
                            jsonList.add(json);
                            message.setObjectsJson(jsonList);
                            break;
                        case "updateRating":
                            UserDTO receivedUser2 = mapper.readValue(received.getObjectsJson().get(2),UserDTO.class);
                            ratingService.updateRating(received.getObjectsJson().get(0),received.getObjectsJson().get(1),
                                    receivedUser2);
                            message=null;
                            break;
                        case "getDiscounts":
                            List<DiscountDTO> discounts = restaurantService.getDiscounts(received.getObjectsJson().get(0));
                            message = Message.builder().objectsJson(new ArrayList<>()).build();
                            for (DiscountDTO discountDTO : discounts) {
                                json = mapper.writeValueAsString(discountDTO);
                                jsonList.add(json);
                            }
                            message.setObjectsJson(jsonList);
                            break;
                        case "addDiscount":
                            restaurantService.addDiscount(received.getObjectsJson().get(0),
                                    received.getObjectsJson().get(1),received.getObjectsJson().get(2));
                            message=null;
                            break;
                        case "deleteDiscount":
                            restaurantService.deleteDiscount(received.getObjectsJson().get(0),
                                    received.getObjectsJson().get(1));
                            message=null;
                            break;
                        case "getDiscount":
                            Integer d = orderService.findApplicableDiscount(Integer.parseInt(received.getObjectsJson().get(0)),
                                    Integer.parseInt(received.getObjectsJson().get(1)));
                            json = mapper.writeValueAsString(d);
                            jsonList.add(json);
                            if(d!=null)
                            {
                                message.setHeader("percentage");
                            }
                            else
                            {
                                message.setHeader(null);
                            }
                            message.setObjectsJson(jsonList);
                            break;
                    }

                } catch (UserNotFoundException | IncorrectPasswordException | EntityNotFoundException | InvalidDataException e) {
                    e.printStackTrace();
                    message = null;
                    json = e.getMessage();
                    jsonList = new ArrayList<>();
                    jsonList.add(json);
                    message = Message.builder().header(e.getClass().getName()).objectsJson(jsonList).build();
                } finally {
                    out.println(mapper.writeValueAsString(message));
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("Client disconnected");
            notificationService.removeClient(this);
        }
    }

    void sendMessage(String message) {
        out.println(message);
    }

    public ConcreteObserver getObserver()
    {
        return observer;
    }

    public ConcreteObservable getSubject()
    {
        return subject;
    }
}