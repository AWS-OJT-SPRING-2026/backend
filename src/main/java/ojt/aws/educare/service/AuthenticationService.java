package ojt.aws.educare.service;


import com.nimbusds.jose.JOSEException;
import ojt.aws.educare.dto.request.IntrospectRequest;
import ojt.aws.educare.dto.request.LoginRequest;
import ojt.aws.educare.dto.request.LogoutRequest;
import ojt.aws.educare.dto.response.AuthenticationResponse;
import ojt.aws.educare.dto.response.IntrospectResponse;


import java.text.ParseException;

public interface AuthenticationService {
    AuthenticationResponse authenticate(LoginRequest request);
    IntrospectResponse introspect(IntrospectRequest request);
    void logout(LogoutRequest request) throws ParseException, JOSEException;
}
