package com.example.prenotazioni_aule.service;
import com.example.prenotazioni_aule.model.Booking;
import com.example.prenotazioni_aule.model.Room;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final List<Room> rooms = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private final AtomicLong bookingIdGenerator = new AtomicLong(1);

    public BookingService() {
        // Aule di esempio
        rooms.add(new Room(1L, "Cloud Platforms and Infrastructures as Code", 40));
        rooms.add(new Room(2L, "Network Security", 30));
        rooms.add(new Room(3L, "System Security", 25));
        rooms.add(new Room(4L, "Risk Assessment", 40));
        rooms.add(new Room(5L, "Software Security", 30));
        rooms.add(new Room(6L, "Network and Cloud Infrastuctures", 25));
        rooms.add(new Room(7L, "Machine Learning", 40));
        rooms.add(new Room(8L, "Algorithms and Data Structures", 30));
        rooms.add(new Room(9L, "Web and Real-Time Comunication", 25));
    }

    public List<Room> getAllRooms() {
        return rooms;
    }

    public List<Booking> getAllBookings() {
        return bookings;
    }

    public List<Booking> getBookingsByUser(String username) {
        return bookings.stream()
                .filter(b -> b.getUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }

    public boolean isRoomAvailable(Long roomId, LocalDate date) {
        return bookings.stream()
                .noneMatch(b -> b.getRoom().getId().equals(roomId)
                        && b.getDate().equals(date));
    }

    public Optional<Room> findRoomById(Long id) {
        return rooms.stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    public Optional<Booking> findBookingById(Long id) {
        return bookings.stream().filter(b -> b.getId().equals(id)).findFirst();
    }

    public Booking createBooking(String username, Long roomId, LocalDate date) {
        Room room = findRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Aula non trovata"));

        if (!isRoomAvailable(roomId, date)) {
            throw new IllegalStateException("Aula già prenotata per questa data");
        }

        Booking booking = new Booking(
                bookingIdGenerator.getAndIncrement(),
                username,
                room,
                date
        );
        bookings.add(booking);
        return booking;
    }

    public void deleteBooking(Long bookingId) {
        bookings.removeIf(b -> b.getId().equals(bookingId));
    }
}
