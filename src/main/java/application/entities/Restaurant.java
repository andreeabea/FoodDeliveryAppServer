package application.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;

    @OneToMany(mappedBy = "restaurant", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Item> items;

    @OneToMany(mappedBy = "favouriteRestaurant", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JsonManagedReference
    private List<UserFavouriteRestaurant> users;

    @OneToMany(mappedBy = "ratedRestaurant", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JsonManagedReference
    private List<Rating> ratings;

    @OneToMany(mappedBy = "discRestaurant", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JsonManagedReference
    private List<Discount> discounts;
}
