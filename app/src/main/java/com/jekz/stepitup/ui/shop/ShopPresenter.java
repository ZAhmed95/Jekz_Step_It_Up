package com.jekz.stepitup.ui.shop;

import com.jekz.stepitup.model.avatar.Avatar;
import com.jekz.stepitup.model.item.Item;
import com.jekz.stepitup.model.item.ItemInteractor;
import com.jekz.stepitup.model.step.AndroidStepCounter;
import com.jekz.stepitup.model.step.StepCounter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

/**
 * Created by evanalmonte on 12/2/17.
 */

public class ShopPresenter implements Presenter, StepCounter.StepCounterCallback,
        com.jekz.stepitup.graphtest.AsyncResponse {
    private ItemInteractor itemInteractor;
    private ShopView shopView;
    private Avatar avatar;
    private NumberFormat numberFormat = NumberFormat.getInstance();
    private AndroidStepCounter androidStepCounter;

    ShopPresenter(AndroidStepCounter androidStepCounter, ItemInteractor instance) {
        this.itemInteractor = instance;
        this.androidStepCounter = androidStepCounter;
        this.androidStepCounter.addListener(this);
        avatar = new Avatar();
        avatar.addCurrency(13000);
    }


    void reloadAnimations() {
        Item hat = avatar.getHat();
        Item shirt = avatar.getShirt();
        Item pants = avatar.getPants();
        Item shoes = avatar.getShoes();

        if (hat != null && hat.isAnimated()) {
            shopView.animateHat(true);
        }
        if (shirt != null && shirt.isAnimated()) {
            shopView.animateShirt(true);
        }
        if (pants != null && pants.isAnimated()) {
            shopView.animatePants(true);
        }
        if (shoes != null && shoes.isAnimated()) {
            shopView.animateShoes(true);
        }
        shopView.animateAvatar(true);

    }

    public void loadHats() {
        shopView.showItems(itemInteractor.getItems(Item.Item_Type.HAT));
    }

    public void loadShirts() {
        shopView.showItems(itemInteractor.getItems(Item.Item_Type.SHIRT));
    }

    public void loadPants() {
        shopView.showItems(itemInteractor.getItems(Item.Item_Type.PANTS));

    }

    public void loadShoes() {
        shopView.showItems(itemInteractor.getItems(Item.Item_Type.SHOES));
    }

    public void buyItem(Item item) {
        if (!avatar.buyItem(item)) { return; }
        ShopRequest asyncTask = new ShopRequest(null);
        ShopRequest asyncTask2 = new ShopRequest(null);

        asyncTask.delegate = this;
        asyncTask2.delegate = this;

        JSONObject postData = new JSONObject();

        try {
            postData.put("data_type", "items");
            postData.put("userid", 1);
            postData.put("itemid", 1);
            postData.put("amount", 1);

        } catch (JSONException e) {e.printStackTrace();}

        asyncTask.postData = postData;
        asyncTask.execute("https://jekz.herokuapp.com/api/db/update");

        postData = new JSONObject();
        try {
            postData.put("data_type", "items");
            postData.put("userid", 1);

        } catch (JSONException e) {e.printStackTrace();}

        asyncTask2.postData = postData;
        asyncTask2.execute("https://jekz.herokuapp.com/api/db/retrieve");
        shopView.setCurrencyText("x" + NumberFormat.getInstance().format(avatar.getCurrency()));
        shopView.reloadAdapter();
    }

    public boolean checkForItem(Item item) {
        return avatar.hasItem(item);
    }

    public void equipItem(Item item) {
        avatar.wearItem(item);
        int resourceID = itemInteractor.getItem(item.getId()).second;
        switch (item.getType()) {
            case HAT:
                shopView.setHatImage(resourceID);
                break;
            case SHIRT:
                shopView.setShirtImage(resourceID);
                break;
            case PANTS:
                shopView.setPantsImage(resourceID);
                break;
            case SHOES:
                shopView.setShoesImage(resourceID);
                break;
        }
        if (item.isAnimated()) {
            reloadAnimations();
        }
        shopView.reloadAdapter();
    }

    public void changeGender() {
        boolean isMale = avatar.isMale();
        avatar.setMale(!isMale);
        String modelString = avatar.isMale() ? "male" : "female";
        int modelID = itemInteractor.getModel(modelString);
        shopView.setAvatarImage(modelID);
        reloadAnimations();
    }

    public int resourceRequested(Item item) {
        return itemInteractor.getItem(item.getId()).second;
    }

    public void unequipItem(Item item) {
        avatar.removeItem(item.getType());
        switch (item.getType()) {
            case HAT:
                shopView.setHatImage(0);
            case SHIRT:
                shopView.setShirtImage(0);
                break;
            case PANTS:
                shopView.setPantsImage(0);
                break;
            case SHOES:
                shopView.setShoesImage(0);
                break;
        }
        shopView.reloadAdapter();
    }

    @Override
    public void onViewAttached(ShopView view) {
        this.shopView = view;
        shopView.setCurrencyText("x" + numberFormat.format(avatar.getCurrency()));
    }

    @Override
    public void onViewDetached() {
        this.shopView = null;
    }

    public boolean isItemEquipped(Item item) {
        return avatar.isItemEquipped(item);
    }

    public void registerStepCounter(boolean b) {
        if (b) {
            androidStepCounter.registerSensor();
        } else {
            androidStepCounter.unregisterSensor();
        }
    }

    @Override
    public void onStepDetected() {
        avatar.addCurrency(1);
        shopView.setCurrencyText("x" + NumberFormat.getInstance().format(avatar.getCurrency()));
    }

    @Override
    public void processFinish(JSONArray output) {
        //Here you will receive the result fired from async class
        //of onPostExecute(result) method.

        //Log.d("myTest", "made it to processFinish in main thread");
        try {
            //Log.d("myTest", "Test Test")
            for (int i = 0; i < output.length(); i++) {
                JSONObject r = output.getJSONObject(i);
                //Log.d("myTest", "rowCount value: " + r.getInt("rowCount"));
            }
        } catch (JSONException e) {e.printStackTrace();}

    }
}
