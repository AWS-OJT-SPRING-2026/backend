package ojt.aws.educare.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
@Slf4j
public class CognitoAdminConfig {

    @Value("${aws.region}")
    private String awsRegion;

    @Bean
    public CognitoIdentityProviderClient cognitoIdentityProviderClient() {
        log.info("[CognitoConfigAudit] creating CognitoIdentityProviderClient with awsRegion={}", awsRegion);
        return CognitoIdentityProviderClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }
}

