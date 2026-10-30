package com.splitcart.product.api;

import com.splitcart.product.api.model.Availability;
import com.splitcart.product.api.model.Product;
import com.splitcart.product.service.ProductService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductsApiDelegateImpl implements ProductsApiDelegate {

  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_SIZE = 10;

  private final ProductService service;

  @Override
  public Mono<ResponseEntity<Flux<Product>>> listProducts(
      String category, BigDecimal maxPrice, Integer page, Integer size, ServerWebExchange exchange) {
    int resolvedPage = page == null || page < 0 ? DEFAULT_PAGE : page;
    int resolvedSize = size == null || size < 1 ? DEFAULT_SIZE : size;
    return Mono.just(
        ResponseEntity.ok(service.list(category, maxPrice, resolvedPage, resolvedSize)));
  }

  @Override
  public Mono<ResponseEntity<Product>> getProductById(String id, ServerWebExchange exchange) {
    return service
        .getById(id)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @Override
  public Mono<ResponseEntity<Flux<Availability>>> getAvailability(
      String ids, ServerWebExchange exchange) {
    List<String> skus =
        Arrays.stream(ids.split(",")).map(String::trim).filter(sku -> !sku.isEmpty()).toList();
    return Mono.just(ResponseEntity.ok(service.availability(skus)));
  }
}
