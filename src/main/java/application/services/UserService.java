package application.services;

import application.dto.RestaurantDTO;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.IncorrectPasswordException;
import application.services.exceptions.InvalidDataException;
import application.services.exceptions.UserNotFoundException;
import application.dto.UserDTO;
import application.entities.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    User findById(int id);

    User findByUsername(String username);

    User save(String username);

    List<UserDTO> getAllRegularUsers();

    UserDTO loginUser(String username, String password) throws UserNotFoundException, IncorrectPasswordException;

    UserDTO getUser(String idString) throws UserNotFoundException, InvalidDataException;

    UserDTO createNewUser(String name, String username, String password, String password2, String walletString)
            throws InvalidDataException;

    void updateWalletAmount(String idString, String newAmountString) throws InvalidDataException, EntityNotFoundException;

    List<RestaurantDTO> getFavouriteRestaurants(String idString) throws InvalidDataException;

    UserDTO addFavouriteRestaurant(String restaurantId, String userId) throws InvalidDataException, EntityNotFoundException;

    UserDTO deleteFavouriteRestaurant(String restaurantId, String userId) throws InvalidDataException, EntityNotFoundException;

    UserDTO rateRestaurant(String restaurantId, String rateId, String userId) throws InvalidDataException, EntityNotFoundException;
}
