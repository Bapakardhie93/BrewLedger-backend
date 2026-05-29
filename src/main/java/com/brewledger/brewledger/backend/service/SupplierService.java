package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.supplier.CreateSupplierRequest;
import com.brewledger.brewledger.backend.dto.supplier.SupplierResponse;
import com.brewledger.brewledger.backend.entity.Supplier;
import com.brewledger.brewledger.backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository repository;

    public SupplierResponse create(
            CreateSupplierRequest request
    ) {

        if (repository.existsByName(
                request.getName()
        )) {
            throw new RuntimeException(
                    "Supplier sudah ada"
            );
        }

        Supplier supplier =
                new Supplier();

        supplier.setName(request.getName());
        supplier.setContactPerson(
                request.getContactPerson()
        );
        supplier.setPhone(
                request.getPhone()
        );
        supplier.setEmail(
                request.getEmail()
        );
        supplier.setAddress(
                request.getAddress()
        );

        repository.save(supplier);

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

    public List<SupplierResponse> findAll() {

        return repository.findAll()
                .stream()
                .map(supplier ->
                        new SupplierResponse(
                                supplier.getId(),
                                supplier.getName(),
                                supplier.getContactPerson(),
                                supplier.getPhone(),
                                supplier.getEmail(),
                                supplier.getAddress(),
                                supplier.getActive()
                        )
                )
                .toList();
    }
}