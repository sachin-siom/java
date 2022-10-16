package com.games.controller;

import static com.games.util.GameUtil.upcomingDrawTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.games.exception.ResourceNotFoundException;
import com.games.model.LoadResponse;
import com.games.model.Retailer;
import com.games.model.RetailerAudit;
import com.games.model.User;
import com.games.payload.*;
import com.games.repository.RetailerAuditRepository;
import com.games.repository.RetailerRepository;
import com.games.repository.UserServiceRepository;
import com.games.service.AdminService;
import com.games.service.PointPlayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = {"${settings.cors_origin}", "${settings.cors_origin.localhost}"})
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

    @Autowired
    private PointPlayService pointPlayService;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return new ResponseEntity<>("Hello admin page", HttpStatus.OK);
    }

    List<Long> ids = Arrays.asList(0l,1l);

    @GetMapping("/onlyRetailerIds")
    public ResponseEntity<List<User>> getAllRetailers() {
        List<User> userList = null;
        try {
            userList = userServiceRepository.selectUsersExcept(ids);
        } catch (Exception e) {
            log.error("there is issue while fetching user details: ", e);
        }
        return new ResponseEntity(userList, HttpStatus.OK);
    }

    @GetMapping("/retailers")
    public ResponseEntity<List<RetailerResponse>> getAllRetailer() {
        Set<Retailer> reatilerDetails = retailerRepository.findAll().stream().collect(Collectors.toSet());
        List<RetailerResponse> response = new ArrayList<>();
        for (Retailer retailer : reatilerDetails) {
            if(retailer.getRetailId().equals("1") || retailer.getRetailId().equals("0")){
                continue;
            }
            try {
                Optional<User> isEnabled = Optional.of(userServiceRepository.getById(Long.parseLong(retailer.getRetailId())));
                if (isEnabled.isPresent()) {
                    response.add(RetailerResponse.builder().retailId(retailer.getRetailId()).balance(retailer.getBalance())
                            .status(isEnabled.get().isEnabled()).profitPercentage(String.valueOf(retailer.getProfitPercentage()))
                            .username(retailer.getUsername()).macAddress(isEnabled.get().getMacAddress())
                            .includeNumbers(retailer.getIncludeNumbers()).build());
                }
            } catch (Exception e) {
                log.error("there is issue while fetching user details: ", e);
            }
        }
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @GetMapping("/retailer/{retailId}")
    public ResponseEntity<Retailer> getRetailer(@PathVariable String retailId) {
        if(Objects.isNull(retailId) || StringUtils.isEmpty(retailId)){
            throw new ResourceNotFoundException("retailid can not be empty or null", 1);
        }
        try {
            Optional<Retailer> retailer = retailerRepository.findById(retailId);
            Optional<User> isEnabled = Optional.of(userServiceRepository.getById(Long.parseLong(retailer.get().getRetailId())));
            if(isEnabled.isPresent()){
                RetailerResponse retailerResponse = RetailerResponse.builder().retailId(retailer.get().getRetailId()).balance(retailer.get().getBalance())
                        .username(retailer.get().getUsername()).macAddress(isEnabled.get().getMacAddress()).includeNumbers(retailer.get().getIncludeNumbers()).
                        profitPercentage(String.valueOf(retailer.get().getProfitPercentage())).build();
                return new ResponseEntity(retailerResponse, HttpStatus.OK);
            }
        } catch (Exception e) {
            log.error("there is issue while fetching retailer details: ", e);
            throw new ResourceNotFoundException("retailid can not be empty or null or issue while fetching the details", 2);
        }
        return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/manageRetailer/changepassword/{retailId}")
    public ResponseEntity<String> changePassword(@PathVariable String retailId, @RequestBody @NotBlank @NotNull RetailerPassword newPassowrd) {
        Optional<User> user = userServiceRepository.findById(Long.parseLong(retailId));
        if (user.isPresent()) {
            log.info("password:{}", newPassowrd);
            userServiceRepository.updateUserPassword(bCryptPasswordEncoder.encode(newPassowrd.getPassword()), Long.parseLong(retailId));
        } else {
            throw new ResourceNotFoundException("Retailid not exists", 7);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/registerMac/{retailId}")
    public ResponseEntity<AuthenticationBean> registerMac(@PathVariable String retailId, @RequestBody @NotBlank @NotNull MACAddressPayload macPayload) {
        Optional<User> user = userServiceRepository.findById(Long.parseLong(retailId));
        if (user.isPresent()) {
            if(Objects.nonNull(macPayload)) {
                user.get().setMacAddress(macPayload.getMacAddress());
                userServiceRepository.save(user.get());
                return new ResponseEntity<>(new AuthenticationBean("Mac registration is successful "), HttpStatus.OK);
            }
        } else {
            throw new ResourceNotFoundException("Retailid not exists", 7);
        }
        return new ResponseEntity<>(new AuthenticationBean("Invalid mac address or retail id"), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/adminPortalRetailers")
    public ResponseEntity<List<RetailerPortalResponse>> getAdminPortalRetailers() {
        Set<Retailer> reatilerDetails = retailerRepository.findAll().stream().collect(Collectors.toSet());
        List<RetailerPortalResponse> response = new ArrayList<>();
        for (Retailer retailer : reatilerDetails) {
            if(retailer.getRetailId().equals("1") || retailer.getRetailId().equals("0")){
                continue;
            }
            try {
                Optional<User> isEnabled = Optional.of(userServiceRepository.getById(Long.parseLong(retailer.getRetailId())));
                if (isEnabled.isPresent()) {
                    response.add(RetailerPortalResponse.builder().retailId(retailer.getRetailId()).balance(retailer.getBalance())
                            .status(isEnabled.get().isEnabled()?"Activated":"De-Activated").live(isEnabled.get().isEnabled()?"Live":"Offline").profitPercentage(String.valueOf(retailer.getProfitPercentage()))
                            .username(retailer.getUsername()).macAddress(isEnabled.get().getMacAddress())
                            .includeNumbers(retailer.getIncludeNumbers()).build());
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

    @PostMapping("/manageRetailer/updateBalance/{retailId}")
    public ResponseEntity<String> updateBalance(@PathVariable String retailId, @RequestBody @NotBlank @NotNull RetailerBalance balance) {
        adminService.manageUser(retailId, balance);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/manageRetailer/updateProfitPercentage/{retailId}")
    public ResponseEntity<String> updateProfitPercentage(@PathVariable String retailId, @RequestBody @NotBlank @NotNull PercentagePayload profitPercentage) {
        Optional<Retailer> retailer = retailerRepository.findById(retailId);
        if (retailer.isPresent()) {
            retailerRepository.save(Retailer.builder().retailId(retailer.get().getRetailId()).username(retailer.get().getUsername())
                    .balance(retailer.get().getBalance()).includeNumbers(retailer.get().getIncludeNumbers()).profitPercentage(Double.parseDouble(profitPercentage.getProfitPercentage())).build());
        } else {
            throw new ResourceNotFoundException("Retailid not exists", 12);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/basicAuth")
    public ResponseEntity<AuthenticationBean> basicAuth() {
        return new ResponseEntity<>(new AuthenticationBean("You are authenticated"), HttpStatus.OK);
    }

    @PostMapping("/checkMac/{retailId}")
    public ResponseEntity<AuthenticationBean> checkMac(@PathVariable String retailId, @RequestBody @NotBlank @NotNull MACAddressPayload macPayload) {
        Optional<User> user = userServiceRepository.findById(Long.parseLong(retailId));
        if (user.isPresent()) {
            if(Objects.nonNull(user.get().getMacAddress()) && Objects.nonNull(macPayload.getMacAddress())){
                if(user.get().getMacAddress().equals(macPayload.getMacAddress())) {
                    return new ResponseEntity<>(new AuthenticationBean("Mac address validation are successful"), HttpStatus.OK);
                }
            }
        } else {
            throw new ResourceNotFoundException("Retailid not exists", 7);
        }
        return new ResponseEntity<>(new AuthenticationBean("Invalid mac address"), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/lastXTxn/{retailId}/{limit}")
    public ResponseEntity<LastXTxn> lastXTxn(@PathVariable String retailId, @PathVariable String limit) {
        List<RetailerAudit> retailerAudit = retailerAuditRepository.getLastXtxn(retailId, Integer.parseInt(limit) );
        if (Objects.nonNull(retailerAudit)) {
            AtomicInteger i = new AtomicInteger(1);
            List<LastXTxn> txnIds = retailerAudit.stream().map(audit -> new LastXTxn(i.getAndIncrement(), audit.getAmount(), audit.getCreationTime())).collect(Collectors.toList());
            return new ResponseEntity(txnIds, HttpStatus.OK);
        } else {
            throw new ResourceNotFoundException("Retailid audit not exists", 103);
        }
    }

    @GetMapping("/runDraw/{drawTime}")
    public ResponseEntity runDraw(@PathVariable String drawTime) {
        try {
            pointPlayService.decideWinner(drawTime);
        } catch (Exception e) {
            log.error("there is issue while fetching user details: ", e);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/load")
    public ResponseEntity<LoadResponse> load()
        throws JsonProcessingException {
        return new ResponseEntity(pointPlayService.checkDrawLoad(upcomingDrawTime()),HttpStatus.OK);
    }

    @GetMapping("/load/{drawTime}")
    public ResponseEntity<LoadResponse> loadCurrent(@PathVariable String drawTime)
        throws JsonProcessingException {
        return new ResponseEntity(pointPlayService.checkDrawLoadPast(drawTime),HttpStatus.OK);
    }

}