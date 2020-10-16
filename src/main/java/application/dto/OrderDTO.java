package application.dto;

import application.entities.DeliveryUser;
import application.entities.Order;
import application.entities.RegularUser;
import application.entities.Status;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

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
public class OrderDTO {

    private int id;

    private UserDTO customer;

    private UserDTO courier;

    private String datetime;

    private Status status;

    public OrderDTO(Order order)
    {
        this.id=order.getId();
        this.customer=new UserDTO(order.getCustomer());
        this.courier=new UserDTO(order.getCourier());
        this.datetime=order.getDatetime();
        this.status=order.getStatus();
    }
}
