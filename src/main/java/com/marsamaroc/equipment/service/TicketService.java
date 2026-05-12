package com.marsamaroc.equipment.service;

import com.marsamaroc.equipment.dto.TicketDTO;
import com.marsamaroc.equipment.model.entity.Ticket;
import com.marsamaroc.equipment.model.entity.User;
import com.marsamaroc.equipment.repository.TicketRepository;
import com.marsamaroc.equipment.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {
    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;

    public TicketService(TicketRepository ticketRepo, UserRepository userRepo, EmailService emailService) {
        this.ticketRepo = ticketRepo;
        this.userRepo = userRepo;
        this.emailService = emailService;
    }

    public TicketDTO createTicket(Ticket ticket) {
        ticket.setStatus("OUVERT");
        Ticket saved = ticketRepo.save(ticket);

        List<User> technicians = userRepo.findByRole("TECHNICIEN");
        List<String> techEmails = technicians.stream()
            .map(User::getEmail)
            .filter(e -> e != null && !e.isEmpty())
            .collect(Collectors.toList());
        emailService.notifyTechnicians(saved, techEmails);

        return toDTO(saved);
    }

    public List<TicketDTO> getAllTickets() {
        return ticketRepo.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toDTO).collect(Collectors.toList());
    }

    public List<TicketDTO> getMyTickets(Long requesterId) {
        return ticketRepo.findByRequesterIdOrderByCreatedAtDesc(requesterId).stream()
            .map(this::toDTO).collect(Collectors.toList());
    }

    public TicketDTO getTicket(Long id) {
        return ticketRepo.findById(id).map(this::toDTO).orElse(null);
    }

    public TicketDTO updateStatus(Long id, String newStatus) {
        Ticket ticket = ticketRepo.findById(id).orElse(null);
        if (ticket == null) return null;

        ticket.setStatus(newStatus);
        if ("CLOTURE".equals(newStatus)) {
            ticket.setClosedAt(LocalDateTime.now());
        }
        Ticket saved = ticketRepo.save(ticket);
        return toDTO(saved);
    }

    private TicketDTO toDTO(Ticket t) {
        TicketDTO dto = new TicketDTO();
        dto.setId(t.getId());
        dto.setTitle(t.getTitle());
        dto.setEquipmentType(t.getEquipmentType());
        dto.setEquipmentName(t.getEquipmentName());
        dto.setPriority(t.getPriority());
        dto.setInterventionType(t.getInterventionType());
        dto.setDescription(t.getDescription());
        dto.setStatus(t.getStatus());
        dto.setRequesterId(t.getRequesterId());
        dto.setRequesterName(t.getRequesterName());
        dto.setRequesterEmail(t.getRequesterEmail());
        dto.setTechnicianEmail(t.getTechnicianEmail());
        if (t.getCreatedAt() != null) dto.setCreatedAt(t.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        if (t.getUpdatedAt() != null) dto.setUpdatedAt(t.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        if (t.getClosedAt() != null) dto.setClosedAt(t.getClosedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        return dto;
    }
}
