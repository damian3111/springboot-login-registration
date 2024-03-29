package com.example.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TokenDeserialization {

    @JsonProperty("id")
    private Long id;
    @JsonProperty("token")
    private String token;
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;
    @JsonProperty("appUser")
    private AppUserDeserialization appUser;
    @JsonProperty("confirmedAt")
    private LocalDateTime confirmedAt;
}
