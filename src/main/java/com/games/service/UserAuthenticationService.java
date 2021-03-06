package com.games.service;


import com.games.exception.ResourceNotFoundException;
import com.games.repository.UserServiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserAuthenticationService implements UserDetailsService {
    @Autowired
    private UserServiceRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.games.model.User user = userRepository.findByUsernameAndEnabled(username, true);
        if (user == null) {
            throw new ResourceNotFoundException("Username not found or disabled: " + username, 45);
        }
        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        grantedAuthorities.add(new SimpleGrantedAuthority(user.getRole()));
        log.info("User details: {}, grantedAuth: {}", user, grantedAuthorities);
        return new User(user.getUsername(), user.getPassword(), grantedAuthorities);
    }
}
