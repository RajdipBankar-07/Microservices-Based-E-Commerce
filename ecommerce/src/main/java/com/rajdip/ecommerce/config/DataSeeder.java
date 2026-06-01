package com.rajdip.ecommerce.config;

import com.rajdip.ecommerce.model.Category;
import com.rajdip.ecommerce.model.Coupon;
import com.rajdip.ecommerce.model.Product;
import com.rajdip.ecommerce.model.User;
import com.rajdip.ecommerce.repository.CategoryRepository;
import com.rajdip.ecommerce.repository.CouponRepository;
import com.rajdip.ecommerce.repository.ProductRepository;
import com.rajdip.ecommerce.repository.UserRepository;
import com.rajdip.ecommerce.service.SequenceGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final SequenceGeneratorService sequenceService;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      ProductRepository productRepository,
                      CouponRepository couponRepository,
                      SequenceGeneratorService sequenceService,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
        this.sequenceService = sequenceService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking database data seeding state...");

        // 1. Seed Users
        if (userRepository.count() == 0) {
            log.info("Seeding users...");

            User admin = new User();
            admin.setId(sequenceService.nextId("users"));
            admin.setName("ShopEasy Admin");
            admin.setEmail("admin@shopeasy.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);

            User customer = new User();
            customer.setId(sequenceService.nextId("users"));
            customer.setName("John Doe");
            customer.setEmail("customer@shopeasy.com");
            customer.setPassword(passwordEncoder.encode("customer123"));
            customer.setRole("CUSTOMER");
            userRepository.save(customer);

            log.info("Successfully seeded Admin and Customer accounts.");
        } else {
            log.info("Users database is already seeded.");
        }

        // 2. Seed Categories
        Category electronics = null;
        Category apparel = null;
        Category home = null;
        Category books = null;

        if (categoryRepository.count() == 0) {
            log.info("Seeding categories...");

            electronics = new Category();
            electronics.setId(sequenceService.nextId("categories"));
            electronics.setName("Electronics");
            electronics.setDescription("Gadgets, devices, and accessories");
            electronics.setCreatedAt(LocalDateTime.now());
            electronics = categoryRepository.save(electronics);

            apparel = new Category();
            apparel.setId(sequenceService.nextId("categories"));
            apparel.setName("Apparel");
            apparel.setDescription("Clothing, footwear, and fashion accessories");
            apparel.setCreatedAt(LocalDateTime.now());
            apparel = categoryRepository.save(apparel);

            home = new Category();
            home.setId(sequenceService.nextId("categories"));
            home.setName("Home & Living");
            home.setDescription("Furniture, appliances, and kitchen items");
            home.setCreatedAt(LocalDateTime.now());
            home = categoryRepository.save(home);

            books = new Category();
            books.setId(sequenceService.nextId("categories"));
            books.setName("Books");
            books.setDescription("Fictional, educational, and reference books");
            books.setCreatedAt(LocalDateTime.now());
            books = categoryRepository.save(books);

            log.info("Successfully seeded 4 product categories.");
        } else {
            log.info("Categories database is already seeded.");
            electronics = categoryRepository.findByNameIgnoreCase("Electronics").orElse(null);
            apparel = categoryRepository.findByNameIgnoreCase("Apparel").orElse(null);
            home = categoryRepository.findByNameIgnoreCase("Home & Living").orElse(null);
            books = categoryRepository.findByNameIgnoreCase("Books").orElse(null);
        }

        // 3. Seed Products
        if (productRepository.count() == 0) {
            log.info("Seeding products...");

            if (electronics != null) {
                Product phone = new Product();
                phone.setId(sequenceService.nextId("products"));
                phone.setName("iPhone 15 Pro");
                phone.setPrice(129999.00);
                phone.setQuantity(50);
                phone.setCategory(electronics);
                productRepository.save(phone);
            }

            if (apparel != null) {
                Product jacket = new Product();
                jacket.setId(sequenceService.nextId("products"));
                jacket.setName("Leather Jacket");
                jacket.setPrice(4999.00);
                jacket.setQuantity(30);
                jacket.setCategory(apparel);
                productRepository.save(jacket);
            }

            if (home != null) {
                Product coffeeMaker = new Product();
                coffeeMaker.setId(sequenceService.nextId("products"));
                coffeeMaker.setName("Espresso Coffee Maker");
                coffeeMaker.setPrice(12499.00);
                coffeeMaker.setQuantity(15);
                coffeeMaker.setCategory(home);
                productRepository.save(coffeeMaker);
            }

            if (books != null) {
                Product springBook = new Product();
                springBook.setId(sequenceService.nextId("products"));
                springBook.setName("Spring Boot Deep Dive");
                springBook.setPrice(799.00);
                springBook.setQuantity(100);
                springBook.setCategory(books);
                productRepository.save(springBook);
            }

            log.info("Successfully seeded 4 sample products.");
        } else {
            log.info("Products database is already seeded.");
        }

        // 4. Seed Coupons
        if (couponRepository.count() == 0) {
            log.info("Seeding coupons...");

            Coupon welcomeCoupon = new Coupon();
            welcomeCoupon.setId(sequenceService.nextId("coupons"));
            welcomeCoupon.setCode("WELCOME10");
            welcomeCoupon.setDescription("10% off for new shoppers");
            welcomeCoupon.setDiscountType("PERCENTAGE");
            welcomeCoupon.setDiscountValue(10.0);
            welcomeCoupon.setMaxDiscountAmount(500.0);
            welcomeCoupon.setMinOrderAmount(1000.0);
            welcomeCoupon.setMaxUses(100);
            welcomeCoupon.setCurrentUses(0);
            welcomeCoupon.setExpiryDate(LocalDate.now().plusMonths(6));
            welcomeCoupon.setActive(true);
            couponRepository.save(welcomeCoupon);

            Coupon flatCoupon = new Coupon();
            flatCoupon.setId(sequenceService.nextId("coupons"));
            flatCoupon.setCode("FLAT50");
            flatCoupon.setDescription("Flat ₹50 off on orders");
            flatCoupon.setDiscountType("FIXED");
            flatCoupon.setDiscountValue(50.0);
            flatCoupon.setMaxDiscountAmount(50.0);
            flatCoupon.setMinOrderAmount(200.0);
            flatCoupon.setMaxUses(500);
            flatCoupon.setCurrentUses(0);
            flatCoupon.setExpiryDate(LocalDate.now().plusMonths(3));
            flatCoupon.setActive(true);
            couponRepository.save(flatCoupon);

            log.info("Successfully seeded 2 sample coupons.");
        } else {
            log.info("Coupons database is already seeded.");
        }

        log.info("Data seeding inspection finished successfully!");
    }
}
