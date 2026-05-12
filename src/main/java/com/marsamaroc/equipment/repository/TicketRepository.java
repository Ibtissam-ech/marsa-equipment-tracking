package com.marsamaroc.equipment.repository;

import com.marsamaroc.equipment.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStatus(String status);
    List<Ticket> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    List<Ticket> findAllByOrderByCreatedAtDesc();
}
