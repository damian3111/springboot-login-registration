package com.example.project.config;

import com.example.project.entity.AppUser;
import com.example.project.entity.AppUserRole;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        if (oAuth2User == null){
            throw new OAuth2AuthenticationException("User is null");
        }

        Map<String, Object> attributes = oAuth2User.getAttributes();

        AppUser user;
        try {
            userRepository.findByEmail(attributes.get("email").toString())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        }catch (UsernameNotFoundException e){

            user = new AppUser();
            user.setAppUserRole(AppUserRole.USER);
            user.setEmail(attributes.get("email").toString());
            user.setPassword("$2a$10$2yrAnYbzUEE04EQopKzpZORHgaRZzz1VkR5CyV75ZfxIUiywnk6Oi");
            user.setFirstName(attributes.get("given_name").toString());
            user.setEnabled(true);
            user.setLastName(attributes.get("family_name").toString());
            user.setPicture(attributes.get("picture").toString());

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
                return attributes.get("email").toString();
            }
        };
    }
}
