package com.example.project.service;

import com.example.project.dto.PostContentRequest;
import com.example.project.entity.AppUser;
import com.example.project.entity.Post;
import com.example.project.repository.PostRepository;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
@EnableCaching
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Cacheable(key = "#root.target.MY_KEY", value = "posts")
    public List<Post> getAllPosts(){
        return postRepository.findAllFetch();
    }

    public Post getById(Long id){return postRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));}

    @CacheEvict(value = "posts", allEntries = true)
    public int setContentById(Long id, String content){

        return postRepository.setContentById(id, content);}

    @CacheEvict(value = "posts", allEntries = true)
    public void deletePostById(Long id){
         postRepository.deleteById(id);
    }

    public Post insertPost(Post post){
        return postRepository.save(post);
    }

    public boolean validate(Long id, Principal principal) {

        Post post = getById(id);

        String name = principal.getName();

        return name.equals(post.getUser().getEmail());
    }

    public void insert(PostContentRequest postContentRequest) throws IOException {
        AppUser user = userRepository.findByEmail(postContentRequest.getEmail())
                .orElseThrow(() -> new IOException("User not found"));

        Post post = new Post();

        post.setUser(user);
        post.setContent(postContentRequest.getContent());

        insertPost(post);

    }

}
