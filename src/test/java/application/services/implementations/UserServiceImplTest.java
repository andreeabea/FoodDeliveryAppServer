package application.services.implementations;

import application.dto.RestaurantDTO;
import application.dto.UserDTO;
import application.entities.*;
import application.repositories.*;
import application.services.UserService;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.IncorrectPasswordException;
import application.services.exceptions.InvalidDataException;
import application.services.exceptions.UserNotFoundException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
class UserServiceImplTest {

    private UserRepository userRepository;

    private RestaurantRepository restaurantRepository;

    private UserFavouriteRestaurantRepository userFavouriteRestaurantRepository;

    private RatingRepository ratingRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        restaurantRepository = Mockito.mock(RestaurantRepository.class);
        userFavouriteRestaurantRepository=Mockito.mock(UserFavouriteRestaurantRepository.class);
        ratingRepository=Mockito.mock(RatingRepository.class);
        userService = new UserServiceImpl(userRepository,restaurantRepository,userFavouriteRestaurantRepository,ratingRepository);
    }

    @Test
    void findById() {
        User user = RegularUser.builder().id(1).name("andreea").username("andreeabea").userType(UserType.REGULAR)
                .password("0000").wallet(10.0f).build();

        Mockito.when(userRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(user));

        User userTest = User.builder().id(1).build();
        User obtainedUser = userService.findById(1);
        Assert.assertTrue(new ReflectionEquals(userTest,"name","username","userType","password","wallet","orders","favourites","ratings").matches(obtainedUser));
    }

    @Test
    void findByUsername() {
        User user = RegularUser.builder().id(1).name("andreea").username("andreeabea").userType(UserType.REGULAR)
                .password("0000").wallet(10.0f).build();

        Mockito.when(userRepository.findByUsername("andreeabea")).thenReturn(user);

        User userTest = User.builder().username("andreeabea").build();
        User obtainedUser = userService.findByUsername("andreeabea");
        Assert.assertTrue(new ReflectionEquals(userTest,"name","id","userType","password","wallet","orders","favourites","ratings").matches(obtainedUser));
    }

    @Test
    void save() {
        User user = RegularUser.builder().username("andreeabea").build();

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof User)
                return user;
            return null;
        }).when(userRepository).save(Mockito.any(User.class));

        User userTest = RegularUser.builder().username("andreeabea").build();
        User obtainedUser = userService.save("andreeabea");
        Assert.assertTrue(userTest.getUsername().equals(obtainedUser.getUsername()));
    }

    @Test
    void getAllRegularUsers() {
        RegularUser user = RegularUser.builder().id(1).name("andreea").username("andreeabea").userType(UserType.REGULAR)
                .password("0000").wallet(10.0f).build();
        RegularUser user2 = RegularUser.builder().id(2).name("victor").username("victorp").userType(UserType.REGULAR)
                .password("0000").wallet(10.0f).build();

        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(user2);

        List<UserDTO> userDTOS = new ArrayList<>();

        Mockito.when(userRepository.findAll()).thenReturn(users);
        List<UserDTO> obtainedUsers = userService.getAllRegularUsers();

        userDTOS.add(obtainedUsers.get(0));
        userDTOS.add(obtainedUsers.get(1));

        Assert.assertThat(obtainedUsers, CoreMatchers.is(userDTOS));
    }

    @Test
    void loginUser() throws UserNotFoundException, IncorrectPasswordException {

        User user = RegularUser.builder().id(1).name("andreea").username("andreeabea").userType(UserType.REGULAR)
                .password("0000").wallet(10.0f).build();

        Mockito.when(userRepository.findByUsername("andreeabea")).thenReturn(user);

        UserDTO userTest = new UserDTO(user);
        UserDTO obtainedUser = userService.loginUser("andreeabea","0000");
        Assert.assertTrue(new ReflectionEquals(userTest,"name","id","userType","wallet","orders").matches(obtainedUser));
    }

    @Test
    void createNewUser() throws InvalidDataException {

        User user = RegularUser.builder().id(1).name("andreea").username("andreeabea").userType(UserType.REGULAR)
                .password("0000").wallet(10.0f).build();

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof User)
                return user;
            return null;
        }).when(userRepository).save(Mockito.any(User.class));

        UserDTO userTest = new UserDTO(user);
        UserDTO obtainedUser = userService.createNewUser("andreea","andreeabea","0000","0000","10");
        Assert.assertTrue(new ReflectionEquals(userTest).matches(obtainedUser));
    }

    @Test
    void updateWalletAmount() throws InvalidDataException, EntityNotFoundException {

        User user = RegularUser.builder().id(1).name("andreea").username("andreeabea").userType(UserType.REGULAR)
                .password("0000").wallet(10.0f).build();

        Mockito.when(userRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);

        ((RegularUser)user).setWallet(20.0f);
        RegularUser userTest = RegularUser.builder().wallet(20.0f).build();
        userService.updateWalletAmount("1","20");

        Assert.assertTrue(((RegularUser) user).getWallet() == userTest.getWallet());
    }

    @Test
    void addFavouriteRestaurant() throws InvalidDataException, EntityNotFoundException {
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").users(new ArrayList<>()).build();
        User user = RegularUser.builder().id(1).name("andreea").username("andreeabea").userType(UserType.REGULAR)
                .password("0000").wallet(10.0f).favourites(new ArrayList<>()).build();

        UserFavouriteRestaurant favouriteRestaurant = UserFavouriteRestaurant.builder().id(2).favouriteRestaurant(restaurant)
                .regularUser((RegularUser) user).build();

        Mockito.when(restaurantRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(restaurant));
        Mockito.when(userRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(user));

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof User)
                return user;
            return null;
        }).when(userRepository).save(Mockito.any(User.class));

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof UserFavouriteRestaurant)
                return favouriteRestaurant;
            return null;
        }).when(userFavouriteRestaurantRepository).save(Mockito.any(UserFavouriteRestaurant.class));

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof Restaurant)
                return restaurant;
            return null;
        }).when(restaurantRepository).save(Mockito.any(Restaurant.class));

        userService.addFavouriteRestaurant("1","1");

        Assert.assertEquals(restaurant.getUsers().size(),((RegularUser) user).getFavourites().size());
    }

    @Test
    void getFavouriteRestaurants() throws InvalidDataException {
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").items(new ArrayList<>()).build();
        User user = RegularUser.builder().id(1).name("andreea").username("andreeabea").userType(UserType.REGULAR)
                .password("0000").wallet(10.0f).favourites(new ArrayList<>()).build();

        List<UserFavouriteRestaurant> favouriteRestaurants = new ArrayList<>();
        UserFavouriteRestaurant favouriteRestaurant = UserFavouriteRestaurant.builder().id(2).favouriteRestaurant(restaurant)
                .regularUser((RegularUser) user).build();

        favouriteRestaurants.add(favouriteRestaurant);
        restaurant.setUsers(favouriteRestaurants);
        ((RegularUser) user).setFavourites(favouriteRestaurants);

        Mockito.when(userRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(user));

        List<RestaurantDTO> restaurantDTOS = userService.getFavouriteRestaurants("1");

        Assert.assertEquals(restaurantDTOS.size(),1);
        Assert.assertEquals(restaurantDTOS.get(0).getId(),restaurant.getId());
    }

    @Test
    void deleteFavouriteRestaurant() throws InvalidDataException, EntityNotFoundException {
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").items(new ArrayList<>()).build();
        User user = RegularUser.builder().id(1).name("andreea").username("andreeabea").userType(UserType.REGULAR)
                .password("0000").wallet(10.0f).favourites(new ArrayList<>()).build();

        List<UserFavouriteRestaurant> favouriteRestaurants = new ArrayList<>();
        UserFavouriteRestaurant favouriteRestaurant = UserFavouriteRestaurant.builder().id(2).favouriteRestaurant(restaurant)
                .regularUser((RegularUser) user).build();

        favouriteRestaurants.add(favouriteRestaurant);
        restaurant.setUsers(favouriteRestaurants);
        ((RegularUser) user).setFavourites(favouriteRestaurants);

        Mockito.when(userRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(restaurantRepository.findById(1)).thenReturn(java.util.Optional.of(restaurant));

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof UserFavouriteRestaurant)
                return favouriteRestaurant;
            return null;
        }).when(userFavouriteRestaurantRepository).delete(Mockito.any(UserFavouriteRestaurant.class));

        userService.deleteFavouriteRestaurant("1","1");

        Assert.assertEquals(((RegularUser) user).getFavourites().size(),0);
    }

    @Test
    void rateRestaurant() throws InvalidDataException, EntityNotFoundException {
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").items(new ArrayList<>()).build();
        User user = RegularUser.builder().id(1).name("andreea").username("andreeabea").userType(UserType.REGULAR)
                .password("0000").wallet(10.0f).favourites(new ArrayList<>()).ratings(new ArrayList<>()).build();

        Rating rating = Rating.builder().rate(5).ratedRestaurant(restaurant).user((RegularUser) user).build();
        Mockito.when(userRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(user));
        Mockito.when(restaurantRepository.findById(1)).thenReturn(java.util.Optional.of(restaurant));

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof Rating)
                return rating;
            return null;
        }).when(ratingRepository).save(Mockito.any(Rating.class));

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof User)
                return user;
            return null;
        }).when(userRepository).save(Mockito.any(User.class));

        userService.rateRestaurant("1","5","1");

        Assert.assertEquals(((RegularUser) user).getRatings().size(),1);
        Assert.assertTrue(new ReflectionEquals(rating).matches(((RegularUser) user).getRatings().get(0)));
    }
}