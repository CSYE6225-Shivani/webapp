package springboot.csye6225.UserWebApp.image;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class ImageServices {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AmazonS3 s3_client = AmazonS3ClientBuilder.defaultClient();

//    @Autowired
//    private AmazonS3 s3_client;

    @Autowired
    public ImageRepository imageRepository;

    Random random = new Random();
    @Value("${aws_s3.s3_bucket_name}")
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

    public HashMap<String,String> imageJSON(Image image)
    {
        HashMap<String, String> map = new HashMap<>();
        map.put("image_id",image.getImage_id().toString());
        map.put("product_id",image.getProduct_id().toString());
        map.put("file_name",image.getFile_name());
        map.put("date_created",image.getDate_created());
        map.put("s3_bucket_path",image.getS3_bucket_path());
        return map;
    }

    public ResponseEntity<Object> uploadImage(HttpServletRequest httpRequest, MultipartFile multipartFile, Long product_id ) throws IOException
    {
        logger.info("Inside uploadImage service logic");
        String userDetails = httpRequest.getHeader("Authorization");
        if(userDetails == null)
        {
            logger.error("Username & password is not provided");
            logger.info("Exiting uploadImage service logic");
            return new ResponseEntity<>("Please enter your username and password",HttpStatus.UNAUTHORIZED);
        }

        //Authenticating User Credentials
        ResponseEntity<Object> req_header = userServices.performBasicAuth(httpRequest);
        if((req_header.getStatusCode().equals(HttpStatus.BAD_REQUEST)) ||
                (req_header.getStatusCode().equals(HttpStatus.UNAUTHORIZED)))
        {
            logger.error("Check if username & password provided are correct");
            logger.info("Exiting uploadImage service logic");
            return new ResponseEntity<Object>(req_header.getBody(),req_header.getStatusCode());
        }
        String[] userCredentials = userDetails == null?null:userServices.decodeLogin(userDetails);
        User current = userCredentials.length == 0?null:userServices.fetchUser(userCredentials[0]);

        //Fetching Logged in User and product against provided product_id
        Product product = productServices.fetchProduct(product_id);

        //Converting multipartfile to file object
        InputStream file = multipartFile.getInputStream();

        if(product != null && product.getOwner_user_id() != current.getId())
        {
            logger.error("Trying to upload image for product created by another user");
            logger.info("Exiting uploadImage service logic");
            return new ResponseEntity<Object>("You can only add image for the product you have uploaded",HttpStatus.FORBIDDEN);
        }
        else if (s3_bucket_name == null) {
            logger.error("Bucket name is not provided");
            logger.info("Exiting uploadImage service logic");
            return new ResponseEntity<>("Bucket is not created.",HttpStatus.BAD_REQUEST);
        }
        else if(product == null)
        {
            logger.error("Product does not exist in the database");
            logger.info("Exiting uploadImage service logic");
            return new ResponseEntity<>("Product does not exist", HttpStatus.NOT_FOUND);
        }
        else if(multipartFile.getContentType() == null || multipartFile.isEmpty()){
            logger.error("No image provided to be uploaded");
            logger.info("Exiting uploadImage service logic");
            return new ResponseEntity<Object>("No image was provided",HttpStatus.BAD_REQUEST);
        }
        else if(!multipartFile.getContentType().equals("image/jpeg") && !multipartFile.getContentType().equals("image/png")
         && !multipartFile.getContentType().equals("image/jpg"))
        {
            logger.error("Invalid file format");
            logger.info("Exiting uploadImage service logic");
            return new ResponseEntity<Object>("Invalid file format",HttpStatus.BAD_REQUEST);
        }
        else
        {
            logger.info("All checks passed");
            //Get multipartfile metadata
            String content_type = multipartFile.getContentType();
            Long multipart_filesize = multipartFile.getSize();

            String file_name;
            logger.info("Creating file name");
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

            logger.info("Creating bucket path");
            String s3_bucket_path = s3_bucket_name+"/"+current.getFirst_name()+"/"+current.getId()+"/"+product.getName()+"/"+product.getId()+"/"+file_name;

            //Creating an Image object
            Image newImage = new Image();
            newImage.setProduct_id(product.getId());
            newImage.setDate_created(timeInZ.toString());
            newImage.setFile_name(file_name);
            newImage.setS3_bucket_path(s3_bucket_path);

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(multipart_filesize);
            objectMetadata.setContentType(multipartFile.getContentType());

            logger.info("Saving image to S3 bucket");
            //Saving image to Amazon S3
            PutObjectRequest putObjectRequest = new PutObjectRequest(s3_bucket_name,file_name,file,objectMetadata);
            s3_client.putObject(putObjectRequest);

            imageRepository.save(newImage);
            logger.info("Saving image details to image repository");
            logger.info("Exiting uploadImage service logic");
            return new ResponseEntity<Object>(imageJSON(newImage),HttpStatus.CREATED);
        }
    }

    public Image fetchImage(Long image_id) {
        logger.info("Fetching image object from image list");
        Image image_obj_url = new Image();
        List<Image> image_list = imageRepository.findAll();

        for(Image i:image_list)
        {
            if(image_id == i.getImage_id())
            {
                logger.info("Image found in database");
                image_obj_url = i;
            }
        }
        logger.info("Image does not exist in the database");
        return image_obj_url;
    }

    public List<Image> fetchImageByProductID(Long product_id)
    {
        logger.info("Fetching image list for a particular product");
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

    public ResponseEntity<Object> getSpecificImageDetails(HttpServletRequest httpRequest, Long product_id, Long image_id)
    {
        logger.info("Inside getSpecificImageDetails service logic");
        String userDetails = httpRequest.getHeader("Authorization");

        if(userDetails == null)
        {
            logger.error("Username or Password not provided");
            logger.info("Exiting getSpecificImageDetails service logic");
            return new ResponseEntity<>("Please enter your username and password",HttpStatus.UNAUTHORIZED);
        }

        //Authenticating User Credentials
        ResponseEntity<Object> req_header = userServices.performBasicAuth(httpRequest);
        if((req_header.getStatusCode().equals(HttpStatus.BAD_REQUEST)) ||
                (req_header.getStatusCode().equals(HttpStatus.UNAUTHORIZED)))
        {
            logger.error("Check if username & password provided are correct");
            logger.info("Exiting getSpecificImageDetails service logic");
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
            logger.error("Trying to view image details for a product created by a different user");
            logger.info("Exiting getSpecificImageDetails service logic");
            return new ResponseEntity<Object>("You can only view image details for the product you have created.",HttpStatus.FORBIDDEN);
        }
        else if(image == null)
        {
            logger.error("Image does not exist");
            logger.info("Exiting getSpecificImageDetails service logic");
            return new ResponseEntity<>("This image does not exist",HttpStatus.NOT_FOUND);
        }
        else if(product == null)
        {
            logger.error("Product does not exist in the database");
            logger.info("Exiting getSpecificImageDetails service logic");
            return new ResponseEntity<>("Product does not exist", HttpStatus.NOT_FOUND);
        }
        else if(current == null)
        {
            logger.error("User does not exist in the database");
            logger.info("Exiting getSpecificImageDetails service logic");
            return new ResponseEntity<Object>("This user does not exist",HttpStatus.NOT_FOUND);
        }
        else if(fetchImageByProductID(product_id).size() == 0)
        {
            logger.error("Image details for this product does not exist");
            logger.info("Exiting getSpecificImageDetails service logic");
            return new ResponseEntity<>("Image details for this product does not exist",HttpStatus.NOT_FOUND);
        }
        else if(current.getId() == product.getOwner_user_id() && product.getId() != image.getProduct_id())
        {
            logger.error("Trying to access details of an image uploaded by a different user");
            logger.info("Exiting getSpecificImageDetails service logic");
            return new ResponseEntity<Object>("You cannot access details of an image which is not uploaded by you.",HttpStatus.FORBIDDEN);
        }
        else if(fetchImage(image_id).getProduct_id() != product_id)
        {
            logger.error("This image ID is not mapped against provided product ID");
            logger.info("Exiting getSpecificImageDetails service logic");
            return new ResponseEntity<>("This image ID is not mapped against provided product ID",HttpStatus.BAD_REQUEST);
        }
        else if((current != null && product != null) && (current.getId() != product.getOwner_user_id() || product.getId() != fetchImage(image_id).getProduct_id()))
        {
            logger.error("This request is not allowed");
            logger.info("Exiting getSpecificImageDetails service logic");
            return new ResponseEntity<>("This request is not allowed",HttpStatus.UNAUTHORIZED);
        }
        else
        {
            logger.info("All checks passed");
            logger.info("Fetching image details for requested product ID");
            logger.info("Exiting getSpecificImageDetails service logic");
            return new ResponseEntity<>(imageJSON(image),HttpStatus.OK);
        }
    }

    public ResponseEntity<Object> getAllImages(HttpServletRequest httpRequest, Long product_id) {
        logger.info("Inside getAllImages service logic");
        String userDetails = httpRequest.getHeader("Authorization");

        if(userDetails == null)
        {
            logger.error("Username or password is not provided");
            logger.info("Exiting getAllImages service logic");
            return new ResponseEntity<>("Please enter your username and password",HttpStatus.UNAUTHORIZED);
        }

        //Authenticating User Credentials
        ResponseEntity<Object> req_header = userServices.performBasicAuth(httpRequest);
        if((req_header.getStatusCode().equals(HttpStatus.BAD_REQUEST)) ||
                (req_header.getStatusCode().equals(HttpStatus.UNAUTHORIZED)))
        {
            logger.error("Check if username & password provided are correct");
            logger.info("Exiting getAllImages service logic");
            return new ResponseEntity<Object>(req_header.getBody(),req_header.getStatusCode());
        }

        String[] userCredentials = userDetails == null?null:userServices.decodeLogin(userDetails);
        User current = userCredentials.length == 0?null:userServices.fetchUser(userCredentials[0]);

        Product product = productServices.fetchProduct(product_id);

        if(product == null)
        {
            logger.error("Product does not exist in the database");
            logger.info("Exiting getAllImages service logic");
            return new ResponseEntity<>("Product does not exist", HttpStatus.BAD_REQUEST);
        }
        else if(product != null && product.getOwner_user_id() != current.getId())
        {
            logger.error("Trying to view image details for a product created by another user");
            logger.info("Exiting getAllImages service logic");
            return new ResponseEntity<Object>("You can only view image list for the product you have created",HttpStatus.FORBIDDEN);
        }
        else
        {
            List<Image> result = fetchImageByProductID(product_id);
            if(result.size() != 0)
            {
                logger.info("Displaying list of images for requested product");
                logger.info("Exiting getAllImages service logic");
                return new ResponseEntity<>(result,HttpStatus.OK);
            }
            else {
                logger.error("No images found against this product");
                logger.info("Exiting getAllImages service logic");
                return new ResponseEntity<>("There are no images against this product",HttpStatus.NOT_FOUND);
            }
        }
    }


    public ResponseEntity<Object> deleteAnImageForAProduct(HttpServletRequest httpRequest, Long product_id, Long image_id)
    {
        logger.info("Inside deleteAnImageForAProduct service logic");
        String userDetails = httpRequest.getHeader("Authorization");

        if(userDetails == null)
        {
            logger.info("Username or password not provided");
            logger.info("Exiting deleteAnImageForAProduct service logic");
            return new ResponseEntity<>("Please enter your username and password",HttpStatus.UNAUTHORIZED);
        }

        //Authenticating User Credentials
        ResponseEntity<Object> req_header = userServices.performBasicAuth(httpRequest);
        if((req_header.getStatusCode().equals(HttpStatus.BAD_REQUEST)) ||
                (req_header.getStatusCode().equals(HttpStatus.UNAUTHORIZED)))
        {
            logger.error("Check if username & password provided are correct");
            logger.info("Exiting deleteAnImageForAProduct service logic");
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
            logger.error("Trying to delete an image uploaded by another user");
            logger.info("Exiting deleteAnImageForAProduct service logic");
            return new ResponseEntity<Object>("You can only delete an image for the product you have created.",HttpStatus.FORBIDDEN);
        }
        else if(current == null)
        {
            logger.error("User does not exist in the database");
            logger.info("Exiting deleteAnImageForAProduct service logic");
            return new ResponseEntity<>("This user does not exist",HttpStatus.NOT_FOUND);
        }
        else if(product == null)
        {
            logger.error("Product does not exist in the database");
            logger.info("Exiting deleteAnImageForAProduct service logic");
            return new ResponseEntity<>("Product does not exist", HttpStatus.NOT_FOUND);
        }
        else if(image == null)
        {
            logger.error("Image does not exist");
            logger.info("Exiting deleteAnImageForAProduct service logic");
            return new ResponseEntity<Object>("This image does not exist",HttpStatus.NOT_FOUND);
        }
        else if(fetchImageByProductID(product_id).size() == 0)
        {
            logger.error("Image details for this product does not exist");
            logger.info("Exiting deleteAnImageForAProduct service logic");
            return new ResponseEntity<>("Image details for this product does not exist",HttpStatus.NOT_FOUND);
        }
        else if(product != null && current.getId() == product.getOwner_user_id() && (image != null && product.getId() != image.getProduct_id()))
        {
            logger.error("Trying to delete an image uploaded by another user");
            logger.info("Exiting deleteAnImageForAProduct service logic");
            return new ResponseEntity<Object>("You cannot delete an image which is not uploaded by you.",HttpStatus.FORBIDDEN);
        }
        else if(fetchImage(image_id).getProduct_id() != product_id)
        {
            logger.error("This image ID is not mapped against provided product ID");
            logger.info("Exiting deleteAnImageForAProduct service logic");
            return new ResponseEntity<>("This image ID is not mapped against provided product ID",HttpStatus.BAD_REQUEST);
        }
        else if((current != null && product != null) && (current.getId() != product.getOwner_user_id() || product.getId() != fetchImage(image_id).getProduct_id()))
        {
            logger.error("Exiting deleteAnImageForAProduct service logic");
            logger.info("Exiting deleteAnImageForAProduct service logic");
            return new ResponseEntity<>("This request is not allowed",HttpStatus.UNAUTHORIZED);
        }
        else
        {
            logger.info("All checks passed");
            logger.info("Deleting image...");
            s3_client.deleteObject(s3_bucket_name,image.getFile_name());

            imageRepository.deleteById(image.getImage_id());
            logger.info("Deleting image details for this image");
            logger.info("Image and image details deleted successfully");
            logger.info("Exiting deleteAnImageForAProduct service logic");
            return new ResponseEntity<>("Image Deleted successfully",HttpStatus.NO_CONTENT);
        }
    }

    public void deleteAllImages(Long productId) {
        logger.info("Deleting all images & image details for particular product");
        Product product = productServices.fetchProduct(productId);
        if(product != null)
        {
            List<Image> imageList = fetchImageByProductID(productId);

            System.out.println(imageList.toString());
            if (imageList.size() != 0) {
                for (Image each_image : imageList) {
                    s3_client.deleteObject(s3_bucket_name, each_image.getFile_name());
                    imageRepository.deleteById(each_image.getImage_id());
                }
            }
        }
    }
}
