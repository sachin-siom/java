package com.games.repository;

import com.games.model.PointsDetails;
import com.games.model.RetailerAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface RetailerAuditRepository extends JpaRepository<RetailerAudit, String> {
    @Query("SELECT p from RetailerAudit p where p.retailId = ?1 and p.creationTime between ?2 and ?3 ")
    List<RetailerAudit> getAuditData(String retailId, LocalDateTime atStartOfDay, LocalDateTime atEndOfDay);
}
