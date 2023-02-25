package springboot.csye6225.UserWebApp.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping
@Transactional
public class ImageController {
    private ImageServices imageServices;

    @Autowired
    public ImageController(ImageServices imageServices) {
        this.imageServices = imageServices;
    }

    @PostMapping(produces = "application/json",path = "v1/product/{product_id}/image")
    public ResponseEntity<Object> uploadImage(HttpServletRequest request, @RequestPart(value = "file") MultipartFile multipartFile, @PathVariable("product_id") Long product_id) {
        ResponseEntity<Object> result = imageServices.uploadImage(request,multipartFile,product_id);
        return result;
    }
}
