package application.services.implementations;

import application.dto.RatingDTO;
import application.dto.UserDTO;
import application.entities.*;
import application.repositories.RatingRepository;
import application.repositories.RestaurantRepository;
import application.repositories.UserRepository;
import application.services.RatingService;
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

class RatingServiceImplTest {

    private RatingRepository ratingRepository;

    private RestaurantRepository restaurantRepository;

    private UserRepository userRepository;

    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingRepository= Mockito.mock(RatingRepository.class);
        restaurantRepository=Mockito.mock(RestaurantRepository.class);
        userRepository=Mockito.mock(UserRepository.class);
        ratingService=new RatingServiceImpl(ratingRepository,restaurantRepository,userRepository);
    }

    @Test
    void addRating() throws InvalidDataException {
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

        ratingService.addRating("1","5","1");

        Assert.assertEquals(((RegularUser) user).getRatings().size(),1);
        Assert.assertTrue(new ReflectionEquals(rating).matches(((RegularUser) user).getRatings().get(0)));
    }

    @Test
    void updateRating() throws InvalidDataException, EntityNotFoundException {

        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").items(new ArrayList<>()).build();
        User user = RegularUser.builder().id(1).name("andreea").username("andreeabea").userType(UserType.REGULAR)
                .password("0000").wallet(10.0f).favourites(new ArrayList<>()).ratings(new ArrayList<>()).build();

        List<Rating> ratingList = new ArrayList<>();
        Rating rating = Rating.builder().id(1).rate(5).ratedRestaurant(restaurant).user((RegularUser) user).build();
        ratingList.add(rating);
        ((RegularUser) user).setRatings(ratingList);

        Mockito.when(ratingRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(rating));

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof Rating)
                return rating;
            return null;
        }).when(ratingRepository).delete(Mockito.any(Rating.class));

        ratingService.updateRating("1","3",new UserDTO(user));

        Assert.assertEquals(((RegularUser) user).getRatings().get(0).getRate(),3);
    }

    @Test
    void getRatings() throws InvalidDataException {
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").items(new ArrayList<>()).build();
        User user = RegularUser.builder().id(1).name("andreea").username("andreeabea").userType(UserType.REGULAR)
                .password("0000").wallet(10.0f).favourites(new ArrayList<>()).ratings(new ArrayList<>()).build();
        List<Rating> ratingList = new ArrayList<>();
        Rating rating = Rating.builder().id(1).rate(5).ratedRestaurant(restaurant).user((RegularUser) user).build();
        ratingList.add(rating);
        ((RegularUser) user).setRatings(ratingList);

        List<RatingDTO> ratingDTOS = new ArrayList<>();

        Mockito.when(ratingRepository.findAll()).thenReturn(ratingList);

        List<RatingDTO> obtainedRatings = ratingService.getRatings("1");
        ratingDTOS.add(obtainedRatings.get(0));

        Assert.assertThat(obtainedRatings, CoreMatchers.is(ratingDTOS));
    }

    @Test
    void deleteRating() throws InvalidDataException, EntityNotFoundException {
        Restaurant restaurant = Restaurant.builder().id(1).name("Sushi Restaurant").items(new ArrayList<>()).build();
        User user = RegularUser.builder().id(1).name("andreea").username("andreeabea").userType(UserType.REGULAR)
                .password("0000").wallet(10.0f).favourites(new ArrayList<>()).ratings(new ArrayList<>()).build();

        List<Rating> ratingList = new ArrayList<>();
        Rating rating = Rating.builder().id(1).rate(5).ratedRestaurant(restaurant).user((RegularUser) user).build();
        ratingList.add(rating);
        ((RegularUser) user).setRatings(ratingList);

        Mockito.when(ratingRepository.findById(1)).thenReturn(java.util.Optional.ofNullable(rating));

        Mockito.doAnswer(invocationOnMock -> {
            if(invocationOnMock.getArguments()[0] instanceof Rating)
                return rating;
            return null;
        }).when(ratingRepository).delete(Mockito.any(Rating.class));

        ratingService.deleteRating("1",new UserDTO(user));
    }
}