package springboot.csye6225.UserWebApp.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import springboot.csye6225.UserWebApp.user.UserServices;

@RestController
@RequestMapping
@Transactional
public class ProductController {
    private ProductServices productServices;

    @Autowired
    UserServices userService;

    @Autowired
    public ProductController(ProductServices productServices) {
        this.productServices = productServices;
    }

    @PostMapping(produces = "application/json",path = "v1/product")
    public ResponseEntity<Object> createProduct(HttpServletRequest httpRequest, @RequestBody Product product){
        ResponseEntity<Object> result = productServices.createProduct(httpRequest, product);
        return new ResponseEntity<>(result.getBody(),result.getStatusCode());
    }

    @GetMapping(produces = "application/json",path = "v1/product/{productId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getProductDetails(@PathVariable("productId") Long productId)
    {
        ResponseEntity<Object> result = productServices.getProductDetails(productId);
        return new ResponseEntity<>(result.getBody(),result.getStatusCode());
    }

    @DeleteMapping(produces = "application/json",path = "v1/product/{productId}")
    public ResponseEntity<Object> deleteProduct(@PathVariable("productId") Long productId, HttpServletRequest httpRequest)
    {
        ResponseEntity<Object> result = productServices.deleteProduct(httpRequest, productId);
        return new ResponseEntity<>(result.getBody(),result.getStatusCode());
    }

    @PutMapping(produces = "application/json",path = "v1/product/{productId}")
    public ResponseEntity<Object> updateProduct(@PathVariable("productId") Long productId, HttpServletRequest httpRequest, @RequestBody Product product)
    {
        ResponseEntity<Object> result = productServices.updateProduct(httpRequest,productId,product);
        return new ResponseEntity<>(result.getBody(),result.getStatusCode());
    }

    @PatchMapping(produces = "application/json",path = "v1/product/{productId}")
    public ResponseEntity<Object> patchProduct(@PathVariable("productId") Long productId, HttpServletRequest httpRequest, @RequestBody Product product)
    {
        ResponseEntity<Object> result = productServices.updateProduct(httpRequest,productId,product);
        return new ResponseEntity<>(result.getBody(),result.getStatusCode());
    }
}
