package com.splitcart.product.repo;

import com.splitcart.product.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

  Flux<Product> findByCategory(String category);

  Flux<Product> findByPriceLessThanEqual(Double maxPrice);

  Flux<Product> findByCategoryAndPriceLessThanEqual(String category, Double maxPrice);
}
