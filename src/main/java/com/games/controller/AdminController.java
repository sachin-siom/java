package com.games.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.games.exception.ResourceNotFoundException;
import com.games.model.Creditaor;
import com.games.model.Retailer;
import com.games.model.RetailerAudit;
import com.games.model.User;
import com.games.payload.*;
import com.games.repository.RetailerAuditRepository;
import com.games.repository.RetailerRepository;
import com.games.repository.UserServiceRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

import static com.games.util.GameUtil.ROLE;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private RetailerRepository retailerRepository;

    @Autowired
    private RetailerAuditRepository retailerAuditRepository;

    @Autowired
    private UserServiceRepository userServiceRepository;

    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return new ResponseEntity<>("Hello admin page", HttpStatus.OK);
    }

    @GetMapping("/onlyRetailerIds")
    public ResponseEntity<Set<Retailer>> getAllRetailers() {
        Set<Retailer> reatilerDetails = null;
        try {
            reatilerDetails = retailerRepository.findAll().stream().collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("there is issue while fetching user details: ", e);
        }
        return new ResponseEntity(reatilerDetails, HttpStatus.OK);
    }

    @GetMapping("/retailer/{retailId}")
    public ResponseEntity<Retailer> getRetailer(@PathVariable String retailId) {
        if(Objects.isNull(retailId) || StringUtils.isEmpty(retailId)){
            throw new ResourceNotFoundException("retailid can not be empty", 103);
        }
        try {
            Optional<Retailer> retailer = retailerRepository.findById(retailId);
            if(retailer.isPresent()){
                return new ResponseEntity(retailer.get(), HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("there is issue while fetching retailer details: ", e);
            throw new ResourceNotFoundException("retailer not found exception: {}", e);
        }
        return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/retailers")
    public ResponseEntity<List<RetailerResponse>> getAllRetailer() {
        Set<Retailer> reatilerDetails = retailerRepository.findAll().stream().collect(Collectors.toSet());
        List<RetailerResponse> response = new ArrayList<>();
        for (Retailer retailer : reatilerDetails) {
            try {
                Optional<User> isEnabled = Optional.of(userServiceRepository.getById(Long.parseLong(retailer.getRetailId())));
                if (isEnabled.isPresent()) {
                    response.add(RetailerResponse.builder().retailId(retailer.getRetailId()).balance(retailer.getBalance())
                            .status(isEnabled.get().isEnabled()).username(retailer.getUsername()).build());
                }
            } catch (Exception e) {
                log.error("there is issue while fetching user details: ", e);
            }
        }
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/addRetailer")
    @Transactional
    public ResponseEntity addRetailer(@RequestBody @Valid RetailerRequest newRetailerRequest) {
        User savedEntity = userServiceRepository.save(User.builder().username(newRetailerRequest.getUsername()).
                password(bCryptPasswordEncoder.encode(newRetailerRequest.getPassword())).isEnabled(true).role(ROLE).build());
        retailerRepository.save(Retailer.builder().retailId(String.valueOf(savedEntity.getId())).username(newRetailerRequest.getUsername())
                .balance(0.0).build());
        return new ResponseEntity(HttpStatus.OK);
    }

    @PutMapping("/disable/{retailId}")
    public ResponseEntity disableRetailer(@PathVariable String retailId) {
        try{
            if(Objects.isNull(retailId) || Long.parseLong(retailId) == 1){
                throw new ResourceNotFoundException("can not disable admin reatilId", 101);
            }
            userServiceRepository.disableRetailer(false, Long.parseLong(retailId));
        }catch (Exception e){
            log.error("diable request failed", e);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @PutMapping("/enabled/{retailId}")
    public ResponseEntity enableRetailer(@PathVariable String retailId) {
        try{
            if(Objects.isNull(retailId) || Long.parseLong(retailId) == 1){
                throw new ResourceNotFoundException("can not enable admin reatilId", 102);
            }
            userServiceRepository.enabledRetailer(true, Long.parseLong(retailId));
        }catch (Exception e){
            log.error("enabled request failed", e);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @PutMapping("/includeNumber/{retailId}")
    public ResponseEntity includeNumber(@PathVariable String retailId, @RequestBody @NotBlank @NotNull List<Integer> includeNumbers) {
        try{
            if(Objects.isNull(retailId) || Long.parseLong(retailId) != 1){
                throw new ResourceNotFoundException("can not enable admin reatilId", 102);
            }
            log.info("includers :{}", includeNumbers);
            String includeNumber = objectMapper.writeValueAsString(new HashSet(includeNumbers));
            log.info("incluber :{}", includeNumber);
            retailerRepository.includeWiningNumberByAdmin(includeNumber, retailId);
        }catch (Exception e){
            log.error("enabled request failed", e);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/manageRetailer/changepassword/{retailId}")
    public ResponseEntity<String> changepassword(@PathVariable String retailId, @RequestBody @NotBlank @NotNull String newPassowrd) {
        Optional<User> user = userServiceRepository.findById(Long.parseLong(retailId));
        if (user.isPresent()) {
            log.info("pasword:{}", newPassowrd);
            userServiceRepository.updateUserPassword(bCryptPasswordEncoder.encode(newPassowrd), Long.parseLong(retailId));
        } else {
            throw new ResourceNotFoundException("Retailid not exists", 100);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/manageRetailer/updateBalance/{retailId}")
    public ResponseEntity<String> updateBalance(@PathVariable String retailId, @RequestBody @NotBlank @NotNull RetailerBalance balance) {

        if (Objects.isNull(balance) || Objects.isNull(balance.getBalance())) {
            throw new ResourceNotFoundException("balance is zero or negative", 201);
        }
        double bal = 0.0;
        try {
            bal = Double.parseDouble(balance.getBalance());
        } catch (Exception e) {
            log.error("parsing error in updating balance", e);
            throw new ResourceNotFoundException("parsing error in updating balance", 200);
        }
        if (bal <= 0) {
            throw new ResourceNotFoundException("balance is zero or negative", 201);
        }
        Optional<Retailer> retailer = retailerRepository.findById(retailId);
        if (retailer.isPresent()) {
            log.info("retailer:{} updated balance:{}", retailer, balance);
            RetailerAudit audit = RetailerAudit.builder().retailId(retailId).ticketId("PORTAL_UPDATE").
                    isCredit(1).creditor(Creditaor.ADMIN.getVal()).amount(bal).balance(bal + retailer.get().getBalance()).build();
            retailerAuditRepository.save(audit);
            retailerRepository.updateBalance(Double.parseDouble(balance.getBalance()), retailId);
        } else {
            throw new ResourceNotFoundException("Retailid not exists", 100);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/manageRetailer/updateProfitPercentage/{retailId}")
    public ResponseEntity<String> updateProfitPercentage(@PathVariable String retailId, @RequestBody @NotBlank @NotNull String profitPercentage) {
        Optional<Retailer> retailer = retailerRepository.findById(retailId);
        if (retailer.isPresent()) {
            retailerRepository.save(Retailer.builder().retailId(retailer.get().getRetailId()).username(retailer.get().getUsername())
                    .balance(retailer.get().getBalance()).profitPercentage(Double.parseDouble(profitPercentage)).build());
        } else {
            throw new ResourceNotFoundException("Retailid not exists", 100);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/basicAuth")
    public ResponseEntity<AuthenticationBean> basicAuth() {
        return new ResponseEntity<>(new AuthenticationBean("You are authenticated"), HttpStatus.OK);
    }

}