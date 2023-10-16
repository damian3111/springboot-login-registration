package com.example.project.IT;

import com.example.project.BaseIT;
import com.example.project.dto.FilterRequest;
import com.example.project.dto.PostContentRequest;
import com.example.project.entity.AppUser;
import com.example.project.entity.AppUserRole;
import com.example.project.entity.Post;
import com.example.project.repository.PostRepository;
import com.example.project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
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

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void beforeEach() {
        userRepository.deleteAll();
        postRepository.deleteAll();
    }

    @Test
    void itShouldAuthenticateUserAndReturnViewsWithPostsCorrespondingToFilters() throws Exception {
        //given

        String email = "janNowakowski@gmial.com";
        String sampleEmail = "sampleUsername@gmial.com";

        String password = "asdASD123!@#";
        String postsContent1 = "post 1";
        String postsContent2 = "post 2";
        String postsContent3 = "post 3";
        String postsContent4 = "post 4";
        String postsContent5 = "post 5";


        PostContentRequest postContentRequest1 = new PostContentRequest(email, postsContent1);
        PostContentRequest postContentRequest2 = new PostContentRequest(email, postsContent2);
        PostContentRequest postContentRequest3 = new PostContentRequest(email, postsContent3);

        AppUser user = new AppUser("Jan", "Nowakowski", email, password, AppUserRole.USER);

        AppUser user2 = new AppUser("Krzysznof", "Kowalski", sampleEmail, password, AppUserRole.USER);

        Post post4 = new Post(4L, postsContent4, user2);
        Post post5 = new Post(5L, postsContent5, user2);


        userRepository.saveAll(List.of(user, user2));
        postRepository.saveAll(List.of(post4, post5));

        mockMvc.perform(post("/insertPost")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("postC", postContentRequest1))
                .andExpect(status().is(302));

        mockMvc.perform(post("/insertPost")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("postC", postContentRequest2))
                .andExpect(status().is(302));

        mockMvc.perform(post("/insertPost")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("postC", postContentRequest3))
                .andExpect(status().is(302));



        FilterRequest filterRequest1 = new FilterRequest(false, null);
        FilterRequest filterRequest2 = new FilterRequest(false, "post 4");
        FilterRequest filterRequest3 = new FilterRequest(true, null);
        FilterRequest filterRequest4 = new FilterRequest(true, "post 2");

        ResultActions perform1 = mockMvc.perform(get("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("filters", filterRequest1))
                .andExpect(status().is(200));

        String view1 = perform1.andReturn().getModelAndView().getViewName();
        List<Post> resAllPosts1 = (List<Post>)perform1.andReturn().getModelAndView().getModel().get("post_attr");

        ResultActions perform2 = mockMvc.perform(get("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("filters", filterRequest2))
                .andExpect(status().is(200));

        String view2 = perform2.andReturn().getModelAndView().getViewName();
        List<Post> resAllPosts2 = (List<Post>)perform2.andReturn().getModelAndView().getModel().get("post_attr");


        ResultActions perform3 = mockMvc.perform(get("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .flashAttr("filters", filterRequest3))
                .andExpect(status().is(200));

        String view3 = perform3.andReturn().getModelAndView().getViewName();
        List<Post> resAllPosts3 = (List<Post>)perform3.andReturn().getModelAndView().getModel().get("post_attr");


        ResultActions perform4 = mockMvc.perform(get("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("filters", filterRequest4))
                .andExpect(status().is(200));

        String view4 = perform4.andReturn().getModelAndView().getViewName();
        List<Post> resAllPosts4 = (List<Post>)perform4.andReturn().getModelAndView().getModel().get("post_attr");


        //then
        assertThat(resAllPosts1)
                .hasSize(5)
                .extracting("content")
                .contains("post 1", "post 2", "post 3", "post 4", "post 5");

        assertThat(view1).isEqualTo("posts/posts");

        assertThat(resAllPosts2)
                .hasSize(1)
                .extracting("content")
                .contains("post 4")
                .doesNotContain("post 2", "post 3", "post 1", "post 5");

        assertThat(view2).isEqualTo("posts/posts");


        assertThat(resAllPosts3)
                .hasSize(3)
                .extracting("content")
                .contains("post 1", "post 2", "post 3")
                .doesNotContain("post 4", "post 5");

        assertThat(view3).isEqualTo("posts/posts");

        assertThat(resAllPosts4)
                .hasSize(1)
                .extracting("content")
                .contains("post 2")
                .doesNotContain("post 4", "post 3", "post 1", "post 5");

        assertThat(view4).isEqualTo("posts/posts");

    }

    @Test
    void itShouldAuthenticateUserAndInsertUpdateDeletePosts() throws Exception {
        //given
        String email = "janNowakowski@gmial.com";
        String password = "asdASD123!@#";
        String postsContent = "post 1";
        Long id = 1L;
        String updatedContent = "updated content";

        PostContentRequest postContentRequest = new PostContentRequest(email, postsContent);

        AppUser user = new AppUser("Jan", "Nowakowski", email, password, AppUserRole.USER);
        userRepository.save(user);

        FilterRequest filterRequest = new FilterRequest(false, "");
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
