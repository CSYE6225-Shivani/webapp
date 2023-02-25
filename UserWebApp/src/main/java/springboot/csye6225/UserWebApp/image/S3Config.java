package springboot.csye6225.UserWebApp.image;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class S3Config {
    @Value("${AWS_REGION}")
    private String aws_region;

    @Bean
    @Primary
    AmazonS3 getS3Client()
    {
        return AmazonS3ClientBuilder
                .standard()
                .withRegion(aws_region)
                .build();
    }
}
