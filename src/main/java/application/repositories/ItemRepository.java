package application.repositories;

import application.entities.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item,Integer> {

    Item findByName(String name);

    List<Item> findByRestaurantId(Integer id);
}
