package springboot.csye6225.UserWebApp.image;

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
import java.io.IOException;

@RestController
@RequestMapping
@Transactional
public class ImageController {
    private ImageServices imageServices;

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
            ResponseEntity<Object> result = imageServices.uploadImage(request,multipartFile,product_id);
            if(!result.getStatusCode().equals(HttpStatus.CREATED))
            {
                message.setMessage(result.getBody().toString());
                message.setMessageToken(result.getStatusCode().toString());
                return new ResponseEntity<>(productServices.getJSONMessageBody(message),result.getStatusCode());
            }
            else {
                return new ResponseEntity(result.getBody(),result.getStatusCode());
            }

    }

    @GetMapping(path = "v1/product/{product_id}/image/{image_id}")
    public ResponseEntity<Object> getSpecificImageDetails(HttpServletRequest request, @PathVariable("product_id") Long product_id, @PathVariable("image_id") Long image_id)
    {
        ResponseEntity<Object> result = imageServices.getSpecificImageDetails(request,product_id,image_id);
        if(!result.getStatusCode().equals(HttpStatus.OK))
        {
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            return new ResponseEntity<>(productServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else {
            return new ResponseEntity(result.getBody(),result.getStatusCode());
        }
    }

    @GetMapping(path = "v1/product/{product_id}/image")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getAllImagesForAProduct(HttpServletRequest request,@PathVariable("product_id") Long product_id)
    {
        ResponseEntity<Object> result = imageServices.getAllImages(request,product_id);
        if(!result.getStatusCode().equals(HttpStatus.OK))
        {
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            return new ResponseEntity<>(productServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else {
            return new ResponseEntity(result.getBody(),result.getStatusCode());
        }
    }

    @DeleteMapping(path = "v1/product/{product_id}/image/{image_id}")
    public ResponseEntity<Object> deleteAnImageForAProduct(HttpServletRequest request, @PathVariable("product_id") Long product_id, @PathVariable("image_id") Long image_id)
    {
        ResponseEntity<Object> result = imageServices.deleteAnImageForAProduct(request,product_id,image_id);
        if(!result.getStatusCode().equals(HttpStatus.NO_CONTENT))
        {
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            return new ResponseEntity<>(productServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else {
            return new ResponseEntity(result.getBody(),result.getStatusCode());
        }
    }


}
