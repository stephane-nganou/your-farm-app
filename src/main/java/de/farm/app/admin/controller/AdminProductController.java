package de.farm.app.admin.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.farm.app.admin.dto.AdjustRequest;
import de.farm.app.admin.dto.ProductRequest;
import de.farm.app.catalog.Category;
import de.farm.app.catalog.CategoryRepository;
import de.farm.app.catalog.Inventory;
import de.farm.app.catalog.InventoryRepository;
import de.farm.app.catalog.Product;
import de.farm.app.catalog.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final InventoryRepository inventoryRepo;

    @GetMapping
    public Page<Product> list(@RequestParam Optional<String> q,
            @RequestParam Optional<UUID> categoryId,
            @RequestParam Optional<Boolean> active,
            Pageable pageable) {
        // Simple filtering: use Example or custom query; here ExampleMatcher
        var probe = new Product();
        active.ifPresent(probe::setActive);
        categoryId.ifPresent(id -> {
            var c = new Category();
            c.setId(id);
            probe.setCategory(c);
        });
        ExampleMatcher matcher = ExampleMatcher.matchingAll().withIgnoreNullValues()
                .withMatcher("name", m -> m.contains().ignoreCase());
        q.ifPresent(s -> probe.setName(s));
        return productRepo.findAll(Example.of(probe, matcher), pageable);
    }

    @PostMapping
    public Product create(@Valid @RequestBody ProductRequest pRequest) {
        var category = categoryRepo.findById(pRequest.categoryId()).orElseThrow();
        var product = new Product();
        product.setSku(pRequest.sku());
        product.setName(pRequest.name());
        product.setDescription(pRequest.description());
        product.setPriceCents(pRequest.priceCents());
        product.setCurrency(pRequest.currency());
        product.setCategory(category);
        product.setUnit(pRequest.unit());
        product.setImageUrl(pRequest.imageUrl());
        product.setActive(pRequest.active());
        product.setTaxRate(pRequest.taxRate());

        final Product savedProduct = productRepo.save(product);
        inventoryRepo.findByProduct_Id(product.getId())
                .orElseGet(() -> {
                    var inv = new Inventory();
                    inv.setProduct(savedProduct);
                    inv.setQuantity(0);
                    inv.setReservedQuantity(0);
                    return inventoryRepo.save(inv);
                });

        return savedProduct;
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        var product = productRepo.findById(id).orElseThrow();
        var category = categoryRepo.findById(request.categoryId()).orElseThrow();

        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPriceCents(request.priceCents());
        product.setCurrency(request.currency());
        product.setCategory(category);
        product.setUnit(request.unit());
        product.setImageUrl(request.imageUrl());
        product.setActive(request.active());
        product.setTaxRate(request.taxRate());

        return productRepo.save(product);
    }

    @PostMapping("/{id}/inventory-adjust")
    public Inventory adjust(@PathVariable UUID id, @RequestBody AdjustRequest request) {
        var inv = inventoryRepo.findByProduct_Id(id).orElseThrow();
        inv.setQuantity(Math.max(0, inv.getQuantity() + request.delta()));
        return inventoryRepo.save(inv);
    }
}
