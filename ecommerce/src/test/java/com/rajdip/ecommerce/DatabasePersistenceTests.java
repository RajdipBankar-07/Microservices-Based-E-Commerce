package com.rajdip.ecommerce;

import com.rajdip.ecommerce.model.Category;
import com.rajdip.ecommerce.model.Product;
import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.repository.CategoryRepository;
import com.rajdip.ecommerce.repository.ProductRepository;
import com.rajdip.ecommerce.repository.UserRepository;
import com.rajdip.ecommerce.service.SequenceGeneratorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DatabasePersistenceTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SequenceGeneratorService sequenceService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long testUserId;
    private Long testCategoryId;
    private Long testProductId;

    @BeforeEach
    void setUp() {
        // Clean up test data if any leaked from previous runs
        userRepository.findByEmail("test-persist@shopeasy.com").ifPresent(u -> userRepository.delete(u));
        categoryRepository.findByNameIgnoreCase("Test Instruments").ifPresent(c -> {
            productRepository.findByCategory_Id(c.getId()).forEach(p -> productRepository.delete(p));
            categoryRepository.delete(c);
        });
    }

    @AfterEach
    void tearDown() {
        // Clean up test records
        if (testUserId != null) {
            userRepository.deleteById(testUserId);
        }
        if (testProductId != null) {
            productRepository.deleteById(testProductId);
        }
        if (testCategoryId != null) {
            categoryRepository.deleteById(testCategoryId);
        }
    }

    @Test
    void testUserPersistence() {
        // 1. Create a new User
        User user = new User();
        testUserId = sequenceService.nextId("users");
        user.setId(testUserId);
        user.setName("Test Persist User");
        user.setEmail("test-persist@shopeasy.com");
        user.setPassword(passwordEncoder.encode("supersecret"));
        user.setRole("CUSTOMER");

        // 2. Save User
        User savedUser = userRepository.save(user);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isEqualTo(testUserId);

        // 3. Retrieve User
        Optional<User> foundUserOpt = userRepository.findById(testUserId);
        assertThat(foundUserOpt).isPresent();
        User foundUser = foundUserOpt.get();
        assertThat(foundUser.getName()).isEqualTo("Test Persist User");
        assertThat(foundUser.getEmail()).isEqualTo("test-persist@shopeasy.com");
        assertThat(passwordEncoder.matches("supersecret", foundUser.getPassword())).isTrue();

        // 4. Update User
        foundUser.setName("Updated Test Persist");
        User updatedUser = userRepository.save(foundUser);
        assertThat(updatedUser.getName()).isEqualTo("Updated Test Persist");
    }

    @Test
    void testCategoryAndProductPersistence() {
        // 1. Create and Save Category
        Category category = new Category();
        testCategoryId = sequenceService.nextId("categories");
        category.setId(testCategoryId);
        category.setName("Test Instruments");
        category.setDescription("Guitars, keyboards, and other gear");
        category.setCreatedAt(LocalDateTime.now());

        Category savedCategory = categoryRepository.save(category);
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getId()).isEqualTo(testCategoryId);

        // 2. Create and Save Product linked to Category
        Product product = new Product();
        testProductId = sequenceService.nextId("products");
        product.setId(testProductId);
        product.setName("Test Acoustic Guitar");
        product.setPrice(14999.00);
        product.setQuantity(5);
        product.setCategory(savedCategory);

        Product savedProduct = productRepository.save(product);
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isEqualTo(testProductId);
        assertThat(savedProduct.getCategory().getName()).isEqualTo("Test Instruments");

        // 3. Retrieve and Verify Linkage
        Optional<Product> foundProductOpt = productRepository.findById(testProductId);
        assertThat(foundProductOpt).isPresent();
        Product foundProduct = foundProductOpt.get();
        assertThat(foundProduct.getName()).isEqualTo("Test Acoustic Guitar");
        assertThat(foundProduct.getPrice()).isEqualTo(14999.00);
        assertThat(foundProduct.getCategory()).isNotNull();
        assertThat(foundProduct.getCategory().getId()).isEqualTo(testCategoryId);

        // 4. Update Product
        foundProduct.setPrice(15999.00);
        Product updatedProduct = productRepository.save(foundProduct);
        assertThat(updatedProduct.getPrice()).isEqualTo(15999.00);
    }
}
