package com.marsamaroc.equipment.service;

import com.marsamaroc.equipment.model.entity.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void notifyTechnicians(Ticket ticket, List<String> technicianEmails) {
        String subject = "Nouveau ticket IT - " + (ticket.getTitle() != null ? ticket.getTitle() : "Sans titre");
        String body = buildEmailBody(ticket);

        for (String email : technicianEmails) {
            if (email == null || email.isEmpty()) continue;
            try {
                log.info("=== EMAIL SENT TO: {} ===", email);
                log.info("Subject: {}", subject);
                log.info("Body:\n{}", body);
                log.info("=== END EMAIL ===");
            } catch (Exception e) {
                log.error("Failed to send email to {}: {}", email, e.getMessage());
            }
        }
    }

    private String buildEmailBody(Ticket ticket) {
        String statusLabel = ticket.getStatus();
        if ("OUVERT".equals(statusLabel)) statusLabel = "Ouvert";
        else if ("EN_COURS".equals(statusLabel)) statusLabel = "En cours";
        else if ("CLOTURE".equals(statusLabel)) statusLabel = "Cloturé";
        return "NOUVEAU TICKET D'INTERVENTION\n" +
               "===============================\n" +
               "Titre: " + ticket.getTitle() + "\n" +
               "Type d'équipement: " + ticket.getEquipmentType() + "\n" +
               "Nom d'équipement: " + ticket.getEquipmentName() + "\n" +
               "Priorité: " + ticket.getPriority() + "\n" +
               "Statut: " + statusLabel + "\n" +
               "Type d'intervention: " + ticket.getInterventionType() + "\n" +
               "Description: " + ticket.getDescription() + "\n" +
               "Demandeur: " + ticket.getRequesterName() + "\n" +
               "Email: " + ticket.getRequesterEmail() + "\n\n" +
               "Connectez-vous à la plateforme pour gérer ce ticket:\n" +
               "http://localhost:8080\n";
    }
}
