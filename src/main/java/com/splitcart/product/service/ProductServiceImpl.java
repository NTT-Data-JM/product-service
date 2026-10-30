package com.splitcart.product.service;

import com.splitcart.product.api.model.Availability;
import com.splitcart.product.api.model.Product;
import com.splitcart.product.repo.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private static final int LOGICAL_STOCK_WHEN_PRESENT = 10;
  private static final int LOGICAL_STOCK_WHEN_ABSENT = 0;

  private final ProductRepository repository;

  @Override
  public Flux<Product> list(String category, BigDecimal maxPrice, int page, int size) {
    return findByFilters(category, maxPrice)
        .skip((long) page * size)
        .take(size)
        .map(this::toApiProduct);
  }

  @Override
  public Mono<Product> getById(String id) {
    return repository.findById(id).map(this::toApiProduct);
  }

  @Override
  public Flux<Availability> availability(List<String> skus) {
    List<String> requested = skus == null ? List.of() : skus;
    return Flux.fromIterable(requested).concatMap(this::logicalAvailability);
  }

  private Flux<com.splitcart.product.model.Product> findByFilters(
      String category, BigDecimal maxPrice) {
    boolean hasCategory = category != null && !category.isBlank();
    boolean hasMaxPrice = maxPrice != null;
    if (hasCategory && hasMaxPrice) {
      return repository.findByCategoryAndPriceLessThanEqual(category, maxPrice.doubleValue());
    }
    if (hasCategory) {
      return repository.findByCategory(category);
    }
    if (hasMaxPrice) {
      return repository.findByPriceLessThanEqual(maxPrice.doubleValue());
    }
    return repository.findAll();
  }

  private Mono<Availability> logicalAvailability(String sku) {
    return repository
        .existsById(sku)
        .map(
            exists ->
                new com.splitcart.product.model.Availability(
                    sku, exists ? LOGICAL_STOCK_WHEN_PRESENT : LOGICAL_STOCK_WHEN_ABSENT))
        .map(this::toApiAvailability);
  }

  private Product toApiProduct(com.splitcart.product.model.Product product) {
    Product apiProduct = new Product();
    apiProduct.setId(product.getId());
    apiProduct.setSku(product.getSku() != null ? product.getSku() : product.getId());
    apiProduct.setName(product.getName());
    apiProduct.setCategory(product.getCategory());
    if (product.getPrice() != null) {
      apiProduct.setPrice(BigDecimal.valueOf(product.getPrice()));
    }
    return apiProduct;
  }

  private Availability toApiAvailability(com.splitcart.product.model.Availability availability) {
    Availability apiAvailability = new Availability();
    apiAvailability.setSku(availability.getSku());
    apiAvailability.setAvailable(availability.getAvailable());
    return apiAvailability;
  }
}
