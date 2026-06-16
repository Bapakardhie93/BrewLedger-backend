package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    Optional<RestaurantTable> findByNumber(String number);
    boolean existsByNumber(String number);
    boolean existsByNumberAndIdNot(String number, Long id);
}
