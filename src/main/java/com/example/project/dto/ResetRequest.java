package com.example.project.dto;

import com.example.project.validator.ValidPassword;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetRequest {

    public String email;
    @ValidPassword
    public String password1;
    public String password2;
}
