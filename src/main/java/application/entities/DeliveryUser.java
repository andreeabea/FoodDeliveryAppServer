package application.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.util.List;

@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@DiscriminatorValue("DELIVERY")
public class DeliveryUser extends User {

    @OneToMany(mappedBy = "courier", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Order> orders;
}
