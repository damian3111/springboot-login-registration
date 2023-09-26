package com.example.project.controller;

import com.example.project.dto.*;
import com.example.project.entity.*;
import com.example.project.repository.PasswordResetTokenRepository;
import com.example.project.service.PostService;
import com.example.project.service.ResetService;
import com.example.project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.naming.CannotProceedException;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ResetService resetService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PostService postService;

    @ModelAttribute("user")
    public RegistrationRequest registrationRequest(){
        return new RegistrationRequest();
    }

    @ModelAttribute("resetE")
    public ResetEmailRequest resetRequest(){
        return new ResetEmailRequest();
    }

    @ModelAttribute("resetP")
    public ResetRequest resetRequestP(){
        return new ResetRequest();
    }

    @ModelAttribute("postC")
    public PostContentRequest postContentRequest(){
        return new PostContentRequest();
    }


    @GetMapping("/login")
    public String login(Authentication authentication){
        if (!(authentication == null)){
            return "redirect:/posts";
        }

        return "login/login";
    }

    @GetMapping("/register")
    public String register(){
        return "register/registration";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") @RequestBody RegistrationRequest registrationRequest,
                               BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if(userService.validate(registrationRequest, bindingResult, redirectAttributes)){

            try {
                userService.saveUser(registrationRequest);
            } catch (Exception ignored) {}
        }


        return "redirect:/register";
    }

    @GetMapping("/confirm")
    public String confirm(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {


        try {
            userService.confirm(token);
        } catch (CannotProceedException e) {
            redirectAttributes.addFlashAttribute("tabE", "Token has expired  or already confirmed");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("tabE", "Cannot find token");
        }


        return "redirect:register";
    }

    @GetMapping("/reset")
    public String resetGet(){
        return "reset/resetEmail";
    }

    @PostMapping("/reset")
    public String resetPost(@ModelAttribute("resetE")ResetEmailRequest resetEmailRequest, RedirectAttributes redirectAttributes) {

        try {
            userService.reset(resetEmailRequest);
            redirectAttributes.addFlashAttribute("errorReset", "Check your email inbox");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorReset", "You're not registered");
        }

        return "redirect:/reset";
    }

    @GetMapping("/resetPassword")
    public String resetPasswordGet(Model model, @RequestParam("token") String token){

        PasswordResetToken passwordResetToken;
        try {
            passwordResetToken = passwordResetTokenRepository.findByToken(token)
                    .orElseThrow(IOException::new);
        } catch (IOException e) {
            return "redirect:/login";
        }

        if(!resetService.validate(passwordResetToken))
            return "redirect:/login";

        model.addAttribute("email_attr", passwordResetToken.getAppUser().getEmail());


        return "reset/resetPassword";
    }

    @PostMapping("/resetPassword")
    public String resetPasswordPost(@ModelAttribute("resetP") ResetRequest resetRequest) {

        try {
            resetService.reset(resetRequest);
        } catch (IOException e) {
            return "redirect:/login";
        }

        return "register/registration";
    }

    @GetMapping("/posts")
    public String posts(@ModelAttribute("filters") FilterRequest filterRequest, Model model, Principal principal){
        List<Post> posts;
        if (filterRequest.isMyPosts()){
            posts = userService.findByUsername(principal.getName()).getPost();
        }else{
            posts = postService.getAllPosts();
        }

        model.addAttribute("image", userService.findByUsername(principal.getName()).getPicture());
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
