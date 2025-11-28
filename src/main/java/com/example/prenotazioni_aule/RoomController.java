package com.example.prenotazioni_aule;

import com.example.prenotazioni_aule.model.Booking;
import com.example.prenotazioni_aule.service.BookingService;
import com.example.prenotazioni_aule.service.XacmlService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class RoomController {

    private final BookingService bookingService;
    private final XacmlService xacmlService;

    public RoomController(BookingService bookingService, XacmlService xacmlService) {
        this.bookingService = bookingService;
        this.xacmlService = xacmlService;
    }

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATORS"));

            if (isAdmin) {
                return "redirect:/admin";
            } else {
                return "redirect:/user";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/user")
    public String userArea(Principal principal, Model model) {
        String username = principal.getName();
        model.addAttribute("username", username);
        model.addAttribute("rooms", bookingService.getAllRooms());
        model.addAttribute("myBookings", bookingService.getBookingsByUser(username));
        return "user";
    }

    @PostMapping("/user/book")
    public String bookRoom(Principal principal,
                           @RequestParam Long roomId,
                           @RequestParam String date,
                           Model model) {
        String username = principal.getName();
        try {
            LocalDate localDate = LocalDate.parse(date);
            bookingService.createBooking(username, roomId, localDate);
            return "redirect:/user";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("rooms", bookingService.getAllRooms());
            model.addAttribute("myBookings", bookingService.getBookingsByUser(username));
            return "user";
        }
    }

    @GetMapping("/admin")
    public String adminArea(Model model) {
        List<Booking> all = bookingService.getAllBookings();
        model.addAttribute("bookings", all);
        return "admin";
    }

    @PostMapping("/admin/delete/{id}")
    public String deleteBooking(@PathVariable Long id, Authentication authentication) {

        // 1. Recupera ruolo
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATORS"));

        String userRoleToCheck = isAdmin ? "ROLE_ADMINISTRATORS" :
                authentication.getAuthorities().stream().findFirst().get().getAuthority();

        //System.out.println("XACML REQUEST -> Ruolo: " + userRoleToCheck + ", Azione: delete, Risorsa: booking");

        // 2. Chiedi al PDP (XACML)
        boolean isAuthorized = xacmlService.evaluate(userRoleToCheck, "booking", "delete");

        System.out.println("XACML RESPONSE -> Autorizzato: " + isAuthorized);

        // 3. Applica decisione
        if (!isAuthorized) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "XACML DENY: Accesso Negato");
        }

        bookingService.deleteBooking(id);
        return "redirect:/admin";
    }
}