package springboot.csye6225.UserWebApp.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springboot.csye6225.UserWebApp.user.User;

import javax.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import springboot.csye6225.UserWebApp.user.UserServices;

@RestController
@RequestMapping
@Transactional
public class ProductController {
    private final ProductServices productServices;

    @Autowired
    UserServices userService;

    @Autowired
    public ProductController(ProductServices productServices) {
        this.productServices = productServices;
    }

    @PostMapping(produces = "application/json",path = "v1/product")
    public ResponseEntity<Object> createProduct(HttpServletRequest httpRequest, @RequestBody Product product){
        return productServices.createProduct(httpRequest, product);
    }

    @GetMapping(produces = "application/json",path = "v1/product/{productId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getProductDetails(@PathVariable("productId") Long productId)
    {
        return productServices.getProductDetails(productId);
    }

    @DeleteMapping(produces = "application/json",path = "v1/product/{productId}")
    @Transactional
    public ResponseEntity<Object> deleteProduct(@PathVariable("productId") Long productId, HttpServletRequest httpRequest)
    {
        return productServices.deleteProduct(httpRequest, productId);
    }

    @PutMapping(produces = "application/json",path = "v1/product/{productId}")
    public ResponseEntity<Object> updateProduct(@PathVariable("productId") Long productId, HttpServletRequest httpRequest, @RequestBody Product product)
    {
        return productServices.updateProduct(httpRequest,productId,product);
    }

    @PatchMapping(produces = "application/json",path = "v1/product/{productId}")
    public ResponseEntity<Object> patchProduct(@PathVariable("productId") Long productId, HttpServletRequest httpRequest, @RequestBody Product product)
    {
        return productServices.updateProduct(httpRequest,productId,product);
    }
}
