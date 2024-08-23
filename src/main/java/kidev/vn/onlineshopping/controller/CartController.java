package kidev.vn.onlineshopping.controller;

import kidev.vn.onlineshopping.Constants;
import kidev.vn.onlineshopping.config.security.service.UserDetailsImpl;
import kidev.vn.onlineshopping.entity.CartItem;
import kidev.vn.onlineshopping.entity.ProductDetail;
import kidev.vn.onlineshopping.model.CommonResponse;
import kidev.vn.onlineshopping.model.cart.CartItemDetail;
import kidev.vn.onlineshopping.model.cart.CartItemBasic;
import kidev.vn.onlineshopping.model.cart.CartItemRequest;
import kidev.vn.onlineshopping.service.CartItemService;
import kidev.vn.onlineshopping.service.CartService;
import kidev.vn.onlineshopping.service.ProductDetailService;
import lombok.AllArgsConstructor;
import netscape.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/carts")
@AllArgsConstructor
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    private final ProductDetailService productDetailService;

    private final CartService cartService;

    private final CartItemService cartItemService;

    @PostMapping("/items")
    public CommonResponse<List<CartItemDetail>> getCartItems(@RequestBody List<CartItemRequest> requests) {
        CommonResponse<List<CartItemDetail>> response = new CommonResponse<>();
        try {
            List<CartItemDetail> cartItemDetails = new ArrayList<>();
            if (!requests.isEmpty()) {
                for (CartItemRequest item : requests) {
                    ProductDetail pd = productDetailService.findOne(item.getDetailId());
                    if (pd != null) {
                        cartItemDetails.add(new CartItemDetail(pd, item.getQuantity()));
                    } else {
                        response.setStatusCode(Constants.RestApiReturnCode.SYS_ERROR);
                        response.setMessage("Product Detail not exist");
                        response.setOutput(null);
                        response.setError(Constants.RestApiReturnCode.SYS_ERROR_TXT);
                        return response;
                    }
                }
                response.setStatusCode(Constants.RestApiReturnCode.SUCCESS);
                response.setError(Constants.RestApiReturnCode.SUCCESS_TXT);
                response.setOutput(cartItemDetails);
            }
        } catch (Exception e) {
            response.setStatusCode(Constants.RestApiReturnCode.SYS_ERROR);
            response.setMessage("System error");
            response.setOutput(null);
            response.setError(Constants.RestApiReturnCode.SYS_ERROR_TXT);
            logger.error("Get products order info", e);
        }
        return response;
    }

    @GetMapping("/items")
    public CommonResponse<List<CartItemDetail>> getCartItems(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        CommonResponse<List<CartItemDetail>> response = new CommonResponse<>();
        try {
            List<CartItemDetail> cartItemDetails = cartService.getAllCartItemsByUserId(userDetails.getId());
            response.setStatusCode(Constants.RestApiReturnCode.SUCCESS);
            response.setError(Constants.RestApiReturnCode.SUCCESS_TXT);
            response.setMessage("Get cart items success");
            response.setOutput(cartItemDetails);
        } catch (Exception e) {
            response.setStatusCode(Constants.RestApiReturnCode.SYS_ERROR);
            response.setMessage("System error");
            response.setOutput(null);
            response.setError(Constants.RestApiReturnCode.SYS_ERROR_TXT);
            logger.error("Get products order info", e);
        }
        return response;
    }

    @PostMapping("/update-item")
    public CommonResponse<CartItemBasic> calculateProductOrderPrice(@RequestBody CartItemRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        CommonResponse<CartItemBasic> response = new CommonResponse<>();
        try {
            ProductDetail pd = productDetailService.findOne(request.getDetailId());
            if (pd == null) {
                response.setStatusCode(Constants.RestApiReturnCode.SYS_ERROR);
                response.setMessage("Product Detail not exist");
                response.setOutput(null);
                response.setError(Constants.RestApiReturnCode.SYS_ERROR_TXT);
                return response;
            }
            CartItemBasic output = null;
            if (userDetails == null) {
                output = new CartItemBasic(pd, request.getQuantity());
                response.setStatusCode(Constants.RestApiReturnCode.SUCCESS);
                response.setMessage(Constants.RestApiReturnCode.SUCCESS_TXT);
                response.setOutput(output);
                return response;
            }
            CartItem cartItem;
            if (request.getId() == null) {
                cartItem = cartItemService.create(request, userDetails.getId());
            } else {
                cartItem = cartItemService.update(request);
            }
            output = new CartItemBasic(cartItem);

            response.setStatusCode(Constants.RestApiReturnCode.SUCCESS);
            response.setMessage(Constants.RestApiReturnCode.SUCCESS_TXT);
            response.setOutput(output);
        } catch (Exception e) {
            response.setStatusCode(Constants.RestApiReturnCode.SYS_ERROR);
            response.setError(Constants.RestApiReturnCode.SYS_ERROR_TXT);
            response.setOutput(null);
            response.setMessage("System error");
            logger.error("update cart item", e);
        }
        return response;
    }
}
