package application.services;

import application.dto.RatingDTO;
import application.dto.UserDTO;
import application.services.exceptions.EntityNotFoundException;
import application.services.exceptions.InvalidDataException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RatingService {
    void deleteRating(String idString, UserDTO user) throws InvalidDataException, EntityNotFoundException;

    List<RatingDTO> getRatings(String idString) throws InvalidDataException;

    UserDTO addRating(String restaurantId, String rateString, String userId) throws InvalidDataException;

    void updateRating(String idString, String rateString, UserDTO user) throws InvalidDataException, EntityNotFoundException;
}
