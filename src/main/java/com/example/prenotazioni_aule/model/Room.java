package com.example.prenotazioni_aule.model;

public class Room {
    private Long id;
    private String name;
    private int capacity;

    public Room(Long id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }
}
