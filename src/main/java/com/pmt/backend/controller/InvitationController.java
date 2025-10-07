package com.pmt.backend.controller;

import com.pmt.backend.dto.InvitationSendRequest;
import com.pmt.backend.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {
    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    public ResponseEntity<Integer> send(@Valid @RequestBody InvitationSendRequest request) {
        Integer id = invitationService.sendInvitation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<Void> accept(@PathVariable Integer id, @RequestParam String email) {
        invitationService.acceptInvitation(id, email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/decline")
    public ResponseEntity<Void> decline(@PathVariable Integer id) {
        invitationService.declineInvitation(id);
        return ResponseEntity.noContent().build();
    }
}
