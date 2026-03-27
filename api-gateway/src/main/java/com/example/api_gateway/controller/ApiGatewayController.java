package com.example.api_gateway.controller;

import com.example.api_gateway.request.*;
import com.example.api_gateway.response.*;
import com.example.api_gateway.service.CartService;
import com.example.api_gateway.service.JwtService;
import com.example.api_gateway.service.MemberService;
import com.example.api_gateway.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/apiGateway")
public class ApiGatewayController {

    @Autowired
    JwtService jwtService;
    @Autowired
    private CartService cartService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private ProductService productService;


    @PostMapping("/member/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        MessageResponse response = memberService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/member/login")
    public ResponseEntity<MessageResponse> login(@Valid @RequestBody LoginRequest request){
        MessageResponse response = memberService.login(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/member/logout")
    public ResponseEntity<MessageResponse> logout(
            @RequestParam("userEmail")
            @NotBlank(message = "Email is required")
            @Pattern(
                    regexp = "^[A-Za-z0-9+_.-]+@(gmail|yahoo|hotmail)\\.com$",
                    message = "Only Gmail, Yahoo, or Hotmail emails allowed"
            )
            String email
    ){
        MessageResponse response = memberService.logout(email);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
    @PostMapping("/product/addProducts")
    public ResponseEntity<List<ProductResponse>> addProducts(@RequestBody List<ProductRequest> productRequests){
        List<ProductResponse> productResponses = productService.addproducts(productRequests);
        return new ResponseEntity<>(productResponses, HttpStatus.OK);
    }

    @GetMapping("/product/getProductListing")
    public ResponseEntity<ProductListResponse> getProductsListing(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        ProductListResponse productListResponse = productService.getProductsListing(pageNumber,pageSize);
        return new ResponseEntity<>(productListResponse,HttpStatus.OK);
    }

    @GetMapping("/product/detail/{itemSku}")
    public ResponseEntity<ProductResponse> getProductDetailByItemSku(@PathVariable String itemSku){
        ProductResponse productResponse = productService.getProductDetailByItemSku(itemSku);
        return new ResponseEntity<>(productResponse,HttpStatus.OK);
    }

    @GetMapping("/product/search")
    public ResponseEntity<ProductListResponse> getProductsBySearchTerm(
            @RequestParam String searchTerm, @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        ProductListResponse productListResponse = productService.getProductBySearchTerm(searchTerm,pageNumber,pageSize);
        return new ResponseEntity<>(productListResponse,HttpStatus.OK);
    }

    @PutMapping("/product/update/{itemSku}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable String itemSku, @RequestBody UpdateProductRequest updateRequest){
        ProductResponse productResponse = productService.updateProduct(itemSku,updateRequest);
        return new ResponseEntity<>(productResponse,HttpStatus.OK);
    }

    @DeleteMapping("/product/deleteProductByItemSku")
    public void deleteProductByItemSku(@RequestParam("itemSku")String itemSku){
        productService.deleteproductByItemSku(itemSku);
    }

    @PostMapping("/cart/addProductToBag")
    public ResponseEntity<AddToCartResponse> addProductToBag(@RequestParam("token")String token, @Valid @RequestBody AddToCart addToCart) throws Exception{
        AddToCartResponse addToCartResponse = cartService.addProductToBag(token,addToCart);
        return new ResponseEntity<>(addToCartResponse, HttpStatus.OK);
    }

    @GetMapping("/cart/getAllProductInCart")
    public ResponseEntity<CartItemListResponse> getAllCartProducts(@RequestParam("token")String token, @RequestParam(name = "page", defaultValue = "0")int page
            , @RequestParam(name = "size",defaultValue = "5")int size) throws Exception{
        CartItemListResponse cartItemListResponse = cartService.getAllCartProductsOFCustomer(token,page,size);
        return new ResponseEntity<>(cartItemListResponse,HttpStatus.OK);
    }

    @DeleteMapping("/cart/deleteAllProductsByCustomerId")
    public void deleteAllProductsByCustomerId(@RequestParam("token")String token) throws Exception{
        cartService.deleteAllproductsByCustomerId(token);
    }

    @DeleteMapping("/cart/deleteAllProductsByCustomerIdAndProductId")
    public void deleteAllProductsByCustomerIdAndProductId(@RequestParam("token")String token
            ,@RequestParam("productId")String productId) throws Exception{
        cartService.deleteAllProductsByCustomeridAndProductId(token,productId);
    }

}
