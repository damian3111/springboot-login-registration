package com.example.project.IT;

import com.example.project.BaseIT;
import com.example.project.dto.FilterRequest;
import com.example.project.dto.PostContentRequest;
import com.example.project.dto.RegistrationRequest;
import com.example.project.entity.Post;
import com.example.project.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser("janNowakowski@gmial.com")
@ActiveProfiles("IT")
public class PostIntegrationTest extends BaseIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PostRepository postRepository;

    @Test
    void itShouldRegisterUserAndReturnPostsPage() throws Exception {
        //given

        String email = "janNowakowski@gmial.com";
        String password = "asdASD123!@#";
        RegistrationRequest registrationRequest = new RegistrationRequest("Jan", "Nowakowski", email, password,
                password);

        mockMvc.perform(post("/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .flashAttr("user", registrationRequest));


        FilterRequest filterRequest = new FilterRequest(false);
        ResultActions perform = mockMvc.perform(get("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .flashAttr("filters", filterRequest))
                .andExpect(status().is(200));

        String viewName = perform.andReturn().getModelAndView().getViewName();

        //then

        assertThat(viewName).isEqualTo("posts/posts");
    }

    @Test
    void itShouldRegisterUserAndInsertUpdateDeletePost() throws Exception {
        //given
        String email = "janNowakowski@gmial.com";
        String password = "asdASD123!@#";
        String postsContent = "post 1";
        Long id = 1L;
        String updatedContent = "updated content";

        RegistrationRequest registrationRequest = new RegistrationRequest("Jan", "Nowakowski", email, password,
                password);

        PostContentRequest postContentRequest = new PostContentRequest(email, postsContent);

        mockMvc.perform(post("/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .flashAttr("user", registrationRequest));

        FilterRequest filterRequest = new FilterRequest(false);
        mockMvc.perform(get("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("filters", filterRequest))
                .andExpect(status().is(200));

        mockMvc.perform(post("/insertPost")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .flashAttr("postC", postContentRequest))
                .andExpect(status().is(302));

        Post insertedPost = postRepository.findById(id).orElseThrow();

        mockMvc.perform(post("/editPost")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .param("id", String.valueOf(id))
                .param("content", updatedContent))
                .andExpect(status().is(302));

        Post updatedPost = postRepository.findById(id).orElseThrow();

        mockMvc.perform(get("/deletePost/" + id))
                .andExpect(status().is(302));

        Optional<Post> deletedPost = postRepository.findById(id);

        //then

        assertThat(insertedPost)
                .returns(email, r -> r.getUser().getEmail())
                .returns(postsContent, Post::getContent);

        assertThat(updatedPost)
                .returns(email, r -> r.getUser().getEmail())
                .returns(updatedContent, Post::getContent);

        assertThat(deletedPost).isEmpty();
    }
}
