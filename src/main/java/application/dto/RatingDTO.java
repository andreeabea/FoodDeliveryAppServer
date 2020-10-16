package application.dto;

import application.entities.Rating;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RatingDTO {

    private int id;
    private String username;
    private String restaurant;
    private int rate;

    public RatingDTO(Rating rating)
    {
        this.id = rating.getId();
        this.username=rating.getUser().getUsername();
        this.restaurant=rating.getRatedRestaurant().getName();
        this.rate=rating.getRate();
    }
}
