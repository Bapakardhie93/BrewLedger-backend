package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.dto.transaction.CreateTransactionItemRequest;
import com.brewledger.brewledger.backend.dto.transaction.CreateTransactionRequest;
import com.brewledger.brewledger.backend.dto.transaction.TransactionItemResponse;
import com.brewledger.brewledger.backend.dto.transaction.TransactionResponse;
import com.brewledger.brewledger.backend.dto.transaction.ReceiptResponse;
import com.brewledger.brewledger.backend.entity.*;
import com.brewledger.brewledger.backend.enums.PaymentStatus;
import com.brewledger.brewledger.backend.enums.ShiftStatus;
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
@SuppressWarnings("null")
public class TransactionService {

    private static final double TAX_RATE = PosService.TAX_RATE;

    private final TransactionRepository transactionRepository;
    private final TransactionItemRepository transactionItemRepository;
    private final ProductRepository productRepository;
    private final ProductRecipeRepository productRecipeRepository;
    private final IngredientRepository ingredientRepository;
    private final StockMovementRepository stockMovementRepository;
    private final CurrentUserService currentUserService;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;
    private final CashierShiftRepository cashierShiftRepository;
    private final KitchenOrderService kitchenOrderService;
    private final RestaurantTableRepository restaurantTableRepository;

    /**
     * Creates a new transaction, deducts ingredient stock based on recipes,
     * records stock movements, calculates HPP, validates cashier shift, and logs activity.
     */
    @Transactional
    public TransactionResponse create(
            CreateTransactionRequest request
    ) {
        return create(request, null);
    }

    @Transactional
    public TransactionResponse create(
            CreateTransactionRequest request,
            String idempotencyKey
    ) {
        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            java.util.Optional<Transaction> existingOpt = transactionRepository.findByIdempotencyKey(idempotencyKey.trim());
            if (existingOpt.isPresent()) {
                log.info("Duplicate request with Idempotency-Key: {}, returning existing transaction", idempotencyKey);
                return findById(existingOpt.get().getId());
            }
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Transaksi harus memiliki minimal 1 item");
        }

        User currentUser = currentUserService.requireCurrentUser();

        // Cashier shift check for KASIR role checkout requests
        if ("KASIR".equals(currentUser.getRole() != null ? currentUser.getRole().getName() : "")) {
            if (!cashierShiftRepository.existsByCashierIdAndStatus(currentUser.getId(), ShiftStatus.OPEN)) {
                throw new BusinessException("Shift kasir belum dibuka oleh Manajemen.");
            }
        }

        // Validate table for DINE_IN transactions
        if (request.getTransactionType() == com.brewledger.brewledger.backend.enums.TransactionType.DINE_IN) {
            if (request.getTableNumber() == null || request.getTableNumber().trim().isEmpty()) {
                throw new BusinessException("Nomor meja (tableNumber) wajib diisi untuk transaksi Dine In");
            }
            if (!restaurantTableRepository.existsByNumber(request.getTableNumber().trim())) {
                throw new BusinessException("Meja dengan nomor " + request.getTableNumber() + " tidak ditemukan");
            }
        }

        Transaction transaction = new Transaction();

        transaction.setTransactionNumber(
                "TRX-" + System.currentTimeMillis()
        );
        transaction.setIdempotencyKey(idempotencyKey != null ? idempotencyKey.trim() : null);

        transaction.setCashier(currentUser);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setPaymentStatus(PaymentStatus.PAID);
        transaction.setNotes(request.getNotes());
        transaction.setCustomerName(request.getCustomerName());
        transaction.setTableNumber(request.getTableNumber());
        
        double discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : 0.0;
        transaction.setDiscountAmount(discountAmount);
        transaction.setDiscountNotes(request.getDiscountNotes());

        transaction.setSubtotal(0.0);
        transaction.setTax(0.0);
        transaction.setTotal(0.0);
        transaction.setCashReceived(0.0);
        transaction.setChangeAmount(0.0);

        transactionRepository.save(transaction);

        double subtotal = 0.0;
        List<TransactionItemResponse> itemResponses = new ArrayList<>();
        List<TransactionItem> savedItems = new ArrayList<>();

