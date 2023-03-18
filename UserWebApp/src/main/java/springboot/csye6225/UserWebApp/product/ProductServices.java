package springboot.csye6225.UserWebApp.product;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import springboot.csye6225.UserWebApp.image.ImageRepository;
import springboot.csye6225.UserWebApp.image.ImageServices;
import springboot.csye6225.UserWebApp.message.Message;
import springboot.csye6225.UserWebApp.user.User;
import springboot.csye6225.UserWebApp.user.UserRepository;
import springboot.csye6225.UserWebApp.user.UserServices;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

@Service
public class ProductServices {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserServices userServices;

    @Value("${aws_s3.s3_bucket_name}")
    private String s3_bucket_name;

    private AmazonS3 s3_client = AmazonS3ClientBuilder.defaultClient();

//    @Autowired
//    private AmazonS3 s3_client;

    @Autowired
    public ProductRepository productRepository;

    @Autowired
    ImageServices imageServices;

    @Autowired
    public UserRepository userRepository;
    @Autowired
    public ImageRepository imageRepository;

    @Autowired
    public ProductServices(ProductRepository productRepository,
                           UserRepository userRepository,
                           ImageRepository imageRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
    }

    public ProductServices() {
    }

    public ResponseEntity<Object> createProduct(HttpServletRequest httpRequest, Product product) {
        logger.info("Inside createProduct service logic");
        ResponseEntity<Object> req_header = userServices.performBasicAuth(httpRequest);
        ResponseEntity<Object> nullCheckResult = performProductNullCheck(product);
        ResponseEntity<Object> validateProductDetails = performValidations(product);

        //Authorizing User Credentials
        if((req_header.getStatusCode().equals(HttpStatus.BAD_REQUEST)) ||
                (req_header.getStatusCode().equals(HttpStatus.UNAUTHORIZED)))
        {
            logger.error("Check if username & password provided are correct");
            logger.info("Exiting createProduct service logic");
            return new ResponseEntity<Object>(req_header.getBody(),req_header.getStatusCode());
        }

        //If any fields are null
        else if(nullCheckResult.getStatusCode() != HttpStatus.OK)
        {
            logger.error("One or more mandatory fields is null");
            logger.info("Exiting createProduct service logic");
            return nullCheckResult;
        }

        else if(product.getId() != null || product.getOwner_user_id() != null ||
                product.getDate_added() != null || product.getDate_last_updated() != null)
        {
            logger.error("One or more of following fields --- ProductId, userID, product added & created date have been provided");
            logger.info("Exiting createProduct service logic");
            return new ResponseEntity<Object>("Please do not provide productId, userId, product added date and product updated date",HttpStatus.BAD_REQUEST);
        }

        //Product fields validation
        else if(validateProductDetails.getStatusCode() != HttpStatus.OK)
        {
            logger.info("Validating provided product details");
            logger.error(validateProductDetails.toString());
            logger.info("Exiting createProduct service logic");
            return validateProductDetails;
        }

        //Create Product
        else {
            logger.info("All checks passed");
            LocalDateTime localNow = LocalDateTime.now();
            ZonedDateTime timeInZ = localNow.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Z"));
            String userDetails = httpRequest.getHeader("Authorization");
            String[] userCredentials = userServices.decodeLogin(userDetails);
            User current = userCredentials.length == 0?null:userServices.fetchUser(userCredentials[0]);

            product.setName(product.getName());
            product.setDescription(product.getDescription());
            product.setManufacturer(product.getManufacturer());
            product.setSku(product.getSku());
            product.setQuantity(product.getQuantity());
            product.setDate_added(timeInZ.toString());
            product.setDate_last_updated(timeInZ.toString());
            product.setOwner_user_id(current.getId());
            logger.info("Saving product to the database");
            productRepository.save(product);

            logger.info("Exiting createProduct service logic");
            return new ResponseEntity<Object>(getJSONBody(product),HttpStatus.CREATED);
        }

    }

