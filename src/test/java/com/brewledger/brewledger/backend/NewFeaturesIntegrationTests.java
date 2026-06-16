package com.brewledger.brewledger.backend;

import com.brewledger.brewledger.backend.controller.*;
import com.brewledger.brewledger.backend.dto.approval.ApprovalResponse;
import com.brewledger.brewledger.backend.dto.approval.RejectApprovalRequest;
import com.brewledger.brewledger.backend.dto.auth.ChangePasswordRequest;
import com.brewledger.brewledger.backend.dto.kitchen.KitchenOrderResponse;
import com.brewledger.brewledger.backend.dto.shift.CashierShiftResponse;
import com.brewledger.brewledger.backend.dto.shift.CloseShiftRequest;
import com.brewledger.brewledger.backend.dto.shift.OpenShiftRequest;
import com.brewledger.brewledger.backend.dto.table.CreateTableRequest;
import com.brewledger.brewledger.backend.dto.table.TableResponse;
import com.brewledger.brewledger.backend.dto.transaction.*;
import com.brewledger.brewledger.backend.dto.stockrequest.*;
import com.brewledger.brewledger.backend.dto.warehouse.StockAdjustmentRequest;
import com.brewledger.brewledger.backend.dto.warehouse.WarehouseIngredientResponse;
import com.brewledger.brewledger.backend.entity.*;
import com.brewledger.brewledger.backend.enums.PaymentMethod;
import com.brewledger.brewledger.backend.enums.TransactionType;
import com.brewledger.brewledger.backend.exception.BusinessException;
import com.brewledger.brewledger.backend.exception.ResourceNotFoundException;
import com.brewledger.brewledger.backend.exception.ConflictException;
import com.brewledger.brewledger.backend.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class NewFeaturesIntegrationTests {

    @Autowired
    private UserController userController;

    @Autowired
    private IngredientController ingredientController;

    @Autowired
    private ProductController productController;

    @Autowired
    private AuthController authController;

    @Autowired
    private TransactionController transactionController;

    @Autowired
    private CashierShiftController cashierShiftController;

    @Autowired
    private TableController tableController;

    @Autowired
    private KitchenOrderController kitchenOrderController;

    @Autowired
    private ApprovalRequestController approvalRequestController;

    @Autowired
    private WarehouseController warehouseController;

    @Autowired
    private PosController posController;

    @Autowired
    private StockRequestController stockRequestController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private ProductRecipeRepository productRecipeRepository;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private com.brewledger.brewledger.backend.repository.StockMovementRepository stockMovementRepository;

    @Autowired
    private com.brewledger.brewledger.backend.repository.PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private com.brewledger.brewledger.backend.repository.PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Autowired
    private com.brewledger.brewledger.backend.service.PurchaseApprovalService purchaseApprovalService;

    @Autowired
    private com.brewledger.brewledger.backend.controller.ReportController reportController;

    @Autowired
    private com.brewledger.brewledger.backend.controller.HealthController healthController;

    @Autowired
    private com.brewledger.brewledger.backend.controller.PurchaseOrderController purchaseOrderController;

    @Autowired
    private com.brewledger.brewledger.backend.controller.PurchaseApprovalController purchaseApprovalController;

    @Autowired
    private com.brewledger.brewledger.backend.service.ApprovalRequestService approvalRequestService;

    private User kasirUser;
    private User adminUser;
    private User adminUser2;
    private User gudangUser;
    private Product product;
    private Ingredient ingredient;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        // Find or create roles
        Role kasirRole = roleRepository.findByName("KASIR").orElseGet(() -> {
            Role r = new Role();
            r.setName("KASIR");
            r.setDescription("Kasir POS");
            return roleRepository.save(r);
        });

        Role adminRole = roleRepository.findByName("MANAGEMENT").orElseGet(() -> {
            Role r = new Role();
            r.setName("MANAGEMENT");
            r.setDescription("Administrator");
            return roleRepository.save(r);
        });

        Role gudangRole = roleRepository.findByName("GUDANG").orElseGet(() -> {
            Role r = new Role();
            r.setName("GUDANG");
            r.setDescription("Warehouse");
            return roleRepository.save(r);
        });

        // Create Kasir user
        kasirUser = new User();
        kasirUser.setFullName("Kasir POS Test");
        kasirUser.setUsername("kasir_" + suffix);
        kasirUser.setPassword(passwordEncoder.encode("password"));
        kasirUser.setActive(true);
        kasirUser.setMustChangePassword(true);
        kasirUser.setRole(kasirRole);
        kasirUser = userRepository.save(kasirUser);

        // Create Admin user
        adminUser = new User();
        adminUser.setFullName("Admin POS Test");
        adminUser.setUsername("admin_" + suffix);
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setActive(true);
        adminUser.setMustChangePassword(true);
        adminUser.setRole(adminRole);
        adminUser = userRepository.save(adminUser);

        // Create Admin 2 user
        adminUser2 = new User();
        adminUser2.setFullName("Admin POS Test 2");
        adminUser2.setUsername("admin2_" + suffix);
        adminUser2.setPassword(passwordEncoder.encode("password"));
        adminUser2.setActive(true);
        adminUser2.setMustChangePassword(true);
        adminUser2.setRole(adminRole);
        adminUser2 = userRepository.save(adminUser2);

        // Create Gudang user
        gudangUser = new User();
        gudangUser.setFullName("Staf Gudang Test");
        gudangUser.setUsername("gudang_" + suffix);
        gudangUser.setPassword(passwordEncoder.encode("password"));
        gudangUser.setActive(true);
        gudangUser.setMustChangePassword(true);
        gudangUser.setRole(gudangRole);
        gudangUser = userRepository.save(gudangUser);

        // Create Category
        ProductCategory category = new ProductCategory();
        category.setName("Coffee-" + suffix);
        category = categoryRepository.save(category);

        // Create Product
        product = new Product();
        product.setCode("PRD-" + suffix);
        product.setName("Espresso " + suffix);
        product.setSellingPrice(15000.0);
        product.setCategory(category);
        product = productRepository.save(product);

        // Create Supplier
        Supplier supplierEntity = new Supplier();
        supplierEntity.setName("Supplier " + suffix);
        supplierEntity.setActive(true);
        supplierEntity = supplierRepository.save(supplierEntity);

        // Create Ingredient
        ingredient = new Ingredient();
        ingredient.setCode("ING-" + suffix);
        ingredient.setName("Coffee Bean " + suffix);
        ingredient.setSupplier(supplierEntity);
        ingredient.setUnit("gram");
        ingredient.setCurrentStock(1000.0);
        ingredient.setMinimumStock(10.0);
        ingredient.setCostPrice(200.0);
        ingredient.setActive(true);
        ingredient = ingredientRepository.save(ingredient);

        // Create Recipe
        ProductRecipe recipe = new ProductRecipe();
        recipe.setProduct(product);
        recipe.setIngredient(ingredient);
        recipe.setQuantityRequired(10.0);
        productRecipeRepository.save(recipe);

        // Seed Table 12
        RestaurantTable table = new RestaurantTable();
        table.setNumber("12");
        table.setCapacity(4);
        table.setStatus(com.brewledger.brewledger.backend.enums.TableStatus.AVAILABLE);
        restaurantTableRepository.save(table);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cashierShiftEnforcementAndClosingCalculation() {
        // Step 1: Login as KASIR. Transaction should fail because shift is closed.
        authenticateAs(kasirUser);
        
        CreateTransactionRequest txRequest = new CreateTransactionRequest();
        txRequest.setTransactionType(TransactionType.TAKE_AWAY);
        txRequest.setPaymentMethod(PaymentMethod.CASH);
        txRequest.setCashReceived(20000.0);
        
        CreateTransactionItemRequest itemReq = new CreateTransactionItemRequest();
        itemReq.setProductId(product.getId());
        itemReq.setQuantity(1);
        txRequest.setItems(List.of(itemReq));

        assertThatThrownBy(() -> transactionController.create(txRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Shift kasir belum dibuka oleh Manajemen.");

        // Step 2: Open shift by adminUser (MANAGEMENT) for kasirUser
        authenticateAs(adminUser);
        OpenShiftRequest openRequest = new OpenShiftRequest();
        openRequest.setOpeningCash(100000.0);
        openRequest.setNotes("Buka shift pagi");

        CashierShiftResponse shift = cashierShiftController.openShift(kasirUser.getId(), openRequest);
        assertThat(shift.getStatus()).isEqualTo("OPEN");
        assertThat(shift.getOpeningCash()).isEqualTo(100000.0);
        assertThat(shift.getCashierUsername()).isEqualTo(kasirUser.getUsername());
        assertThat(shift.getCashierId()).isEqualTo(kasirUser.getId());
        assertThat(shift.getCashierRole()).isEqualTo("KASIR");

        // Step 3: Transaction should now succeed
        authenticateAs(kasirUser);
        TransactionResponse txResponse = transactionController.create(txRequest);
        assertThat(txResponse.getTotal()).isGreaterThan(0.0);
        // SellingPrice: 15000. Tax: 11% (1650). Total: 16650.
        // Change: 20000 - 16650 = 3350
        assertThat(txResponse.getChangeAmount()).isEqualTo(20000.0 - txResponse.getTotal());

        // Step 4: Close shift by adminUser & calculate difference
        authenticateAs(adminUser);
        CloseShiftRequest closeRequest = new CloseShiftRequest();
        // Expected cash: Opening (100000) + Transaction Total (16650) = 116650
        // We report closing cash: 120000 (meaning 3350 surplus)
        closeRequest.setClosingCash(120000.0);
        closeRequest.setNotes("Tutup shift sore");

        CashierShiftResponse closedShift = cashierShiftController.closeShift(shift.getId(), closeRequest);
        assertThat(closedShift.getStatus()).isEqualTo("CLOSED");
        assertThat(closedShift.getExpectedCash()).isEqualTo(100000.0 + txResponse.getTotal());
        assertThat(closedShift.getCashDifference()).isEqualTo(120000.0 - (100000.0 + txResponse.getTotal()));
    }

    @Test
    void kitchenOrderCreatedAutomaticallyAndManaged() {
        // Open shift first as MANAGEMENT for KASIR
        authenticateAs(adminUser);
        OpenShiftRequest openRequest = new OpenShiftRequest();
        openRequest.setOpeningCash(50000.0);
        cashierShiftController.openShift(kasirUser.getId(), openRequest);

        authenticateAs(kasirUser);

        // Create transaction with table
        CreateTransactionRequest txRequest = new CreateTransactionRequest();
        txRequest.setTransactionType(TransactionType.DINE_IN);
        txRequest.setPaymentMethod(PaymentMethod.CASH);
        txRequest.setTableNumber("12");
        txRequest.setCashReceived(50000.0);

        CreateTransactionItemRequest itemReq = new CreateTransactionItemRequest();
        itemReq.setProductId(product.getId());
        itemReq.setQuantity(2);
        itemReq.setNotes("Less sugar");
        txRequest.setItems(List.of(itemReq));

        TransactionResponse txResponse = transactionController.create(txRequest);

        // Check kitchen orders
        List<KitchenOrderResponse> kitchenOrders = kitchenOrderController.findAll();
        assertThat(kitchenOrders).isNotEmpty();
        
        KitchenOrderResponse latestOrder = kitchenOrders.stream()
                .filter(o -> o.getTransactionId().equals(txResponse.getId()))
                .findFirst()
                .orElseThrow();
        
        assertThat(latestOrder.getTableNumber()).isEqualTo("12");
        assertThat(latestOrder.getStatus()).isEqualTo("WAITING");
        assertThat(latestOrder.getItems()).hasSize(1);
        assertThat(latestOrder.getItems().get(0).getProductName()).isEqualTo(product.getName());
        assertThat(latestOrder.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(latestOrder.getItems().get(0).getNotes()).isEqualTo("Less sugar");
    }

    @Test
    void centralizedApprovalFlowForVoidAndStockAdjustment() {
        // Open shift as MANAGEMENT for KASIR
        authenticateAs(adminUser);
        OpenShiftRequest openRequest = new OpenShiftRequest();
        openRequest.setOpeningCash(50000.0);
        cashierShiftController.openShift(kasirUser.getId(), openRequest);

        // Prepare a transaction
        authenticateAs(kasirUser);

        CreateTransactionRequest txRequest = new CreateTransactionRequest();
        txRequest.setTransactionType(TransactionType.TAKE_AWAY);
        txRequest.setPaymentMethod(PaymentMethod.CASH);
        txRequest.setCashReceived(50000.0);
        CreateTransactionItemRequest itemReq = new CreateTransactionItemRequest();
        itemReq.setProductId(product.getId());
        itemReq.setQuantity(1);
        txRequest.setItems(List.of(itemReq));
        TransactionResponse txResponse = transactionController.create(txRequest);

        // Step 1: Request VOID. It should throw BusinessException because void must be approved.
        authenticateAs(adminUser);
        assertThatThrownBy(() -> transactionController.voidTransaction(txResponse.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Pengajuan void transaksi berhasil diajukan");

        // Verify an ApprovalRequest was created
        List<ApprovalResponse> approvals = approvalRequestController.findAll();
        ApprovalResponse voidApproval = approvals.stream()
                .filter(a -> a.getType().equals("VOID_TRANSACTION") && a.getReferenceId().equals(txResponse.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(voidApproval.getStatus()).isEqualTo("PENDING");

        // Approve VOID
        authenticateAs(gudangUser);
        ApprovalResponse approvedVoid = approvalRequestController.approveRequest(voidApproval.getId());
        assertThat(approvedVoid.getStatus()).isEqualTo("APPROVED");

        // Verify transaction is cancelled/voided
        authenticateAs(adminUser);
        TransactionResponse voidedTx = transactionController.findById(txResponse.getId());
        // Since void was executed, we can check stock return. Product recipe quantity: 10.0. Stock before transaction: 1000.0.
        // During transaction: 1000 - 10 = 990. After void: 990 + 10 = 1000.
        Ingredient updatedIngredient = ingredientRepository.findById(ingredient.getId()).orElseThrow();
        assertThat(updatedIngredient.getCurrentStock()).isEqualTo(1000.0);

        // Step 2: Stock Adjustment via Warehouse Controller (currently gudangUser is active)
        authenticateAs(gudangUser);
        StockAdjustmentRequest adjRequest = new StockAdjustmentRequest();
        adjRequest.setNewStock(500.0);
        adjRequest.setReason("Fisik rusak");

        assertThatThrownBy(() -> warehouseController.adjustStock(ingredient.getId(), adjRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Pengajuan penyesuaian stok berhasil diajukan");

        // Verify Approval Request created
        approvals = approvalRequestController.findAll();
        ApprovalResponse stockApproval = approvals.stream()
                .filter(a -> a.getType().equals("STOCK_ADJUSTMENT") && a.getReferenceId().equals(ingredient.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(stockApproval.getStatus()).isEqualTo("PENDING");

        // Approve Stock Adjustment
        authenticateAs(adminUser);
        ApprovalResponse approvedStock = approvalRequestController.approveRequest(stockApproval.getId());
        assertThat(approvedStock.getStatus()).isEqualTo("APPROVED");

        // Verify stock is adjusted
        Ingredient adjustedIngredient = ingredientRepository.findById(ingredient.getId()).orElseThrow();
        assertThat(adjustedIngredient.getCurrentStock()).isEqualTo(500.0);
    }

    @Test
    void changePasswordChangesUserPassword() {
        authenticateAs(kasirUser);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("password");
        request.setNewPassword("newSecretPassword");

        authController.changePassword(request);

        // Verify password changed on DB
        User updatedKasir = userRepository.findById(kasirUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newSecretPassword", updatedKasir.getPassword())).isTrue();
        assertThat(updatedKasir.getMustChangePassword()).isFalse();

        // Old password match verification
        request.setOldPassword("wrongPassword");
        request.setNewPassword("anotherPassword");
        assertThatThrownBy(() -> authController.changePassword(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Password lama salah");
    }

    @Test
    void testHppCalculationAndMarginRecommendedPrice() {
        authenticateAs(adminUser);
        
        // 1. Create two ingredients with purchase price and pack size
        // Susu UHT: 24000 per 1 liter (1000 ml). Cost price per ml = 24.
        com.brewledger.brewledger.backend.dto.ingredient.CreateIngredientRequest ing1 = new com.brewledger.brewledger.backend.dto.ingredient.CreateIngredientRequest();
        ing1.setCode("ING-SUSU-" + UUID.randomUUID().toString().substring(0, 8));
        ing1.setName("Susu UHT");
        ing1.setSupplierId(ingredient.getSupplier().getId());
        ing1.setUnit("ml");
        ing1.setMinimumStock(10.0);
        ing1.setCostPrice(0.0);
        ing1.setPurchasePrice(24000.0);
        ing1.setPackSize(1000.0);
        com.brewledger.brewledger.backend.dto.ingredient.IngredientResponse savedIng1 = ingredientController.create(ing1);
        assertThat(savedIng1.getCostPrice()).isEqualTo(24.0);

        // Bubuk Matcha: 320000 per 500 grams. Cost price per gram = 640.
        com.brewledger.brewledger.backend.dto.ingredient.CreateIngredientRequest ing2 = new com.brewledger.brewledger.backend.dto.ingredient.CreateIngredientRequest();
        ing2.setCode("ING-MATCHA-" + UUID.randomUUID().toString().substring(0, 8));
        ing2.setName("Bubuk Matcha");
        ing2.setSupplierId(ingredient.getSupplier().getId());
        ing2.setUnit("gram");
        ing2.setMinimumStock(5.0);
        ing2.setCostPrice(0.0);
        ing2.setPurchasePrice(320000.0);
        ing2.setPackSize(500.0);
        com.brewledger.brewledger.backend.dto.ingredient.IngredientResponse savedIng2 = ingredientController.create(ing2);
        assertThat(savedIng2.getCostPrice()).isEqualTo(640.0);

        // 2. Create product with recipe using these ingredients:
        // Susu UHT: 100 ml (100 * 24 = 2400)
        // Bubuk Matcha: 12 gram (12 * 640 = 7680)
        // Calculated HPP = 10080.
        com.brewledger.brewledger.backend.dto.product.CreateProductRequest prodReq = new com.brewledger.brewledger.backend.dto.product.CreateProductRequest();
        prodReq.setCode("PROD-MATCHA-" + UUID.randomUUID().toString().substring(0, 8));
        prodReq.setName("Matcha Drink");
        prodReq.setCategoryId(product.getCategory().getId());
        prodReq.setSellingPrice(25000.0);
        prodReq.setDescription("Matcha Latte");
        prodReq.setUseCustomHpp(false);
        prodReq.setMargin(50.0); // 50% profit margin

        com.brewledger.brewledger.backend.dto.product.RecipeItemRequest item1 = new com.brewledger.brewledger.backend.dto.product.RecipeItemRequest();
        item1.setIngredientId(savedIng1.getId());
        item1.setQuantityRequired(100.0);

        com.brewledger.brewledger.backend.dto.product.RecipeItemRequest item2 = new com.brewledger.brewledger.backend.dto.product.RecipeItemRequest();
        item2.setIngredientId(savedIng2.getId());
        item2.setQuantityRequired(12.0);

        prodReq.setRecipeItems(List.of(item1, item2));

        com.brewledger.brewledger.backend.dto.product.ProductResponse savedProd = productController.create(prodReq);
        assertThat(savedProd.getCalculatedHpp()).isEqualTo(10080.0);
        assertThat(savedProd.getHpp()).isEqualTo(10080.0);
        // profit margin = 50% -> recommended price = HPP / (1 - 0.5) = 10080 / 0.5 = 20160
        assertThat(savedProd.getRecommendedSellingPrice()).isEqualTo(20160.0);

        // 3. Test custom HPP override
        com.brewledger.brewledger.backend.dto.product.UpdateProductRequest updateReq = new com.brewledger.brewledger.backend.dto.product.UpdateProductRequest();
        updateReq.setCode(savedProd.getCode());
        updateReq.setName(savedProd.getName());
        updateReq.setCategoryId(product.getCategory().getId());
        updateReq.setSellingPrice(25000.0);
        updateReq.setDescription("Matcha Latte Updated");
        updateReq.setActive(true);
        updateReq.setUseCustomHpp(true);
        updateReq.setCustomHpp(12000.0);
        updateReq.setMargin(40.0); // 40% margin
        updateReq.setRecipeItems(List.of(item1, item2));

        com.brewledger.brewledger.backend.dto.product.ProductResponse updatedProd = productController.update(savedProd.getId(), updateReq);
        assertThat(updatedProd.getCalculatedHpp()).isEqualTo(10080.0);
        assertThat(updatedProd.getHpp()).isEqualTo(12000.0);
        // recommended price = 12000 / (1 - 0.4) = 20000
        assertThat(updatedProd.getRecommendedSellingPrice()).isEqualTo(20000.0);
    }

    @Test
    void testNewIngredientApprovalRequest() {
        // 1. Submit request as MANAGEMENT
        authenticateAs(adminUser);
        com.brewledger.brewledger.backend.dto.ingredient.CreateIngredientRequest ingReq = new com.brewledger.brewledger.backend.dto.ingredient.CreateIngredientRequest();
        ingReq.setCode("ING-REQ-" + UUID.randomUUID().toString().substring(0, 8));
        ingReq.setName("Susu Almond");
        ingReq.setSupplierId(ingredient.getSupplier().getId());
        ingReq.setUnit("ml");
        ingReq.setMinimumStock(10.0);
        ingReq.setCostPrice(0.0);
        ingReq.setPurchasePrice(45000.0);
        ingReq.setPackSize(1000.0);

        ApprovalResponse approval = ingredientController.submitNewIngredient(ingReq);
        assertThat(approval.getType()).isEqualTo("NEW_INGREDIENT");
        assertThat(approval.getStatus()).isEqualTo("PENDING");

        // 2. GUDANG role approves the request
        Role gudangRole = roleRepository.findByName("GUDANG").orElseGet(() -> {
            Role r = new Role();
            r.setName("GUDANG");
            r.setDescription("Warehouse");
            return roleRepository.save(r);
        });
        User gudangUser = new User();
        gudangUser.setFullName("Staf Gudang");
        gudangUser.setUsername("gudang_" + UUID.randomUUID().toString().substring(0, 8));
        gudangUser.setPassword(passwordEncoder.encode("password"));
        gudangUser.setActive(true);
        gudangUser.setMustChangePassword(true);
        gudangUser.setRole(gudangRole);
        gudangUser = userRepository.save(gudangUser);

        authenticateAs(gudangUser);
        ApprovalResponse approved = approvalRequestController.approveRequest(approval.getId());
        assertThat(approved.getStatus()).isEqualTo("APPROVED");

        // Verify ingredient was created
        Ingredient createdIng = ingredientRepository.findById(approved.getReferenceId()).orElseThrow();
        assertThat(createdIng.getName()).isEqualTo("Susu Almond");
        assertThat(createdIng.getCostPrice()).isEqualTo(45.0);
    }

    @Test
    void testUserManagementAndProfileQuery() {
        authenticateAs(adminUser);

        // Create user with phone number
        com.brewledger.brewledger.backend.dto.user.CreateUserRequest userReq = new com.brewledger.brewledger.backend.dto.user.CreateUserRequest();
        userReq.setFullName("User Baru");
        userReq.setUsername("newuser_" + UUID.randomUUID().toString().substring(0, 8));
        userReq.setPassword("password");
        userReq.setRoleId(kasirUser.getRole().getId());
        userReq.setPhoneNumber("08123456789");

        org.springframework.http.ResponseEntity<com.brewledger.brewledger.backend.dto.user.UserResponse> createdResponse = userController.createUser(userReq);
        assertThat(createdResponse.getBody().getPhoneNumber()).isEqualTo("08123456789");

        // Try to retrieve user as Kasir (should be allowed now because of method-level security)
        authenticateAs(kasirUser);
        org.springframework.http.ResponseEntity<com.brewledger.brewledger.backend.dto.user.UserResponse> fetchedResponse = userController.getUserById(createdResponse.getBody().getId());
        assertThat(fetchedResponse.getBody().getFullName()).isEqualTo("User Baru");
        assertThat(fetchedResponse.getBody().getPhoneNumber()).isEqualTo("08123456789");

        // Verify /api/auth/me
        org.springframework.http.ResponseEntity<com.brewledger.brewledger.backend.dto.user.UserResponse> meResponse = authController.getCurrentUser();
        assertThat(meResponse.getBody().getUsername()).isEqualTo(kasirUser.getUsername());

        // Verify warehouse workspace response contains currentUser
        authenticateAs(adminUser);
        com.brewledger.brewledger.backend.dto.warehouse.WarehouseResponse warehouseResponse = warehouseController.getWorkspace("");
        assertThat(warehouseResponse.getCurrentUser()).isNotNull();
        assertThat(warehouseResponse.getCurrentUser().getUsername()).isEqualTo(adminUser.getUsername());
    }

    @Test
    void testUserHeartbeatPresenceTracking() {
        authenticateAs(kasirUser);

        // Verify initial state: lastActivity is null, isOnline is false
        org.springframework.http.ResponseEntity<com.brewledger.brewledger.backend.dto.user.UserResponse> initialResponse = userController.getUserById(kasirUser.getId());
        assertThat(initialResponse.getBody().getLastActivity()).isNull();
        assertThat(initialResponse.getBody().getIsOnline()).isFalse();

        // Perform heartbeat
        userController.heartbeat(() -> kasirUser.getUsername());

        // Verify updated state: lastActivity is set, isOnline is true
        org.springframework.http.ResponseEntity<com.brewledger.brewledger.backend.dto.user.UserResponse> updatedResponse = userController.getUserById(kasirUser.getId());
        assertThat(updatedResponse.getBody().getLastActivity()).isNotNull();
        assertThat(updatedResponse.getBody().getIsOnline()).isTrue();
    }

    @Test
    void testApprovalFlowPermissionEnforcement() {
        // Open shift as MANAGEMENT for KASIR
        authenticateAs(adminUser2);
        OpenShiftRequest openRequest = new OpenShiftRequest();
        openRequest.setOpeningCash(50000.0);
        cashierShiftController.openShift(kasirUser.getId(), openRequest);

        // Step 1: Login as KASIR and create transaction
        authenticateAs(kasirUser);

        CreateTransactionRequest txRequest = new CreateTransactionRequest();
        txRequest.setTransactionType(TransactionType.TAKE_AWAY);
        txRequest.setPaymentMethod(PaymentMethod.CASH);
        txRequest.setCashReceived(50000.0);
        CreateTransactionItemRequest itemReq = new CreateTransactionItemRequest();
        itemReq.setProductId(product.getId());
        itemReq.setQuantity(1);
        txRequest.setItems(List.of(itemReq));
        TransactionResponse txResponse = transactionController.create(txRequest);

        // Step 2: Request VOID as adminUser (MANAGEMENT)
        authenticateAs(adminUser);
        assertThatThrownBy(() -> transactionController.voidTransaction(txResponse.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Pengajuan void transaksi berhasil diajukan");

        List<ApprovalResponse> approvals = approvalRequestController.findAll();
        ApprovalResponse voidApproval = approvals.stream()
                .filter(a -> a.getType().equals("VOID_TRANSACTION") && a.getReferenceId().equals(txResponse.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(voidApproval.getStatus()).isEqualTo("PENDING");

        // Step 3: adminUser (who requested it) attempts to approve or reject their own request.
        // It must throw AccessDeniedException (403 Forbidden).
        assertThatThrownBy(() -> approvalRequestController.approveRequest(voidApproval.getId()))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("Anda tidak memiliki izin untuk memproses pengajuan ini.");

        RejectApprovalRequest rejectRequest = new RejectApprovalRequest();
        rejectRequest.setReason("Alasan test");
        assertThatThrownBy(() -> approvalRequestController.rejectRequest(voidApproval.getId(), rejectRequest))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("Anda tidak memiliki izin untuk memproses pengajuan ini.");

        // Step 4: Another MANAGEMENT user (adminUser2) tries to approve/reject.
        // It must also throw AccessDeniedException (since it was created by MANAGEMENT, only GUDANG can approve).
        authenticateAs(adminUser2);
        assertThatThrownBy(() -> approvalRequestController.approveRequest(voidApproval.getId()))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("Anda tidak memiliki izin untuk memproses pengajuan ini.");

        assertThatThrownBy(() -> approvalRequestController.rejectRequest(voidApproval.getId(), rejectRequest))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("Anda tidak memiliki izin untuk memproses pengajuan ini.");

        // Step 5: Now test a request created by GUDANG.
        // Authenticate as gudangUser and submit stock adjustment (this creates an approval request).
        authenticateAs(gudangUser);
        StockAdjustmentRequest adjRequest = new StockAdjustmentRequest();
        adjRequest.setNewStock(500.0);
        adjRequest.setReason("Fisik rusak");

        assertThatThrownBy(() -> warehouseController.adjustStock(ingredient.getId(), adjRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Pengajuan penyesuaian stok berhasil diajukan");

        approvals = approvalRequestController.findAll();
        ApprovalResponse stockApproval = approvals.stream()
                .filter(a -> a.getType().equals("STOCK_ADJUSTMENT") && a.getReferenceId().equals(ingredient.getId()) && a.getStatus().equals("PENDING"))
                .findFirst()
                .orElseThrow();

        // Step 6: gudangUser (who requested it) attempts to approve/reject.
        // It must throw AccessDeniedException.
        assertThatThrownBy(() -> approvalRequestController.approveRequest(stockApproval.getId()))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("Anda tidak memiliki izin untuk memproses pengajuan ini.");

        assertThatThrownBy(() -> approvalRequestController.rejectRequest(stockApproval.getId(), rejectRequest))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("Anda tidak memiliki izin untuk memproses pengajuan ini.");

        // Step 7: Now test GUDANG user can approve MANAGEMENT's voidApproval, and MANAGEMENT user can approve GUDANG's stockApproval.
        authenticateAs(gudangUser);
        ApprovalResponse approvedVoid = approvalRequestController.approveRequest(voidApproval.getId());
        assertThat(approvedVoid.getStatus()).isEqualTo("APPROVED");

        authenticateAs(adminUser);
        ApprovalResponse approvedStock = approvalRequestController.approveRequest(stockApproval.getId());
        assertThat(approvedStock.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void testGetOnlineUsersAndPresence() {
        // Set lastActivity and check presence
        authenticateAs(gudangUser);
        userController.heartbeat(() -> gudangUser.getUsername());

        // Fetch online users
        authenticateAs(kasirUser);
        org.springframework.http.ResponseEntity<List<com.brewledger.brewledger.backend.dto.user.UserResponse>> response =
                userController.getOnlineUsers("GUDANG");
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody().get(0).getUsername()).isEqualTo(gudangUser.getUsername());
        assertThat(response.getBody().get(0).getIsOnline()).isTrue();
    }

    @Test
    void testCashierTransactionHistoryAndPosSummary() {
        // Open shift first as MANAGEMENT for KASIR
        authenticateAs(adminUser);
        OpenShiftRequest openRequest = new OpenShiftRequest();
        openRequest.setOpeningCash(50000.0);
        cashierShiftController.openShift(kasirUser.getId(), openRequest);

        authenticateAs(kasirUser);

        // Create a transaction
        CreateTransactionRequest txRequest = new CreateTransactionRequest();
        txRequest.setTransactionType(TransactionType.TAKE_AWAY);
        txRequest.setPaymentMethod(PaymentMethod.CASH);
        txRequest.setCashReceived(50000.0);
        CreateTransactionItemRequest itemReq = new CreateTransactionItemRequest();
        itemReq.setProductId(product.getId());
        itemReq.setQuantity(1);
        txRequest.setItems(List.of(itemReq));
        TransactionResponse txResponse = transactionController.create(txRequest);

        // Fetch my transactions
        List<TransactionResponse> myTransactions = transactionController.findMyTransactions();
        assertThat(myTransactions).isNotEmpty();
        assertThat(myTransactions.get(0).getTransactionNumber()).isEqualTo(txResponse.getTransactionNumber());

        // Fetch POS summary
        com.brewledger.brewledger.backend.dto.pos.PosSummaryResponse summary = posController.getSummary();
        assertThat(summary.getShiftActive()).isTrue();
        assertThat(summary.getTodaySalesCount()).isEqualTo(1L);
        assertThat(summary.getTodaySalesAmount()).isEqualTo(txResponse.getTotal());
    }

    @Test
    void testStockRequestWorkflowsAndValidation() {
        // Step 1: gudangUser submits stock request
        authenticateAs(gudangUser);
        CreateStockRequest req = new CreateStockRequest();
        req.setIngredientId(ingredient.getId());
        req.setRequestedQuantity(50.0);
        req.setNotes("Butuh kopi");
        StockRequestResponse created = stockRequestController.create(req);
        assertThat(created.getStatus()).isEqualTo("REQUESTED");
        assertThat(created.getTargetRole()).isEqualTo("MANAGEMENT");
        assertThat(created.getRequestedByRole()).isEqualTo("GUDANG");

        // Step 2: gudangUser attempts to self-process -> should fail with AccessDeniedException
        assertThatThrownBy(() -> stockRequestController.process(created.getId()))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("Anda tidak memiliki izin");

        // Step 3: KASIR attempts to process -> should fail (not targetRole)
        authenticateAs(kasirUser);
        assertThatThrownBy(() -> stockRequestController.process(created.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access Denied");

        // Step 4: MANAGEMENT (adminUser) processes
        authenticateAs(adminUser);
        StockRequestResponse processed = stockRequestController.process(created.getId());
        assertThat(processed.getStatus()).isEqualTo("PROCESSING");

        // Step 5: adminUser rejects the request
        RejectApprovalRequest rejectReq = new RejectApprovalRequest();
        rejectReq.setReason("Alasan penolakan");
        StockRequestResponse rejected = stockRequestController.reject(processed.getId(), rejectReq);
        assertThat(rejected.getStatus()).isEqualTo("REJECTED");
        assertThat(rejected.getRejectReason()).isEqualTo("Alasan penolakan");
    }

    @Test
    void testStockRequestCompleteWorkflow() {
        // Step 1: gudangUser submits stock request
        authenticateAs(gudangUser);
        CreateStockRequest req = new CreateStockRequest();
        req.setIngredientId(ingredient.getId());
        req.setRequestedQuantity(50.0);
        req.setNotes("Butuh kopi");
        StockRequestResponse created = stockRequestController.create(req);
        assertThat(created.getStatus()).isEqualTo("REQUESTED");

        // Step 2: MANAGEMENT (adminUser) processes
        authenticateAs(adminUser);
        StockRequestResponse processed = stockRequestController.process(created.getId());
        assertThat(processed.getStatus()).isEqualTo("PROCESSING");

        // Step 3: MANAGEMENT (adminUser) completes the request
        double stockBefore = ingredient.getCurrentStock();
        StockRequestResponse completed = stockRequestController.complete(processed.getId());
        assertThat(completed.getStatus()).isEqualTo("COMPLETED");

        // Verify stock is updated
        Ingredient updatedIngredient = ingredientRepository.findById(ingredient.getId()).orElseThrow();
        assertThat(updatedIngredient.getCurrentStock()).isEqualTo(stockBefore + 50.0);
    }


    @Test
    void testIngredientPriceValidation() {
        authenticateAs(adminUser);

        com.brewledger.brewledger.backend.dto.ingredient.CreateIngredientRequest ingReq = new com.brewledger.brewledger.backend.dto.ingredient.CreateIngredientRequest();
        ingReq.setCode("ING-VAL-" + UUID.randomUUID().toString().substring(0, 8));
        ingReq.setName("Bahan Validasi");
        ingReq.setSupplierId(ingredient.getSupplier().getId());
        ingReq.setUnit("ml");
        ingReq.setMinimumStock(10.0);
        ingReq.setCostPrice(10.0);

        // Try negative purchasePrice
        ingReq.setPurchasePrice(-100.0);
        assertThatThrownBy(() -> ingredientController.create(ingReq))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tidak boleh negatif");

        // Try negative packSize
        ingReq.setPurchasePrice(100.0);
        ingReq.setPackSize(-5.0);
        assertThatThrownBy(() -> ingredientController.create(ingReq))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("harus lebih besar dari 0");
    }

    @Test
    void testOpenCloseShiftManagementOnly() {
        // Login as KASIR
        authenticateAs(kasirUser);
        
        OpenShiftRequest openRequest = new OpenShiftRequest();
        openRequest.setOpeningCash(100000.0);
        
        assertThatThrownBy(() -> cashierShiftController.openShift(kasirUser.getId(), openRequest))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
                
        CloseShiftRequest closeRequest = new CloseShiftRequest();
        closeRequest.setClosingCash(150000.0);
        
        assertThatThrownBy(() -> cashierShiftController.closeShift(1L, closeRequest))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    void testOpenShiftUserIdMismatchAndValidation() {
        // Login as MANAGEMENT
        authenticateAs(adminUser);

        OpenShiftRequest openRequest = new OpenShiftRequest();
        openRequest.setUserId(kasirUser.getId());
        openRequest.setOpeningCash(100000.0);

        // Mismatch: query ID != body ID
        assertThatThrownBy(() -> cashierShiftController.openShift(gudangUser.getId(), openRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId query dan body tidak sama");

        // Missing userId: both query and body are null
        OpenShiftRequest missingUserRequest = new OpenShiftRequest();
        missingUserRequest.setOpeningCash(100000.0);
        assertThatThrownBy(() -> cashierShiftController.openShift(null, missingUserRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId wajib diisi");

        // Invalid user ID
        OpenShiftRequest invalidUserRequest = new OpenShiftRequest();
        invalidUserRequest.setUserId(99999L);
        invalidUserRequest.setOpeningCash(100000.0);
        assertThatThrownBy(() -> cashierShiftController.openShift(null, invalidUserRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User target tidak ditemukan");
    }

    @Test
    void testOpenShiftRoleAndActiveValidation() {
        authenticateAs(adminUser);

        // Target role MANAGEMENT (invalid target role)
        OpenShiftRequest openRequest = new OpenShiftRequest();
        openRequest.setUserId(adminUser2.getId());
        openRequest.setOpeningCash(100000.0);
        
        assertThatThrownBy(() -> cashierShiftController.openShift(null, openRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Role target tidak valid");

        // Target user inactive
        // Create an inactive cashier
        User inactiveKasir = new User();
        inactiveKasir.setFullName("Inactive Cashier");
        inactiveKasir.setUsername("inactive_kasir_" + UUID.randomUUID().toString().substring(0, 8));
        inactiveKasir.setPassword(passwordEncoder.encode("password"));
        inactiveKasir.setActive(false);
        inactiveKasir.setRole(kasirUser.getRole());
        inactiveKasir = userRepository.save(inactiveKasir);

        final Long inactiveId = inactiveKasir.getId();
        OpenShiftRequest openRequestInactive = new OpenShiftRequest();
        openRequestInactive.setUserId(inactiveId);
        openRequestInactive.setOpeningCash(100000.0);
        
        assertThatThrownBy(() -> cashierShiftController.openShift(null, openRequestInactive))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User target tidak aktif");
    }

    @Test
    void testOpenShiftNegativeCashAndConflict() {
        authenticateAs(adminUser);

        // Negative opening cash
        OpenShiftRequest negativeCashRequest = new OpenShiftRequest();
        negativeCashRequest.setUserId(kasirUser.getId());
        negativeCashRequest.setOpeningCash(-50000.0);
        
        assertThatThrownBy(() -> cashierShiftController.openShift(null, negativeCashRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Opening cash tidak boleh negatif");

        // Successful open GUDANG with 0 opening cash
        OpenShiftRequest gudangOpenRequest = new OpenShiftRequest();
        gudangOpenRequest.setUserId(gudangUser.getId());
        gudangOpenRequest.setOpeningCash(0.0);
        
        CashierShiftResponse openGudangShift = cashierShiftController.openShift(null, gudangOpenRequest);
        assertThat(openGudangShift.getStatus()).isEqualTo("OPEN");
        assertThat(openGudangShift.getOpeningCash()).isEqualTo(0.0);

        // Conflict: target user already has an active shift
        OpenShiftRequest duplicateRequest = new OpenShiftRequest();
        duplicateRequest.setUserId(gudangUser.getId());
        duplicateRequest.setOpeningCash(0.0);
        
        assertThatThrownBy(() -> cashierShiftController.openShift(null, duplicateRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("User target sudah memiliki shift aktif");
    }

    @Test
    void testCloseShiftNegativeAndClosedValidations() {
        authenticateAs(adminUser);

        OpenShiftRequest openRequest = new OpenShiftRequest();
        openRequest.setUserId(gudangUser.getId());
        openRequest.setOpeningCash(0.0);
        CashierShiftResponse shift = cashierShiftController.openShift(null, openRequest);

        // Negative closing cash
        CloseShiftRequest negativeClose = new CloseShiftRequest();
        negativeClose.setClosingCash(-10000.0);
        
        assertThatThrownBy(() -> cashierShiftController.closeShift(shift.getId(), negativeClose))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Closing cash tidak boleh negatif");

        // Close successfully
        CloseShiftRequest validClose = new CloseShiftRequest();
        validClose.setClosingCash(10000.0);
        CashierShiftResponse closedShift = cashierShiftController.closeShift(shift.getId(), validClose);
        assertThat(closedShift.getStatus()).isEqualTo("CLOSED");

        // Close again (already closed validation)
        assertThatThrownBy(() -> cashierShiftController.closeShift(shift.getId(), validClose))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Shift ini sudah ditutup");
    }

    @Test
    void testCheckoutWithoutActiveShift() {
        // KASIR user attempts checkout when no active shift is set
        authenticateAs(kasirUser);
        
        CreateTransactionRequest txRequest = new CreateTransactionRequest();
        txRequest.setTransactionType(TransactionType.TAKE_AWAY);
        txRequest.setPaymentMethod(PaymentMethod.CASH);
        txRequest.setCashReceived(20000.0);
        
        CreateTransactionItemRequest itemReq = new CreateTransactionItemRequest();
        itemReq.setProductId(product.getId());
        itemReq.setQuantity(1);
        txRequest.setItems(List.of(itemReq));

        assertThatThrownBy(() -> transactionController.create(txRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Shift kasir belum dibuka oleh Manajemen.");
    }

    @Test
    void testShiftFilteringAndCurrentStatus() {
        // Verify current shift is null (204 no content in controller)
        authenticateAs(kasirUser);
        org.springframework.http.ResponseEntity<CashierShiftResponse> current = cashierShiftController.getCurrentShift();
        assertThat(current.getStatusCode().value()).isEqualTo(204);

        // Open shift for KASIR
        authenticateAs(adminUser);
        OpenShiftRequest openRequest = new OpenShiftRequest();
        openRequest.setUserId(kasirUser.getId());
        openRequest.setOpeningCash(50000.0);
        CashierShiftResponse shift = cashierShiftController.openShift(null, openRequest);

        // Check current status
        authenticateAs(kasirUser);
        current = cashierShiftController.getCurrentShift();
        assertThat(current.getStatusCode().value()).isEqualTo(200);
        assertThat(current.getBody()).isNotNull();
        assertThat(current.getBody().getId()).isEqualTo(shift.getId());

        // Check find all filtering
        authenticateAs(adminUser);
        List<CashierShiftResponse> allOpen = cashierShiftController.findAll("OPEN", "KASIR", null);
        assertThat(allOpen).isNotEmpty();
        assertThat(allOpen.stream().anyMatch(s -> s.getId().equals(shift.getId()))).isTrue();

        List<CashierShiftResponse> noneClosed = cashierShiftController.findAll("CLOSED", "KASIR", null);
        assertThat(noneClosed.stream().anyMatch(s -> s.getId().equals(shift.getId()))).isFalse();
    }

    @Test
    void testStockMovementAuditFields() {
        // Open shift for KASIR
        authenticateAs(adminUser);
        com.brewledger.brewledger.backend.dto.shift.OpenShiftRequest openRequest = new com.brewledger.brewledger.backend.dto.shift.OpenShiftRequest();
        openRequest.setUserId(kasirUser.getId());
        openRequest.setOpeningCash(10000.0);
        cashierShiftController.openShift(null, openRequest);

        // Perform checkout to generate SALE_CONSUMPTION movement
        authenticateAs(kasirUser);
        CreateTransactionRequest txRequest = new CreateTransactionRequest();
        txRequest.setTransactionType(com.brewledger.brewledger.backend.enums.TransactionType.TAKE_AWAY);
        txRequest.setPaymentMethod(com.brewledger.brewledger.backend.enums.PaymentMethod.CASH);
        txRequest.setCashReceived(40000.0);
        
        CreateTransactionItemRequest itemReq = new CreateTransactionItemRequest();
        itemReq.setProductId(product.getId());
        itemReq.setQuantity(2); // needs 20.0 ingredient
        txRequest.setItems(List.of(itemReq));

        transactionController.create(txRequest, "IDEMP-STOCK-MOVE");

        // Verify stock movement was created with correct fields
        List<StockMovement> movements = stockMovementRepository.findAll();
        StockMovement saleMove = movements.stream()
                .filter(m -> "SALE_CONSUMPTION".equals(m.getMovementType()))
                .findFirst()
                .orElse(null);

        assertThat(saleMove).isNotNull();
        assertThat(saleMove.getQuantity()).isEqualTo(-20.0); // negative signed
        assertThat(saleMove.getCreatedBy()).isEqualTo(kasirUser.getUsername()); // createdBy set
        assertThat(saleMove.getIngredient().getId()).isEqualTo(ingredient.getId());
    }

    @Test
    void testCentralizedApprovalsConflictException() {
        // Create an approval request
        authenticateAs(gudangUser);
        com.brewledger.brewledger.backend.dto.warehouse.StockAdjustmentRequest adj = new com.brewledger.brewledger.backend.dto.warehouse.StockAdjustmentRequest();
        adj.setNewStock(500.0);
        adj.setReason("Testing conflict");
        com.brewledger.brewledger.backend.dto.approval.ApprovalResponse resp = approvalRequestService.submitStockAdjustment(ingredient.getId(), adj);

        // Approve once
        authenticateAs(adminUser);
        approvalRequestController.approveRequest(resp.getId());

        // Approve again should throw ConflictException
        assertThatThrownBy(() -> approvalRequestController.approveRequest(resp.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Hanya pengajuan PENDING");

        // Reject also throws ConflictException
        com.brewledger.brewledger.backend.dto.approval.RejectApprovalRequest rejectReq = new com.brewledger.brewledger.backend.dto.approval.RejectApprovalRequest();
        rejectReq.setReason("Reject test");
        assertThatThrownBy(() -> approvalRequestController.rejectRequest(resp.getId(), rejectReq))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Hanya pengajuan PENDING");
    }

    @Test
    void testPOReceiveRecalculatesHppAndCostPrice() {
        // Create PO
        authenticateAs(gudangUser);
        com.brewledger.brewledger.backend.dto.purchase.CreatePurchaseOrderRequest poReq = new com.brewledger.brewledger.backend.dto.purchase.CreatePurchaseOrderRequest();
        poReq.setSupplierId(ingredient.getSupplier().getId());
        poReq.setNotes("PO Test Recalculate");
        
        com.brewledger.brewledger.backend.dto.purchase.PurchaseOrderResponse po = purchaseOrderController.create(poReq);

        // Add item: unitPrice = 1000.0, qty = 10
        com.brewledger.brewledger.backend.dto.purchase.CreatePurchaseOrderItemRequest itemReq = new com.brewledger.brewledger.backend.dto.purchase.CreatePurchaseOrderItemRequest();
        itemReq.setIngredientId(ingredient.getId());
        itemReq.setQuantity(10.0);
        itemReq.setUnitPrice(1000.0);
        purchaseOrderController.addItem(po.getId(), itemReq);

        // Submit for approval
        purchaseOrderController.submitForApproval(po.getId());

        // Approve
        authenticateAs(adminUser);
        purchaseApprovalController.approve(po.getId());

        // Try to approve again -> ConflictException
        assertThatThrownBy(() -> purchaseApprovalController.approve(po.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("PO tidak sedang menunggu persetujuan");

        // Set pack size of ingredient to 5.0
        ingredient.setPackSize(5.0);
        ingredientRepository.save(ingredient);

        // Receive PO
        authenticateAs(gudangUser);
        purchaseOrderController.receive(po.getId());

        // Verify ingredient purchasePrice and costPrice
        Ingredient updatedIng = ingredientRepository.findById(ingredient.getId()).orElseThrow();
        assertThat(updatedIng.getPurchasePrice()).isEqualTo(1000.0);
        assertThat(updatedIng.getCostPrice()).isEqualTo(200.0); // 1000.0 / 5.0
    }

    @Test
    void testReportsGroupingAndQueryParams() {
        authenticateAs(adminUser);
        
        // Query sales report with groupBy WEEK
        com.brewledger.brewledger.backend.dto.report.SalesReportResponse salesReport = reportController.getSalesReport(
                java.time.LocalDate.now().minusDays(30), java.time.LocalDate.now(), null, null, "WEEK");
        assertThat(salesReport).isNotNull();

        // Query purchase report with groupBy MONTH
        com.brewledger.brewledger.backend.dto.report.PurchaseReportResponse purchaseReport = reportController.getPurchaseReport(
                java.time.LocalDate.now().minusDays(30), java.time.LocalDate.now(), null, null, "MONTH");
        assertThat(purchaseReport).isNotNull();
    }

    @Test
    void testHealthCheck() {
        java.util.Map<String, String> health = healthController.health();
        assertThat(health.get("status")).isEqualTo("UP");
        assertThat(health.get("database")).isEqualTo("UP");
        assertThat(health.get("timestamp")).isNotNull();
    }

    private void authenticateAs(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
