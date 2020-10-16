package application.dto;

import application.entities.RegularUser;
import application.entities.User;
import application.entities.UserType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {

    private int id;
    private String name;
    private String username;
    private String password;
    private float wallet;

    protected UserType userType;

    public UserDTO(User user)
    {
        this.id=user.getId();
        this.name=user.getName();
        this.username=user.getUsername();
        this.password=user.getPassword();
        this.userType = user.getUserType();
        if(userType==UserType.REGULAR)
        {
            this.wallet=((RegularUser)user).getWallet();
        }
    }

}
