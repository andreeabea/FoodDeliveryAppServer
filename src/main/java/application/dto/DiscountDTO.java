package application.dto;

import application.entities.Discount;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscountDTO {

    private int id;
    private String discount;

    public DiscountDTO(Discount discount)
    {
        this.id = discount.getId();
        this.discount = discount.getMinItemNumber()+"+ items: "+discount.getPercentage()+"%";
    }
}
