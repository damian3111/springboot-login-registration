package com.example.project.config;

import com.example.project.entity.AppUser;
import com.example.project.entity.AppUserRole;
import com.example.project.repository.UserRepository;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        if (oAuth2User == null){
            return null;
        }
        String[] values = oAuth2User.getAttributes().toString().split(", ");

        List<String> startsWith= Arrays.asList("email=", "given_name=");



        List<String> collect = Arrays.stream(values).filter(r -> startsWith(r, startsWith)).collect(Collectors.toList());
        String a = collect.get(0);
        String b = collect.get(1);

        String nameS = a.substring(11);

        int length1 = b.length();
        String substring = b.substring(6, length1);
        AppUser user;
        try {
            user = userRepository.findByEmail(substring).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        }catch (Exception e){



            user = new AppUser();
            user.setAppUserRole(AppUserRole.USER);
            user.setEmail(substring);
            user.setPassword("$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi");
            user.setFirstName(nameS);
            user.setEnabled(true);
            user.setLastName("x");

            userRepository.save(user);
        }


        return new OAuth2User() {
            @Override
            public Map<String, Object> getAttributes() {
                return oAuth2User.getAttributes();
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return oAuth2User.getAuthorities();
            }

            @Override
            public String getName() {
                return substring;
            }
        };
    }

    public static boolean startsWith(String word, List<String> prefixes) {
        for(String prefix : prefixes) {
            if (word.startsWith(prefix))
                return true;
        }
        return false;
    }
}
