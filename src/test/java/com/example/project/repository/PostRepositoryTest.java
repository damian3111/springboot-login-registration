package com.example.project.repository;

import com.example.project.BaseIT;
import com.example.project.entity.Post;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostRepositoryTest extends BaseIT {

    @Autowired
    PostRepository underTest;
    @Autowired
    private UserRepository userRepository;

    @Test
    void itShouldSetPostsContent() {

        //given
        Long id = 1L;
        String content = "new content";

        //when
        underTest.setContentById(id, content);
        //then
        Optional<Post> post = underTest.findById(id);
        assertThat(post).hasValueSatisfying(c -> {
            assertThat(c.getContent()).isEqualTo(content);
        });
    }

    @Test
    void itShouldFindAllPosts() {
        //given
        //when
        Page<Post> allPosts = underTest.findAllFetch(PageRequest.of(0, 5));
        //then
        assertThat(allPosts.getContent().size()).isEqualTo(3);
    }

    @ParameterizedTest
    @MethodSource("itShouldFindPostsBySentenceDATA")
    void itShouldFindPostsBySentence(String sentence, List<String> result) {
        //given

        //when
        List<Post> content = underTest.findAllFetchBySentence(sentence, PageRequest.of(0, 10)).getContent();

        //then
        assertThat(content)
                .extracting("content")
                .containsExactlyElementsOf(result);
    }

    @ParameterizedTest
    @MethodSource("itShouldFindPostsBySentenceAndUsernameDATA")
    void itShouldFindPostsBySentenceAndUsername(String sentence, List<String> result, String username) {
        //given

        //when
        List<Post> content = underTest.findUserPostBySentence(username, sentence, PageRequest.of(0, 10)).getContent();

        //then
        assertThat(content)
                .extracting("content")
                .containsExactlyElementsOf(result);
    }

    static Stream<Arguments> itShouldFindPostsBySentenceDATA() {
        return Stream.of(
                Arguments.of( "post content", Arrays.asList("post content 1", "post content 2", "post content 3")),
                Arguments.of( "post content 2", Arrays.asList("post content 2")),
                Arguments.of( "3", Arrays.asList("post content 3")),
                Arguments.of( "wrong", Collections.emptyList())
        );
    }

    static Stream<Arguments> itShouldFindPostsBySentenceAndUsernameDATA() {
        return Stream.of(
                Arguments.of( "post content", Arrays.asList("post content 1", "post content 2", "post content 3"), "jakubNowak@gmail.com"),
                Arguments.of( "post content 2", Collections.emptyList(), "sampleEmail@gmail.com"),
                Arguments.of( "3", List.of("post content 3"), "jakubNowak@gmail.com"),
                Arguments.of( "wrong", Collections.emptyList(), "jakubNowak@gmail.com")
        );
    }
}