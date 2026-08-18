package com.ifpr.backend.controller;

import static com.ifpr.backend.dto.CategoryDtos.*;

import com.ifpr.backend.model.TipoTransacao;
import com.ifpr.backend.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService service;
    public CategoryController(CategoryService service) { this.service = service; }

    @GetMapping
    public List<CategoryResponse> list(@RequestParam Long walletId, @RequestParam(required = false) TipoTransacao type) {
        return service.list(walletId, type);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@RequestParam Long walletId, @Valid @RequestBody CategoryRequest request) {
        return service.create(walletId, request);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@RequestParam Long walletId, @PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return service.update(walletId, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestParam Long walletId, @PathVariable Long id) {
        service.delete(walletId, id);
        return ResponseEntity.noContent().build();
    }
}
