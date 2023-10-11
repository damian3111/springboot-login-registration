package com.example.project.service;

import com.example.project.dto.RegistrationRequest;
import com.example.project.email.EmailSender;
import com.example.project.entity.AppUser;
import com.example.project.entity.AppUserRole;
import com.example.project.entity.ConfirmationToken;
import com.example.project.repository.PostRepository;
import com.example.project.repository.TokenRepository;
import com.example.project.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.naming.CannotProceedException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    UserService underTest;

    @Mock
    UserRepository userRepository;
    @Mock
    TokenRepository tokenRepository;
    @Mock
    ResetService resetService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    EmailSender emailSender;
    @Mock
    BindingResult bindingResult;
    @Mock
    RedirectAttributes redirectAttributes;
    @Mock
    PostRepository postRepository;



    @BeforeEach
    void beforeEach() {
       underTest = new UserService(userRepository, passwordEncoder, tokenRepository, emailSender, resetService, postRepository);
    }


    @Test
    void itShouldFindUserByEmail() {
        //given
        String email = "sampleEmail@gmail.com";
        AppUser user = new AppUser("Jan", "Kowalski", email,
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi", AppUserRole.USER);
        user.setEnabled(true);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        //when
        UserDetails userDetails = underTest.loadUserByUsername(email);

        //then
        assertThat(user).isEqualTo(userDetails);
    }

    @Test
    void itShouldThrowWhenUserAccountNotEnabled() {
        //given
        String email = "sampleEmail@gmail.com";
        AppUser user = new AppUser("Jan", "Kowalski", email,
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi", AppUserRole.USER);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        //when
        assertThatThrownBy(() -> underTest.loadUserByUsername(email))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User is disabled");
        //then
    }

    @Test
    void itShouldValidateUserAccount() {
        String email = "sampleEmail@gmail.com";

        RegistrationRequest registrationRequest = new RegistrationRequest("Jan", "Kowalski", email,
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi",
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi");

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());
        given(bindingResult.hasErrors()).willReturn(false);
        //given
        //when
        boolean result = underTest.validate(registrationRequest, bindingResult, redirectAttributes);
        //then
        assertThat(result).isEqualTo(true);
        then(redirectAttributes).should().addFlashAttribute("tabE", "Confirm your email address");

    }

    @Test
    void itShouldNotValidateUserAccountWhenUserIsPresent() {
        //given

        String email = "sampleEmail@gmail.com";
        AppUser user = new AppUser("Jan", "Kowalski", email,
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi", AppUserRole.USER);

        RegistrationRequest registrationRequest = new RegistrationRequest("Jan", "Kowalski", email,
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi",
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi");

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        //when
        boolean result = underTest.validate(registrationRequest, bindingResult, redirectAttributes);
        //then
        assertThat(result).isEqualTo(false);
        then(redirectAttributes).should().addFlashAttribute("tabE", "You're already registered");
    }

    @Test
    void itShouldNotValidateUserAccountWhenPasswordsArentSame() {
        //given
        String email = "sampleEmail@gmail.com";

        RegistrationRequest registrationRequest = new RegistrationRequest("Jan", "Kowalski", email,
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi",
                "$2a$10$.2pXqtzrnYYtRcErjbPHsusMFN.9xaTN7qwG4XJO.M5W908wSWfLG");

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        //when
        boolean result = underTest.validate(registrationRequest, bindingResult, redirectAttributes);
        //then
        assertThat(result).isEqualTo(false);
        then(redirectAttributes).should().addFlashAttribute("tabE", "Pass the same password");
    }

    @Test
    void itShouldNotValidateUserAccountWhenBindingResultHasErrors() {
        //given
        ObjectError objectError = new ObjectError("error", "exception occurred");

        String email = "sampleEmail@gmail.com";

        RegistrationRequest registrationRequest = new RegistrationRequest("Jan", "Kowalski", email,
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi",
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi");

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());
        given(bindingResult.hasErrors()).willReturn(true);
        given(bindingResult.getAllErrors()).willReturn(List.of(objectError));

        //when
        boolean result = underTest.validate(registrationRequest, bindingResult, redirectAttributes);
        //then
        assertThat(result).isEqualTo(false);
        then(redirectAttributes).should().addFlashAttribute("tabE", List.of("exception occurred"));
    }

    @ParameterizedTest
    @CsvSource({"sampleEmail.gmail.com", "sampleEmail@gmail.comdsaas", "sampleEmail@l.comdsaas"})
    void itShouldNotValidateUserAccountWhenEmailIncorrect(String email) {

        RegistrationRequest registrationRequest = new RegistrationRequest("Jan", "Kowalski", email,
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi",
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi");

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());
        given(bindingResult.hasErrors()).willReturn(false);
        //given
        //when
        boolean result = underTest.validate(registrationRequest, bindingResult, redirectAttributes);
        //then
        assertThat(result).isEqualTo(false);
        then(redirectAttributes).should().addFlashAttribute("tabE", "Incorrect email address");

    }

    @Test
    void itShouldConfirmUserAccount() throws CannotProceedException, IOException {
        //given
        LocalDateTime createdAt = LocalDateTime.of(2023, 9, 29, 0, 29, 58);
        LocalDateTime now = LocalDateTime.of(2023, 9, 29, 0, 39, 58);
        LocalDateTime expiresAt = LocalDateTime.of(2023, 9, 29, 0, 44, 58);
        String token = "9a5ddb6d-6241-44fa-ae75-4b7cf8132855";

        AppUser user = new AppUser("Jan", "Kowalski", "sampleEmail@gmail.com",
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi", AppUserRole.USER);

        ConfirmationToken confirmationToken = new ConfirmationToken(token, createdAt, expiresAt, user);
        confirmationToken.setId(1L);


        given(tokenRepository.findByToken(token)).willReturn(Optional.of(confirmationToken));

        //when
        try (MockedStatic<LocalDateTime> utilities = Mockito.mockStatic(LocalDateTime.class)) {
            utilities.when(LocalDateTime::now)
                    .thenReturn(now);

            underTest.confirm(token);
        }

        //then
        then(tokenRepository).should().updateConfirmedAt(any(), anyLong());
        then(userRepository).should().updateEnabled(user.getId());
    }

    @Test
    void itShouldThrowWhenTokenExpired() {
        //given
        LocalDateTime createdAt = LocalDateTime.of(2023, 9, 29, 0, 29, 58);
        LocalDateTime now = LocalDateTime.of(2023, 9, 29, 0, 59, 58);
        LocalDateTime expiresAt = LocalDateTime.of(2023, 9, 29, 0, 44, 58);
        String token = "9a5ddb6d-6241-44fa-ae75-4b7cf8132855";

        AppUser user = new AppUser("Jan", "Kowalski", "sampleEmail@gmail.com",
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi", AppUserRole.USER);

        ConfirmationToken confirmationToken = new ConfirmationToken(token, createdAt, expiresAt, user);

        given(tokenRepository.findByToken(token)).willReturn(Optional.of(confirmationToken));
        //when

        try (MockedStatic<LocalDateTime> utilities = Mockito.mockStatic(LocalDateTime.class)) {
            utilities.when(LocalDateTime::now)
                    .thenReturn(now);

            assertThatThrownBy(() -> underTest.confirm(token))
                    .hasMessage("Token has expired")
                    .isInstanceOf(CannotProceedException.class);
        }
        //then

    }

    @ParameterizedTest
    @CsvSource({"sampleEmail@gmial.com, true", "sampleDomain@gmial.com, true", "sampleDomain.gmial.com, false",
            "sampleDomain@m.com, false", "as@gmial.com, false"})
    void name(String email, boolean valid) {

        //given
        //when
        boolean result = underTest.emailValidation(email);
        //then
        assertThat(result).isEqualTo(valid);
    }

    @Test
    void itShouldSaveUser() throws MessagingException {
        //given
        String email = "sampleEmail@gmail.com", name = "Jan", lastName = "Kowalski", password = "samplePassword123!@#";
        RegistrationRequest registrationRequest = new RegistrationRequest(name, lastName, email, password, password);

        AppUser user = new AppUser(name, lastName, email, password, AppUserRole.USER);
        user.setPicture("img/profile_img.jpg");

        given(passwordEncoder.encode(any())).willReturn(password);
        //when
        AppUser result = underTest.saveUser(registrationRequest);
        //then
        assertThat(result).isEqualTo(user);
        then(userRepository).should().save(user);
        then(tokenRepository).should().save(any());
        then(emailSender).should().send(any(), any());
    }

}