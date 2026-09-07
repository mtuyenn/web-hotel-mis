package com.hospitality.mis.guest.adapter;

import com.hospitality.mis.guest.domain.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for the canonical guest owner.
 */
public interface GuestRepository extends JpaRepository<Guest, Long> {
    @Query("select g from Guest g where lower(g.fullName) like lower(concat('%', :q, '%')) " +
            "or g.identityNumber like concat('%', :q, '%') or g.phone like concat('%', :q, '%') " +
            "order by g.fullName asc, g.id asc")
    List<Guest> search(@Param("q") String query);

    @Query("select g from Guest g order by g.fullName asc, g.id asc")
    List<Guest> findAllOrdered();
}
