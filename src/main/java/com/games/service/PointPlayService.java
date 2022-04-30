package com.games.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.games.exception.ResourceNotFoundException;
import com.games.model.*;
import com.games.payload.*;
import com.games.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.games.util.GameUtil.*;

@Slf4j
@Service
public class PointPlayService {

    public static final String ADMIN_RETAIL_ID = "1";

    private double MULTIPLIER = 180;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PointPlayAlgo pointPlayAlgo;

    @Autowired
    private SequenceRepository sequenceRepository;

    @Autowired
    private PointPlayRepository pointPlayRepository;

    @Autowired
    private WinnerPointRepository winnerPointRepository;

    @Autowired
    private RetailerRepository retailerRepository;

    @Autowired
    private RetailerAuditRepository retailerAuditRepository;


    @Transactional("transactionManager")
    public PointPlayResponse playBet(PointPlayRequest gamePlayRequest) {
        if (Objects.nonNull(gamePlayRequest.getDrawTime()) && !validateDrawTime(gamePlayRequest.getDrawTime())) {
            log.error("draw time can not be in past");
            throw new ResourceNotFoundException("draw time can not be in past", 23);
        }
        Optional<Retailer> optionalRetailer = retailerRepository.findById(gamePlayRequest.getRetailId());
        if (!optionalRetailer.isPresent()) {
            log.error("Retail id not found or not exist");
            throw new ResourceNotFoundException("Retail id not found or not exist", 24);
        }
        Retailer retailer = optionalRetailer.get();
        Optional<Integer> OptionalPlayerWager = gamePlayRequest.getPointArrays().stream().map
                (val -> val.getPricePerPoint() * val.getPoints().values().stream().mapToInt(i -> i).sum()).reduce(Integer::sum);
        Integer wager = OptionalPlayerWager.get();
        if (isSufficientBalance(wager, retailer.getBalance())) {
            log.error("Retail id not having sufficient balance");
            throw new ResourceNotFoundException("Retail id not having sufficient balance", 25);
        }
        double remainingBal = subtract(retailer.getBalance(), wager);
        PointsDetails pointsDetails = null;
        try {
            pointsDetails = PointsDetails.builder().isWinner(0).
                    points(objectMapper.writeValueAsString(gamePlayRequest.getPointArrays())).
                    retailId(gamePlayRequest.getRetailId()).drawTime(getDrawTime(gamePlayRequest.getDrawTime())).
                    ticketId(getTicketId(getDrawTime(gamePlayRequest.getDrawTime()), gamePlayRequest.getRetailId(), sequenceRepository))
                    .isPrinted(false).isDeleted(false).totalPoints(wager).build();
        } catch (JsonProcessingException e) {
            throw new ResourceNotFoundException("point array in not in proper JSON format", 26);
        }
        RetailerAudit audit = RetailerAudit.builder().retailId(pointsDetails.getRetailId()).ticketId(pointsDetails.getTicketId()).
                isCredit(0).creditor(Creditaor.USER.getVal()).amount(wager).balance(remainingBal).build();
        retailer.setBalance(remainingBal);
        retailerRepository.save(retailer);
        retailerAuditRepository.save(audit);
        pointsDetails = pointPlayRepository.saveAndFlush(pointsDetails);
        return PointPlayResponse.builder().drawTime(pointsDetails.getDrawTime()).
                currentTime(pointsDetails.getCreationTime()).points(pointsDetails.getPoints()).
                ticketId(pointsDetails.getTicketId()).retailId(gamePlayRequest.getRetailId()).totalTicketValue(wager).build();
    }

    public PointPlayResponse getTicketDetails(String ticketId) {
        PointsDetails pointsDetails = pointPlayRepository.findByTicketId(ticketId);
        if(Objects.isNull(pointsDetails)){
            log.error("invalid ticket details: {}", ticketId);
            throw new ResourceNotFoundException("ticket not found or invalid",46);
        }
        if(pointsDetails.isPrinted()){
            log.error(" ticket is already printed: {}", ticketId);
            throw new ResourceNotFoundException("ticket is already printed",101);
        }
        try{
            pointsDetails.setPrinted(true);
            pointPlayRepository.saveAndFlush(pointsDetails);
            return PointPlayResponse.builder().drawTime(pointsDetails.getDrawTime()).
                    currentTime(pointsDetails.getCreationTime()).points(pointsDetails.getPoints()).
                    ticketId(pointsDetails.getTicketId()).retailId(pointsDetails.getRetailId()).totalTicketValue(pointsDetails.getTotalPoints()).build();
        } catch(Exception e){
            log.error("error is parsing in points detailin get ticket details :", e);
            throw  new ResourceNotFoundException(" error is parsing in points detailin get ticket details ",100);
        }
    }

