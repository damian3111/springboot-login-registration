package com.example.project.controller;

import com.example.project.dto.FilterRequest;
import com.example.project.dto.PostContentRequest;
import com.example.project.entity.AppUser;
import com.example.project.entity.Post;
import com.example.project.service.PostService;
import com.example.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Log4j2
public class PostController {

    private final UserService userService;
    private final PostService postService;

    @ModelAttribute("postC")
    public PostContentRequest postContentRequest(){
        return new PostContentRequest();
    }

    @GetMapping("/posts")
    public String posts(@ModelAttribute("filters") FilterRequest filterRequest, @RequestParam("page") Optional<Integer> pageParam, Model model, Principal principal){
        List<Post> posts;
        int page = pageParam.orElse(0);

        Optional<AppUser> user = userService.findUser(principal.getName());
        Page<Post> pagePosts;
        if (filterRequest.isMyPosts()) {
            if (filterRequest.getSentence() != null) {
                pagePosts = postService.findUserPostBySentence(principal.getName(), filterRequest.getSentence(), page);
                posts = pagePosts.getContent();
                model.addAttribute("sentence", filterRequest.getSentence());

            }else {
                pagePosts = postService.findUserPost(principal.getName(), page);
                posts = pagePosts.getContent();
            }

            model.addAttribute("myPosts", true);

        } else {
            if (filterRequest.getSentence() != null) {
                pagePosts = postService.getAllPostsBySentence(page, filterRequest.getSentence());
                posts = pagePosts.getContent();
                model.addAttribute("sentence", filterRequest.getSentence());
            } else {
                pagePosts = postService.getAllPosts(page);
                posts = pagePosts.getContent();
                model.addAttribute("sentence", "");

            }
            model.addAttribute("myPosts", false);
        }

        if (pagePosts.getTotalPages() - 2 >= page)
            model.addAttribute("next", page + 1);
        if (page >= 1)
            model.addAttribute("prev", page - 1);

        model.addAttribute("image", user.get().getPicture());
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
