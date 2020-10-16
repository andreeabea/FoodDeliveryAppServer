package application.services.implementations;

import application.dto.RatingDTO;
import application.dto.UserDTO;
import application.entities.Rating;
import application.entities.RegularUser;
import application.entities.Restaurant;
import application.entities.User;
import application.repositories.RatingRepository;
import application.repositories.RestaurantRepository;
import application.repositories.UserRepository;
import application.services.RatingService;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {

    private RatingRepository ratingRepository;

    private RestaurantRepository restaurantRepository;

    private UserRepository userRepository;

    @Autowired
    public RatingServiceImpl(RatingRepository ratingRepository,RestaurantRepository restaurantRepository,UserRepository userRepository)
    {
        this.ratingRepository=ratingRepository;
        this.restaurantRepository=restaurantRepository;
        this.userRepository=userRepository;
    }

    @Override
    public void deleteRating(String idString, UserDTO user) throws InvalidDataException, EntityNotFoundException {
        int id;
        Rating foundRating;
        if (idString != null && !idString.equals("") && idString.matches("-?\\d+(\\.\\d+)?")) {
            id = Integer.parseInt(idString);
        } else throw new InvalidDataException("The given id is invalid!");

        if (id > 0) {
            foundRating = ratingRepository.findById(id).orElse(null);
            if (foundRating == null || foundRating.getUser().getId()!=user.getId()) {
                throw new EntityNotFoundException("Rating not found for user "+user.getUsername()+".");
            }
        } else throw new InvalidDataException("The given id is invalid!");

        ratingRepository.delete(foundRating);
    }

    @Override
    public List<RatingDTO> getRatings(String idString) throws InvalidDataException {
        int id;
        if (idString != null && !idString.equals("") && idString.matches("-?\\d+(\\.\\d+)?")) {
            id = Integer.parseInt(idString);
        } else throw new InvalidDataException("The given id is invalid!");

        List<Rating> ratings = ratingRepository.findAll();
        List<RatingDTO> ratingDTOS = new ArrayList<>();
        for(Rating r:ratings)
        {
            if(r.getUser().getId()==id)
            {
                ratingDTOS.add(new RatingDTO(r));
            }
        }
        return ratingDTOS;
    }

    @Override
    public UserDTO addRating(String restaurantId, String rateString, String userId) throws InvalidDataException {
        int rate = Integer.parseInt(rateString);
        int restId = Integer.parseInt(restaurantId);
        int id = Integer.parseInt(userId);

        Restaurant foundRestaurant;
        foundRestaurant = restaurantRepository.findById(restId).orElse(null);

        User foundUser = userRepository.findById(id).orElse(null);

        List<Rating> userRatings = ((RegularUser) foundUser).getRatings();

        for (Rating rating : userRatings) {
            if (rating.getRatedRestaurant().getId() == foundRestaurant.getId()) {
                throw new InvalidDataException("Rating on this restaurant already exists!");
            }
        }

        Rating newRating = Rating.builder().rate(rate).ratedRestaurant(foundRestaurant).user((RegularUser) foundUser).build();
        userRatings.add(newRating);
        ratingRepository.save(newRating);

        ((RegularUser) foundUser).setRatings(userRatings);

        userRepository.save(foundUser);

        return new UserDTO(foundUser);
    }

    @Override
    public void updateRating(String idString, String rateString, UserDTO user) throws InvalidDataException, EntityNotFoundException {
        int rate = Integer.parseInt(rateString);

        int id;
        Rating foundRating;
        if (idString != null && !idString.equals("") && idString.matches("-?\\d+(\\.\\d+)?")) {
            id = Integer.parseInt(idString);
        } else throw new InvalidDataException("The given id is invalid!");

        if (id > 0) {
            foundRating = ratingRepository.findById(id).orElse(null);
            if (foundRating == null || foundRating.getUser().getId()!=user.getId()) {
                throw new EntityNotFoundException("Rating not found for user "+user.getUsername()+".");
            }
        } else throw new InvalidDataException("The given id is invalid!");

        foundRating.setRate(rate);
        ratingRepository.save(foundRating);
    }
}
