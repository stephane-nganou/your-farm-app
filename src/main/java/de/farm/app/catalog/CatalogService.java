package de.farm.app.catalog;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public interface CatalogService {

    public List<Product> listProducts();

    public Product getProduct(UUID id);

    public Inventory getOrCreateInventory(Product p);
}
