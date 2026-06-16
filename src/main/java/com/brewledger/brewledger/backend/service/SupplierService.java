package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.supplier.CreateSupplierRequest;
import com.brewledger.brewledger.backend.dto.supplier.SupplierResponse;
import com.brewledger.brewledger.backend.dto.supplier.UpdateSupplierRequest;
import com.brewledger.brewledger.backend.entity.Supplier;
import com.brewledger.brewledger.backend.exception.BusinessException;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.repository.IngredientRepository;
import com.brewledger.brewledger.backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository repository;
    private final IngredientRepository ingredientRepository;
    private final ActivityLogService activityLogService;

    @Transactional
    public SupplierResponse create(CreateSupplierRequest request) {

        if (repository.existsByName(request.getName())) {
            throw new BusinessException(
                    "Supplier dengan nama '" + request.getName() + "' sudah ada"
            );
        }

        Supplier supplier = new Supplier();
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());

        Supplier saved = repository.save(supplier);
        activityLogService.record("CREATE_SUPPLIER", 
                "Created supplier: " + saved.getName(),
                "SUPPLIER", saved.getId());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> findAll() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public SupplierResponse update(Long id, UpdateSupplierRequest request) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier tidak ditemukan dengan ID: " + id
                ));

        if (repository.existsByNameAndIdNot(request.getName(), id)) {
            throw new BusinessException(
                    "Supplier dengan nama '" + request.getName() + "' sudah ada"
            );
        }

        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setActive(request.getActive());

        Supplier saved = repository.save(supplier);
        activityLogService.record("UPDATE_SUPPLIER", 
                "Updated supplier: " + saved.getName(),
                "SUPPLIER", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier tidak ditemukan dengan ID: " + id
                ));

        if (ingredientRepository.existsBySupplierId(id)) {
            throw new BusinessException(
                    "Supplier tidak dapat dihapus karena masih digunakan oleh ingredient"
            );
        }

        repository.delete(supplier);
        activityLogService.record("DELETE_SUPPLIER", 
                "Deleted supplier: " + supplier.getName(),
                "SUPPLIER", supplier.getId());
    }

    @Transactional(readOnly = true)
    public SupplierResponse findById(Long id) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier tidak ditemukan dengan ID: " + id
                ));
        return mapToResponse(supplier);
    }

    @Transactional
    public SupplierResponse toggleActive(Long id, boolean active) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier tidak ditemukan dengan ID: " + id
                ));

        supplier.setActive(active);
        Supplier saved = repository.save(supplier);
        activityLogService.record("TOGGLE_SUPPLIER_ACTIVE", 
                "Toggled active status of supplier: " + saved.getName() + " to " + active,
                "SUPPLIER", saved.getId());
        return mapToResponse(saved);
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getAddress(),
                supplier.getActive()
        );
    }
}
