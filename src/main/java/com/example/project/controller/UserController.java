package com.example.project.controller;

import com.example.project.dto.PostContentRequest;
import com.example.project.dto.RegistrationRequest;
import com.example.project.dto.ResetEmailRequest;
import com.example.project.dto.ResetRequest;
import com.example.project.entity.*;
import com.example.project.repository.PasswordResetTokenRepository;
import com.example.project.repository.TokenRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.PostService;
import com.example.project.service.ResetService;
import com.example.project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.Banner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final ResetService resetService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
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

        return "login/logowanie";
    }

    @GetMapping("/register")
    public String register(){
        return "register/registration";
    }


    @PostMapping("/todelete")
    public String todetele(@RequestBody RegistrationRequest registrationRequest){

        return "register/registration";
    }
    /////////////////////////////

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user")@RequestBody RegistrationRequest registrationRequest, BindingResult bindingResult, RedirectAttributes redirectAttributes) throws Exception {
        if (registrationRequest.getPassword2().equals(registrationRequest.getPassword1())) {
            List<String> errors = new ArrayList<>();

            if (bindingResult.hasErrors()) {
                List<ObjectError> allErrors = bindingResult.getAllErrors();
                errors = Arrays.stream(allErrors.get(0).getDefaultMessage().split(",")).toList();
                redirectAttributes.addFlashAttribute("tabE", errors);

                return "redirect:/register";
            }
            if (!userService.emailValidation(registrationRequest.getEmail())){
                redirectAttributes.addFlashAttribute("tabE", "Incorrect email address");
                return "redirect:/register";
            }

            userService.saveUser(registrationRequest);
            return "redirect:/register";
        }


        redirectAttributes.addFlashAttribute("tabE", "Pass the same password");

        return "redirect:/register";
    }

    @GetMapping("/confirm")
    public String confirm(@RequestParam("token") String token) throws Exception {

        //TODO
        // przeniesc do service
        ConfirmationToken confirmationToken = tokenRepository.findByToken(token).orElseThrow(() -> new Exception("Token not found"));

        if(confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())){
            return "redirect:/login";
        }

        tokenRepository.updateConfirmedAt(LocalDateTime.now(), confirmationToken.getId());
        userRepository.updateEnabled(confirmationToken.getAppUser().getId());

        return "redirect:register/registration";
    }

    @GetMapping("/reset")
    public String resetPassword(){
        return "reset/resetEmail";
    }

    @PostMapping("/reset")
    public String resetPassword2(@ModelAttribute("resetE")ResetEmailRequest resetEmailRequest, RedirectAttributes redirectAttributes) throws Exception {

        try {
            AppUser user = userRepository.findByEmail(resetEmailRequest.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("errorReset", "You're not registered");
            return "redirect:/reset";
        }

        resetService.sendEmail(resetEmailRequest.getEmail());
        return "redirect:/register";
    }

    @GetMapping("/changePassword")
    public String resetPassword2Post(@RequestParam("token") String token, RedirectAttributes model) throws Exception {
        //TODO
        // Do service

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token).orElseThrow(() -> new UsernameNotFoundException("Token not found"));

        if(passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new Exception("Token's expired");
        }

        model.addFlashAttribute("email_attr", passwordResetToken.getAppUser().getEmail());
        return "redirect:/resetPassword";
    }

    @GetMapping("/resetPassword")
    public String resetPasswordGet(){

        return "reset/resetPassword";
    }

    @PostMapping("/resetPassword")
    public String resetPasswordPost(@ModelAttribute("resetP") ResetRequest resetRequest, @RequestParam("email_attr") String email){
        AppUser user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        userRepository.changePassword(user.getEmail(), passwordEncoder.encode(resetRequest.getPassword1()));

        return "register/registration";
    }

    @GetMapping("/posts")
    public String getPosts(Model model){
        List<Post> posts = postService.getAllPosts();

        model.addAttribute("post_attr", posts);

        return "posts/posts";
    }

    @GetMapping("/editPost/{id}")
    public String editPost(@PathVariable Long id, Model model, Principal principal, @AuthenticationPrincipal OAuth2User ouser) throws Exception {

        Post post = postService.getById(id);

        String substring = userService.findEmail(ouser, principal);

        if (!substring.equals(post.getUser().getEmail())){
            throw new Exception("Wrong");
        }

        model.addAttribute("edit_id", id);

        return "posts/editPost";

    }

    @PostMapping("/editPost")
    public String editPost(@RequestParam("id") Long id, @RequestParam("content") String content, Principal principal, @AuthenticationPrincipal OAuth2User ouser) throws Exception {

        Post post = postService.getById(id);

        String substring = userService.findEmail(ouser, principal);




        if (!substring.equals(post.getUser().getEmail())){
            throw new Exception("Wrong");
        }

        postService.setContentById(id, content);

        return "redirect:/posts";
    }

    @GetMapping("/deletePost/{id}")
    public String deletePost(@PathVariable Long id, Principal principal, @AuthenticationPrincipal OAuth2User ouser) throws Exception {
        Post post = postService.getById(id);

        String substring = userService.findEmail(ouser, principal);


        if (!substring.equals(post.getUser().getEmail())){
            throw new Exception("Wrong");
        }

        postService.deletePostById(id);

        return "redirect:/posts";
    }

    @GetMapping("/insertPost")
    public String insertPost(){

        return "posts/insertPost";
    }

    @PostMapping("/insertPost")
    public String post_insertPost(@ModelAttribute("postC") PostContentRequest postContentRequest, Authentication authentication){
        String string = authentication.getPrincipal().getClass().toString();

            AppUser user = userRepository.findByEmail(postContentRequest.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Post post = new Post();

            post.setUser(user);
            post.setContent(postContentRequest.getContent());

            postService.insertPost(post);

            return "redirect:/posts";

    }

}
