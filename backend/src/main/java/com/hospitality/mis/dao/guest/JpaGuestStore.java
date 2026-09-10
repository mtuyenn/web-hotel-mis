package com.hospitality.mis.dao.guest;



import com.hospitality.mis.dao.guest.GuestStore;

import com.hospitality.mis.entity.guest.Guest;

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

    public Optional<Guest> findSharedById(Long id) {
        return repository.findSharedById(id);
    }

    @Override
    public Optional<Guest> findByPhone(String phone) {
        return repository.findByPhone(phone);
    }

    @Override
    public Optional<Guest> findByIdentityNumber(String identityNumber) {
        return repository.findByIdentityNumber(identityNumber);
    }

    @Override
    public List<Guest> findAllShared() {
        return repository.findAllShared();
    }

    @Override
    public List<Guest> searchShared(String query) {
        return repository.searchShared(query);
    }
}
