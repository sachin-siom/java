package com.games.controller;

import com.games.exception.ResourceNotFoundException;
import com.games.payload.PointPlayRequest;
import com.games.payload.PointPlayResponse;
import com.games.payload.DrawResponse;
import com.games.payload.PointWinnerResponse;
import com.games.service.PointPlayService;
import com.games.service.PointTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/retail")
public class RetailController {

    @Autowired
    private PointPlayService pointPlayService;

    @Autowired
    private PointTicketService ticketPDF;

    @GetMapping("/hello")
    public ResponseEntity<String> hello(){
        return new ResponseEntity<>("Hello retailer page", HttpStatus.OK);
    }

    @PostMapping("/play")
    public ResponseEntity<PointPlayResponse> play(@Valid @RequestBody PointPlayRequest gamePlayRequest) {
        try{
            PointPlayResponse playResponse = pointPlayService.playBet(gamePlayRequest);
            return new ResponseEntity<>(playResponse, HttpStatus.CREATED);
        }
        catch (ResourceNotFoundException exc) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, exc.getMessage(), exc);
        }
    }

    @GetMapping(value = "/printTicket/{ticketId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> getTicket(@PathVariable("ticketId") String ticketId) {
        ByteArrayInputStream bis = ticketPDF.citiesReport(ticketId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=citiesreport.pdf");
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping(value = "/winnerList/{drawTime}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DrawResponse> getWinnerList(@PathVariable("drawTime") String drawTime) {
        DrawResponse winnerWagerResponse = pointPlayService.getWinnerList(drawTime);
        return ResponseEntity
                .ok()
                .body(winnerWagerResponse);
    }

    @GetMapping(value = "/checkWinner/{ticketId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PointWinnerResponse> checkWinner(@PathVariable("ticketId") String ticketId) {
        PointWinnerResponse winnerWagerResponse = pointPlayService.checkWinner(ticketId);
        return ResponseEntity
                .ok()
                .body(winnerWagerResponse);
    }

    @GetMapping(value = "/claimWinner/{ticketId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PointWinnerResponse> claimWinner(@PathVariable("ticketId") String ticketId) {
        PointWinnerResponse winnerWagerResponse = pointPlayService.claimWinner(ticketId);
        return ResponseEntity
                .ok()
                .body(winnerWagerResponse);
    }

}