    private ResponseEntity<Object> performValidations(Product product) {
        if(product.getSku() != null && productRepository.findProductBySku(product.getSku()).isPresent())
        {
            return new ResponseEntity<Object>("This sku already exists, please enter a different sku",HttpStatus.BAD_REQUEST);
        }
        else if (product.getQuantity() != null && product.getQuantity() < 0) {
            return new ResponseEntity<Object>("Quantity cannot be less than 0",HttpStatus.BAD_REQUEST);
        }
        else if(product.getQuantity() != null && product.getQuantity() > 100)
        {
            return new ResponseEntity<Object>("Quantity cannot be greater than 100",HttpStatus.BAD_REQUEST);
        }
        else {
            return new ResponseEntity<Object>("Product details validated", HttpStatus.OK);
        }
    }

    public ResponseEntity<Object> performProductNullCheck(Product product)
    {
        logger.info("Validating that required fields are not null");
        if(product.getName() == null || product.getName().trim().length() == 0 ||
                product.getSku() == null || product.getSku().trim().length() == 0 ||
                product.getManufacturer() == null || product.getManufacturer().trim().length() == 0 ||
                product.getDescription() == null || product.getDescription().trim().length() == 0 ||
                product.getQuantity() == null || product.getQuantity().toString().trim().length() == 0)
        {
            logger.info("One or more of the required fields is null");
            return new ResponseEntity<Object>("Please verify if you have provided product name, desc, sku, manufacturer, quantity without changing field labels",HttpStatus.BAD_REQUEST);
        }
        else
            logger.info("All required details to create a new product have been provided");
            return new ResponseEntity<Object>("All fields are provided",HttpStatus.OK);
    }

    public ResponseEntity<Object> getProductDetails(Long productId)
    {
        logger.info("Inside getProductDetails service logic");
        logger.info("Fetching details for provided product ID");
        Product product = fetchProduct(productId);
        if(product != null) {
            logger.info("Product exists and response is being returned");
            logger.info("Exiting getProductDetails service logic");
            return new ResponseEntity<Object>(getJSONBody(product),HttpStatus.OK);
        }
         else
        {
            logger.error("Product does not exist in the database");
            logger.info("Exiting getProductDetails service logic");
            return new ResponseEntity<Object>("Product not found",HttpStatus.NOT_FOUND);
        }
    }

    public Product fetchProduct(Long productId)
    {
        logger.info("Fetching the product from the product database..");
        List<Product> productList;
        productList = productRepository.findAll();
        for(Product p: productList)
        {
            if(p.getId() == productId)
            {
                logger.info("Product exists in the database");
                return p;
            }
        }
        logger.info("Product does not exist in the database");
        return null;
    }

    private HashMap<String,Object> getJSONBody(Product product)
    {
        logger.info("Product JSON body being created");
        HashMap<String,Object> map = new HashMap<>();
        map.put("id",product.getId());
        map.put("name",product.getName());
        map.put("sku",product.getSku());
        map.put("description",product.getDescription());
        map.put("manufacturer",product.getManufacturer());
        map.put("quantity",product.getQuantity());
        map.put("date_added",product.getDate_added());
        map.put("date_last_updated",product.getDate_last_updated());
        map.put("owner_user_id",product.getOwner_user_id());

        return map;
    }

