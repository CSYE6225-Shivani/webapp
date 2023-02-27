package springboot.csye6225.UserWebApp.image;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class S3Config {
//    @Value("${amazonProperties.region}")
//    private String aws_region;
//
//    // Access key id will be read from the application.properties file during the application intialization.
//    @Value("${amazonProperties.access_key_id}")
//    private String accessKeyId;
//    // Secret access key will be read from the application.properties file during the application intialization.
//    @Value("${amazonProperties.secret_access_key}")
//    private String secretAccessKey;

    public AWSCredentials credentials() {
        AWSCredentials credentials = new BasicAWSCredentials(
                "AKIA35FCJK2XRLFRHSM2",
                "U435haQyMOuTIBb3C2RGllTO1yE8hePU95ZJGsqt"
        );
        return credentials;
    }

    @Bean
    @Primary
    public AmazonS3 getS3Client()
    {
        AmazonS3 amazonS3= AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials()))
                .withRegion("us-east-1")
                .build();
        return amazonS3;
    }
}
