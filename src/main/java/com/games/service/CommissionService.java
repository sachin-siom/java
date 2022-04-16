package com.games.service;

import com.games.exception.ResourceNotFoundException;
import com.games.model.Retailer;
import com.games.model.RetailerAudit;
import com.games.model.RetailerDailyReport;
import com.games.repository.RetailerAuditRepository;
import com.games.repository.RetailerDailyReportRepository;
import com.games.repository.RetailerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    public RetailerDailyReport getTodaysReport(String retailId){
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

}
