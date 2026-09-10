package com.hospitality.mis.dao.guest;



import com.hospitality.mis.entity.guest.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;



import java.util.List;
import java.util.Optional;


/**

 * Repository for the canonical guest owner.
 */
public interface GuestRepository extends JpaRepository<Guest, Long> {
    Optional<Guest> findByPhone(String phone);

    Optional<Guest> findByIdentityNumber(String identityNumber);
    @Query("select g from Guest g where g.id = :id")
    Optional<Guest> findSharedById(@Param("id") Long id);

    @Query("select g from Guest g where lower(g.fullName) like lower(concat('%', :q, '%')) " +
            "or g.identityNumber like concat('%', :q, '%') or g.phone like concat('%', :q, '%') " +
            "order by g.fullName asc, g.id asc")
    List<Guest> searchShared(@Param("q") String query);

    @Query("select g from Guest g order by g.fullName asc, g.id asc")
    List<Guest> findAllShared();
}
