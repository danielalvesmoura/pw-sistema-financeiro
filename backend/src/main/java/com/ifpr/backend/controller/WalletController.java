package com.ifpr.backend.controller;

import static com.ifpr.backend.dto.WalletDtos.*;

import com.ifpr.backend.service.WalletService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {
    private final WalletService service;

    public WalletController(
        WalletService service
    ) {
        this.service = service;
    }

    @GetMapping
    public List<WalletResponse> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponse create(@Valid @RequestBody WalletRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public WalletResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public WalletResponse update(
        @PathVariable Long id,
        @Valid @RequestBody WalletRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    public List<MemberResponse> members(@PathVariable Long id) {
        return service.listMembers(id);
    }

    @PostMapping("/{id}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse addMember(@PathVariable Long id, @Valid @RequestBody AddMemberRequest request) {
        return service.addMember(id, request);
    }

    @PatchMapping("/{id}/members/{userId}")
    public MemberResponse updateMember(@PathVariable Long id, @PathVariable Long userId, @Valid @RequestBody UpdateMemberRoleRequest request) {
        return service.updateMember(id, userId, request);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        service.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }
}
