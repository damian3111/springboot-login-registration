package com.example.project.controller;

import com.example.project.dto.*;
import com.example.project.entity.*;
import com.example.project.repository.PasswordResetTokenRepository;
import com.example.project.repository.TokenRepository;
import com.example.project.service.ResetService;
import com.example.project.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.naming.CannotProceedException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ResetService resetService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${project.host}")
    String host;
    private final TokenRepository tokenRepository;

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


    @GetMapping("/login")
    public String login(Authentication authentication){
        if (authentication != null){
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
    public String resetPasswordPost(@Valid @ModelAttribute("resetP") ResetRequest resetRequest, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
            if (resetService.reset(resetRequest, bindingResult, redirectAttributes))
                return "register/registration";

        return "redirect:/login";
    }

    @GetMapping("/resendEmail")
    public String resendEmail() {
        return "login/resendEmail";
    }

    @PostMapping("/resendEmail")
    public String resendEmailPost(@ModelAttribute("resetE") ResetEmailRequest resetRequest, RedirectAttributes attributes) throws MessagingException {

        Optional<AppUser> user = userService.findUser(resetRequest.getEmail());
        ConfirmationToken token;

        if (user.isEmpty() || user.get().isEnabled()){
            attributes.addFlashAttribute("infoReset", "wrong email address");
            return "redirect:/resendEmail";
        }

        token = tokenRepository.findLatestCreatedConfirmationToken(user.get().getEmail());
        if (LocalDateTime.now().minus(5, ChronoUnit.MINUTES).isBefore(token.getCreatedAt())) {
            attributes.addFlashAttribute("infoReset", "You can resend email once every 5 minutes!");
        }else{
            userService.sendEmail(user.get());

            attributes.addFlashAttribute("infoReset", "Check your email inbox");
        }

        return "redirect:/resendEmail";
    }


}
