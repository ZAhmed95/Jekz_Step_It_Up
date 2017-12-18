package com.jekz.stepitup.data.request;

/**
 * Created by evanalmonte on 12/12/17.
 */

public interface LoginManager {
    /**
     * Attempt to login in using specified username and password, on completion, callback is
     * notified
     */
    void login(String username, String password, LoginCallback callback);

    void logout(LogoutCallback callback);

    boolean isLoggedIn();

    String getSession();

    String getUsername();

    /**
     * Interface representing callback to be notified on completion of login
     */
    interface LoginCallback {
        /**
         * @param loginSuccess true if login success, false otherwise
         */
        void loginResult(LoginResult result);

        enum LoginResult {
            NETWORK_ERROR,
            INVALID_CREDENTIALS,
            SUCCESS
        }
    }

    interface LogoutCallback {

        void onLogout(boolean logoutSuccessful);
    }
}
