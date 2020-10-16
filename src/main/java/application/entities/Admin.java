package application.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@AllArgsConstructor
@SuperBuilder
@DiscriminatorValue("ADMIN")
public class Admin extends User {
}
