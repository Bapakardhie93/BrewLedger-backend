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

        repository.save(supplier);

        return mapToResponse(supplier);
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

        return mapToResponse(repository.save(supplier));
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
