package com.jekz.stepitup.ui.home;

import android.util.Log;
import android.util.Pair;

import com.jekz.stepitup.customview.AvatarImage;
import com.jekz.stepitup.data.LoginPreferences;
import com.jekz.stepitup.data.SharedPrefsManager;
import com.jekz.stepitup.data.request.LoginManager;
import com.jekz.stepitup.data.request.RequestString;
import com.jekz.stepitup.model.avatar.Avatar;
import com.jekz.stepitup.model.avatar.AvatarRepo;
import com.jekz.stepitup.model.item.Item;
import com.jekz.stepitup.model.item.ItemInteractor;
import com.jekz.stepitup.model.step.ManualStepCounter;
import com.jekz.stepitup.model.step.Session;
import com.jekz.stepitup.model.step.SessionStepCounter;
import com.jekz.stepitup.ui.shop.AsyncResponse;
import com.jekz.stepitup.ui.shop.ShopRequest;
import com.jekz.stepitup.util.SessionSaver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

import static com.jekz.stepitup.util.Formatter.formatSteps;


/**
 * Created by evanalmonte on 12/10/17.
 */

public class HomePresenter implements HomeContract.Presenter, AsyncResponse,
        SessionStepCounter.SessionListener, Avatar.AvatarObserver {
    private static final String TAG = HomePresenter.class.getName();
    private ItemInteractor itemInteractor;
    private AvatarRepo repo = AvatarRepo.getInstance();
    private HomeContract.View view;

    private LoginManager loginManager;
    private ManualStepCounter stepCounter;
    private SessionSaver sessionSaver;
    private LoginPreferences loginPreferences;

    public HomePresenter(ItemInteractor itemInteractor, LoginManager loginManager,
                         ManualStepCounter stepCounter, SessionSaver sessionSaver,
                         LoginPreferences preferences) {
        this.itemInteractor = itemInteractor;
        this.loginManager = loginManager;
        this.stepCounter = stepCounter;
        this.sessionSaver = sessionSaver;
        this.loginPreferences = preferences;
        stepCounter.addSessionListener(this);
        retrieveItem("get_items");
        retrieveItem("user_data");
    }

    @Override
    public void loadAvatar() {
        Avatar avatar = repo.getAvatar();
        Item hat = avatar.getHat();
        Item shirt = avatar.getShirt();
        Item pants = avatar.getPants();
        Item shoes = avatar.getShoes();

        String model = avatar.getModel();
        if (hat != null) {
            view.setAvatarImagePart(AvatarImage.AvatarPart.HAT, itemInteractor.getItem(hat.getId()
            ).second);

        }

        if (shirt != null) {
            view.setAvatarImagePart(AvatarImage.AvatarPart.SHIRT, itemInteractor.getItem(shirt
                    .getId()).second);

        }

        if (pants != null) {
            view.setAvatarImagePart(AvatarImage.AvatarPart.PANTS, itemInteractor.getItem(pants
                    .getId()).second);

        }

        if (shoes != null) {
            view.setAvatarImagePart(AvatarImage.AvatarPart.SHOES, itemInteractor.getItem(shoes
                    .getId()).second);
        }
        view.setAvatarImagePart(AvatarImage.AvatarPart.MODEL, itemInteractor.getModel(model));
    }

    @Override
    public void accessShop() {
        view.navigateToShop();
    }

    @Override
    public void accessLoginScreen() {
        view.navigateToLoginScreen();
    }

    @Override
    public void accessGraphs() {
        view.navigateToGraphs();
    }

    @Override
    public void accessFriends() {
        view.navigateToFriendsScreen();
    }

    @Override
    public void logout() {
        endSession();
        sessionSaver.sendStoredSessions();
        loginManager.logout(new LoginManager.LogoutCallback() {
            @Override
            public void onLogout(boolean logoutSuccessful) {
                view.showMessage("Successfully logged out");
                repo.resetAvatar();
                view.navigateToLoginScreen();
            }
        });
    }

    @Override
    public void login() {
        view.navigateToLoginScreen();
    }

    @Override
    public void endSession() {
        if (sessionSaver.stopStoringSession()) {
            stepCounter.endSession();
        }
    }

    @Override
    public void startSession() {
        if (sessionSaver.startStoringSession()) {
            stepCounter.startSession();
        }
    }

    @Override
    public void accessProfile() {
        view.navigateToProfile();
    }

    @Override
    public void onViewAttached(HomeContract.View view) {
        this.view = view;
        stepCounter.addSessionListener(this);
        repo.addRepoObserver(this);
        view.setCurrency("x" + NumberFormat.getInstance().format(repo.getAvatar().getCurrency()));
        view.setUsername(loginManager.getUsername());
        view.setSteps(formatSteps(sessionSaver.getCurrentSteps()));
        if (sessionSaver.isStoringSteps()) {
            view.disableSession();
        } else {
            view.enableSession();
        }
        loadAvatar();
    }

    @Override
    public void onViewDetached() {
        sessionSaver.sendStoredSessions();
        stepCounter.removeSessionListener(this);
        repo.removeRepoObserver(this);
        this.view = null;
    }

    private void retrieveItem(String datatype) {
        String session = loginManager.getSession();
        JSONObject postData = new JSONObject();
        try {
            postData.put("data_type", datatype);

        } catch (JSONException e) {e.printStackTrace();}

        ShopRequest asyncTask2 = new ShopRequest(postData, session);
        asyncTask2.delegate = this;
        asyncTask2.execute(RequestString.getURL() + "/api/db/retrieve");
    }


    @Override
    public void processFinish(JSONObject returnObject) {
        Avatar avatar = repo.getAvatar();
        try {
            String actionType = returnObject.getString("return_data");
            Log.d(TAG, "Recieved Output: " + returnObject.toString());
            Log.d(TAG, "Action Type: " + actionType);
            if (view == null) { return; }
            JSONArray output = returnObject.getJSONArray("rows");
            for (int i = 0; i < output.length(); i++) {
                JSONObject q = output.getJSONObject(i);
                switch (actionType) {
                    case "get_items": {
                        int itemcount = q.getInt("count");
                        if (itemcount > 0) {
                            int itemid = q.getInt("itemid");
                            Pair<Item, Integer> itemPair;
                            itemPair = itemInteractor.getItem(itemid);
                            if (itemPair != null) {
                                Log.d(TAG, "Adding item: " + itemPair.first.toString());
                                avatar.addItem(itemPair.first);
                            }
                        }
                        break;
                    }
                    case "user_data": {
                        String model = q.getString("gender");
                        int modelID = itemInteractor.getModel(model);
                        int hatid = q.getInt("hat");
                        int shirtid = q.getInt("shirt");
                        int pantsid = q.getInt("pants");
                        int shoesid = q.getInt("shoes");

                        String height = q.getString("height");
                        String weight = q.getString("weight");
                        String dailyGoal = q.getString("daily_goal");
                        saveToPrefs(height, SharedPrefsManager.Key.HEIGHT);
                        saveToPrefs(weight, SharedPrefsManager.Key.WEIGHT);
                        saveToPrefs(dailyGoal, SharedPrefsManager.Key.GOAL);
                        saveToPrefs(model, SharedPrefsManager.Key.GENDER);

                        Pair<Item, Integer> hat = itemInteractor.getItem(hatid);
                        Pair<Item, Integer> shirt = itemInteractor.getItem(shirtid);
                        Pair<Item, Integer> pants = itemInteractor.getItem(pantsid);
                        Pair<Item, Integer> shoes = itemInteractor.getItem(shoesid);
                        avatar.wearItem(hat.first);
                        avatar.wearItem(shirt.first);
                        avatar.wearItem(pants.first);
                        avatar.wearItem(shoes.first);
                        avatar.setModel(model);
                        view.setAvatarImagePart(AvatarImage.AvatarPart.MODEL, modelID);
                        view.animateAvatarImagePart(AvatarImage.AvatarPart.MODEL, true);
                        view.setAvatarImagePart(AvatarImage.AvatarPart.HAT, hat.second);
                        view.animateAvatarImagePart(AvatarImage.AvatarPart.HAT, true);
                        view.setAvatarImagePart(AvatarImage.AvatarPart.SHIRT, shirt.second);
                        view.animateAvatarImagePart(AvatarImage.AvatarPart.SHIRT, true);
                        view.setAvatarImagePart(AvatarImage.AvatarPart.PANTS, pants.second);
                        view.animateAvatarImagePart(AvatarImage.AvatarPart.PANTS, true);
                        view.setAvatarImagePart(AvatarImage.AvatarPart.SHOES, shoes.second);
                        view.animateAvatarImagePart(AvatarImage.AvatarPart.SHOES, true);
                        int currency = q.getInt("currency");
                        avatar.setCurrency(currency);
                        view.setCurrency(
                                "x" + NumberFormat.getInstance().format(avatar.getCurrency()));
                        break;
                    }
                }
            }
        } catch (JSONException ignored) {}
    }

    private void saveToPrefs(String value, SharedPrefsManager.Key key) {
        if (value.equals("null")) { value = "0"; }
        loginPreferences.put(key, value);
    }

    @Override
    public void sessionEnded(Session session) {
        if (view != null) {
            view.setSteps(formatSteps(0));
        }
    }

    @Override
    public void onStepCountIncreased(int stepcount) {
        if (view != null) {
            view.setSteps(NumberFormat.getInstance().format(stepcount) + " steps");
        }
    }

    @Override
    public void onCurrencyChanged(int steps) {
        if (view == null) { return; }
        view.setCurrency(formatSteps(steps));
    }


}
