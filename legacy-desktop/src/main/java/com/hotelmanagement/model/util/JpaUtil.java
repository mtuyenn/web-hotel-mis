package com.hotelmanagement.model.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {
    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("hotelPU");

    private JpaUtil(){

    }

    public static EntityManagerFactory getEMF() {
        return EMF;
    }

    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    public static void Shutdown(){
        if(EMF != null && EMF.isOpen()){
            EMF.close();
        }
    }
}
