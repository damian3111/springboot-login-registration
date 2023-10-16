package com.example.project.service;

import com.example.project.dto.RegistrationRequest;
import com.example.project.dto.ResetEmailRequest;
import com.example.project.email.EmailSender;
import com.example.project.entity.AppUser;
import com.example.project.entity.AppUserRole;
import com.example.project.entity.ConfirmationToken;
import com.example.project.repository.PostRepository;
import com.example.project.repository.TokenRepository;
import com.example.project.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.naming.CannotProceedException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final TokenRepository tokenRepository;
    private final EmailSender emailSender;
    private final ResetService resetService;
    private final PostRepository postRepository;

    @Value("${project.host}")
    String host;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!user.getEnabled())
            throw new IllegalStateException("User is disabled");

        return user;
    }

    public Optional<AppUser> findUser(String username){
        return userRepository.findByEmail(username);
    }

    public boolean validate(RegistrationRequest registrationRequest, BindingResult bindingResult, RedirectAttributes redirectAttributes){

        if (findUser(registrationRequest.email).isPresent()){
            redirectAttributes.addFlashAttribute("tabE", "You're already registered");
            return false;

        }

        if (!registrationRequest.getPassword2().equals(registrationRequest.getPassword1())) {

            redirectAttributes.addFlashAttribute("tabE", "Pass the same password");

            return false;
        }

        if (bindingResult.hasErrors()) {
            List<String> errors;

            List<ObjectError> allErrors = bindingResult.getAllErrors();
            errors = Arrays.stream(Objects.requireNonNull(allErrors.get(0).getDefaultMessage()).split(",")).toList();
            redirectAttributes.addFlashAttribute("tabE", errors);

            return false;

        }
        if (!emailValidation(registrationRequest.getEmail())) {
            redirectAttributes.addFlashAttribute("tabE", "Incorrect email address");
            return false;
        }
        redirectAttributes.addFlashAttribute("tabE", "Confirm your email address");

        return true;

    }

    @Transactional
    public void confirm(String token) throws CannotProceedException, IOException {

        ConfirmationToken confirmationToken = tokenRepository.findByToken(token).
                orElseThrow(() -> new IOException("Token not found"));
        if(confirmationToken.getExpiresAt().isBefore(LocalDateTime.now()) || confirmationToken.getConfirmedAt() != null){
            throw new CannotProceedException("Token has expired");
        }

        tokenRepository.updateConfirmedAt(LocalDateTime.now(), confirmationToken.getId());
        userRepository.updateEnabled(confirmationToken.getAppUser().getId());

    }


    public boolean emailValidation(String email){
        String pattern = "\\w{5,20}@\\w{2,10}.\\w{2,5}";
        return Pattern.compile(pattern).matcher(email).matches();
    }

    @Transactional(rollbackFor = {MessagingException.class, Error.class})
    public AppUser saveUser(RegistrationRequest registrationRequest) throws MessagingException {

        AppUser appUser = new AppUser();
        appUser.setEmail(registrationRequest.getEmail());
        appUser.setFirstName(registrationRequest.getFirstName());
        appUser.setLastName(registrationRequest.getLastName());
        appUser.setAppUserRole(AppUserRole.USER);
        appUser.setPassword(bCryptPasswordEncoder.encode(registrationRequest.password1));
        appUser.setPicture("img/profile_img.jpg");
        userRepository.save(appUser);

        sendEmail(appUser);
        return appUser;
    }

    public void reset(ResetEmailRequest resetEmailRequest) throws IOException {

        AppUser user = userRepository.findByEmail(resetEmailRequest.getEmail())
                .orElseThrow(() -> new IOException("User not found"));

        resetService.resetPassword(resetEmailRequest.getEmail(), user);

    }


    public void sendEmail(AppUser user) throws MessagingException {
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), user);

        tokenRepository.save(confirmationToken);
        String link = "http://" + host + "/confirm?token=" + token;

        emailSender.send(user.getEmail(), buildEmail(user.getFirstName(), link), "Activate Account");
    }

    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to activate your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 15 minutes. <p>See you soon</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }

}
