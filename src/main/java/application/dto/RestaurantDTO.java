package application.dto;

import application.entities.Item;
import application.entities.Rating;
import application.entities.Restaurant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
@Builder
@ToString
@Getter
@Setter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestaurantDTO {

    private int id;
    private String name;

    private List<ItemDTO> items;

    private String rating = "0";

    public RestaurantDTO(Restaurant restaurant)
    {
        this.id=restaurant.getId();
        this.name=restaurant.getName();

        List<ItemDTO> itemDTOS = new ArrayList<>();
        List<Item> items = restaurant.getItems();
        for(Item item : items)
        {
            itemDTOS.add(new ItemDTO(item));
        }
        this.setItems(itemDTOS);

        List<Rating> ratings = restaurant.getRatings();
        if(ratings!=null && ratings.size()>0) {
            float sum = 0;
            for (Rating r : ratings) {
                sum += r.getRate();
            }
            sum = sum / ratings.size();

            this.rating = sum+" ("+ratings.size()+")";
        }
        else rating="0.0";
    }

    public StringProperty getItemsNumberProperty()
    {
        StringProperty sp =new SimpleStringProperty();
        String numberString = items.size()+" ";
        sp.setValue(numberString);
        return sp;
    }
}
