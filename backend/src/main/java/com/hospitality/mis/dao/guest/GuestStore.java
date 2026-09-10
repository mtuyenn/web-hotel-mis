package com.hospitality.mis.dao.guest;



import com.hospitality.mis.entity.guest.Guest;



import java.util.List;

import java.util.Optional;



/** Persistence port used by guest application services. */

public interface GuestStore {

    Guest newGuest();



    Guest save(Guest guest);



    /**
     * Finds a guest in the hotel-wide shared registry.
     *
     * The scope is explicit in the port because guest records are not owned
     * by their creator.
     */
    Optional<Guest> findSharedById(Long id);

    Optional<Guest> findByPhone(String phone);

    Optional<Guest> findByIdentityNumber(String identityNumber);

    /** Returns every guest in the hotel-wide shared registry. */
    List<Guest> findAllShared();

    /** Searches the hotel-wide shared registry. */
    List<Guest> searchShared(String query);
}