    public void deleteTicketDetails(String ticketId) {
        PointsDetails pointsDetails = pointPlayRepository.findByTicketId(ticketId);
        if(Objects.isNull(pointsDetails)){
            log.error("invalid ticket details: {}", ticketId);
            throw new ResourceNotFoundException("ticket not found or invalid",46);
        }
        Optional<Retailer> optionalRetailer = retailerRepository.findById(pointsDetails.getRetailId());
        if (!optionalRetailer.isPresent()) {
            log.error("Retail id not found or not exist");
            throw new ResourceNotFoundException("Retail id not found or not exist", 24);
        }
        RetailerAudit audit = RetailerAudit.builder().retailId(pointsDetails.getRetailId()).ticketId(pointsDetails.getTicketId()).
                isCredit(1).creditor(Creditaor.SYSTEM.getVal()).amount(pointsDetails.getTotalPoints()).balance(pointsDetails.getTotalPoints() + optionalRetailer.get().getBalance()).build();
        retailerAuditRepository.save(audit);
        retailerRepository.updateBalance(pointsDetails.getTotalPoints(), pointsDetails.getRetailId());
        pointsDetails.setDeleted(true);
        pointPlayRepository.saveAndFlush(pointsDetails);
    }


    public void decideWinner(String drawTime) throws JsonProcessingException {
        String date = LocalDate.now().toString();
        List<PointsDetails> allBets = pointPlayRepository.getByDrawTime(drawTime, atStartOfDay(date), atEndOfDay(date));
        double collectionAmt = 0.0;
        final Retailer adminRetailer = retailerRepository.getById(ADMIN_RETAIL_ID);
        log.info("admin retailer: {}", adminRetailer);
        if (Objects.isNull(adminRetailer)) {
            throw new ResourceNotFoundException("Admin user not found in DB ", 27);
        }
        double profitPercentage = adminRetailer.getProfitPercentage();
        log.info("profit {}% applied & drawTime: {}", profitPercentage, drawTime);
        Map<Integer, Double> betMap = new HashMap<>();
        for (PointsDetails bet : allBets) {
            ArrayList<Points> betPoints = objectMapper.readValue(bet.getPoints(), new TypeReference<ArrayList<Points>>() {
            });
            for (Points betPoint : betPoints) {
                double winningAmt = MULTIPLIER * betPoint.getWinningMultiplier();
                collectionAmt = collectionAmt + (betPoint.getPoints().values().stream().mapToInt(i -> i).sum() * betPoint.getPricePerPoint());
                final List<Integer> betPointsList = getPoints(betPoint.getPoints());
                for (Integer num : betPointsList) {
                    if (betMap.containsKey(num)) {
                        betMap.put(num, winningAmt + betMap.get(num));
                    } else {
                        betMap.put(num, winningAmt);
                    }
                }
            }
        }
        Set<Integer> includeNumber = new HashSet<>(1);
        try {
            log.info("betMap: {}, collectionAmt:{}", betMap, collectionAmt);
            if (Objects.nonNull(adminRetailer.getIncludeNumbers()) && !adminRetailer.getIncludeNumbers().trim().isEmpty()) {
                includeNumber.addAll(objectMapper.readValue(adminRetailer.getIncludeNumbers(), new TypeReference<ArrayList<Integer>>() {
                }));
            }
            log.info("includeNumber: {}", includeNumber);
        } catch (Exception e) {
            log.error("Problem while evaluating include numbers {}", adminRetailer.getIncludeNumbers(), e);
        }

        SortedSet<Integer> excludeNumber = new TreeSet();
        Map<String, List<PointDetails>> winners = pointPlayAlgo.run(betMap, collectionAmt, profitPercentage, includeNumber, excludeNumber);
        if (Objects.isNull(winners) || winners.isEmpty()) {
            log.error("winner list is empty drawTime: {}", drawTime);
            return;
        }
        log.info("drawTime :{} winners: {}", drawTime, winners);
        //Set<WagerNumber> winnerSet = new HashSet(winners.values().stream().flatMap(List::stream).collect(Collectors.toList()));
        //Map<Integer, Double> winnerNoMap = winnerSet.stream().collect(Collectors.toMap(num -> num.getNum(), num -> num.getWinningPrize()));
        Map<Integer, Double> winnerNoMap = winners.values().stream().flatMap(List::stream).collect(Collectors.toMap(num -> num.getNum(), num -> num.getWinningPrize()));
        for (PointsDetails bet : allBets) {
            ArrayList<Points> points = objectMapper.readValue(bet.getPoints(), new TypeReference<ArrayList<Points>>() {
            });
            double amt = 0.0;
            WinningDetails winningDetails = new WinningDetails();
            for (Points point : points) {
                double winningAmount = MULTIPLIER * point.getWinningMultiplier();
                final List<Integer> betPoints = getPoints(point.getPoints());
                for (Integer num : betPoints) {
                    if (winnerNoMap.containsKey(num)) {
                        amt = amt + winningAmount;
                        if (winningDetails.getWinningNums().get(num) != null) {
                            winningDetails.getWinningNums().put(num, winningDetails.getWinningNums().get(num) + winningAmount);
                        } else {
                            winningDetails.getWinningNums().put(num, winningAmount);
                        }
                        log.info("ticket id: {} , num: {} amount: {}, winningAmount:{}", bet.getTicketId(), num, amt, winningAmount);
                    }
                }
            }
            if (Double.compare(amt, 0.0) > 0 && winningDetails.getWinningNums().size() > 0) {
                bet.setWinningPoints(objectMapper.writeValueAsString(winningDetails));
                bet.setIsWinner(1);
                PointsDetails wagerDetails = pointPlayRepository.save(bet);
                log.info("winner update in DB:{}", wagerDetails);
            }
        }
        WinnerPointDetails wagerWinnerDetails = new WinnerPointDetails();
        wagerWinnerDetails.setDrawTime(drawTime);
        wagerWinnerDetails.setWinnerPoint(objectMapper.writeValueAsString(winnerNoMap));
        winnerPointRepository.save(wagerWinnerDetails);
    }

