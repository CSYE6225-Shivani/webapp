package springboot.csye6225.UserWebApp.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import springboot.csye6225.UserWebApp.image.ImageServices;
import springboot.csye6225.UserWebApp.message.Message;
import springboot.csye6225.UserWebApp.user.UserServices;

@RestController
@RequestMapping
@Transactional
public class ProductController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    ProductServices productServices;

    @Autowired
    ImageServices imageServices;

    @Autowired
    UserServices userService;

    @Autowired
    Message message;

    @Autowired
    public ProductController(ProductServices productServices,ImageServices imageServices) {
        this.productServices = productServices;
        this.imageServices = imageServices;
    }

    @PostMapping(produces = "application/json",path = "v1/product")
    public ResponseEntity<Object> createProduct(HttpServletRequest httpRequest, @RequestBody Product product){

        logger.info("Inside createProduct controller");
        ResponseEntity<Object> result = productServices.createProduct(httpRequest, product);
        if(!result.getStatusCode().equals(HttpStatus.CREATED))
        {
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            logger.info("Exiting createProduct controller");
            return new ResponseEntity(productServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else
        {
            logger.info("Exiting createProduct controller");
            return new ResponseEntity(result.getBody(),result.getStatusCode());
        }

    }

    @GetMapping(produces = "application/json",path = "v1/product/{productId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getProductDetails(@PathVariable("productId") Long productId)
    {
        logger.info("Inside getProductDetails controller");
        ResponseEntity<Object> result = productServices.getProductDetails(productId);
        if(!result.getStatusCode().equals(HttpStatus.OK))
        {
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            logger.info("Exiting getProductDetails controller");
            return new ResponseEntity<>(productServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else
        {
            logger.info("Exiting getProductDetails controller");
            return new ResponseEntity<>(result.getBody(),result.getStatusCode());
        }
    }


    @DeleteMapping(produces = "application/json",path = "v1/product/{productId}")
    public ResponseEntity<Object> deleteProduct(@PathVariable("productId") Long productId, HttpServletRequest httpRequest)
    {
        logger.info("Inside deleteProduct controller");
        ResponseEntity<Object> result = productServices.deleteProduct(httpRequest, productId);
        if(!result.getStatusCode().equals(HttpStatus.NO_CONTENT))
        {
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            logger.info("Exiting deleteProduct controller");
            return new ResponseEntity<>(productServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else {
            logger.info("Exiting deleteProduct controller");
            return new ResponseEntity<>("Product deleted successfully!",result.getStatusCode());
        }
    }

    @PutMapping(produces = "application/json",path = "v1/product/{productId}")
    public ResponseEntity<Object> updateProduct(@PathVariable("productId") Long productId, HttpServletRequest httpRequest, @RequestBody Product product)
    {
        logger.info("Inside updateProduct controller");
        ResponseEntity<Object> result = productServices.updateProduct(httpRequest,productId,product);
        if(!result.getStatusCode().equals(HttpStatus.NO_CONTENT))
        {
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            logger.info("Exiting updateProduct controller");
            return new ResponseEntity<>(productServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else
        {
            logger.info("Exiting updateProduct controller");
            return new ResponseEntity<>("Product Updated successfully", result.getStatusCode());
        }
    }

    @PatchMapping(produces = "application/json",path = "v1/product/{productId}")
    public ResponseEntity<Object> patchProduct(@PathVariable("productId") Long productId, HttpServletRequest httpRequest, @RequestBody Product product)
    {
        logger.info("Inside patchProduct controller");
        ResponseEntity<Object> result = productServices.patchProduct(httpRequest,productId,product);
        if(!result.getStatusCode().equals(HttpStatus.NO_CONTENT))
        {
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            logger.info("Exiting patchProduct controller");
            return new ResponseEntity<>(productServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else
        {
            logger.info("Exiting patchProduct controller");
            return new ResponseEntity<>("Product updated successfully",result.getStatusCode());
        }
    }
}
