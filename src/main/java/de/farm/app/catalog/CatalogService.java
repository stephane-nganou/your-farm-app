package de.farm.app.catalog;

import java.util.List;
import java.util.UUID;


public interface CatalogService {

    public List<Product> listProducts();

    public Product getProduct(UUID id);

    public Inventory getOrCreateInventory(Product p);
}
