package com.games.service;

import com.games.model.Retailer;
import com.games.model.User;
import com.games.payload.RetailerRequest;
import com.games.repository.RetailerAuditRepository;
import com.games.repository.RetailerRepository;
import com.games.repository.UserServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.games.util.GameUtil.ROLE;

@Service
public class AdminService {

    @Autowired
    private RetailerRepository retailerRepository;

    @Autowired
    private UserServiceRepository userServiceRepository;

    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public void createUser(RetailerRequest newRetailerRequest) {
        User savedEntity = userServiceRepository.save(User.builder().username(newRetailerRequest.getUsername()).
                password(bCryptPasswordEncoder.encode(newRetailerRequest.getPassword())).isEnabled(true).role(ROLE).build());
        retailerRepository.save(Retailer.builder().retailId(String.valueOf(savedEntity.getId())).username(newRetailerRequest.getUsername())
                .balance(0.0).profitPercentage(Double.parseDouble(newRetailerRequest.getProfitPercentage())).build());
    }
}
