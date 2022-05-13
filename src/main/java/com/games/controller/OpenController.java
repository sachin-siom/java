package com.games.controller;

import com.games.payload.DrawResponse;
import com.games.service.PointPlayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.games.util.GameUtil.currentDate;


@Slf4j
@RestController
@RequestMapping("/open")
public class OpenController {
    @Autowired
    private PointPlayService pointPlayService;

    @GetMapping(value = "/winnerListByDate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DrawResponse>> getWinnerListByDate() {
        List<DrawResponse> winnerWagerResponse = pointPlayService.getWinnerListByDate(currentDate());
        return ResponseEntity
                .ok()
                .body(winnerWagerResponse);
    }
}
