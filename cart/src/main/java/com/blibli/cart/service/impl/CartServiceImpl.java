package com.blibli.cart.service.impl;

import com.blibli.cart.Feign.ProductFeign;
import com.blibli.cart.dto.AddToCartRequestDTO;
import com.blibli.cart.dto.AddToCartResponseDTO;
import com.blibli.cart.dto.ProductsDTO;
import com.blibli.cart.entity.Cart;
import com.blibli.cart.entity.Items;
import com.blibli.cart.repository.CartRepository;
import com.blibli.cart.service.CartService;
import com.blibli.product.dto.CreateProductResponseDTO;
import com.blibli.product.response.GdnResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class CartServiceImpl implements CartService {
    @Autowired
    CartRepository cartRepository;
    @Autowired
    ProductFeign productFeign;

    @Value("${spring.data.maxLimit}")
    private Integer limit;
    @Override
    public AddToCartResponseDTO addProductToCart(String customerEmail, AddToCartRequestDTO addToCartRequestDTO) {
        if(addToCartRequestDTO.getQuantity()<=0)
            throw new RuntimeException("Invalid qty");
        ResponseEntity<GdnResponse<CreateProductResponseDTO>> res = productFeign.getProductProductById(addToCartRequestDTO.getProductSku());
        CreateProductResponseDTO createProductResponseDTO = res.getBody().getData();
        log.info("Calling cart repository for the user"+customerEmail);
        Cart cart = cartRepository.findByUserEmail(customerEmail);
        if(cart ==null)
            cart = new Cart();
        cart.setUserEmail(customerEmail);

        Double totalPrice = (createProductResponseDTO.getProductPrice()* addToCartRequestDTO.getQuantity());
        List<Items> items = cart.getItemsList();
        boolean itemExist = false;
        if(items.size()==limit)
            throw new RuntimeException("Cart size limit exceeded");
        for (Items itr : items) {
            totalPrice += (itr.getProductPrice()*itr.getQuantity());
            if(itr.getProductSku().contains(addToCartRequestDTO.getProductSku())) {
                itemExist = true;
                itr.setQuantity(itr.getQuantity() + addToCartRequestDTO.getQuantity());
            }
        }
        cart.setTotalPrice(totalPrice);

        if(!itemExist) {
            Items newItem = new Items();
            BeanUtils.copyProperties(createProductResponseDTO, newItem);
            newItem.setQuantity(addToCartRequestDTO.getQuantity());
            items.add(newItem);
        }

        cart.setItemsList(items);

        log.info("Calling cart repository for saving the cart data"+cart);
        Cart response = cartRepository.save(cart);

        AddToCartResponseDTO responseDTO = new AddToCartResponseDTO();
        convertResponseToDTO(response,responseDTO);

        return responseDTO;

    }

    private static void convertResponseToDTO(Cart response,AddToCartResponseDTO responseDTO) {
        BeanUtils.copyProperties(response, responseDTO);
        List<ProductsDTO> productsDTOList = new ArrayList<>();
        for (Items itr : response.getItemsList()) {
            ProductsDTO productsDTO = new ProductsDTO();
            BeanUtils.copyProperties(itr, productsDTO);
            productsDTOList.add(productsDTO);
        }

        responseDTO.setProductsDTOList(productsDTOList);
    }

    @Override
    public AddToCartResponseDTO viewCart(String customerEmail) {
        Cart cart = cartRepository.findByUserEmail(customerEmail);

        if(cart==null) {
            return new AddToCartResponseDTO();
        }

        List<String> productIds = new ArrayList<>();
        for(Items itr:cart.getItemsList()){
            productIds.add(itr.getProductSku());
        }
        log.info("Feign call for Product feign"+productIds);
        ResponseEntity<GdnResponse<List<CreateProductResponseDTO>>> res= productFeign.findByListProductId(productIds);
        log.info("Feign call for  response Product feign");
        List<CreateProductResponseDTO> productList = res.getBody().getData();

        HashMap<String, Items> productItemMap = new HashMap<>();

        for(CreateProductResponseDTO productFeign: productList){
            Items items = new Items();
            items.setProductName(productFeign.getProductName());
            items.setProductSku(productFeign.getProductSku());
            items.setProductDesc(productFeign.getProductDesc());
            items.setProductBrand(productFeign.getProductBrand());
            items.setProductCategory(productFeign.getProductCategory());
            productItemMap.put(productFeign.getProductSku(),items);
        }

        List<Items> responseItems = new ArrayList<>();
        Double totalPrice =0.0;
        for(Items itemlist:cart.getItemsList()){
            Items productItem=productItemMap.get(itemlist.getProductSku());
            if(productItem==null)
                continue;
            productItem.setProductPrice(itemlist.getProductPrice());
            productItem.setQuantity(itemlist.getQuantity());
            responseItems.add(productItem);
            totalPrice+= (productItem.getProductPrice()* itemlist.getQuantity());
        }
        cart.setItemsList(new ArrayList<>());
        cart.setItemsList(responseItems);
        cart.setTotalPrice(totalPrice);

        Cart resCart = cartRepository.save(cart);

        AddToCartResponseDTO responseDTO = new AddToCartResponseDTO();
        BeanUtils.copyProperties(resCart,responseDTO);

        convertResponseToDTO(resCart,responseDTO);

        return responseDTO;
    }

    @Override
    public AddToCartResponseDTO deleteBySku(String customerEmail,String productSku) {
        ResponseEntity<GdnResponse<CreateProductResponseDTO>> res = productFeign.getProductProductById(productSku);
        CreateProductResponseDTO createProductResponseDTO =res.getBody().getData();
        if(createProductResponseDTO==null)
            throw new RuntimeException("Product sku is invali");
        Cart cart = cartRepository.findByUserEmail(customerEmail);

        List<Items> items = cart.getItemsList();
        if(items.isEmpty())
            throw new RuntimeException("Empty product list");
        boolean flag=false;
        List<Items> newList = new ArrayList<>();
        Double price =0.0;
        for(Items itr: items){
            if(itr.getProductSku().contains(productSku)){
                flag =true;
                price = (itr.getProductPrice()* itr.getQuantity());
            }
            else
                newList.add(itr);
        }

        cart.setItemsList(newList);

        Cart response;
        if(newList.isEmpty()){
            log.info("Deleting the user email for customer cart"+customerEmail);
            response = cartRepository.deleteByUserEmail(customerEmail);
            response.setTotalPrice(0.0);
            response.setItemsList(new ArrayList<>());
        }
        else {
            log.info("Deleting the user sku for customer cart"+customerEmail);
            response = cartRepository.save(cart);
            response.setTotalPrice(response.getTotalPrice()-price);
        }
        AddToCartResponseDTO responseDTO = new AddToCartResponseDTO();
        convertResponseToDTO(response, responseDTO);

        return responseDTO;
    }

    @Override
    public AddToCartResponseDTO deletAllItems(String customerEmail) {
        log.info("Deleting the user email for customer cart"+customerEmail);
        Cart response = cartRepository.deleteByUserEmail(customerEmail);
        response.setTotalPrice(0.0);
        response.setItemsList(new ArrayList<>());
        AddToCartResponseDTO responseDTO = new AddToCartResponseDTO();
        convertResponseToDTO(response, responseDTO);

        return responseDTO;
    }
}
