package application.services;

import application.dto.DiscountDTO;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import application.dto.ItemDTO;
import application.dto.RestaurantDTO;
import application.entities.Restaurant;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RestaurantService {

    Restaurant findById(int id);

    List<RestaurantDTO> getAllRestaurants();

    public Restaurant createRestaurant(String nameString) throws InvalidDataException;

    Restaurant updateRestaurant(String idString, String name) throws InvalidDataException, EntityNotFoundException;

    void deleteRestaurant(String idString) throws InvalidDataException, EntityNotFoundException;

    List<ItemDTO> getRestaurantItems(String idString) throws InvalidDataException, EntityNotFoundException;

    RestaurantDTO findSelectedRestaurant(String idString) throws InvalidDataException, EntityNotFoundException;

    List<DiscountDTO> getDiscounts(String idString) throws InvalidDataException;

    void addDiscount(String idString, String minimumNbItems, String discountPercentage) throws InvalidDataException, EntityNotFoundException;

    void deleteDiscount(String restaurantId, String discountId) throws InvalidDataException, EntityNotFoundException;
}
