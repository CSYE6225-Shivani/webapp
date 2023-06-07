//package springboot.csye6225.UserWebApp.image;
//import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.auth.InstanceProfileCredentialsProvider;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//
//@Configuration
//public class S3Config {
//
//    @Value("${aws_region.aws_region}")
//    private String awsRegion;
//
//    public AWSCredentials credentials() {
//        AWSCredentials credentials = new BasicAWSCredentials(
//                "",
//                ""
//        );
//        return credentials;
//    }
//
//    @Bean
//    @Primary
//    public AmazonS3 getS3Client()
//    {
//        AmazonS3 amazonS3= AmazonS3ClientBuilder
//                .standard()
//                .withCredentials(new AWSStaticCredentialsProvider(credentials()))
//                .withRegion(awsRegion)
//                .build();
//        return amazonS3;
//    }
//
////    @Bean
////    @Primary
////    public AmazonS3 amazonS3()
////    {
////        return AmazonS3ClientBuilder
////                .standard()
////                .withCredentials(new InstanceProfileCredentialsProvider(false))
////                .withRegion(awsRegion)
////                .build();
////    }
//}
