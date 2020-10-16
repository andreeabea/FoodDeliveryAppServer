package application.services.implementations;

import application.dto.ItemDTO;
import application.dto.RestaurantDTO;
import application.entities.Item;
import application.entities.Restaurant;
import application.repositories.ItemRepository;
import application.repositories.RestaurantRepository;
import application.services.ItemService;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemServiceImplTest {

    private ItemRepository itemRepository;

    private ItemService itemService;

    private RestaurantRepository restaurantRepository;

    @BeforeEach
    void setUp() {
        itemRepository = Mockito.mock(ItemRepository.class);
        restaurantRepository = Mockito.mock(RestaurantRepository.class);
        itemService = new ItemServiceImpl(itemRepository,restaurantRepository);
    }

    @Test
    void findById() throws InvalidDataException, EntityNotFoundException {
        Item item = Item.builder().id(1).name("suc").price(4.5f).stock(10).build();
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").build();

        List<Item> items = new ArrayList<>();
        items.add(item);

        items.get(0).setRestaurant(restaurant);
        restaurant.setItems(items);

        Mockito.when(itemRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(item));
        Mockito.when(itemRepository.findByRestaurantId(1)).thenReturn(items);

        Item itemTest = Item.builder().id(1).build();
        ItemDTO obtainedItem = itemService.findById("1",new RestaurantDTO(restaurant));

        Assert.assertEquals(itemTest.getId(),obtainedItem.getId());
    }

    @Test
    void getAllItems() {
        Item item = Item.builder().id(1).name("suc").price(4.5f).stock(10).build();
        Item item2 = Item.builder().id(2).name("lava cake").price(16.0f).stock(20).build();

        List<Item> items = new ArrayList<>();
        items.add(item);
        items.add(item2);

        Mockito.when(itemRepository.findAll()).thenReturn(items);

        List<Item> obtainedItems = itemService.getAllItems();
        Assert.assertThat(obtainedItems, CoreMatchers.is(items));
    }

    @Test
    void addItem() throws InvalidDataException {
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").build();
        Item item = Item.builder().id(1).name("suc").price(4.5f).stock(10).build();

        List<Item> items = new ArrayList<>();
        items.add(item);

        items.get(0).setRestaurant(restaurant);
        restaurant.setItems(items);

        Mockito.when(restaurantRepository.findById(1)).thenReturn(java.util.Optional.of(restaurant));
        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof Item)
                return item;
            return null;
        }).when(itemRepository).save(Mockito.any(Item.class));

        Item obtainedItem = itemService.addItem(new RestaurantDTO(restaurant),"suc","10","4.5");
        Assert.assertTrue(obtainedItem.getRestaurant().equals(item.getRestaurant()) && obtainedItem.equals(item));
    }

    @Test
    void updateItem() throws InvalidDataException, EntityNotFoundException {

        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").build();
        Item item = Item.builder().id(1).name("suc").price(4.5f).stock(10).build();

        List<Item> items = new ArrayList<>();
        items.add(item);

        items.get(0).setRestaurant(restaurant);
        restaurant.setItems(items);

        Mockito.when(itemRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(item));
        Mockito.when(restaurantRepository.findById(1)).thenReturn(java.util.Optional.of(restaurant));

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof Item)
                return item;
            return null;
        }).when(itemRepository).save(Mockito.any(Item.class));

        itemService.addItem(new RestaurantDTO(restaurant),"suc","10","4.5");

        Mockito.when(itemRepository.findByRestaurantId(1)).thenReturn(items);

        Item obtainedItem = itemService.updateItem(new RestaurantDTO(restaurant),"1","Coca-Cola",
                "15","4.5");
        item.setName("Coca-Cola");
        item.setStock(15);

        Assert.assertTrue(obtainedItem.equals(item));
    }

    @Test
    void deleteItem() throws InvalidDataException, EntityNotFoundException {
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").build();
        Item item = Item.builder().id(1).name("suc").price(4.5f).stock(10).build();

        List<Item> items = new ArrayList<>();
        items.add(item);

        items.get(0).setRestaurant(restaurant);
        restaurant.setItems(items);

        Mockito.when(itemRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(item));
        Mockito.when(restaurantRepository.findById(1)).thenReturn(java.util.Optional.of(restaurant));

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof Item)
                return item;
            return null;
        }).when(itemRepository).save(Mockito.any(Item.class));

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof Item)
                return item;
            return null;
        }).when(itemRepository).delete(Mockito.any(Item.class));

        itemService.addItem(new RestaurantDTO(restaurant),"suc","10","4.5");

        itemService.deleteItem(new RestaurantDTO(restaurant),"1");

        Mockito.when(itemRepository.findById(1)).thenReturn(null);

        Assert.assertEquals(itemRepository.findById(1),null);
    }
}