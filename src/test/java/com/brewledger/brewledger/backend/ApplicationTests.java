package com.brewledger.brewledger.backend;

import com.brewledger.brewledger.backend.dto.dashboard.DashboardResponse;
import com.brewledger.brewledger.backend.dto.pos.PosCatalogResponse;
import com.brewledger.brewledger.backend.dto.purchase.CreatePurchaseOrderItemRequest;
import com.brewledger.brewledger.backend.dto.purchase.CreatePurchaseOrderRequest;
import com.brewledger.brewledger.backend.dto.purchase.PurchaseOrderResponse;
import com.brewledger.brewledger.backend.dto.warehouse.WarehouseResponse;
import com.brewledger.brewledger.backend.entity.Ingredient;
import com.brewledger.brewledger.backend.entity.PurchaseOrderStatus;
import com.brewledger.brewledger.backend.entity.Supplier;
import com.brewledger.brewledger.backend.repository.IngredientRepository;
import com.brewledger.brewledger.backend.repository.SupplierRepository;
import com.brewledger.brewledger.backend.service.DashboardService;
import com.brewledger.brewledger.backend.service.PosService;
import com.brewledger.brewledger.backend.service.PurchaseApprovalService;
import com.brewledger.brewledger.backend.service.PurchaseOrderService;
import com.brewledger.brewledger.backend.service.WarehouseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import com.brewledger.brewledger.backend.repository.UserRepository;
import com.brewledger.brewledger.backend.repository.RoleRepository;
import com.brewledger.brewledger.backend.entity.User;
import com.brewledger.brewledger.backend.entity.Role;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationTests {

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private PosService posService;

	@Autowired
	private WarehouseService warehouseService;

	@Autowired
	private PurchaseOrderService purchaseOrderService;

	@Autowired
	private PurchaseApprovalService purchaseApprovalService;

	@Autowired
	private SupplierRepository supplierRepository;

	@Autowired
	private IngredientRepository ingredientRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void dashboardLoadsAllMetrics() {
		DashboardResponse response = dashboardService.getDashboard();

		assertThat(response.getGeneratedAt()).isNotNull();
		assertThat(response.getLastSevenDays()).hasSize(7);
		assertThat(response.getTopSellingProducts()).isNotNull();
		assertThat(response.getRecentTransactions()).isNotNull();
		assertThat(response.getSalesByCategory()).isNotNull();
		assertThat(response.getCriticalStocks()).isNotNull();
	}

	@Test
	@WithMockUser(username = "testadmin", roles = "KASIR")
	void posCatalogLoads() {
		PosCatalogResponse response = posService.getCatalog("");

		assertThat(response.getCashierName()).isEqualTo("Test Admin");
		assertThat(response.getTaxRate()).isEqualTo(0.11);
		assertThat(response.getPaymentMethods()).contains("CASH", "QRIS", "CARD");
		assertThat(response.getProducts()).isNotNull();
	}

	@Test
	@WithMockUser(username = "testadmin", roles = "MANAGEMENT")
	void warehouseWorkspaceLoads() {
		WarehouseResponse response = warehouseService.getWorkspace("");

		assertThat(response.getGeneratedAt()).isNotNull();
		assertThat(response.getInventory()).isNotNull();
		assertThat(response.getProductComposition()).isNotNull();
		assertThat(response.getStockMovements()).isNotNull();
		assertThat(response.getApprovalRequests()).isNotNull();
	}

	@Test
	@WithMockUser(username = "testadmin", roles = "MANAGEMENT")
	void purchaseOrderApprovalFlowUpdatesStock() {
		Supplier supplier = new Supplier();
		supplier.setName("Integration Supplier");
		supplier = supplierRepository.save(supplier);

		Ingredient ingredient = new Ingredient();
		ingredient.setCode("TEST-ING");
		ingredient.setName("Integration Ingredient");
		ingredient.setSupplier(supplier);
		ingredient.setUnit("gram");
		ingredient.setCurrentStock(0.0);
		ingredient.setMinimumStock(10.0);
		ingredient.setCostPrice(2.0);
		ingredient = ingredientRepository.save(ingredient);

		CreatePurchaseOrderRequest createRequest = new CreatePurchaseOrderRequest();
		createRequest.setSupplierId(supplier.getId());
		createRequest.setNotes("Integration approval flow");
		PurchaseOrderResponse purchaseOrder = purchaseOrderService.create(createRequest);

		CreatePurchaseOrderItemRequest itemRequest = new CreatePurchaseOrderItemRequest();
		itemRequest.setIngredientId(ingredient.getId());
		itemRequest.setQuantity(25.0);
		itemRequest.setUnitPrice(2.0);
		purchaseOrderService.addItem(purchaseOrder.getId(), itemRequest);

		assertThat(purchaseOrderService.submitForApproval(purchaseOrder.getId()).getStatus())
				.isEqualTo(PurchaseOrderStatus.PENDING_APPROVAL.name());

		// Create and authenticate as a different admin user to approve (to avoid self-approval error)
		Role adminRole = roleRepository.findByName("MANAGEMENT").orElseGet(() -> {
			Role r = new Role();
			r.setName("MANAGEMENT");
			r.setDescription("Administrator");
			return roleRepository.save(r);
		});
		User secondAdmin = new User();
		secondAdmin.setFullName("Second Admin");
		secondAdmin.setUsername("second_admin_int_test");
		secondAdmin.setPassword("password");
		secondAdmin.setActive(true);
		secondAdmin.setMustChangePassword(false);
		secondAdmin.setRole(adminRole);
		secondAdmin = userRepository.save(secondAdmin);

		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(
						secondAdmin.getUsername(),
						"N/A",
						List.of(new SimpleGrantedAuthority("ROLE_" + secondAdmin.getRole().getName()))
				);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		assertThat(purchaseApprovalService.approve(purchaseOrder.getId()).getStatus())
				.isEqualTo(PurchaseOrderStatus.APPROVED.name());
		assertThat(purchaseOrderService.receive(purchaseOrder.getId()).getStatus())
				.isEqualTo(PurchaseOrderStatus.RECEIVED.name());
		assertThat(ingredientRepository.findById(ingredient.getId()).orElseThrow().getCurrentStock())
				.isEqualTo(25.0);
	}
}
