package springboot.csye6225.UserWebApp.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping
@Transactional
public class ImageController {
    private ImageServices imageServices;

    @Autowired
    public ImageController(ImageServices imageServices) {
        this.imageServices = imageServices;
    }

    @PostMapping(path = "v1/product/{product_id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadImage(HttpServletRequest request,@RequestPart("file") MultipartFile multipartFile, @PathVariable("product_id") Long product_id) throws IOException
    {
        ResponseEntity<Object> result = imageServices.uploadImage(request,multipartFile,product_id);
        return new ResponseEntity<>(result.getBody(), result.getStatusCode());
    }
}
