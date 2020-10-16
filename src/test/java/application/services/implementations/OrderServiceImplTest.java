package application.services.implementations;

import application.dto.OrderDTO;
import application.dto.RestaurantDTO;
import application.dto.UserDTO;
import application.entities.*;
import application.repositories.*;
import application.services.OrderService;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceImplTest {

    OrderRepository orderRepository;

    OrderService orderService;

    ItemRepository itemRepository;

    UserRepository userRepository;

    OrderedItemRepository orderedItemRepository;

    DiscountRepository discountRepository;

    @BeforeEach
    void setUp() {
        orderRepository = Mockito.mock(OrderRepository.class);
        itemRepository = Mockito.mock(ItemRepository.class);
        userRepository=Mockito.mock(UserRepository.class);
        orderedItemRepository=Mockito.mock(OrderedItemRepository.class);
        discountRepository=Mockito.mock(DiscountRepository.class);
        orderService = new OrderServiceImpl(orderRepository, itemRepository, userRepository, orderedItemRepository, discountRepository);
    }

    @Test
    void createNewOrder() throws InvalidDataException, EntityNotFoundException {

        User user = RegularUser.builder().id(1).name("andreea").username("andreeabea").userType(UserType.REGULAR)
                .password("0000").wallet(10.0f).build();

        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").build();
        Item item = Item.builder().id(1).name("sashimi").price(5.0f).stock(10).orderedItems(new ArrayList<>()).build();

        List<Item> items = new ArrayList<>();
        items.add(item);

        items.get(0).setRestaurant(restaurant);
        restaurant.setItems(items);

        Mockito.when(itemRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(item));
        Mockito.when(userRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(user));

        List<User> deliveryUsers = new ArrayList<>();
        deliveryUsers.add(DeliveryUser.builder().id(2).name("curier").userType(UserType.DELIVERY).build());

        DeliveryUser courier = (DeliveryUser) deliveryUsers.get(0);

        Mockito.when(userRepository.findAllByUserType(UserType.DELIVERY)).thenReturn(deliveryUsers);

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof User)
                return user;
            return null;
        }).when(userRepository).save(Mockito.any(User.class));

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof Item)
                return item;
            return null;
        }).when(itemRepository).save(Mockito.any(Item.class));

        Order order = Order.builder().customer((RegularUser) user).courier(courier).status(Status.CREATED).build();

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof Order)
                return order;
            return null;
        }).when(orderRepository).save(Mockito.any(Order.class));

        OrderedItem orderedItem = OrderedItem.builder().item(item).quantity(1).orderObj(order).build();

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof OrderedItem)
                return orderedItem;
            return null;
        }).when(orderedItemRepository).save(Mockito.any(OrderedItem.class));

        Map<String,String> itemsToOrder = new HashMap<>();
        itemsToOrder.put("1","1");
        OrderDTO obtainedOrder = orderService.createNewOrder(new UserDTO(user),new RestaurantDTO(restaurant),itemsToOrder);

        //Assert.assertTrue(new ReflectionEquals(order.getCustomer(),"wallet","orders","favourites","ratings").matches(obtainedOrder.getCustomer()));
        Assert.assertEquals(order.getStatus(),obtainedOrder.getStatus());
        Assert.assertEquals(10.0f-item.getPrice(),obtainedOrder.getCustomer().getWallet(),0.01);
        Assert.assertEquals(order.getCourier().getId(),obtainedOrder.getCourier().getId());
    }

    @Test
    void findApplicableDiscount()
    {
        Discount d1 = Discount.builder().minItemNumber(2).percentage(10).build();
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").items(new ArrayList<>()).build();
        d1.setDiscRestaurant(restaurant);

        List<Discount> discounts = new ArrayList<>();
        discounts.add(d1);
        restaurant.setDiscounts(discounts);

        Mockito.when(discountRepository.findByDiscRestaurantId(1)).thenReturn(discounts);

        Discount obtainedDiscount = orderService.findApplicableDiscount(new RestaurantDTO(restaurant),5);

        Assert.assertTrue(new ReflectionEquals(d1).matches(obtainedDiscount));

        obtainedDiscount = orderService.findApplicableDiscount(new RestaurantDTO(restaurant),1);

        Assert.assertTrue(new ReflectionEquals(null).matches(obtainedDiscount));
    }

    @Test
    void getAllOrders()
    {
        List<Order> orders = new ArrayList<>();
        Mockito.when(orderRepository.findAll()).thenReturn(orders);
        List<OrderDTO> orderDTOS = orderService.getAllOrders();

        Assert.assertEquals(orders.size(),orderDTOS.size());
    }

    @Test
    void changeOrderStatus() throws InvalidDataException, EntityNotFoundException {
        User user = RegularUser.builder().id(1).name("andreea").username("andreeabea").userType(UserType.REGULAR)
                .password("0000").wallet(10.0f).build();
        List<User> deliveryUsers = new ArrayList<>();
        deliveryUsers.add(DeliveryUser.builder().id(2).name("curier").userType(UserType.DELIVERY).build());

        DeliveryUser courier = (DeliveryUser) deliveryUsers.get(0);
        Order order = Order.builder().id(5).customer((RegularUser) user).courier(courier).status(Status.CREATED).build();

        Mockito.when(orderRepository.findById(5)).thenReturn(java.util.Optional.ofNullable(order));

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof Order)
                return order;
            return null;
        }).when(orderRepository).save(Mockito.any(Order.class));

        orderService.changeOrderStatus("5","IN_PROGRESS");

        Assert.assertEquals(order.getStatus(),Status.IN_PROGRESS);
    }
}