    public ResponseEntity<Object> deleteProduct(HttpServletRequest httpRequest, Long productId)
    {
        logger.info("Inside deleteProduct service logic");
        ResponseEntity<Object> req_header = userServices.performBasicAuth(httpRequest);
        String userDetails = httpRequest.getHeader("Authorization");
        if(userDetails == null)
        {
            logger.error("Login details have not been provided");
            logger.info("Exiting deleteProduct service logic");
            return new ResponseEntity<>("Please enter your username and password",HttpStatus.UNAUTHORIZED);
        }
        String[] userCredentials = userDetails == null?null:userServices.decodeLogin(userDetails);
        User current = userCredentials.length == 0?null:userServices.fetchUser(userCredentials[0]);
        Product product = fetchProduct(productId);

        //Authorizing User Credentials
        if((req_header.getStatusCode().equals(HttpStatus.BAD_REQUEST)) ||
                (req_header.getStatusCode().equals(HttpStatus.UNAUTHORIZED)))
        {
            logger.error("Check if username & password provided are correct");
            logger.info("Exiting deleteProduct service logic");
            return new ResponseEntity<Object>(req_header.getBody(),req_header.getStatusCode());
        }

        else if(product != null && product.getOwner_user_id() != current.getId())
        {
            logger.error("Trying to delete a product created by another user");
            logger.info("Exiting deleteProduct service logic");
            return new ResponseEntity<Object>("You can only delete the product you have created!",HttpStatus.FORBIDDEN);
        }
        else {
            if(product != null)
            {
                logger.info("Deleting all the images from S3 bucket for the product before deleting the product itself");
                imageServices.deleteAllImages(productId);
                logger.info("Deleting the product from the database");
                productRepository.deleteById(productId);
                logger.info("Product & its images deleted successfully");
                logger.info("Exiting deleteProduct service logic");
                return new ResponseEntity<Object>("Product deleted successfully",HttpStatus.NO_CONTENT);
            }
            else {
                logger.error("Product does not exist in the database");
                logger.info("Exiting deleteProduct service logic");
                return new ResponseEntity<Object>("Product does not exist",HttpStatus.NOT_FOUND);
            }
        }
    }

    public ResponseEntity<Object> updateProduct(HttpServletRequest httpRequest, Long productId, Product product) {
        logger.info("Inside updateProduct service logic");
        ResponseEntity<Object> req_header = userServices.performBasicAuth(httpRequest);
        String userDetails = httpRequest.getHeader("Authorization");

        if(userDetails == null)
        {
            logger.error("Username & password not provided");
            logger.info("Exiting updateProduct service logic");
            return new ResponseEntity<>("Please enter your username and password",HttpStatus.UNAUTHORIZED);
        }

        String[] userCredentials = userDetails == null? null: userServices.decodeLogin(userDetails);
        User current = userCredentials.length == 0?null:userServices.fetchUser(userCredentials[0]);
        Product productinDB = fetchProduct(productId);

        //Authorizing User Credentials
        if((req_header.getStatusCode().equals(HttpStatus.BAD_REQUEST)) ||
                (req_header.getStatusCode().equals(HttpStatus.UNAUTHORIZED)))
        {
            logger.error("Check if username & password provided are correct");
            logger.info("Exiting updateProduct service logic");
            return new ResponseEntity<>(req_header.getBody(),req_header.getStatusCode());
        }
        else if(productinDB == null)
        {
            logger.error("Product to be updated does not exist in the database");
            logger.info("Exiting updateProduct service logic");
            return new ResponseEntity<Object>("Product does not exist in DB",HttpStatus.NOT_FOUND);
        }
        else if(productinDB != null && productinDB.getOwner_user_id() != current.getId())
        {
            logger.error("Trying to update product created by another user");
            logger.info("Exiting updateProduct service logic");
            return new ResponseEntity<Object>("You can only update the product you have created",HttpStatus.FORBIDDEN);
        }

        else if(!performProductNullCheck(product).getStatusCode().equals(HttpStatus.OK)) {
            logger.error("One or more mandatory fields have not been provided");
            logger.info("Exiting updateProduct service logic");
            return new ResponseEntity<Object>("Please check if you have provided all the required details",HttpStatus.BAD_REQUEST);
        }

        else if(product.getId() != null || product.getOwner_user_id() != null ||
        product.getDate_added() != null || product.getDate_last_updated() != null)
        {
            logger.error("Updates cannot be made to productId, userId, product added date and product updated date");
            logger.info("Exiting updateProduct service logic");
            return new ResponseEntity<Object>("You cannot make updates to productId, userId, product added date and product updated date, please remove these fields.",HttpStatus.BAD_REQUEST);
        }

        else if(product.getName() == null || product.getDescription() == null
        || product.getQuantity() == null || product.getManufacturer() == null ||
        product.getSku() == null)

        {
            logger.error("One or more mandatory fields have not been provided");
            logger.info("Exiting updateProduct service logic");
            return new ResponseEntity<Object>("Please check if you provided name, desc, manufacturer, sku & quantity details",HttpStatus.BAD_REQUEST);
        }
        else if(product.getQuantity() != null && product.getQuantity() < 0)
        {
            logger.error("Entered product quantity is less than 0");
            logger.info("Exiting updateProduct service logic");
            return new ResponseEntity<Object>("Quantity cannot be less than 0",HttpStatus.BAD_REQUEST);
        }
        else if(product.getQuantity() != null && product.getQuantity() > 100)
        {
            logger.error("Entered product quantity is greater than 100");
            logger.info("Exiting updateProduct service logic");
            return new ResponseEntity<Object>("Quantity cannot be greater than 100",HttpStatus.BAD_REQUEST);
        }
        else if(product.getSku() != null && productRepository.findProductBySku(product.getSku()).isPresent() &&
                productRepository.findProductBySku(product.getSku()).get().getId() != productinDB.getId())
        {
            logger.error("Entered sku already exists in the database");
            logger.info("Exiting updateProduct service logic");
            return new ResponseEntity<Object>("This sku already exists, please enter a different sku",HttpStatus.BAD_REQUEST);
        }
        else {
            logger.info("All checks passed");
            logger.info("Updating product details in database....");
            LocalDateTime localNow = LocalDateTime.now();
            ZonedDateTime timeInZ = localNow.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Z"));

            if(productinDB != null) {

                productinDB.setName(product.getName());
                productinDB.setDescription(product.getDescription());
                productinDB.setSku(product.getSku());
                productinDB.setQuantity(product.getQuantity());
                productinDB.setManufacturer(product.getManufacturer());
                productinDB.setDate_last_updated(timeInZ.toString());

                productRepository.save(productinDB);
                logger.info("Updated product details saved to database successfully");
                logger.info("Exiting updateProduct service logic");
                return new ResponseEntity<Object>("Product updated successfully", HttpStatus.NO_CONTENT);
            }
            else {
                logger.error("Product to be updated does not exist in the database");
                logger.info("Exiting updateProduct service logic");
                return new ResponseEntity<Object>("Product does not exist",HttpStatus.NOT_FOUND);
            }
        }
    }

