package com.hospitality.mis.guest.adapter;

import com.hospitality.mis.guest.application.GuestStore;
import com.hospitality.mis.guest.domain.Guest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** JPA adapter for the canonical guest entity. */
@Repository
public class JpaGuestStore implements GuestStore {
    private final GuestRepository repository;

    public JpaGuestStore(GuestRepository repository) {
        this.repository = repository;
    }

    @Override
    public Guest newGuest() {
        return new Guest();
    }

    @Override
    public Guest save(Guest guest) {
        return repository.saveAndFlush(guest);
    }

    @Override
    public Optional<Guest> findById(Long id) {
        return repository.findById(id).map(guest -> guest);
    }

    @Override
    public List<Guest> findAll() {
        return repository.findAllOrdered().stream().map(guest -> (Guest) guest).toList();
    }

    @Override
    public List<Guest> search(String query) {
        return repository.search(query).stream().map(guest -> (Guest) guest).toList();
    }
}
