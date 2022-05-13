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

    @Query(value = "select * from retailer_audit where retail_id = ?1 and ticket_id = 'PORTAL_UPDATE' order by creation_time desc limit ?2", nativeQuery = true)
    List<RetailerAudit> getLastXtxn(String retailId, int limit);
}
