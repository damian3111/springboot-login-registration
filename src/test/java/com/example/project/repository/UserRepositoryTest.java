package com.example.project.repository;

import com.example.project.BaseIT;
import com.example.project.entity.AppUser;
import com.example.project.entity.AppUserRole;
import com.example.project.entity.Post;
import com.github.dockerjava.api.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest extends BaseIT {

    @Autowired
    UserRepository underTest;
    @Autowired
    private PostRepository postRepository;

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
        AppUser user = underTest.findById(2L).orElseThrow();
        //then
        assertThat(user.getEnabled()).isEqualTo(true);

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

    @ParameterizedTest
    @MethodSource("itShouldFindUserByUsernameAndPostsContentDATA")
    void itShouldFindUserByUsernameAndPostsContent(List<String> result, String sentence, String email, boolean exc) {
        //given
        //when

        if (exc) {
            assertThatThrownBy(() -> underTest.findByEmailFetchBySentence(sentence, email).orElseThrow(() -> new NotFoundException("User not found")))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Status 404: User not found");
        } else {
            AppUser user = underTest.findByEmailFetchBySentence(sentence, email).orElseThrow(() -> new NotFoundException("User not found"));
            List<Post> posts = user.getPost();
            //then

            assertThat(posts)
                    .extracting("content")
                    .containsExactlyElementsOf(result);

            assertThat(user.getEmail())
                    .isEqualTo(email);

        }


    }

    static Stream<Arguments> itShouldFindUserByUsernameAndPostsContentDATA(){
        return Stream.of(
                Arguments.of(List.of("post content 1"), "1", "jakubNowak@gmail.com", false),
                Arguments.of(List.of("post content 1", "post content 2", "post content 3"), "post content", "jakubNowak@gmail.com", false),
                Arguments.of(Collections.emptyList(), "post content", "sampleEmail@gmail.com", true),
                Arguments.of(Collections.emptyList(), "wrong", "jakubNowak@gmail.com", true)
        );
    }
}