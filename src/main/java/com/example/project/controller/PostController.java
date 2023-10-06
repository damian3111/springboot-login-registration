package com.example.project.controller;

import com.example.project.dto.FilterRequest;
import com.example.project.dto.PostContentRequest;
import com.example.project.entity.AppUser;
import com.example.project.entity.Post;
import com.example.project.service.PostService;
import com.example.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final UserService userService;
    private final PostService postService;

    @ModelAttribute("postC")
    public PostContentRequest postContentRequest(){
        return new PostContentRequest();
    }

    @GetMapping("/posts")
    public String posts(@ModelAttribute("filters") FilterRequest filterRequest, Model model, Principal principal){
        List<Post> posts;
        AppUser user = userService.findByUsername(principal.getName());
        if (filterRequest.isMyPosts()){
            posts = user.getPost();
        }else{
            posts = postService.getAllPosts();
        }

        model.addAttribute("image", user.getPicture());
        model.addAttribute("post_attr", posts);

        return "posts/posts";
    }

    @GetMapping("/editPost/{id}")
    public String editPost(@PathVariable Long id, Model model, Principal principal) {

        if (!postService.validate(id, principal))
            return "redirect:/posts";

        model.addAttribute("edit_id", id);

        return "posts/editPost";

    }

    @PostMapping("/editPost")
    public String editPost(@RequestParam("id") Long id, @RequestParam("content") String content, Principal principal) {

        if (postService.validate(id, principal))
            postService.setContentById(id, content);

        return "redirect:/posts";
    }

    @GetMapping("/deletePost/{id}")
    public String deletePost(@PathVariable Long id, Principal principal) {
        if (postService.validate(id, principal))
            postService.deletePostById(id);

        return "redirect:/posts";
    }

    @GetMapping("/insertPost")
    public String insertPostGet(){

        return "posts/insertPost";
    }

    @PostMapping("/insertPost")
    public String insertPost_Post(@ModelAttribute("postC") PostContentRequest postContentRequest){


        try {
            postService.insert(postContentRequest);
        } catch (IOException ignored) {}

        return "redirect:/posts";

    }

}