    private List<Integer> getPoints(Map<Integer, Integer> points) {
        List<Integer> generatedPoints = new ArrayList<>();
        for (Integer num : points.keySet()) {
            for (int i = 0; i < points.get(num); i++) {
                generatedPoints.add(num);
            }
        }
        return generatedPoints;
    }

    public DrawResponse getWinnerList(String drawTime) {
        try {
            WinnerPointDetails winnerDetails = winnerPointRepository.findByDrawTimeAndCreationTime(drawTime, LocalDate.now());
            if (Objects.isNull(winnerDetails)) {
                throw new ResourceNotFoundException("invalid draw time or result is not calculated yet", 28);
            }
            DrawResponse wagerWinnerResponse = new DrawResponse();
            wagerWinnerResponse.setDrawTime(winnerDetails.getDrawTime());
            Map<String, String> winner = objectMapper.readValue(winnerDetails.getWinnerPoint(), new TypeReference<Map<String, String>>() {
            });
            wagerWinnerResponse.setWinnerNumber(winner.keySet().stream().map(val -> Integer.parseInt(val)).collect(Collectors.toList()));
            wagerWinnerResponse.setDate(winnerDetails.getCreationTime().toString());
            return wagerWinnerResponse;
        } catch (Exception e) {
            log.error("problem is parsing the JSON or pasrsing errror", e);
            throw new ResourceNotFoundException(e.getMessage(), 29);
        }

    }

    public PointWinnerResponse checkWinner(String ticketId) {
        if (Objects.isNull(ticketId) || ticketId.isEmpty()) {
            throw new ResourceNotFoundException("ticketId is empty or null", 30);
        }
        PointsDetails pointsDetails = pointPlayRepository.getById(ticketId);
        if (Objects.isNull(pointsDetails)) {
            throw new ResourceNotFoundException("ticketId not found in system", 31);
        }
        return getPlayerResponse(pointsDetails);
    }

