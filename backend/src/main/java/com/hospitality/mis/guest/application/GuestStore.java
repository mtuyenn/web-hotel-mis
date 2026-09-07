package com.hospitality.mis.guest.application;

import com.hospitality.mis.guest.domain.Guest;

import java.util.List;
import java.util.Optional;

/** Persistence port used by guest application services. */
public interface GuestStore {
    Guest newGuest();

    Guest save(Guest guest);

    Optional<Guest> findById(Long id);

    List<Guest> findAll();

    List<Guest> search(String query);
}
