package de.farm.app.catalog;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("""
            SELECT p FROM PRODUCT p
            WHERE p.active=true
            AND (lower(p.name) like lower(concat('%',:query,'%')) OR
            lower(p.description) like lower(concat('%',:query,'%')))
            """)
    List<Product> search(String query);
}
