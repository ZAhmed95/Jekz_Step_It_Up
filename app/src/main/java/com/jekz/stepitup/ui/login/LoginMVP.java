package com.jekz.stepitup.ui.login;

/**
 * Created by evanalmonte on 12/8/17.
 */

import com.jekz.stepitup.ui.BasePresenter;

/**
 * Contract for Login MVP
 */
interface LoginMVP {
    /**
     * Interface representing a view
     */
    interface View {
        /**
         * Displays a message
         *
         * @param message message to be displayed
         */
        void showMessage(String message);

        /**
         * Navigate to home activity
         */
        void startHomeActivity();

        /**
         * Display the number of steps walked so far
         * @param text number of steps walked for the current session
         */
        void setStepText(String text);

        /**
         * Display the current progress
         * @param progress current progress
         */
        void setStepProgress(float progress);

        void enableLoginButton(boolean enabled);

        void enableLogoutButton(boolean enabled);
    }

    /**
     * Interface representing a presenter
     */
    interface Presenter extends BasePresenter<LoginMVP.View> {
        void onViewAttached(LoginMVP.View view);

        void onViewDetached();

        /**
         * Method which attempts to login using the specified credentials
         *
         * @param username string representing username
         * @param password string representing password
         */
        void login(String username, String password);

        /**
         * Logs out of the application
         */
        void logout();

        void registerSensor(boolean register);
    }
}