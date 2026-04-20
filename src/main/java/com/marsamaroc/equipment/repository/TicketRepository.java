package com.marsamaroc.equipment.repository;

import com.marsamaroc.equipment.model.entity.InterventionTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<InterventionTicket, Long> {
    List<InterventionTicket> findByStatus(String status);
}