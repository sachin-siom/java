package com.games.repository;

import com.games.model.PointsDetails;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointPlayRepository extends JpaRepository<PointsDetails, String> {

    @Query("SELECT p from PointsDetails p where p.ticketId =:ticketId ")
    PointsDetails findByTicketId(@Param("ticketId") String ticketId);

    @Query("SELECT p from PointsDetails p where p.retailId = ?1 and p.creationTime between ?2 and ?3 ")
    List<PointsDetails> findByRetailerIdAndCreationDate(
            @Param("retailId") String retailerId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("SELECT p from PointsDetails p where p.creationTime between ?1 and ?2 ")
    List<PointsDetails> findByCreationDate(LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("SELECT p from PointsDetails p where p.drawTime = ?1 and p.creationTime between ?2 and ?3 ")
    List<PointsDetails> getByDrawTime(String drawTime, LocalDateTime startDateTime, LocalDateTime endDateTime);

}
