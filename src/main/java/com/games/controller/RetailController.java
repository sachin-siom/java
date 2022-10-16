package com.games.controller;

import com.games.exception.ResourceNotFoundException;
import com.games.payload.*;
import com.games.service.PointPlayService;
import com.games.service.PointTicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/retail")
@CrossOrigin(origins = {"${settings.cors_origin}", "${settings.cors_origin.localhost}"})
public class RetailController {

    @Autowired
    private PointPlayService pointPlayService;

    @Autowired
    private PointTicketService ticketPDF;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return new ResponseEntity<>("Hello retailer page", HttpStatus.OK);
    }

    @PostMapping("/play")
    public ResponseEntity<PointPlayResponse> play(@Valid @RequestBody PointPlayRequest gamePlayRequest) {
        try {
            PointPlayResponse playResponse = pointPlayService.playBet(gamePlayRequest);
            return new ResponseEntity<>(playResponse, HttpStatus.CREATED);
        } catch (Exception ex) {
            log.error("problem in server playoing reqest gamePlayReq:{}", gamePlayRequest, ex);
            throw new ResourceNotFoundException("problem in processing request",20);
        }
    }

    @GetMapping(value = "/printTicket/{ticketId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PointPlayResponse> getTicket(@PathVariable("ticketId") String ticketId) {
        return ResponseEntity
                .ok()
                .body(pointPlayService.getTicketDetails(ticketId));
    }

    @DeleteMapping(value = "/deleteTicket/{ticketId}")
    public ResponseEntity<PointPlayResponse> deleteTicket(@PathVariable("ticketId") String ticketId) {
        pointPlayService.deleteTicketDetails(ticketId);
        return ResponseEntity
                .ok().build();
    }

    @GetMapping(value = "/winnerList/{drawTime}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DrawResponse> getWinnerList(@PathVariable("drawTime") String drawTime) {
        DrawResponse winnerWagerResponse = pointPlayService.getWinnerList(drawTime);
        return ResponseEntity
                .ok()
                .body(winnerWagerResponse);
    }

    @GetMapping(value = "/winnerListByDate/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DrawResponse>> getWinnerListByDate(@PathVariable("date") String date) {
        List<DrawResponse> winnerWagerResponse = pointPlayService.getWinnerListByDate(date);
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

    @GetMapping(value = "/checkWinnerNew/{ticketId}/{retailId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PointWinnerResponse> checkWinnerNew(@PathVariable("ticketId") String ticketId, @PathVariable("retailId") String retailId) {
        PointWinnerResponse winnerWagerResponse = pointPlayService.checkWinnerNew(ticketId, retailId);
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

    @GetMapping(value = "/claimWinnerNew/{ticketId}/{retailId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PointWinnerResponse> claimWinnerNew(@PathVariable("ticketId") String ticketId, @PathVariable("retailId") String retailId) {
        PointWinnerResponse winnerWagerResponse = pointPlayService.claimWinnerWithRetailId(ticketId, retailId);
        return ResponseEntity
            .ok()
            .body(winnerWagerResponse);
    }

    @GetMapping("/basicAuth")
    public ResponseEntity<AuthenticationBean> basicAuth() {
        return new ResponseEntity<>(new AuthenticationBean("You are authenticated"), HttpStatus.OK);
    }

}
