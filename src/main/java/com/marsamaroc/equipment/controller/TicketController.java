package com.marsamaroc.equipment.controller;

import com.marsamaroc.equipment.dto.TicketDTO;
import com.marsamaroc.equipment.model.entity.Ticket;
import com.marsamaroc.equipment.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/tickets")
    public TicketDTO createTicket(@RequestBody Ticket ticket) {
        return ticketService.createTicket(ticket);
    }

    @GetMapping("/tickets")
    public List<TicketDTO> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/tickets/my")
    public List<TicketDTO> getMyTickets(@RequestParam Long userId) {
        return ticketService.getMyTickets(userId);
    }

    @GetMapping("/tickets/{id}")
    public TicketDTO getTicket(@PathVariable Long id) {
        return ticketService.getTicket(id);
    }

    @PutMapping("/tickets/{id}/status")
    public ResponseEntity<TicketDTO> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null || (!newStatus.equals("EN_COURS") && !newStatus.equals("CLOTURE"))) {
            return ResponseEntity.badRequest().build();
        }
        TicketDTO dto = ticketService.updateStatus(id, newStatus);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }
}
