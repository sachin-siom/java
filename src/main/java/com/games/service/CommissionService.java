package com.games.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.games.exception.ResourceNotFoundException;
import com.games.model.PointsDetails;
import com.games.model.Retailer;
import com.games.model.RetailerAudit;
import com.games.model.RetailerDailyReport;
import com.games.payload.CommissionReportResponse;
import com.games.payload.CommissionResponse;
import com.games.payload.WinningDetails;
import com.games.repository.PointPlayRepository;
import com.games.repository.RetailerAuditRepository;
import com.games.repository.RetailerDailyReportRepository;
import com.games.repository.RetailerRepository;
import com.games.util.GameUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.games.util.GameUtil.*;

@Slf4j
@Service
public class CommissionService {

    @Autowired
    RetailerDailyReportRepository retailerDailyReportRepository;

    @Autowired
    private RetailerAuditRepository retailerAuditRepository;

    @Autowired
    private RetailerRepository retailerRepository;

    @Autowired
    private PointPlayRepository pointPlayRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public RetailerDailyReport getTodaysReport(String retailId) {
        if(Objects.isNull(retailId)){
            throw new ResourceNotFoundException("Retails id is null", 21);
        }
        Optional<Retailer> retailerOptional = retailerRepository.findById(retailId);
        if(retailerOptional.isPresent()){
            return prepareDayWiseReport(currentDate(), retailId, retailerOptional.get().getProfitPercentage());
        } else {
            throw new ResourceNotFoundException("Retails id not found", 22);
        }
    }

    public void getTodaysAllRetailersReport() {
        List<Retailer> retailers = retailerRepository.findAll();
        for (Retailer retailer : retailers) {
            try {
                if (Integer.parseInt(retailer.getRetailId()) != 1) {
                    RetailerDailyReport retailerDailyReport = prepareDayWiseReport(currentDate(), retailer.getRetailId(), retailer.getProfitPercentage());
                    retailerDailyReportRepository.save(retailerDailyReport);
                }
            } catch (Exception e) {
                log.error("problem in preparing the daily report for retail id: {}", retailer.getRetailId(), e);
            }
        }
    }

    public List<RetailerDailyReport> getCounterRetailersReport(String retailId, LocalDate fromDate, LocalDate toDate) {
        return retailerDailyReportRepository.getAuditData(retailId, getDate(fromDate.toString()), getDate(toDate.toString()));
    }

    public RetailerDailyReport prepareDayWiseReport(String date, String retailsId, double commission){
        List<RetailerAudit> auditData = retailerAuditRepository.getAuditData(retailsId, atStartOfDay(date), atEndOfDay(date));
        double playAmount = 0.0;
        double winAmount = 0.0;
        for (RetailerAudit retailerAudit : auditData) {
           if(Objects.nonNull(retailerAudit.getTicketId()) && PORTAL_UPDATE.equalsIgnoreCase(retailerAudit.getTicketId())) {
               log.info("TicketId :{} retailId:{} date:{}", retailerAudit.getTicketId(), retailsId, date);
                continue;
           }
            if(retailerAudit.getCreditor() == 3) {
                log.info("TicketId :{} retailId:{} date:{} deducting from play amount as ticket is deleted", retailerAudit.getTicketId(), retailsId, date);
                playAmount -= retailerAudit.getAmount();
                continue;
            }
           // ticket money debited
           if(Objects.equals(retailerAudit.getIsCredit(), 0)){
               playAmount += retailerAudit.getAmount();
           }
            if(Objects.equals(retailerAudit.getIsCredit(), 1)){
                winAmount += retailerAudit.getAmount();
            }
        }
        double commissionAmt = (commission/100.0) * playAmount;
        return RetailerDailyReport.builder().retailId(retailsId).playAmount(playAmount).winAmount(winAmount).commission(commission).commissionAmt(commissionAmt).
                date(LocalDate.now()).build();
    }

    public CommissionReportResponse commissionReport(String retailId, String fdate, String tdate) {
        final List<PointsDetails> pointsDetails = pointPlayRepository.findByRetailerIdAndCreationDate(retailId, atStartOfDay(fdate), atEndOfDay(tdate));
        return commissionReport(pointsDetails);
    }

    public CommissionReportResponse commissionReport(String fdate, String tdate){
        final List<PointsDetails> pointsDetails = pointPlayRepository.findByCreationDate(1, atStartOfDay(fdate), atEndOfDay(tdate));
        return commissionReport(pointsDetails);
    }

    public CommissionReportResponse commissionReport(List<PointsDetails> pointsDetails) {
        Map<String, CommissionResponse> responseHashMap = new HashMap<>();
        for (PointsDetails pointsDetail : pointsDetails) {
            CommissionResponse retailerData = responseHashMap.getOrDefault(pointsDetail.getRetailId(), new CommissionResponse());
            if(responseHashMap.containsKey(pointsDetail.getRetailId())){
                retailerData.setPointsWon(getWinnerPoints(pointsDetail) + retailerData.getPointsWon());
                retailerData.setTotalPointsPlayed(pointsDetail.getTotalPoints() + retailerData.getTotalPointsPlayed());
            } else {
                retailerData.setRetailId(pointsDetail.getRetailId());
                retailerData.setTotalPointsPlayed(pointsDetail.getTotalPoints());
                retailerData.setPointsWon(getWinnerPoints(pointsDetail));
                retailerData.setTotalPointsPlayed(pointsDetail.getTotalPoints());
            }
            responseHashMap.put(pointsDetail.getRetailId(), retailerData);
        }
        for (Map.Entry<String, CommissionResponse> entry : responseHashMap.entrySet()) {
            Retailer byId = retailerRepository.getById(entry.getKey());
            Double commission = entry.getValue().getTotalPointsPlayed() * (byId.getProfitPercentage() / 100.0f);
            entry.getValue().setCommission(commission);
            entry.getValue().setCommissionPercentage((int)byId.getProfitPercentage());
            if(entry.getValue().getPointsWon() > 0.0){
                double remainBal = subtract(entry.getValue().getTotalPointsPlayed(), entry.getValue().getPointsWon());
                entry.getValue().setAdminProfit(subtract(remainBal, commission));
            } else {
                entry.getValue().setAdminProfit(subtract(entry.getValue().getTotalPointsPlayed(), commission));
            }
        }
        CommissionReportResponse reportResponse = new CommissionReportResponse();
        List<CommissionResponse> commissionResponses = responseHashMap.values().stream().collect(Collectors.toList());
        reportResponse.setCommissionResponseList(commissionResponses);
        for (CommissionResponse commissionRespons : commissionResponses) {
            reportResponse.setTotalPointsPlayed(reportResponse.getTotalPointsPlayed() + commissionRespons.getTotalPointsPlayed());
            reportResponse.setTotalPointsWon(reportResponse.getTotalPointsWon() + commissionRespons.getPointsWon());
            reportResponse.setTotalCommission(reportResponse.getTotalCommission() + commissionRespons.getCommission());
            reportResponse.setTotalProfit(reportResponse.getTotalProfit() + commissionRespons.getAdminProfit());
        }
        return reportResponse;
    }

    private double getWinnerPoints(PointsDetails pointsDetail){
        if(pointsDetail.getIsWinner() == 1) {
            WinningDetails winningDetails = null;
            try {
                winningDetails = objectMapper.readValue(pointsDetail.getWinningPoints(), WinningDetails.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return winningDetails.getWinningNums().values().stream().mapToDouble(i -> i).sum();
        }
        return 0.0;
    }

}
