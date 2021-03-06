package com.games.controller;

import com.games.exception.ResourceNotFoundException;
import com.games.model.Retailer;
import com.games.model.RetailerDailyReport;
import com.games.model.User;
import com.games.payload.*;
import com.games.repository.RetailerAuditRepository;
import com.games.repository.RetailerRepository;
import com.games.repository.UserServiceRepository;
import com.games.service.CommissionService;
import com.games.service.PointPlayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.*;

import static com.games.util.GameUtil.getDate;

@Slf4j
@RestController
@RequestMapping("/common")
@CrossOrigin(origins = {"${settings.cors_origin}", "${settings.cors_origin.localhost}"})
public class CommonController {

    @Autowired
    private PointPlayService pointPlayService;

    @Autowired
    private CommissionService commissionService;

    @Autowired
    private RetailerAuditRepository retailerAuditRepository;

    @Autowired
    private UserServiceRepository userServiceRepository;

    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private RetailerRepository retailerRepository;

    @GetMapping("/hello")
    public ResponseEntity<String> hello(){
        return new ResponseEntity<>("Hello common page", HttpStatus.OK);
    }

    @GetMapping("/serverTime")
    public ResponseEntity<String> serverTime() {
        try{
            return new ResponseEntity<>(LocalDateTime.now().toString(), HttpStatus.OK);
        }
        catch (Exception ex) {
            log.error("problem in fetching /servertime ", ex);
            throw new ResourceNotFoundException("server problem in getting servertime",15);
        }
    }

    @GetMapping("/tickets/{retailerId}")
    public ResponseEntity<List<PointWinnerResponse>> tickets( @PathVariable("retailerId") String retailerId, @RequestParam(required = false) Optional<String> date) {
        try{
            List<PointWinnerResponse> playResponse = pointPlayService.getRetailerTickets(retailerId, date);
            return new ResponseEntity<>(playResponse, HttpStatus.OK);
        }
        catch (Exception ex) {
            log.error("problem in fetching /tickets for reatil id: {} , date:{}", retailerId, date, ex);
            throw new ResourceNotFoundException("server problem in getting tickets",13);
        }
    }

    @GetMapping("/mybalance/{retailerId}")
    public ResponseEntity<RetailerResponse> myBalance( @PathVariable("retailerId") String retailerId) {
        try{
            RetailerResponse playResponse = pointPlayService.getMyBalance(retailerId);
            return new ResponseEntity<>(playResponse, HttpStatus.OK);
        }
        catch (Exception ex) {
            log.error("problem in fetching /mybalance for reatil id: {} ", retailerId, ex);
            throw new ResourceNotFoundException("server problem in getting mybalance",14);
        }
    }

    @GetMapping(value = "/drawDetails/{retailId}/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity drawDetails(@PathVariable("retailId") String retailsId, @PathVariable("date") String date) {
        try {
            List<DrawDetailsReportResponse> responses = pointPlayService.getDrawDetails(retailsId, date);
            return new ResponseEntity(responses, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("problem in fetching /drawDetails retailid:{}, date:{} ",retailsId, date, ex);
            throw new ResourceNotFoundException("problem in getting draw details for retail id",16);
        }
    }

    @GetMapping(value = "/retailerTickets/{retailId}/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity retailerTickets(@PathVariable("retailId")String retailsId, @PathVariable("date")String date) {
        try {
            List<RetailerTicketsReportResponse> responses = pointPlayService.retailerTickets(retailsId, date);
            return new ResponseEntity(responses, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("problem in fetching /retailerTickets retailid:{}, date:{} ",retailsId, date, ex);
            throw new ResourceNotFoundException("problem in getting retailerTickets for retail id",17);
        }
    }


    @GetMapping(value = "/counterSale/{retailId}/{fdate}/{tdate}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity counterSale(@PathVariable("retailId")String retailsId, @PathVariable("fdate")String fdate, @PathVariable("tdate")String tdate ) {
        try {
            if(Objects.isNull(retailsId) || Objects.isNull(fdate) || Objects.isNull(tdate)) {
                throw  new ResourceNotFoundException(" retailsId or fdate or tdate is null", 10003);
            }
            List<RetailerDailyReport> response = new ArrayList<>();
            LocalDate fromDate = getDate(fdate);
            LocalDate toDate = getDate(tdate);
            if(toDate.isEqual(LocalDate.now())){
                response.add(commissionService.getTodaysReport(retailsId));
                toDate = toDate.minusDays(1);
            }
            List<RetailerDailyReport> counterRetailersReport = commissionService.getCounterRetailersReport(retailsId, fromDate, toDate);
            if (Objects.nonNull(counterRetailersReport)){
                response.addAll(counterRetailersReport);
            }
            return new ResponseEntity(response, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("problem in fetching /counterSale retailid:{}, fdate:{} tdate:{} ",retailsId, fdate, tdate, ex);
            throw new ResourceNotFoundException("problem in getting retailerTickets for retail id",18);
        }
    }

    @GetMapping(value = "/runReport", produces = MediaType.APPLICATION_JSON_VALUE)
    public String runReport() {
        try {
            commissionService.getTodaysAllRetailersReport();
            return "success";
        } catch (Exception ex) {
            log.error("problem in running report ", ex);
            throw new ResourceNotFoundException("problem in running report",19);
        }
    }

    @GetMapping(value = "/commissionReport/{fdate}/{tdate}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommissionReportResponse> commissionReport(@PathVariable("fdate")String fdate, @PathVariable("tdate")String tdate ) {
        try {
            CommissionReportResponse responses = commissionService.commissionReport(fdate, tdate);
            return new ResponseEntity(responses, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("problem in running commissionReport ", ex);
            throw new ResourceNotFoundException("problem in running commissionReport",19);
        }
    }

    @GetMapping(value = "/commissionReport/{retailId}/{fdate}/{tdate}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommissionReportResponse> report(@PathVariable("retailId")String retailsId, @PathVariable("fdate")String fdate, @PathVariable("tdate")String tdate ) {
        try {
            CommissionReportResponse responses = commissionService.commissionReport(retailsId, fdate, tdate);
            return new ResponseEntity(responses, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("problem in running commissionReport ", ex);
            throw new ResourceNotFoundException("problem in running commissionReport",19);
        }
    }

}
