//package springboot.csye6225.UserWebApp.image;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//
//@Configuration
//public class S3Config {
//    @Value("${amazonProperties.region}")
//    private String aws_region;
//
//    // Access key id will be read from the application.properties file during the application intialization.
//    @Value("${amazonProperties.access_key_id}")
//    private String accessKeyId;
//    // Secret access key will be read from the application.properties file during the application intialization.
//    @Value("${amazonProperties.secret_access_key}")
//    private String secretAccessKey;
//
//    @Bean
//    @Primary
//    AmazonS3 getS3Client()
//    {
//        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
//        return AmazonS3ClientBuilder
//                .standard()
//                .withRegion(aws_region)
//                .build();
//    }
//}
