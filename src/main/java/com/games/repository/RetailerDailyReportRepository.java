package com.games.repository;

import com.games.model.RetailerAudit;
import com.games.model.RetailerDailyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RetailerDailyReportRepository extends JpaRepository<RetailerDailyReport, Integer> {
    @Query("SELECT p from RetailerDailyReport p where p.retailId = ?1 and p.date between ?2 and ?3 ")
    List<RetailerDailyReport> getAuditData(String retailId, LocalDate atStartOfDay, LocalDate atEndOfDay);
}
