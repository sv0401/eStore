package kr.ac.hansung.cse.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.hansung.cse.model.Cart;
import kr.ac.hansung.cse.model.CartItem;
import kr.ac.hansung.cse.model.Product;
import kr.ac.hansung.cse.model.User;
import kr.ac.hansung.cse.service.CartItemService;
import kr.ac.hansung.cse.service.CartService;
import kr.ac.hansung.cse.service.ProductService;
import kr.ac.hansung.cse.service.UserService;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

	@Autowired
	private CartService cartService;

	@Autowired
	private CartItemService cartItemService;

	@Autowired
	private UserService userService;

	@Autowired
	private ProductService productService;

	@RequestMapping(value = "/{cartId}", method = RequestMethod.GET)
	public ResponseEntity<Cart> getCartById(@PathVariable(value = "cartId") int cartId) {
		Cart cart = cartService.getCartById(cartId);
		
//		HttpHeaders headers = new HttpHeaders();
//		headers.setCacheControl("max-age=10");
		
//		return new ResponseEntity<Cart>(cart, headers, HttpStatus.OK);
		return new ResponseEntity<Cart>(cart, HttpStatus.OK);
	}

	@RequestMapping(value = "/{cartId}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> clearCart(@PathVariable(value = "cartId") int cartId) {
		Cart cart = cartService.getCartById(cartId);
		cartItemService.removeAllCartItems(cart);

		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(value = "/add/{productId}", method = RequestMethod.PUT)
	public ResponseEntity<Void> addItem(@PathVariable(value = "productId") int productId) {
		Product product = productService.getProductById(productId);

		// 현재 인증된 사용자의 이름
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();

		User user = userService.getUserByUsername(username);
		Cart cart = user.getCart();

		// check if cartitem for a given product already exists
		List<CartItem> cartItems = cart.getCartItems();

		for (int i = 0; i < cartItems.size(); i++) {
			if (product.getId() == cartItems.get(i).getProduct().getId()) {
				CartItem cartItem = cartItems.get(i);

				if (cartItem.getQuantity() >= product.getUnitInStock())
					return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);

				cartItem.setQuantity(cartItem.getQuantity() + 1);
				cartItem.setTotalPrice(product.getPrice() * cartItem.getQuantity());
				cartItemService.addCartItem(cartItem);

				return new ResponseEntity<>(HttpStatus.OK);
			}
		}

		if (0 >= product.getUnitInStock())
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);

		// create new cartItem
		CartItem cartItem = new CartItem();
		cartItem.setQuantity(1);
		cartItem.setTotalPrice(product.getPrice() * cartItem.getQuantity());
		cartItem.setProduct(product);
		cartItem.setCart(cart);

		// bidirectional
		cart.getCartItems().add(cartItem);

		cartItemService.addCartItem(cartItem);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(value = "/cartitem/{productId}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> removeItem(@PathVariable(value = "productId") int productId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();

		User user = userService.getUserByUsername(username);
		Cart cart = user.getCart();

		CartItem cartItem = cartItemService.getCartItemByProductId(cart.getId(), productId);
		cartItemService.removeCartItem(cartItem);

		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(value = "/cartitem/minus/{productId}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> minusItemQuantity(@PathVariable(value = "productId") int productId) {
		Product product = productService.getProductById(productId);
		
		// 현재 인증된 사용자의 이름
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();

		User user = userService.getUserByUsername(username);
		Cart cart = user.getCart();

		List<CartItem> cartItems = cart.getCartItems();

		for (int i = 0; i < cartItems.size(); i++) {
			if (product.getId() == cartItems.get(i).getProduct().getId()) {
				CartItem cartItem = cartItems.get(i);

				if (cartItem.getQuantity() <= 0)
					return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);

				cartItem.setQuantity(cartItem.getQuantity() - 1);
				cartItem.setTotalPrice(product.getPrice() * cartItem.getQuantity());
				cartItemService.addCartItem(cartItem);

				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
		}
			
		return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
	}
	/*
	@RequestMapping(value = "/cartitem/plus/{productId}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> plusItemQuantity(@PathVariable(value = "productId") int productId) {
		Product product = productService.getProductById(productId);
		
		// 현재 인증된 사용자의 이름
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();

		User user = userService.getUserByUsername(username);
		Cart cart = user.getCart();

		List<CartItem> cartItems = cart.getCartItems();

		for (int i = 0; i < cartItems.size(); i++) {
			if (product.getId() == cartItems.get(i).getProduct().getId()) {
				CartItem cartItem = cartItems.get(i);

				if (cartItem.getQuantity() >= product.getUnitInStock())
					return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);

				cartItem.setQuantity(cartItem.getQuantity() + 1);
				cartItem.setTotalPrice(product.getPrice() * cartItem.getQuantity());
				cartItemService.addCartItem(cartItem);

				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
		}
			
		return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
	}
	*/
}
