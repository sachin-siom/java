package com.games.repository;

import com.games.model.Retailer;
import com.games.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserServiceRepository extends JpaRepository<User, Long> {

    @Query("Select u from User u where u.username =:username and u.isEnabled =:isEnabled")
    User findByUsernameAndEnabled(String username, boolean isEnabled);

    @Transactional
    @Modifying
    @Query("update User u set u.password =:password where u.id =:id")
    int updateUserPassword(String password, long id);

    @Transactional
    @Modifying
    @Query("update User u set u.isEnabled =:isEnabled where u.id =:id")
    void disableRetailer(boolean isEnabled, long id);

    @Transactional
    @Modifying
    @Query("update User u set u.isEnabled =:isEnabled where u.id =:id")
    void enabledRetailer(boolean isEnabled, long id);

    @Query("Select u from User u where u.id not in ?1 ")
    List<User> selectUsersExcept(List<Long> id);
}
