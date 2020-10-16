package application.services;

import application.dto.ItemDTO;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import application.dto.RestaurantDTO;
import application.entities.Item;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ItemService {

    ItemDTO findById(String idString, RestaurantDTO restaurantDTO) throws InvalidDataException, EntityNotFoundException;

    List<Item> getAllItems();

    Item addItem(RestaurantDTO restaurantDto, String nameString, String stockString, String priceString)
            throws InvalidDataException;

    Item updateItem(RestaurantDTO restaurantDto, String idString, String nameString, String stockString,
                    String priceString) throws InvalidDataException, EntityNotFoundException;

    void deleteItem(RestaurantDTO restaurantDTO, String idString) throws InvalidDataException, EntityNotFoundException;
}
