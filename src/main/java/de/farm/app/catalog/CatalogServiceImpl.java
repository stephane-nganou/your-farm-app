package de.farm.app.catalog;

import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final ProductRepository productRepo;
    private final InventoryRepository inventoryRepo;

    @Override
    @Cacheable(value="products_all")
    public List<Product> listProducts() {
        return productRepo.findAll();
    }

    @Override
    public Product getProduct(UUID id) {
        return productRepo.findById(id).orElseThrow();
    }

    @Override
    public Inventory getOrCreateInventory(Product p) {
        return inventoryRepo.findByProduct_Id(p.getId()).orElseGet(() -> {
            var inv = new Inventory();
            inv.setProduct(p);
            inv.setQuantity(0);
            inv.setReservedQuantity(0);
            return inventoryRepo.save(inv);
        });
    }

}
