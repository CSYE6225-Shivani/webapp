package springboot.csye6225.UserWebApp.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    Optional<Product> findProductById(Long id);
    Optional<Product> findProductBySku(String sku);
}
