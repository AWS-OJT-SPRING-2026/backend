package ojt.aws.educare.service.Impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.service.CognitoIdentityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDisableUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminEnableUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.MessageActionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class CognitoIdentityServiceImpl implements CognitoIdentityService {

    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${cognito.user-pool-id}")
    private String userPoolId;

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${cognito.app-client-id}")
    private String appClientId;

    private final AtomicBoolean firstCallLogged = new AtomicBoolean(false);

    @PostConstruct
    void logRuntimeBindingsAtStartup() {
        log.info("[CognitoConfigAudit] startup userPoolId={} awsRegion={}", userPoolId, awsRegion);
    }

    private void logBeforeFirstCognitoCall() {
        if (firstCallLogged.compareAndSet(false, true)) {
            log.info("[CognitoConfigAudit] first-call userPoolId={} awsRegion={}", userPoolId, awsRegion);
        }
    }

    @Override
    public String createUser(String username, String email, String password, String fullName, String roleName) {
        logBeforeFirstCognitoCall();
        validateRegionBinding();
        validateUserPoolExists();

        try {
            cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .messageAction(MessageActionType.SUPPRESS)
                    .userAttributes(
                            AttributeType.builder().name("email").value(email).build(),
                            AttributeType.builder().name("email_verified").value("true").build(),
                            AttributeType.builder().name("name").value(fullName == null ? username : fullName).build(),
                            AttributeType.builder().name("custom:role").value(roleName).build()
                    )
                    .build());

            cognitoClient.adminSetUserPassword(AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .password(password)
                    .permanent(true)
                    .build());

            var user = cognitoClient.adminGetUser(AdminGetUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .build());

            return user.userAttributes().stream()
                    .filter(a -> "sub".equals(a.name()))
                    .map(AttributeType::value)
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        } catch (UsernameExistsException ignored) {
            var existing = cognitoClient.adminGetUser(AdminGetUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .build());
            return existing.userAttributes().stream()
                    .filter(a -> "sub".equals(a.name()))
                    .map(AttributeType::value)
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        } catch (CognitoIdentityProviderException ex) {
            throw mapCognitoException("createUser", ex);
        }
    }

    @Override
    public void updateUserProfile(User user) {
        logBeforeFirstCognitoCall();
        validateRegionBinding();
        validateUserPoolExists();

        try {
            cognitoClient.adminUpdateUserAttributes(AdminUpdateUserAttributesRequest.builder()
                    .userPoolId(userPoolId)
                    .username(user.getUsername())
                    .userAttributes(
                            AttributeType.builder().name("email").value(user.getEmail()).build(),
                            AttributeType.builder().name("email_verified").value("true").build(),
                            AttributeType.builder().name("name").value(user.getFullName()).build(),
                            AttributeType.builder().name("custom:role").value(user.getRole().getRoleName()).build()
                    )
                    .build());
        } catch (CognitoIdentityProviderException ex) {
            throw mapCognitoException("updateUserProfile", ex);
        }
    }

    @Override
    public void changePassword(User user, String newPassword) {
        logBeforeFirstCognitoCall();
        validateRegionBinding();
        validateUserPoolExists();

        try {
            cognitoClient.adminSetUserPassword(AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(user.getUsername())
                    .password(newPassword)
                    .permanent(true)
                    .build());
        } catch (CognitoIdentityProviderException ex) {
            throw mapCognitoException("changePassword", ex);
        }
    }

    @Override
    public void resetPassword(String usernameOrEmail) {
        logBeforeFirstCognitoCall();
        validateRegionBinding();

        if (appClientId == null || appClientId.isBlank()) {
            throw new IllegalStateException("Missing required property: cognito.app-client-id");
        }

        try {
            cognitoClient.forgotPassword(ForgotPasswordRequest.builder()
                    .clientId(appClientId)
                    .username(usernameOrEmail)
                    .build());
        } catch (CognitoIdentityProviderException ex) {
            throw mapCognitoException("resetPassword", ex);
        }
    }

    @Override
    public void lockUser(String usernameOrEmail) {
        disableUser(usernameOrEmail);
    }

    @Override
    public void unlockUser(String usernameOrEmail) {
        enableUser(usernameOrEmail);
    }

    private void validateRegionBinding() {
        if (userPoolId == null || userPoolId.isBlank()) {
            throw new IllegalStateException("Missing required property: cognito.user-pool-id");
        }
        if (awsRegion == null || awsRegion.isBlank()) {
            throw new IllegalStateException("Missing required property: aws.region");
        }

        var separatorIndex = userPoolId.indexOf('_');
        if (separatorIndex <= 0) {
            throw new IllegalStateException("Invalid Cognito userPoolId format: " + userPoolId);
        }

        var poolRegion = userPoolId.substring(0, separatorIndex);
        if (!poolRegion.equals(awsRegion)) {
            throw new IllegalStateException(
                    "Cognito region mismatch: userPoolId=" + userPoolId +
                            " implies region=" + poolRegion +
                            " but aws.region=" + awsRegion
            );
        }
    }

    private void validateUserPoolExists() {
        try {
            cognitoClient.describeUserPool(request -> request.userPoolId(userPoolId));
        } catch (ResourceNotFoundException ex) {
            var details = ex.awsErrorDetails();
            var errorCode = details != null ? details.errorCode() : "ResourceNotFoundException";
            var errorMessage = details != null ? details.errorMessage() : ex.getMessage();
            var requestId = ex.requestId();

            log.error("[CognitoIdentityService] user pool not found userPoolId={} awsRegion={} awsErrorCode={} awsMessage={} requestId={}",
                    userPoolId, awsRegion, errorCode, errorMessage, requestId, ex);
            throw new IllegalStateException(
                    "Configured Cognito user pool does not exist or is not accessible: " + userPoolId +
                            " in region " + awsRegion +
                            ". Verify AWS account, region, and credentials. requestId=" + requestId,
                    ex
            );
        } catch (CognitoIdentityProviderException ex) {
            var details = ex.awsErrorDetails();
            var errorCode = details != null ? details.errorCode() : "unknown";
            var errorMessage = details != null ? details.errorMessage() : ex.getMessage();
            var requestId = ex.requestId();

            log.error("[CognitoIdentityService] failed to validate user pool userPoolId={} awsRegion={} awsErrorCode={} awsMessage={} requestId={}",
                    userPoolId, awsRegion, errorCode, errorMessage, requestId, ex);
            throw ex;
        }
    }

    @Override
    public void disableUser(String usernameOrEmail) {
        logBeforeFirstCognitoCall();
        validateRegionBinding();
        validateUserPoolExists();
        try {
            cognitoClient.adminDisableUser(AdminDisableUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(usernameOrEmail)
                    .build());
        } catch (CognitoIdentityProviderException ex) {
            throw mapCognitoException("disableUser", ex);
        }
    }

    @Override
    public void enableUser(String usernameOrEmail) {
        logBeforeFirstCognitoCall();
        validateRegionBinding();
        validateUserPoolExists();
        try {
            cognitoClient.adminEnableUser(AdminEnableUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(usernameOrEmail)
                    .build());
        } catch (CognitoIdentityProviderException ex) {
            throw mapCognitoException("enableUser", ex);
        }
    }

    @Override
    public void deleteUser(String usernameOrEmail) {
        logBeforeFirstCognitoCall();
        validateRegionBinding();
        validateUserPoolExists();
        try {
            cognitoClient.adminDeleteUser(AdminDeleteUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(usernameOrEmail)
                    .build());
        } catch (CognitoIdentityProviderException ex) {
            throw mapCognitoException("deleteUser", ex);
        }
    }

    private AppException mapCognitoException(String action, CognitoIdentityProviderException ex) {
        var details = ex.awsErrorDetails();
        var errorCode = details != null ? details.errorCode() : "unknown";
        var errorMessage = details != null ? details.errorMessage() : ex.getMessage();
        var requestId = ex.requestId();

        log.error("[CognitoIdentityService] action={} failed userPoolId={} awsRegion={} awsErrorCode={} awsMessage={} requestId={}",
                action, userPoolId, awsRegion, errorCode, errorMessage, requestId, ex);
        return new AppException(ErrorCode.COGNITO_SYNC_FAILED);
    }
}


