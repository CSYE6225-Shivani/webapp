package springboot.csye6225.UserWebApp.image;

import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springboot.csye6225.UserWebApp.message.Message;
import springboot.csye6225.UserWebApp.product.ProductServices;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping
@Transactional
public class ImageController {

    @Autowired
    private StatsDClient metrics;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    ImageServices imageServices;

    @Autowired
    public ImageController(ImageServices imageServices) {
        this.imageServices = imageServices;
    }
    @Autowired
    Message message;

    @Autowired
    ProductServices productServices;

    @PostMapping(path = "v1/product/{product_id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadImage(HttpServletRequest request,@RequestPart("file") MultipartFile multipartFile, @PathVariable("product_id") Long product_id) throws Exception
    {
        metrics.incrementCounter("uploadImage");
        logger.info("Inside uploadImage controller");
        ResponseEntity<Object> result = imageServices.uploadImage(request,multipartFile,product_id);
        if(!result.getStatusCode().equals(HttpStatus.CREATED))
        {
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            logger.info("Exiting uploadImage controller");
            return new ResponseEntity<>(productServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else
        {
            logger.info("Exiting uploadImage controller");
            return new ResponseEntity(result.getBody(),result.getStatusCode());
        }

    }

    @GetMapping(path = "v1/product/{product_id}/image/{image_id}")
    public ResponseEntity<Object> getSpecificImageDetails(HttpServletRequest request, @PathVariable("product_id") Long product_id, @PathVariable("image_id") Long image_id)
    {
        metrics.incrementCounter("getSpecificImageDetails");
        logger.info("Inside getSpecificImageDetails controller");
        ResponseEntity<Object> result = imageServices.getSpecificImageDetails(request,product_id,image_id);
        if(!result.getStatusCode().equals(HttpStatus.OK))
        {
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            logger.info("Exiting getSpecificImageDetails controller");
            return new ResponseEntity<>(productServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else {
            logger.info("Exiting getSpecificImageDetails controller");
            return new ResponseEntity(result.getBody(),result.getStatusCode());
        }
    }

    @GetMapping(path = "v1/product/{product_id}/image")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getAllImagesForAProduct(HttpServletRequest request,@PathVariable("product_id") Long product_id)
    {
        metrics.incrementCounter("getAllImagesForAProduct");
        logger.info("Inside getAllImagesForAProduct controller");
        ResponseEntity<Object> result = imageServices.getAllImages(request,product_id);
        if(!result.getStatusCode().equals(HttpStatus.OK))
        {
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            logger.info("Exiting getAllImagesForAProduct controller");
            return new ResponseEntity<>(productServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else {
            logger.info("Exiting getAllImagesForAProduct controller");
            return new ResponseEntity(result.getBody(),result.getStatusCode());
        }
    }

    @DeleteMapping(path = "v1/product/{product_id}/image/{image_id}")
    public ResponseEntity<Object> deleteAnImageForAProduct(HttpServletRequest request, @PathVariable("product_id") Long product_id, @PathVariable("image_id") Long image_id)
    {
        metrics.incrementCounter("deleteAnImageForAProduct");
        logger.info("Inside deleteAnImageForAProduct controller");
        ResponseEntity<Object> result = imageServices.deleteAnImageForAProduct(request,product_id,image_id);
        if(!result.getStatusCode().equals(HttpStatus.NO_CONTENT))
        {
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            logger.info("Exiting deleteAnImageForAProduct controller");
            return new ResponseEntity<>(productServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else {
            logger.info("Exiting deleteAnImageForAProduct controller");
            return new ResponseEntity(result.getBody(),result.getStatusCode());
        }
    }


}
