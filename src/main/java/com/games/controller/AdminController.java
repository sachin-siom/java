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
import com.games.service.AdminService;
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

import static com.games.util.GameUtil.PORTAL_UPDATE;
import static com.games.util.GameUtil.ROLE;

@Slf4j
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = {"${settings.cors_origin}"})
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

    @Autowired
    private AdminService adminService;

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
            throw new ResourceNotFoundException("retailid can not be empty or null", 1);
        }
        try {
            Optional<Retailer> retailer = retailerRepository.findById(retailId);
            if(retailer.isPresent()){
                return new ResponseEntity(retailer.get(), HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("there is issue while fetching retailer details: ", e);
            throw new ResourceNotFoundException("retailid can not be empty or null or issue while fetching the details", 2);
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
    public ResponseEntity addRetailer(@RequestBody @Valid RetailerRequest request) {
        try{
            adminService.createUser(request);
            return new ResponseEntity(HttpStatus.OK);
        }catch (Exception e){
            log.error("addRetailer: request :{}", request, e);
            throw new ResourceNotFoundException("add retailer not be created at server due to an error either username not valid opr already taken", 3);
        }
    }

    @PutMapping("/disable/{retailId}")
    public ResponseEntity disableRetailer(@PathVariable String retailId) {
        try{
            if(Objects.isNull(retailId) || Long.parseLong(retailId) == 1){
                throw new ResourceNotFoundException("can not disable admin reatilId", 4);
            }
            userServiceRepository.disableRetailer(false, Long.parseLong(retailId));
        }catch (Exception e){
            log.error("diable retailer request failed", e);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @PutMapping("/enabled/{retailId}")
    public ResponseEntity enableRetailer(@PathVariable String retailId) {
        try{
            if(Objects.isNull(retailId) || Long.parseLong(retailId) == 1){
                throw new ResourceNotFoundException("can not enable admin reatilId", 5);
            }
            userServiceRepository.enabledRetailer(true, Long.parseLong(retailId));
        }catch (Exception e){
            log.error("enabled retail request failed", e);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @PutMapping("/includeNumber/{retailId}")
    public ResponseEntity includeNumber(@PathVariable String retailId, @RequestBody @NotBlank @NotNull List<Integer> includeNumbers) {
        try{
            if(Objects.isNull(retailId) || Long.parseLong(retailId) != 1){
                throw new ResourceNotFoundException("can not enable admin reatilId", 6);
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
            log.info("password:{}", newPassowrd);
            userServiceRepository.updateUserPassword(bCryptPasswordEncoder.encode(newPassowrd), Long.parseLong(retailId));
        } else {
            throw new ResourceNotFoundException("Retailid not exists", 7);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/manageRetailer/updateBalance/{retailId}")
    public ResponseEntity<String> updateBalance(@PathVariable String retailId, @RequestBody @NotBlank @NotNull RetailerBalance balance) {
        adminService.manageUser(retailId, balance);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/manageRetailer/updateProfitPercentage/{retailId}")
    public ResponseEntity<String> updateProfitPercentage(@PathVariable String retailId, @RequestBody @NotBlank @NotNull String profitPercentage) {
        Optional<Retailer> retailer = retailerRepository.findById(retailId);
        if (retailer.isPresent()) {
            retailerRepository.save(Retailer.builder().retailId(retailer.get().getRetailId()).username(retailer.get().getUsername())
                    .balance(retailer.get().getBalance()).profitPercentage(Double.parseDouble(profitPercentage)).build());
        } else {
            throw new ResourceNotFoundException("Retailid not exists", 12);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/basicAuth")
    public ResponseEntity<AuthenticationBean> basicAuth() {
        return new ResponseEntity<>(new AuthenticationBean("You are authenticated"), HttpStatus.OK);
    }

    @PostMapping("/checkMac")
    public ResponseEntity<AuthenticationBean> checkMac(@PathVariable String retailId, @RequestBody @NotBlank @NotNull String macAddress) {
        Optional<User> user = userServiceRepository.findById(Long.parseLong(retailId));
        if (user.isPresent()) {
            if(Objects.nonNull(user.get().getMacAddress()) && Objects.nonNull(macAddress)){
                if(user.get().getMacAddress().equals(macAddress)) {
                    return new ResponseEntity<>(new AuthenticationBean("Mac address validation are successful"), HttpStatus.OK);
                }
            }
        } else {
            throw new ResourceNotFoundException("Retailid not exists", 7);
        }
        return new ResponseEntity<>(new AuthenticationBean("Invalid mac address"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/registerMac")
    public ResponseEntity<AuthenticationBean> registerMac(@PathVariable String retailId, @RequestBody @NotBlank @NotNull String macAddress) {
        Optional<User> user = userServiceRepository.findById(Long.parseLong(retailId));
        if (user.isPresent()) {
            if(Objects.nonNull(macAddress)){
                user.get().setMacAddress(macAddress);
                userServiceRepository.save(user.get());
                return new ResponseEntity<>(new AuthenticationBean("Mac registration is successful "), HttpStatus.OK);
            }
        } else {
            throw new ResourceNotFoundException("Retailid not exists", 7);
        }
        return new ResponseEntity<>(new AuthenticationBean("Invalid mac address or retail id"), HttpStatus.BAD_REQUEST);
    }

}