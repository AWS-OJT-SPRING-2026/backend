package ojt.aws.educare.service;

import ojt.aws.educare.entity.User;

public interface CognitoIdentityService {
    String createUser(String username, String email, String password, String fullName, String roleName);

    void updateUserProfile(User user);

    void changePassword(User user, String newPassword);

    void resetPassword(String usernameOrEmail);

    void lockUser(String usernameOrEmail);

    void unlockUser(String usernameOrEmail);

    void disableUser(String usernameOrEmail);

    void enableUser(String usernameOrEmail);

    void deleteUser(String usernameOrEmail);
}


