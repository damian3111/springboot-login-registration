package com.example.project.repository;

import com.example.project.BaseIT;
import com.example.project.entity.ConfirmationToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TokenRepositoryTest extends BaseIT {

    @Autowired
    TokenRepository underTest;

    @Test
    void itShouldFindConfirmationTokenByToken() {
        //given
        String token = "9a5ddb6d-6241-44fa-ae75-4b7cf8132855";
        //when
        Optional<ConfirmationToken> confirmationToken = underTest.findByToken(token);
        //then
        assertThat(confirmationToken).hasValueSatisfying(c -> {
            assertThat(c.getId()).isEqualTo( 1);
            assertThat(c.getConfirmedAt()).isEqualTo( "2023-09-29T00:11:41");
            assertThat(c.getCreatedAt()).isEqualTo( "2023-09-29T00:10:49");
            assertThat(c.getExpiresAt()).isEqualTo( "2023-09-29T00:25:49");
            assertThat(c.getAppUser().getId()).isEqualTo( 2);
        });
    }

    @Test
    void itShouldSetConfirmedAt() {
        //given
        LocalDateTime localDateTime = LocalDateTime.of(2023, 9, 29, 0, 20, 49);
        //when
        underTest.updateConfirmedAt(localDateTime, 2L);
        //then
        Optional<ConfirmationToken> confirmationToken = underTest.findById(2L);
        assertThat(confirmationToken.get().getConfirmedAt()).isEqualTo(localDateTime);
    }

}