    public PointWinnerResponse claimWinner(String ticketId) {
        if (Objects.isNull(ticketId) || ticketId.isEmpty()) {
            throw new ResourceNotFoundException("ticketId is empty or null", 32);
        }
        try {
            PointsDetails pointsDetails = pointPlayRepository.getById(ticketId);
            if (Objects.isNull(pointsDetails)) {
                throw new ResourceNotFoundException("ticketId not found in system", 33);
            }
            if (Objects.isNull(pointsDetails.getWinningPoints())) {
                throw new ResourceNotFoundException("no winning numbers found in the tickets", 34);
            }

            WinningDetails winningDetails = objectMapper.readValue(pointsDetails.getWinningPoints(), WinningDetails.class);
            double winningPrize = winningDetails.getWinningNums().values().stream().mapToDouble(i -> i).sum();
            Retailer byId = retailerRepository.getById(pointsDetails.getRetailId());
            RetailerAudit audit = RetailerAudit.builder().retailId(pointsDetails.getRetailId()).ticketId(ticketId).
                    isCredit(1).creditor(Creditaor.USER.getVal()).amount(winningPrize).balance(winningPrize + byId.getBalance()).build();
            retailerAuditRepository.save(audit);
            retailerRepository.updateBalance(winningPrize, byId.getRetailId());
            log.info("winning points details: {}", winningDetails);
            pointsDetails.setIsClaimed(1);
            pointsDetails.setIsClaimedTime(LocalDateTime.now());
            pointsDetails = pointPlayRepository.save(pointsDetails);
            return getPlayerResponse(pointsDetails);
        } catch (Exception e) {
            log.error("json parsing exception in claim winner ticketId:{}", ticketId, e);
            throw new ResourceNotFoundException("JSON parsing exception in claim winner or ticket id not found", 35);
        }
    }

    public List<PointWinnerResponse> getRetailerTickets(String retailerId, Optional<String> localDate) {
        String date = null;
        if (!localDate.isPresent()) {
            date = LocalDate.now().toString();
        } else {
            date = localDate.get().toString();
        }
        final List<PointsDetails> pointsDetails = pointPlayRepository.findByRetailerIdAndCreationDate(retailerId, atStartOfDay(date), atEndOfDay(date));
        final List<PointWinnerResponse> pointWinnerResponses = new ArrayList<>();
        for (PointsDetails pointsDetail : pointsDetails) {
            pointWinnerResponses.add(getPlayerResponse(pointsDetail));
        }
        return pointWinnerResponses;
    }

    public PointWinnerResponse getPlayerResponse(PointsDetails pointsDetails) {
        return PointWinnerResponse.builder().retailId(pointsDetails.getRetailId()).drawTime(pointsDetails.getDrawTime())
                .ticketId(pointsDetails.getTicketId()).winningPoints(pointsDetails.getWinningPoints())
                .points(pointsDetails.getPoints()).isClaimed(Objects.nonNull(pointsDetails.getIsClaimed()) && pointsDetails.getIsClaimed() != 0)
                .ticketTime(pointsDetails.getCreationTime().toString())
                .isWinner(Objects.nonNull(pointsDetails.getWinningPoints()) && pointsDetails.getWinningPoints().length() > 1)
                .claimTime(Objects.nonNull(pointsDetails.getIsClaimedTime()) ? pointsDetails.getIsClaimedTime().toString() : "")
                .totalTicketValue(pointsDetails.getTotalPoints())
                .isDeleted(pointsDetails.isDeleted())
                .isPrinted(pointsDetails.isPrinted())
                .build();
    }

    public RetailerResponse getMyBalance(String retailerId) {
        if (Objects.isNull(retailerId) || retailerId.isEmpty()) {
            throw new ResourceNotFoundException("retailer is empty or null", 36);
        }
        final Retailer retailer = retailerRepository.getById(retailerId);
        if (Objects.isNull(retailer)) {
            throw new ResourceNotFoundException("retailer is not found in system", 37);
        }
        return RetailerResponse.builder().retailId(retailerId).balance(retailer.getBalance()).
                isAdmin(Integer.parseInt(retailerId) == 100).build();
    }

    public List<DrawDetailsReportResponse> getDrawDetails(String retailerId, String date) {
        if (Objects.isNull(date) || date.isEmpty()) {
            throw new ResourceNotFoundException("date is empty or null", 38);
        }
        if (Objects.isNull(retailerId) || retailerId.isEmpty()) {
            throw new ResourceNotFoundException("retailId is empty or null", 39);
        }
        final List<PointsDetails> pointsDetails = pointPlayRepository.findByRetailerIdAndCreationDate(retailerId, atStartOfDay(date), atEndOfDay(date));
        Collections.sort(pointsDetails, Comparator.comparing(PointsDetails::getDrawTime));
        List<DrawDetailsReportResponse> responses = new ArrayList<>();
        int i = 1;
        for (PointsDetails pointsDetail : pointsDetails) {
            DrawDetailsReportResponse reportResponse = new DrawDetailsReportResponse();
            reportResponse.setId(i++);
            reportResponse.setDraw(pointsDetail.getDrawTime());
            reportResponse.setSetPoints(betPoints(pointsDetail.getPoints()));
            reportResponse.setWonPoints(winningPoints(pointsDetail.getWinningPoints()));
            reportResponse.setBetCount(betCount(pointsDetail.getPoints()));
            reportResponse.setWinCount(winningCount(pointsDetail.getWinningPoints(), reportResponse));
            responses.add(reportResponse);
        }
        return responses;
    }

