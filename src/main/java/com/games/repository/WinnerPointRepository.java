package com.games.repository;

import com.games.model.WinnerPointDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface WinnerPointRepository extends JpaRepository<WinnerPointDetails, Long> {
    WinnerPointDetails getByDrawTime(String drawTime);

    WinnerPointDetails findByDrawTimeAndCreationTime(String drawTime, LocalDate creationTime);

    List<WinnerPointDetails> findByCreationTime(LocalDate creationTime);
}
