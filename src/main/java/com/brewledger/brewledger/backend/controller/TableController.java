package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.dto.table.CreateTableRequest;
import com.brewledger.brewledger.backend.dto.table.TableResponse;
import com.brewledger.brewledger.backend.dto.table.UpdateTableRequest;
import com.brewledger.brewledger.backend.dto.table.UpdateTableStatusRequest;
import com.brewledger.brewledger.backend.service.RestaurantTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final RestaurantTableService restaurantTableService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGEMENT')")
    public TableResponse create(@Valid @RequestBody CreateTableRequest request) {
        return restaurantTableService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGEMENT')")
    public TableResponse update(@PathVariable Long id, @Valid @RequestBody UpdateTableRequest request) {
        return restaurantTableService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'KASIR')")
    public TableResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateTableStatusRequest request) {
        return restaurantTableService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGEMENT')")
    public void delete(@PathVariable Long id) {
        restaurantTableService.delete(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'KASIR')")
    public List<TableResponse> findAll() {
        return restaurantTableService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'KASIR')")
    public TableResponse findById(@PathVariable Long id) {
        return restaurantTableService.findById(id);
    }
}
