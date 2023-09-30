package com.example.project.repository;

import com.example.project.BaseIT;
import com.example.project.entity.AppUser;
import com.example.project.entity.AppUserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest extends BaseIT {

    @Autowired
    UserRepository underTest;

    @Test
    void itShouldFindUserByEmail() {
        //given
        String email = "sampleEmail@gmail.com";
        //when
        Optional<AppUser> user = underTest.findByEmail(email);
        //then

        assertThat(user).isNotEmpty();

        assertThat(user)
                .isNotEmpty()
                .hasValueSatisfying(c -> {
                    assertThat(c.getEmail()).isEqualTo(email);
                    assertThat(c.getAppUserRole()).isEqualTo(AppUserRole.USER);
                    assertThat(c.getFirstName()).isEqualTo("Jan");
                    assertThat(c.getLastName()).isEqualTo("Kowalski");
                });
    }

    @ParameterizedTest
    @NullSource
    @CsvSource(value = {"sampleEmail2@gmail.com", "sampleEmail@gmail.co"})
    void itShouldNotFindUserWhenEmailIsNullOrWrong(String input) {
        //given
        //when
        Optional<AppUser> user = underTest.findByEmail(input);
        //then
        assertThat(user).isEmpty();

    }

    @Test
    void itShouldEnableUserAccount() {
        //given

        //when
        underTest.updateEnabled(2L);
        Optional<AppUser> user = underTest.findById(2L);
        //then
        assertThat(user.get().getEnabled()).isEqualTo(true);

    }

    @Test
    void itShouldChangeUserPassword() {
        //given
        String email = "jakubNowak@gmail.com";
        String password = "newPassword123!@#";
        //when
        underTest.changePassword(email, password);
        //then
        Optional<AppUser> user = underTest.findByEmail(email);
        assertThat(user).hasValueSatisfying(c -> {
            assertThat(c.getPassword()).isEqualTo(password);
        });

    }


}