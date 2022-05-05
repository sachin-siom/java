package com.games.repository;

import com.games.model.Retailer;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RetailerRepository extends JpaRepository<Retailer, String>  {
    @Lock(LockModeType.PESSIMISTIC_READ)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    public Optional<Retailer> findById(String retailId);

    @Transactional
    @Modifying
    @Query("update Retailer r set r.includeNumbers =:includeNumber where r.retailId =:retailId")
    void includeWiningNumberByAdmin(String includeNumber, String retailId);

    @Transactional
    @Modifying
    @Query("update Retailer r set r.balance =:balance + r.balance where r.retailId =:retailId")
    void updateBalance(double balance, String retailId);

    @Query("Select r from Retailer r where r.retailId !=:retailId ")
    List<Retailer> selectRetailers(String retailId);
}