    public List<RetailerTicketsReportResponse> retailerTickets(String retailerId, String date) {
        if (Objects.isNull(date) || date.isEmpty()) {
            throw new ResourceNotFoundException("date is empty or null", 40);
        }
        if (Objects.isNull(retailerId) || retailerId.isEmpty()) {
            throw new ResourceNotFoundException("retailsId is empty or null", 41);
        }
        final List<PointsDetails> pointsDetails = pointPlayRepository.findByRetailerIdAndCreationDate(retailerId, atStartOfDay(date), atEndOfDay(date));
        Collections.sort(pointsDetails, Comparator.comparing(PointsDetails::getDrawTime));
        List<RetailerTicketsReportResponse> responses = new ArrayList<>();
        int i = 1;
        for (PointsDetails pointsDetail : pointsDetails) {
            RetailerTicketsReportResponse reportResponse = new RetailerTicketsReportResponse();
            reportResponse.setId(i++);
            reportResponse.setRetailerId(pointsDetail.getRetailId());
            reportResponse.setDraw(pointsDetail.getDrawTime());
            reportResponse.setTicketid(pointsDetail.getTicketId());
            reportResponse.setClaimed(Objects.nonNull(pointsDetail.getIsClaimed()) && pointsDetail.getIsClaimed() == 1);
            reportResponse.setClaimedTime(Objects.isNull(pointsDetail.getIsClaimedTime()) ? "" : pointsDetail.getIsClaimedTime().toString());
            reportResponse.setSetPoints(betPoints(pointsDetail.getPoints()));
            reportResponse.setWonPoints(winningPoints(pointsDetail.getWinningPoints()));
            responses.add(reportResponse);
        }
        return responses;
    }

    private int betPoints(String points) {
        if (Objects.isNull(points))
            return 0;
        final List<Points> betPoints = parsePoints(points);
        if (Objects.isNull(betPoints))
            return 0;
        return betPoints.stream().map(betPoint ->
                        betPoint.getPoints().values().stream().mapToInt(Integer::intValue).sum() * betPoint.getPricePerPoint()).
                collect(Collectors.summingInt(Integer::intValue));
    }

    private int betCount(String points) {
        if (Objects.isNull(points))
            return 0;
        final List<Points> betPoints = parsePoints(points);
        if (Objects.isNull(betPoints))
            return 0;
        return betPoints.stream().map(betPoint ->
                        betPoint.getPoints().values().stream().mapToInt(Integer::intValue).sum()).
                collect(Collectors.summingInt(Integer::intValue));
    }

    private Double winningPoints(String winningPoint) {
        if (Objects.isNull(winningPoint)) return 0.0;
        try {
            log.info("winningPoint :{}", winningPoint);
            List<WinningPoint> wonPoints = objectMapper.readValue(winningPoint, new TypeReference<List<WinningPoint>>() {
            });
            return wonPoints.stream().mapToDouble(point -> point.getWinningPrize()).sum();
        } catch (JsonProcessingException e) {
            log.error("winningPoints: {}", winningPoint);
            throw new ResourceNotFoundException("issue in parsing json winning point", 42);
        }
    }

    private int winningCount(String winningPoint, DrawDetailsReportResponse reportResponse) {
        if (Objects.isNull(winningPoint)) return 0;
        try {
            log.info("winningPoint :{}", winningPoint);
            List<WinningPoint> wonPoints = objectMapper.readValue(winningPoint, new TypeReference<List<WinningPoint>>() {
            });
            if (Objects.nonNull(wonPoints))
                reportResponse.setWinNumbers(wonPoints.stream().map(num -> num.getNum()).collect(Collectors.toList()));
            return wonPoints.size();
        } catch (JsonProcessingException e) {
            log.error("winningCount: {}", winningPoint);
            throw new ResourceNotFoundException("issue in parsing json winningCount", 43);
        }
    }

    List<Points> parsePoints(String points) {
        try {
            return objectMapper.readValue(points, new TypeReference<ArrayList<Points>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("parsePoints: {}", points);
            throw new ResourceNotFoundException("issue in parsing json", 44);
        }
    }
}
