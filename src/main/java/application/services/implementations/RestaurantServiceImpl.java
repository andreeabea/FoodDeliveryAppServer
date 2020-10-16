package application.services.implementations;

import application.dto.DiscountDTO;
import application.entities.Discount;
import application.repositories.DiscountRepository;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import application.services.RestaurantService;
import application.dto.ItemDTO;
import application.dto.RestaurantDTO;
import application.entities.Item;
import application.entities.Restaurant;
import application.repositories.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private RestaurantRepository restaurantRepository;

    private DiscountRepository discountRepository;

    @Autowired
    public RestaurantServiceImpl(RestaurantRepository restaurantRepository, DiscountRepository discountRepository) {
        this.restaurantRepository = restaurantRepository;
        this.discountRepository = discountRepository;
    }

    @Override
    public Restaurant findById(int id) {
        return restaurantRepository.findById(id).orElse(null);
    }

    @Override
    public List<RestaurantDTO> getAllRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        List<RestaurantDTO> restaurantDTOS = new ArrayList<>();
        for (Restaurant r : restaurants) {
            restaurantDTOS.add(new RestaurantDTO(r));
        }
        return restaurantDTOS;
    }

    @Override
    public Restaurant createRestaurant(String nameString) throws InvalidDataException {
        if (!nameString.equals("") && restaurantRepository.findByName(nameString) == null) {
            if (restaurantRepository.findByName(nameString) != null) {
                throw new InvalidDataException("Invalid restaurant name! Restaurant " + nameString + " already exists!");
            }
            return restaurantRepository.save(Restaurant.builder().name(nameString).items(new ArrayList<Item>()).build());
        } else throw new InvalidDataException("Invalid restaurant name");
    }

    @Override
    public Restaurant updateRestaurant(String idString, String name) throws InvalidDataException, EntityNotFoundException {
        int id;
        if (!idString.equals("") && idString.matches("-?\\d+(\\.\\d+)?")) {
            id = Integer.parseInt(idString);
        } else throw new InvalidDataException("The given id is invalid!");

        if (name.equals("")) {
            throw new InvalidDataException("Invalid restaurant name");
        }

        if (id > 0) {
            Restaurant foundRestaurant = restaurantRepository.findById(id).orElse(null);
            if (foundRestaurant == null) {
                throw new EntityNotFoundException("Restaurant not found!");
            }
            foundRestaurant.setName(name);
            return restaurantRepository.save(foundRestaurant);
        } else throw new InvalidDataException("The given id is invalid!");
    }

    @Override
    public void deleteRestaurant(String idString) throws InvalidDataException, EntityNotFoundException {

        int id;
        if (!idString.equals("") && idString.matches("-?\\d+(\\.\\d+)?")) {
            id = Integer.parseInt(idString);
        } else throw new InvalidDataException("The given id is invalid!");

        if (id > 0) {
            Restaurant foundRestaurant = restaurantRepository.findById(id).orElse(null);
            if (foundRestaurant == null) {
                throw new EntityNotFoundException("Restaurant not found!");
            }
            restaurantRepository.delete(foundRestaurant);
        } else throw new InvalidDataException("The given id is invalid!");
    }

    @Override
    public List<ItemDTO> getRestaurantItems(String idString) throws InvalidDataException, EntityNotFoundException {
        int id;
        if (idString != null && !idString.equals("") && idString.matches("-?\\d+(\\.\\d+)?")) {
            id = Integer.parseInt(idString);
        } else throw new InvalidDataException("The given id is invalid!");

        if (id > 0) {
            Restaurant foundRestaurant = restaurantRepository.findById(id).orElse(null);
            if (foundRestaurant == null) {
                throw new EntityNotFoundException("Restaurant not found!");
            }
            List<ItemDTO> itemDTOS = new ArrayList<>();
            List<Item> items = foundRestaurant.getItems();
            for (Item item : items) {
                itemDTOS.add(new ItemDTO(item));
            }
            return itemDTOS;
        } else throw new InvalidDataException("The given id is invalid!");
    }

    @Override
    public RestaurantDTO findSelectedRestaurant(String idString) throws InvalidDataException, EntityNotFoundException {
        int id;
        if (!idString.equals("") && idString.matches("-?\\d+(\\.\\d+)?")) {
            id = Integer.parseInt(idString);
        } else throw new InvalidDataException("The given id is invalid!");

        if (id > 0) {
            Restaurant foundRestaurant = restaurantRepository.findById(id).orElse(null);
            if (foundRestaurant == null) {
                throw new EntityNotFoundException("Restaurant not found!");
            }
            return new RestaurantDTO(foundRestaurant);
        } else throw new InvalidDataException("The given id is invalid!");
    }

    @Override
    public List<DiscountDTO> getDiscounts(String idString) throws InvalidDataException {
        int id;
        if (idString != null && !idString.equals("") && idString.matches("-?\\d+(\\.\\d+)?")) {
            id = Integer.parseInt(idString);
        } else throw new InvalidDataException("The given id is invalid!");

        List<Discount> discounts = discountRepository.findAll();
        List<DiscountDTO> discountDTOS = new ArrayList<>();
        for (Discount d : discounts) {
            if (d.getDiscRestaurant().getId() == id) {
                discountDTOS.add(new DiscountDTO(d));
            }
        }
        return discountDTOS;
    }

    @Override
    public void addDiscount(String idString, String minimumNbItems, String discountPercentage) throws InvalidDataException, EntityNotFoundException {
        int id, minItems, percentage;
        if (!idString.equals("") && idString.matches("-?\\d+(\\.\\d+)?")) {
            id = Integer.parseInt(idString);
        } else throw new InvalidDataException("The given id is invalid!");

        if (!minimumNbItems.equals("") && minimumNbItems.matches("-?\\d+(\\.\\d+)?")) {
            minItems = Integer.parseInt(minimumNbItems);
        } else throw new InvalidDataException("The given minimum number of items is invalid!");

        if (!discountPercentage.equals("") && discountPercentage.matches("-?\\d+(\\.\\d+)?")) {
            percentage = Integer.parseInt(discountPercentage);
        } else throw new InvalidDataException("The given discount percentage is invalid!");

        if (id > 0 && minItems > 0 && percentage > 0) {
            Restaurant foundRestaurant = restaurantRepository.findById(id).orElse(null);
            if (foundRestaurant == null) {
                throw new EntityNotFoundException("Restaurant not found!");
            }

            List<Discount> discounts = foundRestaurant.getDiscounts();
            for (Discount d : discounts) {
                if (d.getMinItemNumber() == minItems && d.getPercentage() == percentage) {
                    throw new InvalidDataException("Discount already exists for this restaurant!");
                }
            }

            Discount newDiscount = Discount.builder().minItemNumber(minItems).percentage(percentage).discRestaurant(foundRestaurant).build();
            discounts.add(newDiscount);
            foundRestaurant.setDiscounts(discounts);
            discountRepository.save(newDiscount);
        } else throw new InvalidDataException("The given input is invalid!");
    }

    @Override
    public void deleteDiscount(String restaurantId, String discountId) throws InvalidDataException, EntityNotFoundException {

        int id;
        if (discountId != null && !discountId.equals("") && discountId.matches("-?\\d+(\\.\\d+)?")) {
            id = Integer.parseInt(discountId);
        } else throw new InvalidDataException("The given id is invalid!");

        Discount foundDiscount;
        if (id > 0) {
            foundDiscount = discountRepository.findById(id).orElse(null);
            if (foundDiscount == null || foundDiscount.getDiscRestaurant().getId() != Integer.parseInt(restaurantId)) {
                throw new EntityNotFoundException("Discount not found!");
            }
        } else throw new InvalidDataException("The given id is invalid!");

        discountRepository.delete(foundDiscount);
    }
}
