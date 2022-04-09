package com.games.controller;

import com.games.exception.ResourceNotFoundException;
import com.games.payload.*;
import com.games.service.PointPlayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200/")
@RestController
@RequestMapping("/common")
public class CommonController {

    @Autowired
    private PointPlayService pointPlayService;

    @GetMapping("/hello")
    public ResponseEntity<String> hello(){
        return new ResponseEntity<>("Hello common page", HttpStatus.OK);
    }

    @GetMapping("/tickets/{retailerId}")
    public ResponseEntity<List<PointWinnerResponse>> tickets( @PathVariable("retailerId") String retailerId, @RequestParam(required = false) Optional<String> date) {
        try{
            List<PointWinnerResponse> playResponse = pointPlayService.getRetailerTickets(retailerId, date);
            return new ResponseEntity<>(playResponse, HttpStatus.OK);
        }
        catch (ResourceNotFoundException exc) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, exc.getMessage(), exc);
        }
    }

    @GetMapping("/mybalance/{retailerId}")
    public ResponseEntity<RetailerResponse> myBalance( @PathVariable("retailerId") String retailerId) {
        try{
            RetailerResponse playResponse = pointPlayService.getMyBalance(retailerId);
            return new ResponseEntity<>(playResponse, HttpStatus.OK);
        }
        catch (ResourceNotFoundException exc) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, exc.getMessage(), exc);
        }
    }

    @GetMapping("/serverTime")
    public ResponseEntity<String> serverTime() {
        try{
            return new ResponseEntity<>(LocalDateTime.now().toString(), HttpStatus.OK);
        }
        catch (ResourceNotFoundException exc) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, exc.getMessage(), exc);
        }
    }

    @GetMapping(value = "/drawDetails/{retailId}/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity drawDetails(@PathVariable("retailId") String retailsId, @PathVariable("date") String date) {
        try {
            List<DrawDetailsReportResponse> responses = pointPlayService.getDrawDetails(retailsId, date);
            return new ResponseEntity(responses, HttpStatus.OK);
        } catch (ResourceNotFoundException exc) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, exc.getMessage(), exc);
        }
    }

    @GetMapping(value = "/retailerTickets/{retailId}/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity retailerTickets(@PathVariable("retailId")String retailsId, @PathVariable("date")String date) {
        try {
            List<RetailerTicketsReportResponse> responses = pointPlayService.retailerTickets(retailsId, date);
            return new ResponseEntity(responses, HttpStatus.OK);
        } catch (ResourceNotFoundException exc) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, exc.getMessage(), exc);
        }
    }

    @GetMapping(value = "/commission", produces = MediaType.APPLICATION_JSON_VALUE)
    public String drawDetails() {
        try {
            return "commission";
        } catch (ResourceNotFoundException exc) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, exc.getMessage(), exc);
        }
    }

}
