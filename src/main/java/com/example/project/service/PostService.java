package com.example.project.service;

import com.example.project.dto.PostContentRequest;
import com.example.project.entity.AppUser;
import com.example.project.entity.Post;
import com.example.project.repository.PostRepository;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.Principal;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Cacheable(value = "posts")
    public Page<Post> getAllPosts(int page){
        return postRepository.findAllFetch(PageRequest.of(page, 5));
    }

    @Cacheable(value = "posts")
    public Page<Post> getAllPostsBySentence(int page, String sentence){

        return postRepository.findAllFetchBySentence(sentence, PageRequest.of(page, 5));
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

    @CacheEvict(value = "posts", allEntries = true)
    public void insert(PostContentRequest postContentRequest) throws IOException {
        AppUser user = userRepository.findByEmail(postContentRequest.getEmail())
                .orElseThrow(() -> new IOException("User not found"));

        Post post = new Post();

        post.setUser(user);
        post.setContent(postContentRequest.getContent());

        insertPost(post);

    }

    @Cacheable(value = "posts")
    public Page<Post> findUserPostBySentence(String username, String sentence, int page) {

        return postRepository.findUserPostBySentence(username, sentence, PageRequest.of(page, 5));
    }

    @Cacheable(value = "posts")
    public Page<Post> findUserPost(String username, int page){

        return postRepository.findUserPost(username, PageRequest.of(page, 5));
    }
}
