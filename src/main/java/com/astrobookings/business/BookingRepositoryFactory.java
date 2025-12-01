package com.astrobookings.business;

import com.astrobookings.persistence.BookingRepositoryInMemory;
import com.astrobookings.persistence.BookingRepositoryPort;

public class BookingRepositoryFactory {
    private static BookingRepositoryPort instance;

    public static BookingRepositoryPort getInstance() {
        if (instance == null) {
            instance = new BookingRepositoryInMemory();
        }
        return instance;
    }

    public static void setInstance(BookingRepositoryPort repo) {
        instance = repo;
    }
}
