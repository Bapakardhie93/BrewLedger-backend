package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.kitchen.KitchenOrderItemResponse;
import com.brewledger.brewledger.backend.dto.kitchen.KitchenOrderResponse;
import com.brewledger.brewledger.backend.dto.kitchen.UpdateKitchenOrderStatusRequest;
import com.brewledger.brewledger.backend.entity.KitchenOrder;
import com.brewledger.brewledger.backend.entity.KitchenOrderItem;
import com.brewledger.brewledger.backend.entity.Transaction;
import com.brewledger.brewledger.backend.entity.TransactionItem;
import com.brewledger.brewledger.backend.enums.KitchenOrderStatus;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.repository.KitchenOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KitchenOrderService {

    private final KitchenOrderRepository kitchenOrderRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public KitchenOrderResponse updateStatus(Long id, UpdateKitchenOrderStatusRequest request) {
        KitchenOrder order = kitchenOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pesanan dapur tidak ditemukan dengan ID: " + id));

        order.setStatus(request.getStatus());
        kitchenOrderRepository.save(order);
        log.info("Kitchen order status updated to: {} for order ID: {}", request.getStatus(), id);
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<KitchenOrderResponse> findAll(String cashier) {
        List<KitchenOrder> orders;
        if (cashier != null && !cashier.trim().isEmpty()) {
            String targetUsername = cashier.trim().equals("current")
                    ? currentUserService.requireCurrentUser().getUsername()
                    : cashier.trim();
            orders = kitchenOrderRepository.findByCashierUsername(targetUsername);
        } else {
            orders = kitchenOrderRepository.findAll();
        }
        return orders.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<KitchenOrderResponse> findAll() {
        return findAll(null);
    }

    @Transactional(readOnly = true)
    public KitchenOrderResponse findById(Long id) {
        KitchenOrder order = kitchenOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pesanan dapur tidak ditemukan dengan ID: " + id));
        return mapToResponse(order);
    }

    @Transactional
    public void createFromTransaction(Transaction transaction, List<TransactionItem> items) {
        KitchenOrder order = new KitchenOrder();
        order.setTransaction(transaction);
        order.setTableNumber(transaction.getTableNumber());
        order.setNotes(transaction.getNotes());
        order.setStatus(KitchenOrderStatus.WAITING);

        List<KitchenOrderItem> orderItems = new ArrayList<>();
        for (TransactionItem txItem : items) {
            KitchenOrderItem orderItem = new KitchenOrderItem();
            orderItem.setKitchenOrder(order);
            orderItem.setProductName(txItem.getProductName());
            orderItem.setQuantity(txItem.getQuantity());
            orderItem.setNotes(txItem.getNotes());
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);

        kitchenOrderRepository.save(order);
        log.info("Kitchen order created automatically for transaction: {}", transaction.getTransactionNumber());
    }

    private KitchenOrderResponse mapToResponse(KitchenOrder order) {
        List<KitchenOrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new KitchenOrderItemResponse(
                        item.getId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getNotes()
                ))
                .toList();

        return new KitchenOrderResponse(
                order.getId(),
                order.getTransaction().getId(),
                order.getTransaction().getTransactionNumber(),
                order.getTableNumber(),
                order.getStatus().name(),
                order.getNotes(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}
