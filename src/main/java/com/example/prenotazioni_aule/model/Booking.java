package com.example.prenotazioni_aule.model;
import java.time.LocalDate;

public class Booking {
    private Long id;
    private String username;   // in futuro verrà da LDAP/Keycloak
    private Room room;
    private LocalDate date;

    public Booking(Long id, String username, Room room, LocalDate date) {
        this.id = id;
        this.username = username;
        this.room = room;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getDate() {
        return date;
    }
}
