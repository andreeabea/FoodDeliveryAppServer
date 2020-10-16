package application.services;

import application.dto.*;
import application.entities.Discount;
import application.entities.Restaurant;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import application.entities.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface OrderService {


    Discount findApplicableDiscount(RestaurantDTO restaurant, int nbItems);

    Integer findApplicableDiscount(int restaurantId, int nbItems);

    /**This method was just for assignment 2 to order just 1 item
     * */
//    Order createNewOrder(UserDTO currentUser, RestaurantDTO selectedRestaurant, String idString, String quantityString)
//            throws EntityNotFoundException, InvalidDataException;

    OrderDTO createNewOrder(UserDTO currentUser, RestaurantDTO selectedRestaurant, Map<String, String> itemsToOrder)
            throws EntityNotFoundException, InvalidDataException;

    List<OrderDTO> getAllOrders();

    void changeOrderStatus(String idString, String statusString) throws InvalidDataException, EntityNotFoundException;
}
