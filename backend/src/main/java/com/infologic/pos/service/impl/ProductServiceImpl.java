package com.infologic.pos.service.impl;

import com.infologic.pos.exception.ResourceNotFoundException;
import com.infologic.pos.model.Product;
import com.infologic.pos.model.dto.ProductDTO;
import com.infologic.pos.repository.ProductRepository;
import com.infologic.pos.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    
    @Override
    public List<ProductDTO> getAllProducts(String tenantId) {
        return productRepository.findByTenantId(tenantId)
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public ProductDTO getProductById(String tenantId, Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        // Ensure the product belongs to the current tenant
        if (!product.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        
        return ProductDTO.fromEntity(product);
    }
    
    @Override
    public List<ProductDTO> getProductsByCategory(String tenantId, String category) {
        return productRepository.findByTenantIdAndCategory(tenantId, category)
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductDTO> searchProducts(String tenantId, String query) {
        return productRepository.searchProducts(tenantId, query)
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ProductDTO createProduct(String tenantId, ProductDTO productDTO) {
        // Check if SKU already exists for this tenant
        if (productRepository.findByTenantIdAndSku(tenantId, productDTO.getSku()).isPresent()) {
            throw new IllegalArgumentException("Product with SKU " + productDTO.getSku() + " already exists");
        }
        
        Product product = productDTO.toEntity(tenantId);
        Product savedProduct = productRepository.save(product);
        return ProductDTO.fromEntity(savedProduct);
    }
    
    @Override
    @Transactional
    public ProductDTO updateProduct(String tenantId, Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        // Ensure the product belongs to the current tenant
        if (!existingProduct.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        
        // Check if SKU is being changed and if it already exists
        if (!existingProduct.getSku().equals(productDTO.getSku()) &&
            productRepository.findByTenantIdAndSku(tenantId, productDTO.getSku()).isPresent()) {
            throw new IllegalArgumentException("Product with SKU " + productDTO.getSku() + " already exists");
        }
        
        // Update the existing product
        existingProduct.setName(productDTO.getName());
        existingProduct.setSku(productDTO.getSku());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStock(productDTO.getStock());
        existingProduct.setCategory(productDTO.getCategory());
        
        Product updatedProduct = productRepository.save(existingProduct);
        return ProductDTO.fromEntity(updatedProduct);
    }
    
    @Override
    @Transactional
    public void deleteProduct(String tenantId, Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        // Ensure the product belongs to the current tenant
        if (!product.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        
        productRepository.delete(product);
    }
    
    @Override
    public List<ProductDTO> getLowStockProducts(String tenantId, Integer threshold) {
        return productRepository.findByTenantIdAndStockLessThan(tenantId, threshold)
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void updateProductStock(String tenantId, Long productId, Integer quantitySold) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // Ensure the product belongs to the current tenant
        if (!product.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        
        // Check if there's enough stock
        if (product.getStock() < quantitySold) {
            throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
        }
        
        // Update the stock
        product.setStock(product.getStock() - quantitySold);
        productRepository.save(product);
    }
} 