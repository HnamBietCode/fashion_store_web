package com.fashionstore.fashion_store.config;

import com.fashionstore.fashion_store.entity.Category;
import com.fashionstore.fashion_store.entity.Product;
import com.fashionstore.fashion_store.entity.ProductVariant;
import com.fashionstore.fashion_store.repository.CategoryRepository;
import com.fashionstore.fashion_store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // Chỉ thêm data nếu database trống
        if (categoryRepository.count() == 0) {
            log.info("Initializing sample data...");
            initCategories();
            initProducts();
            log.info("Sample data initialized successfully!");
        }
    }

    private void initCategories() {
        Category aoThun = Category.builder()
                .name("Áo Thun")
                .slug("ao-thun")
                .description("Các loại áo thun nam nữ thời trang")
                .active(true)
                .build();

        Category aoSoMi = Category.builder()
                .name("Áo Sơ Mi")
                .slug("ao-so-mi")
                .description("Áo sơ mi công sở và casual")
                .active(true)
                .build();

        Category quanJean = Category.builder()
                .name("Quần Jean")
                .slug("quan-jean")
                .description("Quần jean nam nữ các kiểu")
                .active(true)
                .build();

        Category quanKaki = Category.builder()
                .name("Quần Kaki")
                .slug("quan-kaki")
                .description("Quần kaki công sở và casual")
                .active(true)
                .build();

        Category vayDam = Category.builder()
                .name("Váy Đầm")
                .slug("vay-dam")
                .description("Váy đầm nữ thời trang")
                .active(true)
                .build();

        categoryRepository.saveAll(Arrays.asList(aoThun, aoSoMi, quanJean, quanKaki, vayDam));
    }

    private void initProducts() {
        Category aoThun = categoryRepository.findBySlug("ao-thun").orElseThrow();
        Category quanJean = categoryRepository.findBySlug("quan-jean").orElseThrow();

        // Product 1
        Product product1 = Product.builder()
                .name("Áo Thun Basic Cotton")
                .slug("ao-thun-basic-cotton")
                .description("Áo thun basic chất liệu cotton 100%, thoáng mát, phù hợp mọi hoạt động.")
                .price(new BigDecimal("199000"))
                .salePrice(new BigDecimal("149000"))
                .stockQuantity(100)
                .featured(true)
                .active(true)
                .category(aoThun)
                .build();

        // Thêm variants
        ProductVariant v1 = ProductVariant.builder()
                .size("S").color("Đen").quantity(20).product(product1).build();
        ProductVariant v2 = ProductVariant.builder()
                .size("M").color("Đen").quantity(25).product(product1).build();
        ProductVariant v3 = ProductVariant.builder()
                .size("L").color("Trắng").quantity(30).product(product1).build();
        product1.setVariants(Arrays.asList(v1, v2, v3));

        // Product 2
        Product product2 = Product.builder()
                .name("Áo Thun Oversize Unisex")
                .slug("ao-thun-oversize-unisex")
                .description("Áo thun oversize phong cách Hàn Quốc, unisex.")
                .price(new BigDecimal("249000"))
                .stockQuantity(80)
                .featured(true)
                .active(true)
                .category(aoThun)
                .build();

        // Product 3
        Product product3 = Product.builder()
                .name("Quần Jean Slim Fit")
                .slug("quan-jean-slim-fit")
                .description("Quần jean slim fit co giãn, thoải mái vận động.")
                .price(new BigDecimal("450000"))
                .salePrice(new BigDecimal("399000"))
                .stockQuantity(60)
                .featured(true)
                .active(true)
                .category(quanJean)
                .build();

        // Product 4
        Product product4 = Product.builder()
                .name("Quần Jean Rách Gối")
                .slug("quan-jean-rach-goi")
                .description("Quần jean rách gối phong cách street style.")
                .price(new BigDecimal("520000"))
                .stockQuantity(45)
                .featured(false)
                .active(true)
                .category(quanJean)
                .build();

        productRepository.saveAll(Arrays.asList(product1, product2, product3, product4));
    }
}