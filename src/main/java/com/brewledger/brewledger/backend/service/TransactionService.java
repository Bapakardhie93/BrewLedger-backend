package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.transaction.CreateTransactionItemRequest;
import com.brewledger.brewledger.backend.dto.transaction.CreateTransactionRequest;
import com.brewledger.brewledger.backend.dto.transaction.TransactionItemResponse;
import com.brewledger.brewledger.backend.dto.transaction.TransactionResponse;
import com.brewledger.brewledger.backend.entity.*;
import com.brewledger.brewledger.backend.enums.PaymentStatus;
import com.brewledger.brewledger.backend.exception.BusinessException;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final double TAX_RATE = PosService.TAX_RATE;

    private final TransactionRepository transactionRepository;
    private final TransactionItemRepository transactionItemRepository;
    private final ProductRepository productRepository;
    private final ProductRecipeRepository productRecipeRepository;
    private final IngredientRepository ingredientRepository;
    private final StockMovementRepository stockMovementRepository;
    private final CurrentUserService currentUserService;

    /**
     * Creates a new transaction, deducts ingredient stock based on recipes,
     * and records stock movements. The entire operation is atomic.
     *
     * @param request the transaction data including items
     * @return the created transaction response
     * @throws BusinessException         if the items list is empty or ingredient stock is insufficient
     * @throws ResourceNotFoundException if a product is not found
     */
    @Transactional
    public TransactionResponse create(
            CreateTransactionRequest request
    ) {
        // Bug Fix #2: Validate that items are not empty
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Transaksi harus memiliki minimal 1 item");
        }

        Transaction transaction = new Transaction();

        transaction.setTransactionNumber(
                "TRX-" + System.currentTimeMillis()
        );

        transaction.setCashier(currentUserService.requireCurrentUser());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setPaymentStatus(PaymentStatus.PAID);
        transaction.setNotes(request.getNotes());
        transaction.setSubtotal(0.0);
        transaction.setTax(0.0);
        transaction.setTotal(0.0);

        transactionRepository.save(transaction);

        double subtotal = 0.0;
        List<TransactionItemResponse> itemResponses = new ArrayList<>();

        for (CreateTransactionItemRequest itemRequest : request.getItems()) {

            // Bug Fix #7: Use ResourceNotFoundException for 404
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Produk tidak ditemukan dengan ID: " + itemRequest.getProductId()
                    ));

            double itemSubtotal = product.getSellingPrice() * itemRequest.getQuantity();
            subtotal += itemSubtotal;

            TransactionItem item = new TransactionItem();
            item.setTransaction(transaction);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(product.getSellingPrice());
            item.setSubtotal(itemSubtotal);

            transactionItemRepository.save(item);

            List<ProductRecipe> recipes = productRecipeRepository.findByProductId(product.getId());

            for (ProductRecipe recipe : recipes) {

                Ingredient ingredient = recipe.getIngredient();
                double qtyNeeded = recipe.getQuantityRequired() * itemRequest.getQuantity();
                double stockBefore = ingredient.getCurrentStock();

                // Bug Fix #1: This check is now safe because @Transactional will rollback
                //             all changes if this exception is thrown
                if (stockBefore < qtyNeeded) {
                    // Bug Fix #7: Use BusinessException (422) instead of RuntimeException (500)
                    throw new BusinessException(
                            "Stok tidak cukup untuk bahan: " + ingredient.getName()
                                    + ". Tersedia: " + stockBefore + ", Dibutuhkan: " + qtyNeeded
                    );
                }

                double stockAfter = stockBefore - qtyNeeded;
                ingredient.setCurrentStock(stockAfter);
                ingredientRepository.save(ingredient);

                StockMovement movement = new StockMovement();
                movement.setIngredient(ingredient);
                movement.setQuantity(qtyNeeded);
                movement.setStockBefore(stockBefore);
                movement.setStockAfter(stockAfter);
                movement.setMovementType("SALE");
                movement.setReferenceNumber(transaction.getTransactionNumber());
                movement.setMovementDate(LocalDateTime.now());

                stockMovementRepository.save(movement);
            }

            itemResponses.add(new TransactionItemResponse(
                    product.getId(),
                    product.getName(),
                    itemRequest.getQuantity(),
                    product.getSellingPrice(),
                    itemSubtotal
            ));
        }

        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;

        transaction.setSubtotal(subtotal);
        transaction.setTax(tax);
        transaction.setTotal(total);

        transactionRepository.save(transaction);

        log.info("Transaction created: {}, total: {}", transaction.getTransactionNumber(), total);

        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionNumber(),
                subtotal,
                tax,
                total,
                itemResponses
        );
    }

    /**
     * Returns all transactions with their items loaded.
     *
     * @return list of transaction responses
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll() {

        return transactionRepository.findAll()
                .stream()
                .map(transaction -> {
                    // Bug Fix #6: Load items for each transaction instead of returning empty list
                    List<TransactionItemResponse> items = transactionItemRepository
                            .findByTransactionId(transaction.getId())
                            .stream()
                            .map(item -> new TransactionItemResponse(
                                    item.getProduct().getId(),
                                    item.getProductName(),
                                    item.getQuantity(),
                                    item.getUnitPrice(),
                                    item.getSubtotal()
                            ))
                            .toList();

                    return new TransactionResponse(
                            transaction.getId(),
                            transaction.getTransactionNumber(),
                            transaction.getSubtotal(),
                            transaction.getTax(),
                            transaction.getTotal(),
                            items
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse findById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaksi tidak ditemukan dengan ID: " + id
                ));

        List<TransactionItemResponse> items = transactionItemRepository
                .findByTransactionId(transaction.getId())
                .stream()
                .map(item -> new TransactionItemResponse(
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionNumber(),
                transaction.getSubtotal(),
                transaction.getTax(),
                transaction.getTotal(),
                items
        );
    }
}
