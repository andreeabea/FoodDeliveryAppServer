package application.services.implementations;

import application.dto.RestaurantDTO;
import application.entities.*;
import application.repositories.RatingRepository;
import application.repositories.RestaurantRepository;
import application.repositories.UserFavouriteRestaurantRepository;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.IncorrectPasswordException;
import application.services.exceptions.InvalidDataException;
import application.services.exceptions.UserNotFoundException;
import application.services.UserService;
import application.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import application.repositories.UserRepository;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    private RestaurantRepository restaurantRepository;

    private UserFavouriteRestaurantRepository userFavouriteRestaurantRepository;

    private RatingRepository ratingRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RestaurantRepository restaurantRepository,
                           UserFavouriteRestaurantRepository userFavouriteRestaurantRepository,
                           RatingRepository ratingRepository) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.userFavouriteRestaurantRepository = userFavouriteRestaurantRepository;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public User findById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User save(String username) {
        return userRepository.save(User.builder().username(username).build());
    }

    @Override
    public List<UserDTO> getAllRegularUsers() {
        List<User> users = userRepository.findAll().stream().filter(user -> user.getUserType() == UserType.REGULAR)
                .map(user -> (RegularUser) user).collect(Collectors.toList());
        List<UserDTO> userDTOS = new ArrayList<>();
        for (User user : users) {
            userDTOS.add(new UserDTO(user));
        }
        return userDTOS;
    }

    @Override
    public UserDTO loginUser(String username, String password) throws UserNotFoundException, IncorrectPasswordException {

        User u = userRepository.findByUsername(username);
        UserDTO user = null;
        if (u != null) {
            user = new UserDTO(u);
        } else {
            throw new UserNotFoundException("User does not exist!");
        }
        if (!u.getPassword().equals(password)) {
            throw new IncorrectPasswordException("Password for user is incorrect! Try again.");
        }
        return user;
    }

    @Override
    public UserDTO getUser(String idString) throws UserNotFoundException, InvalidDataException {

        if (idString == null || idString.equals("") || !idString.matches("-?\\d+(\\.\\d+)?")) {
            throw new InvalidDataException("Invalid user id!");
        }
        int id = Integer.parseInt(idString);
        User u = userRepository.findById(id).orElse(null);
        UserDTO user = null;
        if (u != null) {
            user = new UserDTO(u);
        } else {
            throw new UserNotFoundException("User does not exist!");
        }
        return user;
    }

    @Override
    public UserDTO createNewUser(String name, String username, String password, String password2, String walletString)
            throws InvalidDataException {
        if (!username.equals("") && !name.equals("") && !password.equals("") && !password2.equals("") && !walletString.equals("")
                && walletString.matches("-?\\d+(\\.\\d+)?")) {
            float wallet = Float.parseFloat(walletString);
            if (wallet < 0) {
                throw new InvalidDataException("Invalid wallet amount!");
            }
            if (password.equals(password2)) {
                User newUser = RegularUser.builder().username(username).name(name).password(password)
                        .wallet(wallet).orders(new ArrayList<>()).userType(UserType.REGULAR).build();
                return new UserDTO(userRepository.save(newUser));
            } else throw new InvalidDataException("The passwords doesn't match!");
        } else {
            if (username.equals("") || userRepository.findByUsername(username) != null) {
                throw new InvalidDataException("Invalid username!");
            }
            if (name.equals("")) {
                throw new InvalidDataException("Name field is empty!");
            }
            if (password.equals("")) {
                throw new InvalidDataException("Password field is empty!");
            }
            if (password2.equals("")) {
                throw new InvalidDataException("Second password field is empty!");
            } else {
                throw new InvalidDataException("Invalid input data!");
            }
        }
    }

    @Override
    public void updateWalletAmount(String idString, String newAmountString) throws InvalidDataException, EntityNotFoundException {
        if (idString.equals("") || newAmountString.equals("") || !newAmountString.matches("-?\\d+(\\.\\d+)?")) {
            throw new InvalidDataException("Invalid input data!");
        }
        int id = Integer.parseInt(idString);
        float newAmount = Float.parseFloat(newAmountString);
        if (id <= 0 || newAmount < 0) {
            throw new InvalidDataException("Invalid input data!");
        }
        User foundUser = userRepository.findById(id).orElse(null);
        if (foundUser == null || foundUser.getUserType() != UserType.REGULAR) {
            throw new EntityNotFoundException("Regular user not found!");
        }
        ((RegularUser) foundUser).setWallet(newAmount);
        userRepository.save(foundUser);
    }

    @Override
    public List<RestaurantDTO> getFavouriteRestaurants(String idString) throws InvalidDataException {
        if (idString.equals("") || !idString.matches("-?\\d+(\\\\d+)?")) {
            throw new InvalidDataException("Invalid input data!");
        }
        int id = Integer.parseInt(idString);
        if (id <= 0) {
            throw new InvalidDataException("Invalid input data!");
        }
        User foundUser = userRepository.findById(id).orElse(null);
        List<UserFavouriteRestaurant> favouriteRestaurants = ((RegularUser) foundUser).getFavourites();

        List<RestaurantDTO> restaurantDTOS = new ArrayList<>();

        for (UserFavouriteRestaurant r : favouriteRestaurants) {
            restaurantDTOS.add(new RestaurantDTO(r.getFavouriteRestaurant()));
        }

        return restaurantDTOS;
    }

    @Override
    public UserDTO addFavouriteRestaurant(String restaurantId, String userId) throws InvalidDataException, EntityNotFoundException {
        Restaurant foundRestaurant;
        int restId;
        if (restaurantId != null && !restaurantId.equals("") && restaurantId.matches("-?\\d+(\\.\\d+)?")) {
            restId = Integer.parseInt(restaurantId);
        } else throw new InvalidDataException("The given id is invalid!");

        if (restId > 0) {
            foundRestaurant = restaurantRepository.findById(restId).orElse(null);
            if (foundRestaurant == null) {
                throw new EntityNotFoundException("Restaurant not found!");
            }
        } else throw new InvalidDataException("The given id is invalid!");

        int id = Integer.parseInt(userId);
        User foundUser = userRepository.findById(id).orElse(null);
        List<UserFavouriteRestaurant> favouriteRestaurants = ((RegularUser) foundUser).getFavourites();

        List<Restaurant> restaurants = new ArrayList<>();

        for (UserFavouriteRestaurant r : favouriteRestaurants) {
            restaurants.add(r.getFavouriteRestaurant());
            if (r.getFavouriteRestaurant().getId() == foundRestaurant.getId()) {
                throw new InvalidDataException("This restaurant is already in the favourite list!");
            }
        }

        UserFavouriteRestaurant newFavourite = UserFavouriteRestaurant.builder().regularUser((RegularUser) foundUser)
                .favouriteRestaurant(foundRestaurant).build();
        favouriteRestaurants.add(newFavourite);

        ((RegularUser) foundUser).setFavourites(favouriteRestaurants);
        foundRestaurant.setUsers(favouriteRestaurants);

        userFavouriteRestaurantRepository.save(newFavourite);
        userRepository.save(foundUser);
        restaurantRepository.save(foundRestaurant);
        return new UserDTO(foundUser);
    }

    @Override
    public UserDTO deleteFavouriteRestaurant(String restaurantId, String userId) throws InvalidDataException, EntityNotFoundException {
        Restaurant foundRestaurant;
        int restId;
        if (restaurantId != null && !restaurantId.equals("") && restaurantId.matches("-?\\d+(\\.\\d+)?")) {
            restId = Integer.parseInt(restaurantId);
        } else throw new InvalidDataException("The given id is invalid!");

        if (restId > 0) {
            foundRestaurant = restaurantRepository.findById(restId).orElse(null);
            if (foundRestaurant == null) {
                throw new EntityNotFoundException("Restaurant not found!");
            }
        } else throw new InvalidDataException("The given id is invalid!");

        int id = Integer.parseInt(userId);
        User foundUser = userRepository.findById(id).orElse(null);
        List<UserFavouriteRestaurant> favouriteRestaurants = ((RegularUser) foundUser).getFavourites();

        List<UserFavouriteRestaurant> restaurants = new ArrayList<>();

        boolean ok = false;
        for (UserFavouriteRestaurant r : favouriteRestaurants) {
            if (r.getFavouriteRestaurant().getId() == foundRestaurant.getId()) {
                ok = true;
                userFavouriteRestaurantRepository.delete(r);
            } else {
                restaurants.add(r);
            }
        }

        if (ok == false) {
            throw new InvalidDataException("This restaurant is not in the favourite list!");
        }

        ((RegularUser) foundUser).setFavourites(restaurants);

        return new UserDTO(foundUser);
    }

    @Override
    public UserDTO rateRestaurant(String restaurantId, String rateId, String userId) throws InvalidDataException, EntityNotFoundException {

        int rate = Integer.parseInt(rateId);
        int restId = Integer.parseInt(restaurantId);
        int id = Integer.parseInt(userId);

        Restaurant foundRestaurant;
        if (restId > 0) {
            foundRestaurant = restaurantRepository.findById(restId).orElse(null);
            if (foundRestaurant == null) {
                throw new EntityNotFoundException("Restaurant not found!");
            }
        } else throw new InvalidDataException("The given id is invalid!");

        User foundUser = userRepository.findById(id).orElse(null);

        List<Rating> userRatings = ((RegularUser) foundUser).getRatings();

        boolean ok =true;
        for (Rating rating : userRatings) {
            if (rating.getRatedRestaurant().getId() == foundRestaurant.getId()) {
               ok=false;
               rating.setRate(rate);
               ratingRepository.save(rating);
            }
        }

        if(ok==true)
        {
            Rating newRating = Rating.builder().rate(rate).ratedRestaurant(foundRestaurant).user((RegularUser) foundUser).build();
            userRatings.add(newRating);
            ratingRepository.save(newRating);
        }

        ((RegularUser) foundUser).setRatings(userRatings);

        userRepository.save(foundUser);

        return new UserDTO(foundUser);
    }
}