package springboot.csye6225.UserWebApp.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import springboot.csye6225.UserWebApp.user.User;
import springboot.csye6225.UserWebApp.user.UserServices;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

@Service
public class ProductServices {

    @Autowired
    UserServices userServices;

    ProductRepository productRepository;

    @Autowired
    public ProductServices(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ResponseEntity<Object> createProduct(HttpServletRequest httpRequest, Product product) {
        ResponseEntity<Object> req_header = userServices.performBasicAuth(httpRequest);
        ResponseEntity<Object> nullCheckResult = performProductNullCheck(product);
        ResponseEntity<Object> validateProductDetails = performValidations(product);


        //Authorizing User Credentials
        if((req_header.getStatusCode().equals(HttpStatus.BAD_REQUEST)) ||
                (req_header.getStatusCode().equals(HttpStatus.UNAUTHORIZED)))
        {
            return new ResponseEntity<Object>(req_header.getBody(),req_header.getStatusCode());
        }

        //If any fields are null
        else if(nullCheckResult.getStatusCode() != HttpStatus.OK)
        {
            return nullCheckResult;
        }

        else if(product.getId() != null || product.getOwner_user_id() != null ||
                product.getDate_added() != null || product.getDate_last_updated() != null)
        {
            return new ResponseEntity<Object>("Please do not provide productId, userId, product added date and product updated date",HttpStatus.BAD_REQUEST);
        }

        //Product fields validation
        else if(validateProductDetails.getStatusCode() != HttpStatus.OK)
        {
            return validateProductDetails;
        }

        //Create Product
        else {
            LocalDateTime localNow = LocalDateTime.now();
            ZonedDateTime timeInZ = localNow.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Z"));
            String userDetails = httpRequest.getHeader("Authorization");
            String[] userCredentials = userServices.decodeLogin(userDetails);
            User current = userServices.fetchUser(userCredentials[0]);

            product.setName(product.getName());
            product.setDescription(product.getDescription());
            product.setManufacturer(product.getManufacturer());
            product.setSku(product.getSku());
            product.setQuantity(product.getQuantity());
            product.setDate_added(timeInZ.toString());
            product.setDate_last_updated(timeInZ.toString());
            product.setOwner_user_id(current.getId());
            productRepository.save(product);

            return new ResponseEntity<Object>("Product added successfully!",HttpStatus.CREATED);
        }

    }

    private ResponseEntity<Object> performValidations(Product product) {
        //to be implemented
        return new ResponseEntity<Object>("",HttpStatus.OK);
    }

    public ResponseEntity<Object> performProductNullCheck(Product product)
    {
        if(product.getName() == null || product.getName().trim().length() == 0 ||
                product.getSku() == null || product.getSku().trim().length() == 0 ||
                product.getManufacturer() == null || product.getManufacturer().trim().length() == 0 ||
                product.getDescription() == null || product.getDescription().trim().length() == 0 ||
                product.getQuantity() == null || product.getQuantity() < 0)
        {
            return new ResponseEntity<Object>("Please verify if you have provided product name, desc, sku, manufacturer, quantity without changing field labels & quantity is not less than 0",HttpStatus.BAD_REQUEST);
        }
        else
            return new ResponseEntity<Object>("All fields are provided",HttpStatus.OK);
    }

    public ResponseEntity<Object> getProductDetails(Long productId) {
        Product product = fetchProduct(productId);
        if(product != null) {
            return new ResponseEntity<Object>(getJSONBody(product),HttpStatus.OK);
        }
         else
        {
            return new ResponseEntity<Object>("Product not found",HttpStatus.NOT_FOUND);
        }
    }

    private Product fetchProduct(Long productId) {
        List<Product> productList;
        productList = productRepository.findAll();
        for(Product p: productList)
        {
            if(p.getId() == productId)
            {
                return p;
            }
        }
        return null;
    }

    private HashMap<String,Object> getJSONBody(Product product)
    {
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

    public ResponseEntity<Object> deleteProduct(HttpServletRequest httpRequest, Long productId) {
        ResponseEntity<Object> req_header = userServices.performBasicAuth(httpRequest);
        String userDetails = httpRequest.getHeader("Authorization");
        String[] userCredentials = userServices.decodeLogin(userDetails);
        User current = userServices.fetchUser(userCredentials[0]);
        Product product = fetchProduct(productId);

        //Authorizing User Credentials
        if((req_header.getStatusCode().equals(HttpStatus.BAD_REQUEST)) ||
                (req_header.getStatusCode().equals(HttpStatus.UNAUTHORIZED)))
        {
            return new ResponseEntity<Object>(req_header.getBody(),req_header.getStatusCode());
        }

        else if(product != null && product.getOwner_user_id() != current.getId())
        {
            return new ResponseEntity<Object>("You can only delete the product you have created!",HttpStatus.FORBIDDEN);
        }
        else {
            if(product != null)
            {
                productRepository.deleteById(productId);
                return new ResponseEntity<Object>("Product deleted successfully",HttpStatus.OK);
            }
            else {
                return new ResponseEntity<Object>("Product does not exist",HttpStatus.NOT_FOUND);
            }
        }
    }

    public ResponseEntity<Object> updateProduct(HttpServletRequest httpRequest, Long productId, Product product) {
        ResponseEntity<Object> req_header = userServices.performBasicAuth(httpRequest);
        String userDetails = httpRequest.getHeader("Authorization");
        String[] userCredentials = userServices.decodeLogin(userDetails);
        User current = userServices.fetchUser(userCredentials[0]);
        Product productinDB = fetchProduct(productId);

        //Authorizing User Credentials
        if((req_header.getStatusCode().equals(HttpStatus.BAD_REQUEST)) ||
                (req_header.getStatusCode().equals(HttpStatus.UNAUTHORIZED)))
        {
            return new ResponseEntity<>(req_header.getBody(),req_header.getStatusCode());
        }

        else if(productinDB != null && productinDB.getOwner_user_id() != current.getId())
        {
            return new ResponseEntity<Object>("You can only update the product you have created!",HttpStatus.FORBIDDEN);
        }

        else if(!performProductNullCheck(product).getStatusCode().equals(HttpStatus.OK)) {
            return new ResponseEntity<Object>("Please check if you have provided all the required details",HttpStatus.BAD_REQUEST);
        }

        else if(product.getId() != null || product.getOwner_user_id() != null ||
        product.getDate_added() != null || product.getDate_last_updated() != null)
        {
            return new ResponseEntity<Object>("You cannot make updates to productId, userId, product added date and product updated date, please remove these fields.",HttpStatus.BAD_REQUEST);
        }

        else if(product.getName() == null || product.getDescription() == null
        || product.getQuantity() == null || product.getManufacturer() == null ||
        product.getSku() == null)
        {
            System.out.println(product.getName()+product.getManufacturer()+product.getDescription()
            +product.getSku()+product.getQuantity());
            return new ResponseEntity<Object>("Please check if you provided name, desc, manufacturer, sku & quantity details",HttpStatus.BAD_REQUEST);
        }

        else {
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
                return new ResponseEntity<Object>(getJSONBody(productinDB), HttpStatus.OK);
            }
            else {
                return new ResponseEntity<Object>("Product does not exist",HttpStatus.BAD_REQUEST);
            }
        }
    }
}
