package com.brightloop.system.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        return ProductDTO.fromEntity(product);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        Product product = new Product(
                dto.getName(),
                dto.getType(),
                dto.getSellingPrice(),
                dto.getCostPrice(),
                dto.getNotes()
        );

        if (dto.getVariants() != null) {
            for (ProductVariantDTO vDto : dto.getVariants()) {
                ProductVariant variant = new ProductVariant(
                        product,
                        vDto.getVariantName(),
                        vDto.getAdditionalPrice(),
                        vDto.getSku()
                );
                product.addVariant(variant);
            }
        }

        Product saved = productRepository.save(product);
        return ProductDTO.fromEntity(saved);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

        product.setName(dto.getName());
        product.setType(dto.getType());
        product.setSellingPrice(dto.getSellingPrice());
        product.setCostPrice(dto.getCostPrice());
        product.setNotes(dto.getNotes());
        product.setActive(dto.isActive());

        // Update variants
        product.getVariants().clear();
        if (dto.getVariants() != null) {
            for (ProductVariantDTO vDto : dto.getVariants()) {
                ProductVariant variant = new ProductVariant(
                        product,
                        vDto.getVariantName(),
                        vDto.getAdditionalPrice(),
                        vDto.getSku()
                );
                product.addVariant(variant);
            }
        }

        Product updated = productRepository.save(product);
        return ProductDTO.fromEntity(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        product.setActive(false); // soft-delete to preserve billing history integrity
        productRepository.save(product);
    }
}
