package com.example.project.repository;

import com.example.project.BaseIT;
import com.example.project.entity.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostRepositoryTest extends BaseIT {

    @Autowired
    PostRepository underTest;

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
        List<Post> allPosts = underTest.findAllFetch();
        //then
        assertThat(allPosts.size()).isEqualTo(3);
    }

}