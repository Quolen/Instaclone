package com.example.instaclone.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class PostDTO {

    private Long id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String caption;

    private String location;

    private String username;

    private Long userId;

    private Integer likes;

    private Set<String> usersLiked;
}
