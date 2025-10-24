package com.pmt.backend.controller;

import com.pmt.backend.dto.InvitationSendRequest;
import com.pmt.backend.dto.InvitationListItem;
import com.pmt.backend.entity.InvitationStatus;
import com.pmt.backend.entity.ProjectInvitation;
import com.pmt.backend.service.InvitationService;
import com.pmt.backend.exception.InsufficientProjectPermissionException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping
    public ResponseEntity<List<InvitationListItem>> list(
            @RequestParam String email,
            @RequestParam(name = "status", required = false, defaultValue = "PENDING") String status
    ) {
        InvitationStatus st = InvitationStatus.valueOf(status.toUpperCase());
        List<ProjectInvitation> list = invitationService.getInvitationsFor(email, st);
        List<InvitationListItem> items = list.stream().map(inv -> new InvitationListItem(
                inv.getId(),
                inv.getProject().getId(),
                inv.getProject().getName(),
                inv.getEmail(),
                inv.getStatus(),
                inv.getCreatedAt()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(items);
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

    @ExceptionHandler(InsufficientProjectPermissionException.class)
    public ResponseEntity<String> handleForbidden(InsufficientProjectPermissionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
