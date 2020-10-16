package application.repositories;

import application.entities.OrderedItem;
import application.entities.UserFavouriteRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFavouriteRestaurantRepository extends JpaRepository<UserFavouriteRestaurant, Integer> {
}
