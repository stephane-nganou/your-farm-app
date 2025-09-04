package de.farm.app.catalog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {

    private final CatalogService catalogService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public CatalogController(
        CatalogService catalogService, 
        ProductRepository productRepository,
        CategoryRepository categoryRepository
    ){
        this.catalogService = catalogService;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/products")
    public List<Product> products(@RequestParam Optional<String> query) {
        return query.filter(s -> !s.isBlank())
            .map(productRepository::search)
            .orElse(catalogService.listProducts());
    }

    @GetMapping("/products/{id}")
    public Product product(@PathVariable UUID id) { 
        return catalogService.getProduct(id);
    }

    @GetMapping("/categories")
    public List<Category> categories(){ return categoryRepository.findAll(); }
}
