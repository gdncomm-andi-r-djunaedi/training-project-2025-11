package com.gdn.faurihakim.cart.web.model;

import com.blibli.oss.backend.command.loom.executor.CommandExecutor;
import com.blibli.oss.backend.common.helper.ResponseHelper;
import com.blibli.oss.backend.common.model.response.Response;
import com.gdn.faurihakim.cart.command.AddProductToCartCommand;
import com.gdn.faurihakim.cart.command.DeleteCartCommand;
import com.gdn.faurihakim.cart.command.GetMemberCartCommand;
import com.gdn.faurihakim.cart.command.UpdateCartItemCommand;
import com.gdn.faurihakim.cart.command.model.AddProductToCartCommandRequest;
import com.gdn.faurihakim.cart.command.model.DeleteCartCommandRequest;
import com.gdn.faurihakim.cart.command.model.GetMemberCartCommandRequest;
import com.gdn.faurihakim.cart.command.model.UpdateCartItemCommandRequest;
import com.gdn.faurihakim.cart.web.model.request.AddProductToCartWebRequest;
import com.gdn.faurihakim.cart.web.model.request.UpdateCartItemWebRequest;
import com.gdn.faurihakim.cart.web.model.response.AddProductToCartWebResponse;
import com.gdn.faurihakim.cart.web.model.response.DeleteCartWebResponse;
import com.gdn.faurihakim.cart.web.model.response.GetMemberCartWebResponse;
import com.gdn.faurihakim.cart.web.model.response.UpdateCartItemWebResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping(value = "/api")
public class CartController {

        @Autowired
        private CommandExecutor executor;

        @Operation(summary = "Add products to cart")
        @PostMapping(value = "/carts/items", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        public Response<AddProductToCartWebResponse> addProductToCart(
                        @RequestHeader("X-User-Id") String memberId,
                        @RequestBody AddProductToCartWebRequest requestBody) {
                log.info("Receive add products to cart API for memberId: {}", memberId);

                // Map web request to command request
                AddProductToCartCommandRequest commandRequest = AddProductToCartCommandRequest.builder()
                                .memberId(memberId)
                                .products(requestBody.getProducts().stream()
                                                .map(item -> AddProductToCartCommandRequest.ProductItem.builder()
                                                                .productId(item.getProductId())
                                                                .quantity(item.getQuantity())
                                                                .build())
                                                .toList())
                                .build();

                AddProductToCartWebResponse response = executor.execute(AddProductToCartCommand.class, commandRequest);
                return ResponseHelper.ok(response);
        }

        @Operation(summary = "Get member cart")
        @GetMapping(value = "/carts", produces = MediaType.APPLICATION_JSON_VALUE)
        public Response<GetMemberCartWebResponse> getMemberCart(@RequestHeader("X-User-Id") String memberId) {
                log.info("Receive get member cart API for memberId: {}", memberId);

                GetMemberCartWebResponse response = executor.execute(
                                GetMemberCartCommand.class, GetMemberCartCommandRequest.builder()
                                                .memberId(memberId)
                                                .build());

                return ResponseHelper.ok(response);
        }

        @Operation(summary = "Update cart item quantity")
        @PutMapping(value = "/carts/{cartId}/items/{productId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        public Response<UpdateCartItemWebResponse> updateCartItem(
                        @PathVariable String cartId,
                        @PathVariable String productId,
                        @RequestBody UpdateCartItemWebRequest requestBody) {
                log.info("Receive update cart item API for cartId: {}, productId: {}", cartId, productId);

                UpdateCartItemCommandRequest commandRequest = UpdateCartItemCommandRequest.builder()
                                .cartId(cartId)
                                .productId(productId)
                                .quantity(requestBody.getQuantity())
                                .build();
                UpdateCartItemWebResponse response = executor.execute(UpdateCartItemCommand.class, commandRequest);
                return ResponseHelper.ok(response);
        }

        @Operation(summary = "Delete member cart")
        @DeleteMapping(value = "/carts", produces = MediaType.APPLICATION_JSON_VALUE)
        public Response<DeleteCartWebResponse> deleteCart(@RequestHeader("X-User-Id") String memberId) {
                log.info("Receive delete cart API for memberId: {}", memberId);

                DeleteCartWebResponse response = executor.execute(
                                DeleteCartCommand.class, DeleteCartCommandRequest.builder()
                                                .memberId(memberId)
                                                .build());

                return ResponseHelper.ok(response);
        }

        @ExceptionHandler(CartNotFoundException.class)
        public ResponseEntity<Object> handleCartNotFound(CartNotFoundException ex) {
                log.error("Cart not found: {}", ex.getMessage());
                Map<String, Object> body = new HashMap<>();
                body.put("error", "Cart Not Found");
                body.put("message", ex.getMessage());
                body.put("timestamp", LocalDateTime.now().toString());
                return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(MemberNotFoundException.class)
        public ResponseEntity<Object> handleMemberNotFound(MemberNotFoundException ex) {
                log.error("Member not found: {}", ex.getMessage());
                Map<String, Object> body = new HashMap<>();
                body.put("error", "Member Not Found");
                body.put("message", ex.getMessage());
                body.put("timestamp", LocalDateTime.now().toString());
                return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(ProductNotFoundException.class)
        public ResponseEntity<Object> handleProductNotFound(ProductNotFoundException ex) {
                log.error("Product not found in cart: {}", ex.getMessage());
                Map<String, Object> body = new HashMap<>();
                body.put("error", "Product Not Found");
                body.put("message", ex.getMessage());
                body.put("timestamp", LocalDateTime.now().toString());
                return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Object> handleGeneralException(Exception ex) {
                log.error("General exception caught: {} - Message: {}", ex.getClass().getName(), ex.getMessage());

                // Attempt to unwrap if it's a wrapper exception
                if (ex.getCause() instanceof CartNotFoundException) {
                        return handleCartNotFound((CartNotFoundException) ex.getCause());
                } else if (ex.getCause() instanceof MemberNotFoundException) {
                        return handleMemberNotFound((MemberNotFoundException) ex.getCause());
                } else if (ex.getCause() instanceof ProductNotFoundException) {
                        return handleProductNotFound((ProductNotFoundException) ex.getCause());
                }

                Map<String, Object> body = new HashMap<>();
                body.put("error", "Internal Server Error");
                body.put("message", ex.getMessage());
                body.put("exception_type", ex.getClass().getName());
                body.put("timestamp", LocalDateTime.now().toString());
                return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}
