package application.services.implementations;

import application.dto.DiscountDTO;
import application.dto.ItemDTO;
import application.dto.RestaurantDTO;
import application.entities.Discount;
import application.entities.Item;
import application.entities.Restaurant;
import application.repositories.DiscountRepository;
import application.repositories.RestaurantRepository;
import application.services.RestaurantService;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantServiceImplTest {

    private RestaurantRepository restaurantRepository;

    private DiscountRepository discountRepository;

    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        restaurantRepository = Mockito.mock(RestaurantRepository.class);
        discountRepository = Mockito.mock(DiscountRepository.class);
        restaurantService = new RestaurantServiceImpl(restaurantRepository,discountRepository);
    }

    @Test
    void findById() {
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").build();

        Mockito.when(restaurantRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(restaurant));

        Restaurant testRestaurant = Restaurant.builder().id(1).build();
        Restaurant obtainedRestaurant = restaurantService.findById(1);
        Assert.assertTrue(new ReflectionEquals(testRestaurant,"items", "name").matches(obtainedRestaurant));
    }

    @Test
    void getAllRestaurants() {
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").items(new ArrayList<>()).build();
        Restaurant restaurant2 = Restaurant.builder().id(2).name("Ghandi").items(new ArrayList<>()).build();

        List<Restaurant> restaurants = new ArrayList<>();
        restaurants.add(restaurant);
        restaurants.add(restaurant2);

        List<RestaurantDTO> restaurantDTOS = new ArrayList<>();
        restaurantDTOS.add(new RestaurantDTO(restaurant));
        restaurantDTOS.add(new RestaurantDTO(restaurant2));

        Mockito.when(restaurantRepository.findAll()).thenReturn(restaurants);
        List<RestaurantDTO> obtainedRestaurants = restaurantService.getAllRestaurants();

        Assert.assertThat(obtainedRestaurants, CoreMatchers.is(restaurantDTOS));
    }

    @Test
    void createRestaurant() throws InvalidDataException {
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").build();

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof Restaurant)
                return restaurant;
            return null;
        }).when(restaurantRepository).save(Mockito.any(Restaurant.class));

        Restaurant testRestaurant = Restaurant.builder().name("Sushi Restaurant").build();
        Restaurant obtainedRestaurant = restaurantService.createRestaurant("Sushi Restaurant");
        Assert.assertTrue(new ReflectionEquals(testRestaurant, "id","items").matches(obtainedRestaurant));
    }

    @Test
    void updateRestaurant() throws InvalidDataException, EntityNotFoundException {
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").build();

        Mockito.when(restaurantRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(restaurant));
        Mockito.when(restaurantRepository.save(restaurant)).thenReturn(restaurant);

        Restaurant testRestaurant = Restaurant.builder().name("Tokyo Sushi").build();
        Restaurant obtainedRestaurant = restaurantService.updateRestaurant("1","Tokyo Sushi");

        Assert.assertEquals(testRestaurant.getName(), obtainedRestaurant.getName());
    }

    @Test
    void deleteRestaurant() throws InvalidDataException, EntityNotFoundException {
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").build();

        Mockito.when(restaurantRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(restaurant));
        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof Restaurant)
                return restaurant;
            return null;
        }).when(restaurantRepository).delete(Mockito.any(Restaurant.class));

        restaurantService.deleteRestaurant("1");

        Mockito.when(restaurantRepository.findById(1)).thenReturn(null);

        Assert.assertEquals(restaurantRepository.findById(1),null);
    }

    @Test
    void getRestaurantItems() throws InvalidDataException, EntityNotFoundException {

        Item item1 = Item.builder().id(1).name("sashimi").price(5).stock(100).build();
        Item item2 = Item.builder().id(2).name("salmon nigiri").price(6.5f).stock(100).build();
        List<Item> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").build();

        items.get(0).setRestaurant(restaurant);
        items.get(1).setRestaurant(restaurant);
        restaurant.setItems(items);

        Mockito.when(restaurantRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(restaurant));

        List<ItemDTO> testItems = new ArrayList<>();
        testItems.add(new ItemDTO(item1));
        testItems.add(new ItemDTO(item2));
        List<ItemDTO> obtainedItems = restaurantService.getRestaurantItems("1");

        Assert.assertTrue(testItems.size()== obtainedItems.size());
    }

    @Test
    void findSelectedRestaurant() throws InvalidDataException, EntityNotFoundException {

        List<Item> items = new ArrayList<>();
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").items(items).build();

        Mockito.when(restaurantRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(restaurant));

        RestaurantDTO testRestaurant = new RestaurantDTO(restaurant);
        RestaurantDTO obtainedRestaurant = restaurantService.findSelectedRestaurant("1");
        Assert.assertTrue(new ReflectionEquals(testRestaurant).matches(obtainedRestaurant));
    }

    @Test
    void getDiscounts() throws InvalidDataException {
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").discounts(new ArrayList<>()).build();
        List<Discount> discounts = new ArrayList<>();
        Discount d1 = Discount.builder().id(2).discRestaurant(restaurant).minItemNumber(2).percentage(10).build();
        Discount d2 = Discount.builder().id(3).discRestaurant(Restaurant.builder().build()).build();
        discounts.add(d1);
        discounts.add(d2);
        Mockito.when(discountRepository.findAll()).thenReturn(discounts);

        List<DiscountDTO> obtainedDiscounts = restaurantService.getDiscounts("1");
        List<DiscountDTO> discountsTest = new ArrayList<>();
        discountsTest.add(obtainedDiscounts.get(0));

        Assert.assertThat(discountsTest, CoreMatchers.is(obtainedDiscounts));
    }

    @Test
    void addDiscount() throws InvalidDataException, EntityNotFoundException {
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").discounts(new ArrayList<>()).build();
        Discount d1 = Discount.builder().id(2).discRestaurant(restaurant).minItemNumber(2).percentage(10).build();

        Mockito.when(restaurantRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(restaurant));

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof Discount)
                return d1;
            return null;
        }).when(discountRepository).save(Mockito.any(Discount.class));

        restaurantService.addDiscount("1","2","10");
        Assert.assertEquals(1, restaurant.getDiscounts().size());
        Assert.assertTrue(new ReflectionEquals(d1,"id").matches(restaurant.getDiscounts().get(0)));
    }

    @Test
    void deleteDiscount()
    {
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").discounts(new ArrayList<>()).build();
        Discount d1 = Discount.builder().id(2).discRestaurant(restaurant).minItemNumber(2).percentage(10).build();

        Mockito.when(discountRepository.findById(2)).thenReturn(java.util.Optional.ofNullable(d1));

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof Discount)
                return d1;
            return null;
        }).when(discountRepository).delete(Mockito.any(Discount.class));

        Assert.assertEquals(0, restaurant.getDiscounts().size());
    }
}