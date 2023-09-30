package com.example.project.repository;

import com.example.project.BaseIT;
import com.example.project.entity.PasswordResetToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PasswordResetTokenRepositoryTest extends BaseIT {

    @Autowired
    PasswordResetTokenRepository underTest;

    @Test
    void itShouldFindPasswordResetTokenByToken() {
        //given

        String token = "818edb9f-afca-4390-8ed5-6c55b922d7f8";
        //when
        Optional<PasswordResetToken> passwordResetToken = underTest.findByToken(token);
        //then
        assertThat(passwordResetToken).hasValueSatisfying(c -> {
            assertThat(c.getId()).isEqualTo(1);
            assertThat(c.getExpiryDate()).isEqualTo("2023-09-29T00:11:41");
            assertThat(c.getAppUser().getId()).isEqualTo(2);
        });
    }
}
