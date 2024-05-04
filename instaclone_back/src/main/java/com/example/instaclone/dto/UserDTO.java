package com.example.instaclone.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/*Data Transfer Object (DTO) for transferring user information to clients.
 * This DTO contains a subset of user details intended for presentation
 * in the user interface (UI) or for exposing through an API.*/
@Data
public class UserDTO {

    private Long id;

    @NotEmpty
    private String firstname;

    @NotEmpty
    private String lastname;

    @NotEmpty
    private String username;

    private String bio;
}
