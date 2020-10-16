package application.dto;

import application.entities.Item;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemDTO {

    private int id;

    private String name;
    private int stock;
    private float price;

    //quantity to order
    private String quantity;

    public ItemDTO(Item item)
    {
        this.id=item.getId();
        this.name=item.getName();
        this.stock=item.getStock();
        this.price=item.getPrice();
    }
}
