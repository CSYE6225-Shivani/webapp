package springboot.csye6225.UserWebApp.image;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import springboot.csye6225.UserWebApp.product.Product;
import springboot.csye6225.UserWebApp.product.ProductServices;
import springboot.csye6225.UserWebApp.user.User;
import springboot.csye6225.UserWebApp.user.UserServices;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class ImageServices {

    @Autowired
    private AmazonS3 s3_client;

    ImageRepository imageRepository;
    Random random = new Random();
    @Value("${aws.s3_bucket_name}")
    private String s3_bucket_name;

    @Autowired
    UserServices userServices;

    @Autowired
    ProductServices productServices;

    @Autowired
    public ImageServices(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public ImageServices() {
    }

    private File convertMultiPart_toFile(MultipartFile multipartFile) throws IOException
    {
        File file = new File(multipartFile.getOriginalFilename());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(multipartFile.getBytes());
        fileOutputStream.close();
        return file;
    }

    private HashMap<String,String> imageJSON(Image image)
    {
        HashMap<String, String> map = new HashMap<>();
        map.put("image_id",image.getImage_id().toString());
        map.put("product_id",image.getProduct_id().toString());
        map.put("file_name",image.getFile_name());
        map.put("date_created",image.getDate_created());
        map.put("s3_bucket_path",image.getS3_bucket_path());
        return map;
    }

    public ResponseEntity<Object> uploadImage(HttpServletRequest httpRequest, MultipartFile multipartFile, Long product_id )
    {
        String userDetails = httpRequest.getHeader("Authorization");
        if(userDetails == null)
        {
            return new ResponseEntity<>("Please enter your username and password",HttpStatus.UNAUTHORIZED);
        }

        //Authenticating User Credentials
        ResponseEntity<Object> req_header = userServices.performBasicAuth(httpRequest);
        if((req_header.getStatusCode().equals(HttpStatus.BAD_REQUEST)) ||
                (req_header.getStatusCode().equals(HttpStatus.UNAUTHORIZED)))
        {
            return new ResponseEntity<Object>(req_header.getBody(),req_header.getStatusCode());
        }
        String[] userCredentials = userDetails == null?null:userServices.decodeLogin(userDetails);
        User current = userCredentials.length == 0?null:userServices.fetchUser(userCredentials[0]);

        //Fetching Logged in User and product against provided product_id
        Product product = productServices.fetchProduct(product_id);

        //Converting multipartfile to file object
        File file = null;
        try {
            file = convertMultiPart_toFile(multipartFile);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        if(product != null && product.getOwner_user_id() != current.getId())
        {
            return new ResponseEntity<Object>("You can only add image for the product you have uploaded",HttpStatus.FORBIDDEN);
        }
        else if(product == null)
        {
            return new ResponseEntity<>("Product does not exist", HttpStatus.NOT_FOUND);
        }
        else if(multipartFile.getContentType() == null || multipartFile.isEmpty()){
            return new ResponseEntity<Object>("No file was provided",HttpStatus.BAD_REQUEST);
        }
        else if(!multipartFile.getContentType().equals("image/jpeg") && !multipartFile.getContentType().equals("image/png")
         && !multipartFile.getContentType().equals("image/jpg"))
        {
            return new ResponseEntity<Object>("Invalid file format",HttpStatus.BAD_REQUEST);
        }
        else
        {
            //Get multipartfile metadata
            String content_type = multipartFile.getContentType();
            Long multipart_filesize = multipartFile.getSize();

            String file_name;
            if(content_type.equals("image/png"))
            {
                file_name = product.getName()+random.nextInt()+current.getId()+ product.getId()+UUID.randomUUID()+".png";
            }
            else if(content_type.equals("image/jpeg"))
            {
                file_name = product.getName()+random.nextInt()+current.getId()+ product.getId()+UUID.randomUUID()+".jpeg";
            }
            else
            {
                file_name = product.getName()+random.nextInt()+current.getId()+ product.getId()+UUID.randomUUID()+".jpg";
            }

            LocalDateTime localNow = LocalDateTime.now();
            ZonedDateTime timeInZ = localNow.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Z"));

            String s3_bucket_path = s3_bucket_name+"/"+file_name;

            //Creating an Image object
            Image newImage = new Image();
            newImage.setProduct_id(product.getId());
            newImage.setDate_created(timeInZ.toString());
            newImage.setFile_name(file_name);
            newImage.setS3_bucket_path(s3_bucket_path);

            //Saving image to Amazon S3
            PutObjectRequest putObjectRequest = new PutObjectRequest(s3_bucket_name,file_name,file);
            s3_client.putObject(putObjectRequest);
//            file.delete();

            imageRepository.save(newImage);
            return new ResponseEntity<Object>(imageJSON(newImage),HttpStatus.CREATED);
        }
    }

    private Image fetchImage(Long image_id) {
        Image image_obj_url = new Image();
        List<Image> image_list = imageRepository.findAll();

        for(Image i:image_list)
        {
            if(image_id == i.getImage_id())
            {
                image_obj_url = i;
            }
        }
        return image_obj_url;
    }

    private List<Image> fetchImageByProductID(Long product_id)
    {
        List<Image> all_images_for_prod = new ArrayList<>();
        List<Image> image_list = imageRepository.findAll();
        for(Image i: image_list)
        {
            if(i.getProduct_id() == product_id)
            {
                all_images_for_prod.add(i);
            }
        }
        return all_images_for_prod;
    }

    public ResponseEntity<Object> getSpecificImageDetails(HttpServletRequest httpRequest, Long product_id, Long image_id) {

        String userDetails = httpRequest.getHeader("Authorization");

        if(userDetails == null)
        {
            return new ResponseEntity<>("Please enter your username and password",HttpStatus.UNAUTHORIZED);
        }

        //Authenticating User Credentials
        ResponseEntity<Object> req_header = userServices.performBasicAuth(httpRequest);
        if((req_header.getStatusCode().equals(HttpStatus.BAD_REQUEST)) ||
                (req_header.getStatusCode().equals(HttpStatus.UNAUTHORIZED)))
        {
            return new ResponseEntity<Object>(req_header.getBody(),req_header.getStatusCode());
        }

        String[] userCredentials = userDetails == null?null:userServices.decodeLogin(userDetails);
        User current = userCredentials.length == 0?null:userServices.fetchUser(userCredentials[0]);

        //Fetching Logged-in User and product against provided product_id
        Product product = productServices.fetchProduct(product_id);

        //Fetching image
        Image image = fetchImage(image_id);

        if(product != null && product.getOwner_user_id() != current.getId())
        {
            return new ResponseEntity<Object>("You can only view image details for the product you have created.",HttpStatus.FORBIDDEN);
        }
        else if(current == null)
        {
            return new ResponseEntity<>("This user does not exist",HttpStatus.NOT_FOUND);
        }
        else if(product == null)
        {
            return new ResponseEntity<>("Product does not exist", HttpStatus.NOT_FOUND);
        }
        else if(image == null)
        {
            return new ResponseEntity<Object>("This image does not exist",HttpStatus.NOT_FOUND);
        }
        else if(fetchImageByProductID(product_id).size() == 0)
        {
            return new ResponseEntity<>("Image details for this product does not exist",HttpStatus.NOT_FOUND);
        }
        else if(current.getId() == product.getOwner_user_id() && product.getId() != image.getProduct_id())
        {
            return new ResponseEntity<Object>("You cannot access details of an image which is not uploaded by you.",HttpStatus.FORBIDDEN);
        }
        else if(fetchImage(image_id).getProduct_id() != product_id)
        {
            return new ResponseEntity<>("This image ID is not mapped against provided product ID",HttpStatus.BAD_REQUEST);
        }
        else if((current != null && product != null) && (current.getId() != product.getOwner_user_id() || product.getId() != fetchImage(image_id).getProduct_id()))
        {
            return new ResponseEntity<>("This request is not allowed",HttpStatus.UNAUTHORIZED);
        }
        else
        {
            return new ResponseEntity<>(imageJSON(image),HttpStatus.OK);
        }
    }

    public ResponseEntity<Object> getAllImages(HttpServletRequest httpRequest, Long product_id) {
        String userDetails = httpRequest.getHeader("Authorization");

        if(userDetails == null)
        {
            return new ResponseEntity<>("Please enter your username and password",HttpStatus.UNAUTHORIZED);
        }

        //Authenticating User Credentials
        ResponseEntity<Object> req_header = userServices.performBasicAuth(httpRequest);
        if((req_header.getStatusCode().equals(HttpStatus.BAD_REQUEST)) ||
                (req_header.getStatusCode().equals(HttpStatus.UNAUTHORIZED)))
        {
            return new ResponseEntity<Object>(req_header.getBody(),req_header.getStatusCode());
        }

        String[] userCredentials = userDetails == null?null:userServices.decodeLogin(userDetails);
        User current = userCredentials.length == 0?null:userServices.fetchUser(userCredentials[0]);

        Product product = productServices.fetchProduct(product_id);

        if(product == null)
        {
            return new ResponseEntity<>("Product does not exist", HttpStatus.BAD_REQUEST);
        }
        else if(product != null && product.getOwner_user_id() != current.getId())
        {
            return new ResponseEntity<Object>("You can only view image list for the product you have created",HttpStatus.FORBIDDEN);
        }
        else
        {
            return new ResponseEntity<>(fetchImageByProductID(product_id),HttpStatus.OK);
        }
    }


    public ResponseEntity<Object> deleteAnImageForAProduct(HttpServletRequest httpRequest, Long product_id, Long image_id)
    {
        String userDetails = httpRequest.getHeader("Authorization");

        if(userDetails == null)
        {
            return new ResponseEntity<>("Please enter your username and password",HttpStatus.UNAUTHORIZED);
        }

        //Authenticating User Credentials
        ResponseEntity<Object> req_header = userServices.performBasicAuth(httpRequest);
        if((req_header.getStatusCode().equals(HttpStatus.BAD_REQUEST)) ||
                (req_header.getStatusCode().equals(HttpStatus.UNAUTHORIZED)))
        {
            return new ResponseEntity<Object>(req_header.getBody(),req_header.getStatusCode());
        }

        String[] userCredentials = userDetails == null?null:userServices.decodeLogin(userDetails);
        User current = userCredentials.length == 0?null:userServices.fetchUser(userCredentials[0]);

        //Fetching Logged-in User and product against provided product_id
        Product product = productServices.fetchProduct(product_id);

        //Fetching image
        Image image = fetchImage(image_id);

        if(product != null && product.getOwner_user_id() != current.getId())
        {
            return new ResponseEntity<Object>("You can only delete an image for the product you have created.",HttpStatus.FORBIDDEN);
        }
        else if(current == null)
        {
            return new ResponseEntity<>("This user does not exist",HttpStatus.NOT_FOUND);
        }
        else if(product == null)
        {
            return new ResponseEntity<>("Product does not exist", HttpStatus.NOT_FOUND);
        }
        else if(image == null)
        {
            return new ResponseEntity<Object>("This image does not exist",HttpStatus.NOT_FOUND);
        }
        else if(fetchImageByProductID(product_id).size() == 0)
        {
            return new ResponseEntity<>("Image details for this product does not exist",HttpStatus.NOT_FOUND);
        }
        else if(current.getId() == product.getOwner_user_id() && product.getId() != image.getProduct_id())
        {
            return new ResponseEntity<Object>("You cannot delete an image which is not uploaded by you.",HttpStatus.FORBIDDEN);
        }
        else if(fetchImage(image_id).getProduct_id() != product_id)
        {
            return new ResponseEntity<>("This image ID is not mapped against provided product ID",HttpStatus.BAD_REQUEST);
        }
        else if((current != null && product != null) && (current.getId() != product.getOwner_user_id() || product.getId() != fetchImage(image_id).getProduct_id()))
        {
            return new ResponseEntity<>("This request is not allowed",HttpStatus.UNAUTHORIZED);
        }
        else
        {
            s3_client.deleteObject(s3_bucket_name,image.getFile_name());
            imageRepository.deleteById(image.getImage_id());
            return new ResponseEntity<>("Image Deleted successfully",HttpStatus.NO_CONTENT);
        }
    }
}
