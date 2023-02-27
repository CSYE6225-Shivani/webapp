package springboot.csye6225.UserWebApp.image;

import com.amazonaws.services.s3.AmazonS3Client;
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
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

@Service
public class ImageServices {

    private AmazonS3Client s3_client = new AmazonS3Client();
    ImageRepository imageRepository;
    Random random = new Random();
    @Value("${amazonProperties.s3_bucket_name}")
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
        System.out.println(multipartFile.getContentType());
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

        if(product == null)
        {
            return new ResponseEntity<>("Product does not exist", HttpStatus.BAD_REQUEST);
        }

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
            return new ResponseEntity<Object>("You can only add image for the product you have created",HttpStatus.FORBIDDEN);
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

            String s3_bucket_path = s3_bucket_name+"/"+random.nextInt()+"/"+UUID.randomUUID()+"/"+product.getName()+file_name;

            //Creating an Image object
            Image newImage = new Image();
            newImage.setImage_id(UUID.randomUUID());
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
}