        for (CreateTransactionItemRequest itemRequest : request.getItems()) {

            // Bug Fix #7: Use ResourceNotFoundException for 404
            Long productId = java.util.Objects.requireNonNull(itemRequest.getProductId(), "Product ID must not be null");
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Produk tidak ditemukan dengan ID: " + itemRequest.getProductId()
                    ));

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new BusinessException("Produk tidak aktif: " + product.getName());
            }

            double itemSubtotal = product.getSellingPrice() * itemRequest.getQuantity();
            subtotal += itemSubtotal;

            // Calculate HPP (Cost of Goods Sold) for this product at this exact moment
            List<ProductRecipe> recipes = productRecipeRepository.findByProductId(product.getId());
            double itemCostPrice = 0.0;
            if (Boolean.TRUE.equals(product.getUseCustomHpp()) && product.getCustomHpp() != null) {
                itemCostPrice = product.getCustomHpp();
            } else {
                for (ProductRecipe recipe : recipes) {
                    itemCostPrice += recipe.getQuantityRequired() * (recipe.getIngredient().getCostPrice() != null ? recipe.getIngredient().getCostPrice() : 0.0);
                }
            }
            double itemSubtotalCost = itemCostPrice * itemRequest.getQuantity();

            TransactionItem item = new TransactionItem();
            item.setTransaction(transaction);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(product.getSellingPrice());
            item.setSubtotal(itemSubtotal);
            item.setCostPrice(itemCostPrice);
            item.setSubtotalCost(itemSubtotalCost);
            item.setNotes(itemRequest.getNotes());

            transactionItemRepository.save(item);
            savedItems.add(item);

            for (ProductRecipe recipe : recipes) {

                Ingredient ingredient = recipe.getIngredient();
                double qtyNeeded = recipe.getQuantityRequired() * itemRequest.getQuantity();
                double stockBefore = ingredient.getCurrentStock();

                if (stockBefore < qtyNeeded) {
                    throw new BusinessException(
                            "Stok tidak cukup untuk bahan: " + ingredient.getName()
                                    + ". Tersedia: " + stockBefore + ", Dibutuhkan: " + qtyNeeded
                    );
                }

                double stockAfter = stockBefore - qtyNeeded;
                ingredient.setCurrentStock(stockAfter);
                ingredientRepository.save(ingredient);

                double minStock = ingredient.getMinimumStock() != null ? ingredient.getMinimumStock() : 0.0;
                if (stockAfter <= minStock) {
                    notificationService.sendAlert("Stok bahan baku rendah (Penjualan): " 
                            + ingredient.getName() + " (" + ingredient.getCode() + ") memiliki stok " + stockAfter 
                            + " " + ingredient.getUnit() + " (Minimum: " + minStock + " " + ingredient.getUnit() + ")");
                }

                StockMovement movement = new StockMovement();
                movement.setIngredient(ingredient);
                movement.setQuantity(-qtyNeeded);
                movement.setStockBefore(stockBefore);
                movement.setStockAfter(stockAfter);
                movement.setMovementType("SALE_CONSUMPTION");
                movement.setReferenceNumber(transaction.getTransactionNumber());
                movement.setMovementDate(LocalDateTime.now());
                movement.setCreatedBy(currentUser.getUsername());

                stockMovementRepository.save(movement);
            }

            itemResponses.add(new TransactionItemResponse(
                    product.getId(),
                    product.getName(),
                    itemRequest.getQuantity(),
                    product.getSellingPrice(),
                    itemSubtotal,
                    itemCostPrice,
                    itemSubtotalCost,
                    itemRequest.getNotes()
            ));
        }

        if (discountAmount > subtotal) {
            throw new BusinessException("Diskon tidak boleh melebihi subtotal transaksi");
        }

        double taxableAmount = subtotal - discountAmount;
        double tax = taxableAmount * TAX_RATE;
        double total = taxableAmount + tax;

        transaction.setSubtotal(subtotal);
        transaction.setTax(tax);
        transaction.setTotal(total);

        // Compute cashReceived and changeAmount
        if (request.getPaymentMethod() == com.brewledger.brewledger.backend.enums.PaymentMethod.CASH) {
            double cashReceived = request.getCashReceived() != null ? request.getCashReceived() : 0.0;
            if (cashReceived < total) {
                throw new BusinessException("Uang tunai yang diterima (cashReceived) kurang dari total transaksi. Total: Rp" + total + ", Diterima: Rp" + cashReceived);
            }
            transaction.setCashReceived(cashReceived);
            transaction.setChangeAmount(cashReceived - total);
        } else {
            transaction.setCashReceived(total);
            transaction.setChangeAmount(0.0);
        }

        transactionRepository.save(transaction);

        // Update table status to OCCUPIED if matching table exists
        if (transaction.getTableNumber() != null && !transaction.getTableNumber().trim().isEmpty()) {
            restaurantTableRepository.findByNumber(transaction.getTableNumber().trim())
                    .ifPresent(table -> {
                        table.setStatus(com.brewledger.brewledger.backend.enums.TableStatus.OCCUPIED);
                        restaurantTableRepository.save(table);
                    });
        }

        // Create kitchen order automatically
        kitchenOrderService.createFromTransaction(transaction, savedItems);

        // Record Audit Log
        activityLogService.record("CREATE_TRANSACTION", 
                "Created transaction: " + transaction.getTransactionNumber() + ", Total: Rp" + total + ", Discount: Rp" + discountAmount,
                "TRANSACTION", transaction.getId());

        log.info("Transaction created: {}, total: {}", transaction.getTransactionNumber(), total);

        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionNumber(),
                subtotal,
                tax,
                total,
                discountAmount,
                transaction.getDiscountNotes(),
                transaction.getCustomerName(),
                transaction.getTableNumber(),
                transaction.getCashReceived(),
                transaction.getChangeAmount(),
                itemResponses,
                transaction.getCreatedAt() != null ? transaction.getCreatedAt() : LocalDateTime.now(),
                transaction.getPaymentStatus() != null ? transaction.getPaymentStatus().name() : "PAID",
                transaction.getTransactionType().name(),
                transaction.getPaymentMethod().name(),
                transaction.getCashier() != null ? transaction.getCashier().getFullName() : null,
                transaction.getTransactionNumber()
        );
    }

    /**
     * Returns all transactions with their items loaded.
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll() {

        return transactionRepository.findAll()
                .stream()
                .map(transaction -> {
                    List<TransactionItemResponse> items = transactionItemRepository
                            .findByTransactionId(transaction.getId())
                            .stream()
                            .map(item -> new TransactionItemResponse(
                                    item.getProduct().getId(),
                                    item.getProductName(),
                                    item.getQuantity(),
                                    item.getUnitPrice(),
                                    item.getSubtotal(),
                                    item.getCostPrice() != null ? item.getCostPrice() : 0.0,
                                    item.getSubtotalCost() != null ? item.getSubtotalCost() : 0.0,
                                    item.getNotes()
                            ))
                            .toList();

                    return new TransactionResponse(
                            transaction.getId(),
                            transaction.getTransactionNumber(),
                            transaction.getSubtotal(),
                            transaction.getTax(),
                            transaction.getTotal(),
                            transaction.getDiscountAmount() != null ? transaction.getDiscountAmount() : 0.0,
                            transaction.getDiscountNotes(),
                            transaction.getCustomerName(),
                            transaction.getTableNumber(),
                            transaction.getCashReceived(),
                            transaction.getChangeAmount(),
                            items,
                            transaction.getCreatedAt() != null ? transaction.getCreatedAt() : LocalDateTime.now(),
                            transaction.getPaymentStatus() != null ? transaction.getPaymentStatus().name() : "PAID",
                            transaction.getTransactionType() != null ? transaction.getTransactionType().name() : null,
                            transaction.getPaymentMethod() != null ? transaction.getPaymentMethod().name() : null,
                            transaction.getCashier() != null ? transaction.getCashier().getFullName() : null,
                            transaction.getTransactionNumber()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findMyTransactions() {
        User currentUser = currentUserService.requireCurrentUser();
        return transactionRepository.findByCashierIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(transaction -> {
                    List<TransactionItemResponse> items = transactionItemRepository
                            .findByTransactionId(transaction.getId())
                            .stream()
                            .map(item -> new TransactionItemResponse(
                                    item.getProduct().getId(),
                                    item.getProductName(),
                                    item.getQuantity(),
                                    item.getUnitPrice(),
                                    item.getSubtotal(),
                                    item.getCostPrice() != null ? item.getCostPrice() : 0.0,
                                    item.getSubtotalCost() != null ? item.getSubtotalCost() : 0.0,
                                    item.getNotes()
                            ))
                            .toList();

                    return new TransactionResponse(
                            transaction.getId(),
                            transaction.getTransactionNumber(),
                            transaction.getSubtotal(),
                            transaction.getTax(),
                            transaction.getTotal(),
                            transaction.getDiscountAmount() != null ? transaction.getDiscountAmount() : 0.0,
                            transaction.getDiscountNotes(),
                            transaction.getCustomerName(),
                            transaction.getTableNumber(),
                            transaction.getCashReceived(),
                            transaction.getChangeAmount(),
                            items,
                            transaction.getCreatedAt() != null ? transaction.getCreatedAt() : LocalDateTime.now(),
                            transaction.getPaymentStatus() != null ? transaction.getPaymentStatus().name() : "PAID",
                            transaction.getTransactionType() != null ? transaction.getTransactionType().name() : null,
                            transaction.getPaymentMethod() != null ? transaction.getPaymentMethod().name() : null,
                            transaction.getCashier() != null ? transaction.getCashier().getFullName() : null,
                            transaction.getTransactionNumber()
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
                        item.getSubtotal(),
                        item.getCostPrice() != null ? item.getCostPrice() : 0.0,
                        item.getSubtotalCost() != null ? item.getSubtotalCost() : 0.0,
                        item.getNotes()
                ))
                .toList();

        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionNumber(),
                transaction.getSubtotal(),
                transaction.getTax(),
                transaction.getTotal(),
                transaction.getDiscountAmount() != null ? transaction.getDiscountAmount() : 0.0,
                transaction.getDiscountNotes(),
                transaction.getCustomerName(),
                transaction.getTableNumber(),
                transaction.getCashReceived(),
                transaction.getChangeAmount(),
                items,
                transaction.getCreatedAt() != null ? transaction.getCreatedAt() : LocalDateTime.now(),
                transaction.getPaymentStatus() != null ? transaction.getPaymentStatus().name() : "PAID",
                transaction.getTransactionType() != null ? transaction.getTransactionType().name() : null,
                transaction.getPaymentMethod() != null ? transaction.getPaymentMethod().name() : null,
                transaction.getCashier() != null ? transaction.getCashier().getFullName() : null,
                transaction.getTransactionNumber()
        );
    }

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private ApprovalRequestService approvalRequestService;

    /**
     * Voids a transaction, returns ingredient stock, records stock movements,
     * logs action, and sends a notification.
     */
    @Transactional
    public void voidTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaksi tidak ditemukan dengan ID: " + id
                ));

        if (transaction.getPaymentStatus() == PaymentStatus.CANCELLED) {
            throw new BusinessException("Transaksi ini sudah dibatalkan/void");
        }

        approvalRequestService.submitVoidTransaction(id, "Permintaan pembatalan transaksi " + transaction.getTransactionNumber());
        throw new BusinessException("Pengajuan void transaksi berhasil diajukan dengan status PENDING dan memerlukan persetujuan MANAGEMENT.");
    }

    @Transactional
    public void executeVoidDirectly(Long id, User requestedBy) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaksi tidak ditemukan dengan ID: " + id
                ));

        if (transaction.getPaymentStatus() == PaymentStatus.CANCELLED) {
            throw new BusinessException("Transaksi ini sudah dibatalkan/void");
        }

        transaction.setPaymentStatus(PaymentStatus.CANCELLED);
        transactionRepository.save(transaction);

        List<TransactionItem> items = transactionItemRepository.findByTransactionId(transaction.getId());

        for (TransactionItem item : items) {
            List<ProductRecipe> recipes = productRecipeRepository.findByProductId(item.getProduct().getId());

            for (ProductRecipe recipe : recipes) {
                Ingredient ingredient = recipe.getIngredient();
                double qtyReturned = recipe.getQuantityRequired() * item.getQuantity();
                double stockBefore = ingredient.getCurrentStock() != null ? ingredient.getCurrentStock() : 0.0;
                double stockAfter = stockBefore + qtyReturned;

                ingredient.setCurrentStock(stockAfter);
                ingredientRepository.save(ingredient);

                StockMovement movement = new StockMovement();
                movement.setIngredient(ingredient);
                movement.setQuantity(qtyReturned);
                movement.setStockBefore(stockBefore);
                movement.setStockAfter(stockAfter);
                movement.setMovementType("VOID_REVERSAL");
                movement.setReferenceNumber(transaction.getTransactionNumber());
                movement.setMovementDate(LocalDateTime.now());
                movement.setCreatedBy(requestedBy.getUsername());

                stockMovementRepository.save(movement);
            }
        }

        activityLogService.record("VOID_TRANSACTION", 
                "Voided transaction: " + transaction.getTransactionNumber() + ", Total: Rp" + transaction.getTotal() + " (requested by " + requestedBy.getUsername() + ")",
                "TRANSACTION", transaction.getId());
        notificationService.sendAlert("Transaksi " + transaction.getTransactionNumber() + " senilai Rp " + transaction.getTotal() + " telah di-void (disetujui)");
    }

    /**
     * Generates a formatted text receipt for printing.
     */
    @Transactional(readOnly = true)
    public ReceiptResponse getReceipt(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaksi tidak ditemukan dengan ID: " + id
                ));

        List<TransactionItem> items = transactionItemRepository.findByTransactionId(transaction.getId());
        List<TransactionItemResponse> itemResponses = items.stream()
                .map(item -> new TransactionItemResponse(
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal(),
                        item.getCostPrice() != null ? item.getCostPrice() : 0.0,
                        item.getSubtotalCost() != null ? item.getSubtotalCost() : 0.0,
                        item.getNotes()
                ))
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("               BREWLEDGER               \n");
        sb.append("========================================\n");
        sb.append("No. Trx : ").append(transaction.getTransactionNumber()).append("\n");
        sb.append("Tanggal : ").append(transaction.getCreatedAt().toString()).append("\n");
        sb.append("Kasir   : ").append(transaction.getCashier().getFullName()).append("\n");
        if (transaction.getCustomerName() != null && !transaction.getCustomerName().isEmpty()) {
            sb.append("Pelanggan: ").append(transaction.getCustomerName()).append("\n");
        }
        if (transaction.getTableNumber() != null && !transaction.getTableNumber().isEmpty()) {
            sb.append("Meja     : ").append(transaction.getTableNumber()).append("\n");
        }
        sb.append("========================================\n");
        for (TransactionItem item : items) {
            sb.append(item.getProductName()).append("\n");
            sb.append(String.format("  %d x %,.0f", item.getQuantity(), item.getUnitPrice()))
              .append(String.format("%25s", String.format("%,.0f", item.getSubtotal()))).append("\n");
            if (item.getNotes() != null && !item.getNotes().isEmpty()) {
                sb.append("  * ").append(item.getNotes()).append("\n");
            }
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("Subtotal: %30s", String.format("%,.0f", transaction.getSubtotal()))).append("\n");
        if (transaction.getDiscountAmount() != null && transaction.getDiscountAmount() > 0) {
            sb.append(String.format("Diskon  : -%29s", String.format("%,.0f", transaction.getDiscountAmount()))).append("\n");
        }
        sb.append(String.format("Pajak   : %30s", String.format("%,.0f", transaction.getTax()))).append("\n");
        sb.append("----------------------------------------\n");
        sb.append(String.format("TOTAL   : %30s", String.format("%,.0f", transaction.getTotal()))).append("\n");
        sb.append("Metode  : ").append(transaction.getPaymentMethod().name()).append("\n");
        if (transaction.getPaymentMethod().name().equals("CASH")) {
            sb.append(String.format("Bayar   : %30s", String.format("%,.0f", transaction.getCashReceived() != null ? transaction.getCashReceived() : 0.0))).append("\n");
            sb.append(String.format("Kembali : %30s", String.format("%,.0f", transaction.getChangeAmount() != null ? transaction.getChangeAmount() : 0.0))).append("\n");
        }
        sb.append("========================================\n");
        sb.append("       Terima Kasih Atas Kunjungan      \n");
        sb.append("               Anda!                    \n");
        sb.append("========================================\n");

        return new ReceiptResponse(
                "BrewLedger",
                transaction.getTransactionNumber(),
                transaction.getCreatedAt(),
                transaction.getCashier().getFullName(),
                transaction.getCustomerName(),
                transaction.getTableNumber(),
                transaction.getPaymentMethod().name(),
                transaction.getSubtotal(),
                transaction.getTax(),
                transaction.getDiscountAmount() != null ? transaction.getDiscountAmount() : 0.0,
                transaction.getTotal(),
                transaction.getCashReceived() != null ? transaction.getCashReceived() : 0.0,
                transaction.getChangeAmount() != null ? transaction.getChangeAmount() : 0.0,
                itemResponses,
                sb.toString()
        );
    }
}
