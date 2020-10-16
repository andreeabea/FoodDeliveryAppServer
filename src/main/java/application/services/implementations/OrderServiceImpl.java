package application.services.implementations;

import application.dto.*;
import application.repositories.*;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import application.services.OrderService;
import application.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;

    ItemRepository itemRepository;

    UserRepository userRepository;

    OrderedItemRepository orderedItemRepository;

    DiscountRepository discountRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, ItemRepository itemRepository,
                            UserRepository userRepository, OrderedItemRepository orderedItemRepository,
                            DiscountRepository discountRepository)
    {
        this.orderRepository=orderRepository;
        this.itemRepository=itemRepository;
        this.userRepository=userRepository;
        this.orderedItemRepository=orderedItemRepository;
        this.discountRepository=discountRepository;
    }

    @Override
    public Discount findApplicableDiscount(RestaurantDTO restaurant, int nbItems)
    {
        List<Discount> discounts = discountRepository.findByDiscRestaurantId(restaurant.getId());

        List<Discount> sorted = discounts.stream().sorted((d1, d2)->{
            int nb1 = d1.getMinItemNumber();
            int nb2 = d2.getMinItemNumber();
            if(nb1<nb2) {
                return -1;
            }else if(nb1==nb2){
                return 0;
            }
            else return 1;
        }).collect(Collectors.toList());

        if(discounts.size()==0 || nbItems<sorted.get(0).getMinItemNumber())
        {
            return null;
        }
        else
        {
            Discount applicable=null;
            for(Discount d : sorted)
            {
                if(d.getMinItemNumber()>nbItems)
                {
                    break;
                }
                applicable=d;
            }
            return applicable;
        }
    }

    @Override
    public Integer findApplicableDiscount(int restaurantId, int nbItems)
    {
        List<Discount> discounts = discountRepository.findByDiscRestaurantId(restaurantId);

        List<Discount> sorted = discounts.stream().sorted((d1, d2)->{
            int nb1 = d1.getMinItemNumber();
            int nb2 = d2.getMinItemNumber();
            if(nb1<nb2) {
                return -1;
            }else if(nb1==nb2){
                return 0;
            }
            else return 1;
        }).collect(Collectors.toList());

        if(discounts.size()==0 || nbItems<sorted.get(0).getMinItemNumber())
        {
            return null;
        }
        else
        {
            Discount applicable=null;
            for(Discount d : sorted)
            {
                if(d.getMinItemNumber()>nbItems)
                {
                    break;
                }
                applicable=d;
            }
            return applicable.getPercentage();
        }
    }


    @Override
    public OrderDTO createNewOrder(UserDTO currentUser, RestaurantDTO selectedRestaurant, Map<String, String> itemsToOrder)
            throws EntityNotFoundException, InvalidDataException {

        User user = userRepository.findById(currentUser.getId()).orElse(null);
        List<User> deliveryUsers = userRepository.findAllByUserType(UserType.DELIVERY);
        DeliveryUser courier = (DeliveryUser) deliveryUsers.get(deliveryUsers.size()-1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.YYYY hh:mm:ss");
        Order newOrder = Order.builder().customer((RegularUser) user).datetime(formatter.format(LocalDateTime.now()))
                .status(Status.CREATED).courier(courier).build();

        float totalAmount = 0;
        int nbItems = 0;

        for(Map.Entry<String,String> entry : itemsToOrder.entrySet())
        {
            int id = Integer.parseInt(entry.getKey());
            String quantityString = entry.getValue();

            if(quantityString.equals(""))
            {
                throw new InvalidDataException("Invalid quantity!");
            }
            int quantity = Integer.parseInt(quantityString);
            if(id<=0 || quantity<=0)
            {
                throw new InvalidDataException("Invalid input data!");
            }
            if(currentUser==null || currentUser.getUserType()!= UserType.REGULAR)
            {
                throw new EntityNotFoundException("Regular user not found!");
            }
            if(selectedRestaurant==null)
            {
                throw new EntityNotFoundException("Selected restaurant not found!");
            }
            Item selectedItem = itemRepository.findById(id).orElse(null);
            if(selectedItem == null || selectedItem.getRestaurant().getId()!=selectedRestaurant.getId())
            {
                throw new EntityNotFoundException("Selected item not found!");
            }
            if(quantity>selectedItem.getStock())
            {
                throw new InvalidDataException("Invalid quantity for item " + selectedItem.getName() + ". Not enough in stock.");
            }

            totalAmount+=quantity*selectedItem.getPrice();
            nbItems+=quantity;
        }

        if(((RegularUser)user).getWallet()-totalAmount<0)
        {
            throw new InvalidDataException("Not enough money to order!");
        }

        for(Map.Entry<String,String> entry : itemsToOrder.entrySet())
        {
            int id = Integer.parseInt(entry.getKey());
            String quantityString = entry.getValue();

            int quantity = Integer.parseInt(quantityString);

            Item selectedItem = itemRepository.findById(id).orElse(null);

            OrderedItem orderedItem = OrderedItem.builder().item(selectedItem).quantity(quantity).orderObj(newOrder).build();
            List<OrderedItem> orderedItemList = selectedItem.getOrderedItems();
            orderedItemList.add(orderedItem);
            selectedItem.setOrderedItems(orderedItemList);
            selectedItem.setStock(selectedItem.getStock()-quantity);

           // currentUser.setWallet(((RegularUser)user).getWallet()-quantity*selectedItem.getPrice());
            //((RegularUser)user).setWallet(((RegularUser)user).getWallet()-quantity*selectedItem.getPrice());

            userRepository.save(user);
            itemRepository.save(selectedItem);
            orderRepository.save(newOrder);
            orderedItemRepository.save(orderedItem);
        }

        //apply discounts
        Discount applicable = findApplicableDiscount(selectedRestaurant,nbItems);
        if(applicable==null)
        {
            currentUser.setWallet(((RegularUser)user).getWallet()-totalAmount);
            ((RegularUser)user).setWallet(((RegularUser)user).getWallet()-totalAmount);
        }
        else {
            totalAmount = totalAmount - totalAmount*applicable.getPercentage()/100;
            currentUser.setWallet(((RegularUser)user).getWallet()-totalAmount);
            ((RegularUser)user).setWallet(((RegularUser)user).getWallet()-totalAmount);
        }

        userRepository.save(user);

        return new OrderDTO(newOrder);
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderDTO> orderDTOS = new ArrayList<>();
        for(Order o : orders)
        {
            orderDTOS.add(new OrderDTO(o));
        }
        return orderDTOS;
    }

    @Override
    public void changeOrderStatus(String idString, String statusString) throws InvalidDataException, EntityNotFoundException {
        int id;
        if(!idString.equals("") && idString.matches("-?\\d+(\\.\\d+)?"))
        {
            id = Integer.parseInt(idString);
        }
        else throw new InvalidDataException("The given id is invalid!");

        if(id>0)
        {
            Order foundOrder = orderRepository.findById(id).orElse(null);
            if(foundOrder==null)
            {
                throw new EntityNotFoundException("Order not found!");
            }

            if(statusString.equals("CREATED"))
            {
                throw new InvalidDataException("Invalid status. The order was already created!");
            }
            else if(foundOrder.getStatus()==Status.DELIVERED && statusString.equals("IN_PROGRESS"))
            {
                throw new InvalidDataException("Invalid status. The order cannot be in progress, it was already delivered!");
            }
            else if(foundOrder.getStatus()!=Status.IN_PROGRESS && statusString.equals("DELIVERED"))
            {
                throw new InvalidDataException("Invalid status. The order must be in progress first, then to be delivered!");
            }

            foundOrder.setStatus(Status.valueOf(statusString));
            orderRepository.save(foundOrder);
        }
        else throw new InvalidDataException("The given id is invalid!");
    }
}
