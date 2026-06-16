package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.table.CreateTableRequest;
import com.brewledger.brewledger.backend.dto.table.TableResponse;
import com.brewledger.brewledger.backend.dto.table.UpdateTableRequest;
import com.brewledger.brewledger.backend.dto.table.UpdateTableStatusRequest;
import com.brewledger.brewledger.backend.entity.RestaurantTable;
import com.brewledger.brewledger.backend.enums.TableStatus;
import com.brewledger.brewledger.backend.exception.BusinessException;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepository;

    @Transactional
    public TableResponse create(CreateTableRequest request) {
        if (restaurantTableRepository.existsByNumber(request.getNumber())) {
            throw new BusinessException("Nomor meja sudah terdaftar: " + request.getNumber());
        }

        RestaurantTable table = new RestaurantTable();
        table.setNumber(request.getNumber());
        table.setCapacity(request.getCapacity());
        table.setLocation(request.getLocation());
        table.setStatus(TableStatus.AVAILABLE);

        restaurantTableRepository.save(table);
        log.info("Restaurant table created: {}", table.getNumber());
        return mapToResponse(table);
    }

    @Transactional
    public TableResponse update(Long id, UpdateTableRequest request) {
        RestaurantTable table = restaurantTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meja tidak ditemukan dengan ID: " + id));

        if (restaurantTableRepository.existsByNumberAndIdNot(request.getNumber(), id)) {
            throw new BusinessException("Nomor meja sudah terdaftar: " + request.getNumber());
        }

        table.setNumber(request.getNumber());
        table.setCapacity(request.getCapacity());
        table.setLocation(request.getLocation());

        restaurantTableRepository.save(table);
        log.info("Restaurant table updated: {}", table.getNumber());
        return mapToResponse(table);
    }

    @Transactional
    public TableResponse updateStatus(Long id, UpdateTableStatusRequest request) {
        RestaurantTable table = restaurantTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meja tidak ditemukan dengan ID: " + id));

        table.setStatus(request.getStatus());
        restaurantTableRepository.save(table);
        log.info("Restaurant table status updated to: {} for table: {}", request.getStatus(), table.getNumber());
        return mapToResponse(table);
    }

    @Transactional
    public void delete(Long id) {
        RestaurantTable table = restaurantTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meja tidak ditemukan dengan ID: " + id));
        restaurantTableRepository.delete(table);
        log.info("Restaurant table deleted ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<TableResponse> findAll() {
        return restaurantTableRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TableResponse findById(Long id) {
        RestaurantTable table = restaurantTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meja tidak ditemukan dengan ID: " + id));
        return mapToResponse(table);
    }

    private TableResponse mapToResponse(RestaurantTable table) {
        return new TableResponse(
                table.getId(),
                table.getNumber(),
                table.getCapacity(),
                table.getLocation(),
                table.getStatus().name()
        );
    }
}
