package com.example.project.IT;

import com.example.project.BaseIT;
import com.example.project.dto.RegistrationRequest;
import com.example.project.dto.ResetRequest;
import com.example.project.entity.AppUser;
import com.example.project.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser("janNowakowski@gmial.com")
@ActiveProfiles("IT")
public class UserIntegrationTest extends BaseIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void itShouldRegisterAndAuthenticateUser() throws Exception {

        //given
        String email = "janNowakowski@gmial.com";
        String password = "asdASD123!@#";
        RegistrationRequest registrationRequest = new RegistrationRequest("Jan", "Nowakowski", email, password,
                password);

        mockMvc.perform(post("/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .flashAttr("user", registrationRequest));

        userRepository.updateEnabled(userRepository.findByEmail(email).get().getId());

        mockMvc.perform(formLogin().user(email).password(password))
                .andExpect(authenticated().withUsername(email));

        //then
        assertThat(userRepository.findByEmail(email)).isPresent();
    }

    @Test
    void itShouldRegisterAndChangeUsersPassword() throws Exception {

        //given
        String email = "janNowakowski@gmial.com";
        String password = "asdASD123!@#";
        String newPassword = "newPassword123!@#";
        RegistrationRequest registrationRequest = new RegistrationRequest("Jan", "Nowakowski", email, password,
                password);

        ResetRequest resetRequest = new ResetRequest(email, newPassword, newPassword);

        mockMvc.perform(post("/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .flashAttr("user", registrationRequest));

        userRepository.updateEnabled(userRepository.findByEmail(email).get().getId());

        mockMvc.perform(post("/resetPassword")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .flashAttr("resetP", resetRequest))
                .andExpect(status().is(200));

        AppUser user = userRepository.findByEmail(email).orElseThrow();

        //then

        assertThat(user)
                .returns(email, AppUser::getEmail);

        assertThat(passwordEncoder.matches(newPassword, user.getPassword())).isEqualTo(true);

    }


}

