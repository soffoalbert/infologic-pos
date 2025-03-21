package com.infologic.pos.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infologic.pos.config.tenant.TenantContext;
import com.infologic.pos.dto.ProductDTO;
import com.infologic.pos.event.InventoryEvent;
import com.infologic.pos.event.InventoryEvent.InventoryEventType;
import com.infologic.pos.exception.ResourceAlreadyExistsException;
import com.infologic.pos.exception.ResourceNotFoundException;
import com.infologic.pos.model.Product;
import com.infologic.pos.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final EventPublisherService eventPublisher;

    /**
     * Create a new product
     *
     * @param productDTO the product DTO
     * @return the created product
     */
    @Transactional
    public Product createProduct(ProductDTO productDTO) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating product with name '{}' for tenant {}", productDTO.getName(), tenantId);

        // Check if product already exists with the same SKU
        if (productDTO.getSku() != null && !productDTO.getSku().isEmpty() && 
            productRepository.existsBySkuAndTenantId(productDTO.getSku(), tenantId)) {
            throw new ResourceAlreadyExistsException("Product with SKU " + productDTO.getSku() + " already exists");
        }

        // Check if product already exists with the same barcode
        if (productDTO.getBarcode() != null && !productDTO.getBarcode().isEmpty() && 
            productRepository.existsByBarcodeAndTenantId(productDTO.getBarcode(), tenantId)) {
            throw new ResourceAlreadyExistsException("Product with barcode " + productDTO.getBarcode() + " already exists");
        }

        // Create and save the product
        Product product = Product.builder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .stockQuantity(productDTO.getStockQuantity())
                .alertThreshold(productDTO.getAlertThreshold() != null ? productDTO.getAlertThreshold() : 5)
                .sku(productDTO.getSku())
                .barcode(productDTO.getBarcode())
                .category(productDTO.getCategory())
                .imageUrl(productDTO.getImageUrl())
                .active(true)
                .tenantId(tenantId)
                .build();

        Product savedProduct = productRepository.save(product);
        
        // Publish inventory event for product creation
        eventPublisher.publishInventoryEvent(new InventoryEvent(
            tenantId, 
            "system", 
            savedProduct, 
            savedProduct.getStockQuantity(), 
            InventoryEventType.PRODUCT_CREATED
        ));
        
        return savedProduct;
    }

    /**
     * Get a product by ID
     *
     * @param id the product ID
     * @return the product
     */
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting product with ID {} for tenant {}", id, tenantId);

        return productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    /**
     * Get all products
     *
     * @param pageable pagination information
     * @return page of products
     */
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting all products for tenant {}", tenantId);

        return productRepository.findByTenantId(tenantId, pageable);
    }

    /**
     * Get all products (without pagination)
     * 
     * @return list of all products
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting all products (unpaginated) for tenant {}", tenantId);
        
        return productRepository.findByTenantIdOrderByNameAsc(tenantId);
    }

    /**
     * Search products by name
     *
     * @param name     the name to search for
     * @param pageable pagination information
     * @return page of products
     */
    @Transactional(readOnly = true)
    public Page<Product> searchProductsByName(String name, Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Searching products by name '{}' for tenant {}", name, tenantId);

        return productRepository.findByNameContainingAndTenantId(name, tenantId, pageable);
    }
    
    /**
     * Search products by various criteria (name, SKU, barcode, etc.)
     * 
     * @param query the search term
     * @return list of matching products
     */
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String query) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Searching products with term '{}' for tenant {}", query, tenantId);
        
        return productRepository.searchProducts(tenantId, query);
    }

    /**
     * Get products by category
     *
     * @param category the category
     * @param pageable pagination information
     * @return page of products
     */
    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategory(String category, Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting products by category '{}' for tenant {}", category, tenantId);

        return productRepository.findByCategoryAndTenantId(category, tenantId, pageable);
    }
    
    /**
     * Get products by category (without pagination)
     * 
     * @param category the category name
     * @return list of products in the category
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting products by category '{}' for tenant {}", category, tenantId);
        
        return productRepository.findByCategoryAndTenantIdOrderByNameAsc(category, tenantId);
    }

    /**
     * Update a product
     *
     * @param id         the product ID
     * @param productDTO the product DTO
     * @return the updated product
     */
    @Transactional
    public Product updateProduct(Long id, ProductDTO productDTO) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Updating product with ID {} for tenant {}", id, tenantId);

        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // Check if another product exists with the same SKU
        if (productDTO.getSku() != null && !productDTO.getSku().isEmpty() && 
            !productDTO.getSku().equals(product.getSku()) && 
            productRepository.existsBySkuAndTenantId(productDTO.getSku(), tenantId)) {
            throw new ResourceAlreadyExistsException("Another product with SKU " + productDTO.getSku() + " already exists");
        }

        // Check if another product exists with the same barcode
        if (productDTO.getBarcode() != null && !productDTO.getBarcode().isEmpty() && 
            !productDTO.getBarcode().equals(product.getBarcode()) && 
            productRepository.existsByBarcodeAndTenantId(productDTO.getBarcode(), tenantId)) {
            throw new ResourceAlreadyExistsException("Another product with barcode " + productDTO.getBarcode() + " already exists");
        }

        // Update the product
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setSku(productDTO.getSku());
        product.setBarcode(productDTO.getBarcode());
        product.setCategory(productDTO.getCategory());
        product.setImageUrl(productDTO.getImageUrl());
        if (productDTO.getAlertThreshold() != null) {
            product.setAlertThreshold(productDTO.getAlertThreshold());
        }

        Product updatedProduct = productRepository.save(product);
        
        // Publish inventory event for product update
        eventPublisher.publishInventoryEvent(new InventoryEvent(
            tenantId, 
            "system", 
            updatedProduct, 
            0, // No stock change in this operation
            InventoryEventType.PRODUCT_UPDATED
        ));
        
        return updatedProduct;
    }

    /**
     * Delete a product
     *
     * @param id the product ID
     */
    @Transactional
    public void deleteProduct(Long id) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Deleting product with ID {} for tenant {}", id, tenantId);

        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        productRepository.delete(product);
    }

    /**
     * Get products with low stock
     *
     * @param threshold the stock threshold
     * @return list of products with low stock
     */
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(Integer threshold) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting products with stock below {} for tenant {}", threshold, tenantId);

        return productRepository.findByStockQuantityLessThanAndTenantId(threshold, tenantId);
    }
    
    /**
     * Get products with stock below their alert threshold
     * 
     * @return list of products with low stock
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsBelowAlertThreshold() {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting products below alert threshold for tenant {}", tenantId);
        
        return productRepository.findProductsBelowAlertThreshold(tenantId);
    }

    /**
     * Update product stock
     *
     * @param id       the product ID
     * @param quantity the quantity to add (positive) or remove (negative)
     * @return the updated product
     */
    @Transactional
    public Product updateStock(Long id, Integer quantity) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Updating stock for product with ID {} by {} for tenant {}", id, quantity, tenantId);

        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        int oldStock = product.getStockQuantity();
        int newStock = oldStock + quantity;
        
        if (newStock < 0) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        product.setStockQuantity(newStock);
        Product updatedProduct = productRepository.save(product);
        
        // Publish appropriate inventory event based on stock change
        InventoryEventType eventType = quantity > 0 ? 
            InventoryEventType.STOCK_INCREASED : 
            InventoryEventType.STOCK_DECREASED;
            
        eventPublisher.publishInventoryEvent(new InventoryEvent(
            tenantId, 
            "system", 
            updatedProduct, 
            Math.abs(quantity), 
            eventType
        ));
        
        // Check if we need to send a stock alert
        if (updatedProduct.getAlertThreshold() != null && 
            newStock <= updatedProduct.getAlertThreshold() && 
            oldStock > updatedProduct.getAlertThreshold()) {
            
            eventPublisher.publishInventoryEvent(new InventoryEvent(
                tenantId, 
                "system", 
                updatedProduct, 
                0, 
                InventoryEventType.STOCK_ALERT
            ));
        }
        
        // Check if we're out of stock
        if (newStock == 0 && oldStock > 0) {
            eventPublisher.publishInventoryEvent(new InventoryEvent(
                tenantId, 
                "system", 
                updatedProduct, 
                0, 
                InventoryEventType.OUT_OF_STOCK
            ));
        }
        
        return updatedProduct;
    }
} 