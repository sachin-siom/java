package com.games.service;

import com.games.exception.ResourceNotFoundException;
import com.games.model.Creditaor;
import com.games.model.Retailer;
import com.games.model.RetailerAudit;
import com.games.model.User;
import com.games.payload.RetailerBalance;
import com.games.payload.RetailerRequest;
import com.games.repository.RetailerAuditRepository;
import com.games.repository.RetailerRepository;
import com.games.repository.UserServiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

import static com.games.util.GameUtil.PORTAL_UPDATE;
import static com.games.util.GameUtil.ROLE;

@Slf4j
@Service
public class AdminService {

    @Autowired
    private RetailerRepository retailerRepository;

    @Autowired
    private UserServiceRepository userServiceRepository;

    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private RetailerAuditRepository retailerAuditRepository;

    @Transactional
    public void createUser(RetailerRequest newRetailerRequest) {
        User savedEntity = userServiceRepository.save(User.builder().username(newRetailerRequest.getUsername()).
                password(bCryptPasswordEncoder.encode(newRetailerRequest.getPassword())).isEnabled(true).role(ROLE).build());
        retailerRepository.save(Retailer.builder().retailId(String.valueOf(savedEntity.getId())).username(newRetailerRequest.getUsername())
                .balance(0.0).profitPercentage(Double.parseDouble(newRetailerRequest.getProfitPercentage())).build());
    }

    @Transactional
    public void manageUser(String retailId, RetailerBalance balance){
        if (Objects.isNull(balance) || Objects.isNull(balance.getBalance())) {
            throw new ResourceNotFoundException("balance can not zero or negative", 8);
        }
        double bal = 0.0;
        try {
            bal = Double.parseDouble(balance.getBalance());
        } catch (Exception e) {
            log.error("parsing error in updating balance", e);
            throw new ResourceNotFoundException("Balance must be in integer", 9);
        }
        if (bal <= 0) {
            throw new ResourceNotFoundException("balance must be positive integer", 10);
        }
        Optional<Retailer> retailer = retailerRepository.findById(retailId);
        if (retailer.isPresent()) {
            log.info("retailer:{} updated balance:{}", retailer, balance);
            RetailerAudit audit = RetailerAudit.builder().retailId(retailId).ticketId(PORTAL_UPDATE).
                    isCredit(1).creditor(Creditaor.ADMIN.getVal()).amount(bal).balance(bal + retailer.get().getBalance()).build();
            retailerAuditRepository.save(audit);
            retailerRepository.updateBalance(Double.parseDouble(balance.getBalance()), retailId);
        } else {
            throw new ResourceNotFoundException("Retailid not exists", 11);
        }
    }
}
