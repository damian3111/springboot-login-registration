package com.example.project.service;

import com.example.project.dto.PostContentRequest;
import com.example.project.entity.AppUser;
import com.example.project.entity.AppUserRole;
import com.example.project.entity.Post;
import com.example.project.repository.PostRepository;
import com.example.project.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    PostService underTest;
    @Mock
    PostRepository postRepository;

    @Mock
    UserRepository userRepository;

    @Captor
    ArgumentCaptor<Post> argumentCaptor;

    @BeforeEach
    public void tearDown() {
        underTest = new PostService(postRepository, userRepository);
    }

    @Test
    public void itShouldFindPostById() {
        //given
        Long id = 2L;
        AppUser user = new AppUser();

        Post post = new Post();
        post.setId(2L);
        post.setContent("post content");
        post.setUser(user);

        given(postRepository.findById(id)).willReturn(Optional.of(post));
        //when

        Post result = underTest.getById(id);
        //then

        assertThat(result).isEqualTo(post);
    }

    @Test
    void itShouldThrowWhenPostNotFound() {
        //given
        Long id = 3L;
        given(postRepository.findById(id)).willReturn(Optional.empty());
        //when
        assertThatThrownBy(() -> underTest.getById(id))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");

        //then
    }

    @Test
    void itShouldValidatePostBelongsToLoggedInUser() {
        //given
        Long id = 2L;

        AppUser user = new AppUser("Jan", "Kowalski", "sampleEmail@gmail.com",
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi", AppUserRole.USER);
        user.setId(1L);

        Post post = new Post(id, "post content", user);

        Principal principal = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());

        given(postRepository.findById(id)).willReturn(Optional.of(post));
        //when

        boolean result = underTest.validate(id, principal);
        //then
        assertThat(result).isEqualTo(true);
    }

    @Test
    void itShouldInsertPost() throws IOException {
        //given
        PostContentRequest postContentRequest = new PostContentRequest("sampleEmial@gmail.com", "post content");
        AppUser user = new AppUser("Jan", "Kowalski", "sampleEmail@gmail.com",
                "$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi", AppUserRole.USER);
        user.setId(1L);

        Post post = new Post(null, postContentRequest.getContent(), user);

        given(userRepository.findByEmail(postContentRequest.getEmail())).willReturn(Optional.of(user));

        //when
        underTest.insert(postContentRequest);

        //then
        then(postRepository).should().save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(post);
    }

    @Test
    void itShouldNotInsertPostAndThrowWhenUserNotFound() {
        //given
        PostContentRequest postContentRequest = new PostContentRequest("sampleEmial@gmail.com", "post content");

        given(userRepository.findByEmail(postContentRequest.getEmail())).willReturn(Optional.empty());
        //when
        assertThatThrownBy(() ->underTest.insert(postContentRequest))
                .isInstanceOf(IOException.class)
                .hasMessage("User not found");

        //then
        then(postRepository).shouldHaveNoInteractions();
    }

}