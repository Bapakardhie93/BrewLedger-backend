package com.brewledger.brewledger.backend;

import com.brewledger.brewledger.backend.controller.ProductController;
import com.brewledger.brewledger.backend.controller.StockRequestController;
import com.brewledger.brewledger.backend.controller.SupplierController;
import com.brewledger.brewledger.backend.dto.product.ProductResponse;
import com.brewledger.brewledger.backend.dto.product.UpdateProductRequest;
import com.brewledger.brewledger.backend.dto.stockrequest.CreateStockRequest;
import com.brewledger.brewledger.backend.dto.stockrequest.StockRequestResponse;
import com.brewledger.brewledger.backend.dto.supplier.SupplierResponse;
import com.brewledger.brewledger.backend.dto.supplier.UpdateSupplierRequest;
import com.brewledger.brewledger.backend.entity.Ingredient;
import com.brewledger.brewledger.backend.entity.Product;
import com.brewledger.brewledger.backend.entity.ProductCategory;
import com.brewledger.brewledger.backend.entity.StockRequestStatus;
import com.brewledger.brewledger.backend.entity.Supplier;
import com.brewledger.brewledger.backend.exception.BusinessException;
import com.brewledger.brewledger.backend.repository.IngredientRepository;
import com.brewledger.brewledger.backend.repository.ProductCategoryRepository;
import com.brewledger.brewledger.backend.repository.ProductRepository;
import com.brewledger.brewledger.backend.repository.SupplierRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ManagementWorkflowIntegrationTests {

    @Autowired
    private ProductController productController;

    @Autowired
    private SupplierController supplierController;

    @Autowired
    private StockRequestController stockRequestController;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void productUpdateValidatesUniquenessAndRole() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        ProductCategory category = new ProductCategory();
        category.setName("Category-" + suffix);
        category = categoryRepository.save(category);

        Product product = createProduct("PRD-A-" + suffix, "Product A", category);
        Product otherProduct = createProduct("PRD-B-" + suffix, "Product B", category);

        authenticateAs("MANAGEMENT");
        UpdateProductRequest request = productUpdateRequest(
                product.getCode(),
                "Product A Updated",
                category.getId(),
                42000.0,
                false
        );

        ProductResponse response = productController.update(product.getId(), request);

        assertThat(response.getName()).isEqualTo("Product A Updated");
        assertThat(response.getSellingPrice()).isEqualTo(42000.0);
        assertThat(response.getActive()).isFalse();

        request.setCode(otherProduct.getCode());
        assertThatThrownBy(() -> productController.update(product.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Kode produk sudah digunakan");

        authenticateAs("GUDANG");
        assertThatThrownBy(() -> productController.update(product.getId(), request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void supplierUpdateAndDeletePreserveIngredientReferences() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Supplier supplier = createSupplier("Supplier A-" + suffix);
        Supplier duplicateSupplier = createSupplier("Supplier B-" + suffix);
        Ingredient ingredient = createIngredient("ING-" + suffix, supplier, true, 10.0);

        authenticateAs("MANAGEMENT");
        UpdateSupplierRequest updateRequest = supplierUpdateRequest(
                supplier.getName(),
                "Contact Updated",
                false
        );
        SupplierResponse response = supplierController.update(supplier.getId(), updateRequest);

        assertThat(response.getContactPerson()).isEqualTo("Contact Updated");
        assertThat(response.getActive()).isFalse();

        updateRequest.setName(duplicateSupplier.getName());
        assertThatThrownBy(() -> supplierController.update(supplier.getId(), updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("sudah ada");

        assertThatThrownBy(() -> supplierController.delete(supplier.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("masih digunakan oleh ingredient");
        assertThat(ingredientRepository.findById(ingredient.getId())).isPresent();
        assertThat(supplierRepository.findById(supplier.getId())).isPresent();

        Supplier unusedSupplier = createSupplier("Unused Supplier-" + suffix);
        supplierController.delete(unusedSupplier.getId());
        assertThat(supplierRepository.findById(unusedSupplier.getId())).isEmpty();

        authenticateAs("GUDANG");
        assertThatThrownBy(() -> supplierController.update(supplier.getId(), updateRequest))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void stockRequestLifecycleValidatesRbacStatusAndDoesNotChangeStock() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Supplier supplier = createSupplier("Stock Supplier-" + suffix);
        Ingredient ingredient = createIngredient("REQ-" + suffix, supplier, true, 55.0);

        CreateStockRequest createRequest = new CreateStockRequest();
        createRequest.setIngredientId(ingredient.getId());
        createRequest.setRequestedQuantity(25.0);
        createRequest.setNotes("Restock untuk operasional");

        authenticateAs("MANAGEMENT");
        StockRequestResponse created = stockRequestController.create(createRequest);

        assertThat(created.getStatus()).isEqualTo(StockRequestStatus.REQUESTED.name());
        assertThat(created.getRequestedByName()).isEqualTo("Test Admin");
        assertThat(created.getProcessedByName()).isNull();

        authenticateAs("KASIR");
        assertThatThrownBy(() -> stockRequestController.process(created.getId()))
                .isInstanceOf(AccessDeniedException.class);

        authenticateAs("GUDANG");
        assertThatThrownBy(() -> stockRequestController.complete(created.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("harus berstatus PROCESSING");

        StockRequestResponse processing = stockRequestController.process(created.getId());
        assertThat(processing.getStatus()).isEqualTo(StockRequestStatus.PROCESSING.name());
        assertThat(processing.getProcessedByName()).isEqualTo("Test Admin");

        assertThatThrownBy(() -> stockRequestController.process(created.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("harus berstatus REQUESTED");

        StockRequestResponse completed = stockRequestController.complete(created.getId());
        assertThat(completed.getStatus()).isEqualTo(StockRequestStatus.COMPLETED.name());
        assertThat(completed.getCompletedAt()).isNotNull();
        assertThat(ingredientRepository.findById(ingredient.getId()).orElseThrow().getCurrentStock())
                .isEqualTo(80.0);

        assertThat(stockRequestController.findAll())
                .extracting(StockRequestResponse::getId)
                .contains(created.getId());

        authenticateAs("KASIR");
        assertThatThrownBy(() -> stockRequestController.create(createRequest))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void stockRequestRejectsInactiveIngredient() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Supplier supplier = createSupplier("Inactive Supplier-" + suffix);
        Ingredient ingredient = createIngredient("OFF-" + suffix, supplier, false, 0.0);

        CreateStockRequest request = new CreateStockRequest();
        request.setIngredientId(ingredient.getId());
        request.setRequestedQuantity(5.0);

        authenticateAs("MANAGEMENT");
        assertThatThrownBy(() -> stockRequestController.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ingredient tidak aktif");
    }

    private Product createProduct(String code, String name, ProductCategory category) {
        Product product = new Product();
        product.setCode(code);
        product.setName(name);
        product.setCategory(category);
        product.setSellingPrice(30000.0);
        return productRepository.save(product);
    }

    private UpdateProductRequest productUpdateRequest(
            String code,
            String name,
            Long categoryId,
            Double sellingPrice,
            Boolean active
    ) {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setCode(code);
        request.setName(name);
        request.setCategoryId(categoryId);
        request.setSellingPrice(sellingPrice);
        request.setDescription("Updated description");
        request.setActive(active);
        return request;
    }

    private Supplier createSupplier(String name) {
        Supplier supplier = new Supplier();
        supplier.setName(name);
        return supplierRepository.save(supplier);
    }

    private UpdateSupplierRequest supplierUpdateRequest(
            String name,
            String contactPerson,
            Boolean active
    ) {
        UpdateSupplierRequest request = new UpdateSupplierRequest();
        request.setName(name);
        request.setContactPerson(contactPerson);
        request.setPhone("08123456789");
        request.setEmail("supplier@example.com");
        request.setAddress("Jakarta");
        request.setActive(active);
        return request;
    }

    private Ingredient createIngredient(
            String code,
            Supplier supplier,
            boolean active,
            double currentStock
    ) {
        Ingredient ingredient = new Ingredient();
        ingredient.setCode(code);
        ingredient.setName("Ingredient " + code);
        ingredient.setSupplier(supplier);
        ingredient.setUnit("gram");
        ingredient.setCurrentStock(currentStock);
        ingredient.setMinimumStock(10.0);
        ingredient.setCostPrice(100.0);
        ingredient.setActive(active);
        return ingredientRepository.save(ingredient);
    }

    private void authenticateAs(String role) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "testadmin",
                        "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
