package application.repositories;

import application.entities.Discount;
import application.entities.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount,Integer> {

    List<Discount> findByDiscRestaurantId(Integer id);
}
