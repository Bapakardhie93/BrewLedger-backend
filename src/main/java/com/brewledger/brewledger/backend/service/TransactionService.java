package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.transaction.CreateTransactionItemRequest;
import com.brewledger.brewledger.backend.dto.transaction.CreateTransactionRequest;
import com.brewledger.brewledger.backend.dto.transaction.TransactionItemResponse;
import com.brewledger.brewledger.backend.dto.transaction.TransactionResponse;
import com.brewledger.brewledger.backend.entity.*;
import com.brewledger.brewledger.backend.enums.PaymentStatus;
import com.brewledger.brewledger.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionItemRepository transactionItemRepository;
    private final ProductRepository productRepository;
    private final ProductRecipeRepository productRecipeRepository;
    private final IngredientRepository ingredientRepository;
    private final StockMovementRepository stockMovementRepository;

    public TransactionResponse create(
            CreateTransactionRequest request
    ) {

        Transaction transaction =
                new Transaction();

        transaction.setTransactionNumber(
                "TRX-" + System.currentTimeMillis()
        );

        transaction.setTransactionType(
                request.getTransactionType()
        );

        transaction.setPaymentMethod(
                request.getPaymentMethod()
        );

        transaction.setPaymentStatus(
                PaymentStatus.PAID
        );

        transaction.setNotes(
                request.getNotes()
        );

        transaction.setSubtotal(0.0);
        transaction.setTax(0.0);
        transaction.setTotal(0.0);

        transactionRepository.save(
                transaction
        );

        double subtotal = 0.0;

        List<TransactionItemResponse> itemResponses =
                new ArrayList<>();

        for (
                CreateTransactionItemRequest itemRequest
                : request.getItems()
        ) {

            Product product =
                    productRepository.findById(
                                    itemRequest.getProductId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Produk tidak ditemukan"
                                    ));

            double itemSubtotal =
                    product.getSellingPrice()
                            * itemRequest.getQuantity();

            subtotal += itemSubtotal;

            TransactionItem item =
                    new TransactionItem();

            item.setTransaction(
                    transaction
            );

            item.setProduct(
                    product
            );

            item.setProductName(
                    product.getName()
            );

            item.setQuantity(
                    itemRequest.getQuantity()
            );

            item.setUnitPrice(
                    product.getSellingPrice()
            );

            item.setSubtotal(
                    itemSubtotal
            );

            transactionItemRepository.save(
                    item
            );

            List<ProductRecipe> recipes =
                    productRecipeRepository
                            .findByProductId(
                                    product.getId()
                            );

            for (ProductRecipe recipe : recipes) {

                Ingredient ingredient =
                        recipe.getIngredient();

                double qtyNeeded =
                        recipe.getQuantityRequired()
                                * itemRequest.getQuantity();

                double stockBefore =
                        ingredient.getCurrentStock();

                if (stockBefore < qtyNeeded) {
                    throw new RuntimeException(
                            "Stok tidak cukup untuk "
                                    + ingredient.getName()
                    );
                }

                double stockAfter =
                        stockBefore - qtyNeeded;

                ingredient.setCurrentStock(
                        stockAfter
                );

                ingredientRepository.save(
                        ingredient
                );

                StockMovement movement =
                        new StockMovement();

                movement.setIngredient(
                        ingredient
                );

                movement.setQuantity(
                        qtyNeeded
                );

                movement.setStockBefore(
                        stockBefore
                );

                movement.setStockAfter(
                        stockAfter
                );

                movement.setMovementType(
                        "SALE"
                );

                movement.setReferenceNumber(
                        transaction.getTransactionNumber()
                );

                movement.setMovementDate(
                        LocalDateTime.now()
                );

                stockMovementRepository.save(
                        movement
                );
            }

            itemResponses.add(
                    new TransactionItemResponse(
                            product.getId(),
                            product.getName(),
                            itemRequest.getQuantity(),
                            product.getSellingPrice(),
                            itemSubtotal
                    )
            );
        }

        double tax = subtotal * 0.11;
        double total = subtotal + tax;

        transaction.setSubtotal(
                subtotal
        );

        transaction.setTax(
                tax
        );

        transaction.setTotal(
                total
        );

        transactionRepository.save(
                transaction
        );

        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionNumber(),
                subtotal,
                tax,
                total,
                itemResponses
        );
    }
}