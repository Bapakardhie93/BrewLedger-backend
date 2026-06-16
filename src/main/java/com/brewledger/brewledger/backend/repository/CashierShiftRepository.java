package com.brewledger.brewledger.backend.repository;

import com.brewledger.brewledger.backend.entity.CashierShift;
import com.brewledger.brewledger.backend.enums.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CashierShiftRepository extends JpaRepository<CashierShift, Long> {

    Optional<CashierShift> findByCashierIdAndStatus(Long cashierId, ShiftStatus status);

    boolean existsByCashierIdAndStatus(Long cashierId, ShiftStatus status);
}
