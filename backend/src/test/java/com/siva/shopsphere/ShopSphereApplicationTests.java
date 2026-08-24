package com.siva.shopsphere;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.siva.shopsphere.accounts.repository.AddressRepository;
import com.siva.shopsphere.accounts.repository.UserRepository;
import com.siva.shopsphere.cart.repository.CartItemRepository;
import com.siva.shopsphere.cart.repository.CartRepository;
import com.siva.shopsphere.inventory.repository.InventoryMovementRepository;
import com.siva.shopsphere.inventory.repository.InventoryRepository;
import com.siva.shopsphere.products.repository.BrandRepository;
import com.siva.shopsphere.products.repository.CategoryRepository;
import com.siva.shopsphere.products.repository.ProductRepository;
import com.siva.shopsphere.inventory.service.InventoryService;
import com.siva.shopsphere.security.CurrentUserService;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class ShopSphereApplicationTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AddressRepository addressRepository;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private BrandRepository brandRepository;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private InventoryRepository inventoryRepository;

    @MockBean
    private InventoryMovementRepository inventoryMovementRepository;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private CartItemRepository cartItemRepository;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private CurrentUserService currentUserService;

    @Test
    void applicationContextLoads() {
    }
}