    public HashMap<String,String> getJSONMessageBody(Message message) {
        HashMap<String,String> map = new HashMap<>();

        logger.info("Populating JSON response for API calls");
        map.put("Message:",message.getMessage());
        map.put("Status Code:",message.getMessageToken());
        return map;
    }

    public ResponseEntity<Object> patchProduct(HttpServletRequest httpRequest, Long productId, Product product)
    {
        logger.info("Inside patchProduct service logic");
        ResponseEntity<Object> req_header = userServices.performBasicAuth(httpRequest);
        String userDetails = httpRequest.getHeader("Authorization");
        if(userDetails == null)
        {
            logger.error("Username & password not provided");
            logger.info("Exiting patchProduct service logic");
            return new ResponseEntity<>("Please enter your username and password",HttpStatus.UNAUTHORIZED);
        }
        String[] userCredentials = userDetails == null? null: userServices.decodeLogin(userDetails);
        User current = userCredentials.length == 0?null:userServices.fetchUser(userCredentials[0]);
        Product productinDB = fetchProduct(productId);

        //Authorizing User Credentials
        if((req_header.getStatusCode().equals(HttpStatus.BAD_REQUEST)) ||
                (req_header.getStatusCode().equals(HttpStatus.UNAUTHORIZED)))
        {
            logger.error("Check if username & password provided are correct");
            logger.info("Exiting patchProduct service logic");
            return new ResponseEntity<>(req_header.getBody(),req_header.getStatusCode());
        }

        else if(productinDB != null && productinDB.getOwner_user_id() != current.getId())
        {
            logger.error("Trying to update product created by another user");
            logger.info("Exiting patchProduct service logic");
            return new ResponseEntity<Object>("You can only update the product that you have created",HttpStatus.FORBIDDEN);
        }

        else if((product.getId() != null || product.getDate_last_updated() != null ||
                product.getOwner_user_id() != null || product.getDate_added() != null))
        {
            logger.error("Updates cannot be made to productId, userId, product added date and product updated date");
            logger.info("Exiting patchProduct service logic");
            return new ResponseEntity<Object>("You cannot make updates to productId, userId, product added date and product updated date, please remove these fields.",HttpStatus.BAD_REQUEST);
        }
        else if(product.getName() == null && product.getSku() == null && product.getQuantity() == null
        && product.getDescription() == null && product.getManufacturer() == null)
        {
            logger.error("One or more mandatory fields have not been provided");
            logger.info("Exiting patchProduct service logic");
            return new ResponseEntity<Object>("Please submit at least one field",HttpStatus.BAD_REQUEST);
        }
        else if(product.getQuantity()!= null && product.getQuantity() < 0)
        {
            logger.error("Entered product quantity is less than 0");
            logger.info("Exiting patchProduct service logic");
            return new ResponseEntity<>("Quantity cannot be less than 0",HttpStatus.BAD_REQUEST);
        }
        else if(product.getQuantity() != null && product.getQuantity() > 100)
        {
            logger.error("Entered product quantity is greater than 100");
            logger.info("Exiting patchProduct service logic");
            return new ResponseEntity<Object>("Quantity cannot be more than 100",HttpStatus.BAD_REQUEST);
        }
        else if(product.getName() != null && product.getName().trim().length() == 0)
        {
            logger.error("Valid name is not provided");
            logger.info("Exiting patchProduct service logic");
            return new ResponseEntity<Object>("Please provide a valid name",HttpStatus.BAD_REQUEST);
        }
        else if(product.getDescription() != null && product.getDescription().trim().length() == 0)
        {
            logger.error("Valid description is not provided");
            logger.info("Exiting patchProduct service logic");
            return new ResponseEntity<Object>("Please provide a valid description",HttpStatus.BAD_REQUEST);
        }
        else if(product.getManufacturer() != null && product.getManufacturer().trim().length() == 0)
        {
            logger.error("Valid manufacturer is not provided");
            logger.info("Exiting patchProduct service logic");
            return new ResponseEntity<Object>("Please provide a valid manufacturer",HttpStatus.BAD_REQUEST);
        }
        else if(product.getSku() != null && product.getSku().trim().length() == 0)
        {
            logger.error("Valid sku is not provided");
            logger.info("Exiting patchProduct service logic");
            return new ResponseEntity<Object>("Please provide a valid sku",HttpStatus.BAD_REQUEST);
        }
        else if(product.getSku() != null && productRepository.findProductBySku(product.getSku()).isPresent() &&
                productRepository.findProductBySku(product.getSku()).get().getId() != productinDB.getId())
        {
            logger.error("Entered sku already exists in the database");
            logger.info("Exiting patchProduct service logic");
            return new ResponseEntity<Object>("This sku already exists, please enter a different sku",HttpStatus.BAD_REQUEST);
        }
        else {
            logger.info("All checks passed");
            logger.info("Updating product details in database....");

            if(productinDB != null)
            {
                LocalDateTime localNow = LocalDateTime.now();
                ZonedDateTime timeInZ = localNow.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Z"));
                if(product.getName() != null)
                {
                    productinDB.setName(product.getName());
                }
                if(product.getQuantity() != null)
                {
                    productinDB.setQuantity(product.getQuantity());
                }
                if(product.getSku() != null)
                {
                    productinDB.setSku(product.getSku());
                }
                if(product.getDescription() != null)
                {
                    productinDB.setDescription(product.getDescription());
                }
                if(product.getManufacturer() != null)
                {
                    productinDB.setDescription(product.getDescription());
                }
                productinDB.setDate_last_updated(timeInZ.toString());

                productRepository.save(productinDB);
                logger.info("Updated product details saved to database successfully");
                logger.info("Exiting patchProduct service logic");
                return new ResponseEntity<Object>("Product updated successfully",HttpStatus.NO_CONTENT);
            }
            else
            {
                logger.error("Product to be updated does not exist in the database");
                logger.info("Exiting patchProduct service logic");
                return new ResponseEntity<>("Product does not exist in DB",HttpStatus.NOT_FOUND);
            }
        }
    }

}
