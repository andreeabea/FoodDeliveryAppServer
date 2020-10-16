package application.services.implementations;

import application.dto.ItemDTO;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import application.services.ItemService;
import application.dto.RestaurantDTO;
import application.entities.Item;
import application.entities.Restaurant;
import application.repositories.ItemRepository;
import application.repositories.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    ItemRepository itemRepository;

    RestaurantRepository restaurantRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, RestaurantRepository restaurantRepository)
    {
        this.itemRepository=itemRepository;
        this.restaurantRepository=restaurantRepository;
    }

    @Override
    public ItemDTO findById(String idString, RestaurantDTO restaurantDTO) throws InvalidDataException, EntityNotFoundException {
        if(restaurantDTO==null)
        {
            throw new InvalidDataException("Invalid restaurant");
        }
        if(idString.equals("") || !idString.matches("-?\\d+(\\.\\d+)?"))
        {
            throw new InvalidDataException("Invalid input data");
        }
        List<Item> restaurantItems = itemRepository.findByRestaurantId(restaurantDTO.getId());
        Item foundItem=null;
        int id = Integer.parseInt(idString);
        if(id<=0)
        {
            throw new InvalidDataException("Invalid id");
        }
        for(Item item: restaurantItems)
        {
            if(item.getId() == id)
            {
                foundItem = item;
            }
        }
        if(foundItem==null)
        {
            throw new EntityNotFoundException("Item "+ idString+ " doesn't exist!");
        }
        return new ItemDTO(foundItem);
    }

    @Override
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @Override
    public Item addItem(RestaurantDTO restaurantDto, String nameString, String stockString, String priceString)
            throws InvalidDataException {
        if(restaurantDto==null)
        {
            throw new InvalidDataException("Invalid restaurant");
        }
        if(nameString.equals("") || stockString.equals("") || priceString.equals("")
            || !stockString.matches("-?\\d+(\\.\\d+)?") || !priceString.matches("-?\\d+(\\.\\d+)?"))
        {
            throw new InvalidDataException("Invalid input data");
        }
        List<Item> restaurantItems = itemRepository.findByRestaurantId(restaurantDto.getId());
        for(Item item: restaurantItems)
        {
            if(item.getName().equals(nameString))
            {
                throw new InvalidDataException("Item "+ nameString+ " already exists!");
            }
        }
        int stock = Integer.parseInt(stockString);
        float price = Float.parseFloat(priceString);
        if(stock>0 && price>0)
        {
            Item item = Item.builder().name(nameString).price(price).stock(stock).build();
            Restaurant restaurant = restaurantRepository.findById(restaurantDto.getId()).orElse(null);
            item.setRestaurant(restaurant);
            //item.setOrderedItems(new ArrayList<>());
            List<Item> newItems = restaurant.getItems();
            newItems.add(item);
            restaurant.setItems(newItems);
            return itemRepository.save(item);
        }
        else throw new InvalidDataException("Invalid input data");
    }

    @Override
    public Item updateItem(RestaurantDTO restaurantDto, String idString, String nameString, String stockString,
                           String priceString) throws InvalidDataException, EntityNotFoundException {
        if(restaurantDto==null)
        {
            throw new InvalidDataException("Invalid restaurant");
        }
        if(idString.equals("") || nameString.equals("") || stockString.equals("") || priceString.equals("")
                || !stockString.matches("-?\\d+(\\.\\d+)?") || !priceString.matches("-?\\d+(\\.\\d+)?")
                || !idString.matches("-?\\d+(\\.\\d+)?"))
        {
            throw new InvalidDataException("Invalid input data");
        }
        List<Item> restaurantItems = itemRepository.findByRestaurantId(restaurantDto.getId());
        Item foundItem=null;
        int id = Integer.parseInt(idString);
        if(id<=0)
        {
            throw new InvalidDataException("Invalid id");
        }
        for(Item item: restaurantItems)
        {
            if(item.getId() == id)
            {
                foundItem = item;
            }
        }
        if(foundItem==null)
        {
            throw new EntityNotFoundException("Item "+ idString+ " doesn't exist!");
        }
        int stock = Integer.parseInt(stockString);
        float price = Float.parseFloat(priceString);
        if(stock>0 && price>0)
        {
            Restaurant restaurant = restaurantRepository.findById(restaurantDto.getId()).orElse(null);
            List<Item> newItems = restaurant.getItems();
            newItems.remove(foundItem);

            foundItem.setName(nameString);
            foundItem.setPrice(price);
            foundItem.setStock(stock);

            newItems.add(foundItem);
            restaurant.setItems(newItems);
            return itemRepository.save(foundItem);
        }
        else throw new InvalidDataException("Invalid input data");
    }

    @Override
    public void deleteItem(RestaurantDTO restaurantDTO, String idString) throws InvalidDataException, EntityNotFoundException {

        int id;
        if(!idString.equals("") && idString.matches("-?\\d+(\\.\\d+)?"))
        {
            id = Integer.parseInt(idString);
        }
        else throw new InvalidDataException("The given id is invalid!");

        if(id>0)
        {
            Item foundItem = itemRepository.findById(id).orElse(null);
            if(foundItem==null || foundItem.getRestaurant().getId()!=restaurantDTO.getId())
            {
                throw new EntityNotFoundException("Item not found!");
            }
            Restaurant restaurant = restaurantRepository.findById(restaurantDTO.getId()).orElse(null);
            List<Item> newItems = restaurant.getItems();
            newItems.remove(foundItem);
            restaurant.setItems(newItems);

            itemRepository.delete(foundItem);
        }
        else throw new InvalidDataException("The given id is invalid!");
    }
}
