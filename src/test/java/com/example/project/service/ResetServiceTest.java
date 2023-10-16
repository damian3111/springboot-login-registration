package com.example.project.service;

import com.example.project.email.EmailSender;
import com.example.project.entity.AppUser;
import com.example.project.entity.AppUserRole;
import com.example.project.entity.PasswordResetToken;
import com.example.project.repository.PasswordResetTokenRepository;
import com.example.project.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ResetServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    EmailSender emailSender;
    @Mock
    PasswordResetTokenRepository passwordResetTokenRepository;
    @Captor
    ArgumentCaptor<PasswordResetToken> argumentCaptor;

    ResetService underTest;

    @BeforeEach
    void beforeEach() {
        underTest = new ResetService(emailSender, userRepository, passwordResetTokenRepository, passwordEncoder);
    }

    @Test
    void itShouldResetUsersPassword() throws MessagingException {

        //given
        String email = "sampleEmail@gmail.com";
        AppUser user = new AppUser("Jan", "Kowalski", email,
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi", AppUserRole.USER);


        //when
        underTest.resetPassword(email, user);
        //then
        then(passwordResetTokenRepository).should().save(argumentCaptor.capture());
        PasswordResetToken value = argumentCaptor.getValue();
        assertThat(value.getAppUser().getEmail()).isEqualTo(email);
        then(emailSender).should().send(any(), any(), any());
    }

}