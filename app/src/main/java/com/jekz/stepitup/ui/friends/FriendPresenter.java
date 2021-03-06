package com.jekz.stepitup.ui.friends;

import android.util.Log;

import com.jekz.stepitup.R;
import com.jekz.stepitup.customview.AvatarImage;
import com.jekz.stepitup.data.request.LoginManager;
import com.jekz.stepitup.data.request.RequestString;
import com.jekz.stepitup.model.friend.Friend;
import com.jekz.stepitup.model.item.ItemInteractor;
import com.jekz.stepitup.ui.shop.AsyncResponse;
import com.jekz.stepitup.ui.shop.ShopRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;

import static com.jekz.stepitup.adapter.FriendsListRecyclerAdapter.FriendRowView;
import static com.jekz.stepitup.adapter.FriendsListRecyclerAdapter.FriendsListPresenter;

/**
 * Created by Evan and Kevin
 */

public class FriendPresenter implements FriendContract.Presenter, FriendsListPresenter,
        AsyncResponse {
    private static final String TAG = FriendPresenter.class.getName();

    private HashSet<Friend> friendList = new HashSet<>();

    private FriendContract.View view;

    private LoginManager loginManager;

    private ItemInteractor itemInteractor;

    private CurrentList currentList = CurrentList.FRIEND;

    FriendPresenter(LoginManager loginManager, ItemInteractor itemInteractor) {
        this.loginManager = loginManager;
        this.itemInteractor = itemInteractor;
    }

    @Override
    public void onViewAttached(FriendContract.View view) {
        this.view = view;
    }

    @Override
    public void onViewDetached() {
        this.view = null;
    }

    @Override
    public void searchUser(String username) {
        friendList.clear();
        view.showSearch(true);
        view.reloadFriendsList();
        currentList = CurrentList.SEARCH;
        doSearchRequest(username);
    }

    @Override
    public void loadPending() {
        friendList.clear();
        retrieveFriends("pending_friends");
        view.reloadFriendsList();
        currentList = CurrentList.PENDING;
        view.showSearch(false);
    }

    @Override
    public void loadFriends() {
        friendList.clear();
        retrieveFriends("friends");
        view.reloadFriendsList();
        currentList = CurrentList.FRIEND;
        view.showSearch(false);
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Friend friend = (Friend) friendList.toArray()[position];
        return friend.getFriendType().ordinal();
    }

    @Override
    public void onBindFriendsRowViewAtPosition(int position, FriendRowView
            rowView, int selectedPosition) {
        Friend friend = (Friend) friendList.toArray()[position];
        rowView.setUsername(friend.getUsername());
        if (position == selectedPosition) {
            rowView.setBackgroundColor(R.drawable.shape_selected_friend);
        } else {
            rowView.setBackgroundColor(R.drawable.shape_shop_item_border);
        }
    }

    @Override
    public void confirmFriend(int position) {
        Friend friend = (Friend) friendList.toArray()[position];
        adjustFriends("accept_friend", friend.getId());
    }

    @Override
    public void denyFriend(int position) {
        Friend friend = (Friend) friendList.toArray()[position];
        view.showMessage(friend.getUsername() + " has been denied");
        adjustFriends("deny_friend", friend.getId());
    }

    @Override
    public void friendSelected(int position) {
        Friend friend = (Friend) friendList.toArray()[position];
        view.showMessage("Loading " + friend.getUsername() + "'s avatar");
        retrieveFriendEquip(friend.getId());
    }

    @Override
    public void removeFriend(int position) {
        Friend friend = (Friend) friendList.toArray()[position];
        view.showMessage(friend.getUsername() + " has been removed");
        adjustFriends("remove_friend", friend.getId());
    }

    @Override
    public void requestFriend(int position) {
        Friend friend = (Friend) friendList.toArray()[position];
        adjustFriends("request_friend", friend.getId());
    }

    private void retrieveFriends(String datatype) {
        String session = loginManager.getSession();

        JSONObject postData = new JSONObject();
        try {
            postData.put("action", datatype);

        } catch (JSONException e) {e.printStackTrace();}

        ShopRequest asyncTask = new ShopRequest(postData, session);
        asyncTask.delegate = this;
        asyncTask.execute(RequestString.getURL() + "/api/db/retrieve");
    }

    private void doSearchRequest(String searchName) {
        String session = loginManager.getSession();

        JSONObject postData = new JSONObject();
        try {
            postData.put("action", "search_user");
            postData.put("username", searchName);

        } catch (JSONException e) {e.printStackTrace();}

        ShopRequest asyncTask = new ShopRequest(postData, session);
        asyncTask.delegate = this;
        asyncTask.execute(RequestString.getURL() + "/api/db/retrieve");
    }

    private void adjustFriends(String type, int id) {
        String session = loginManager.getSession();

        JSONObject postData = new JSONObject();

        try {
            postData.put("action", type);
            postData.put("friendid", id);

        } catch (JSONException e) {e.printStackTrace();}


        ShopRequest asyncTask = new ShopRequest(postData, session);
        asyncTask.delegate = this;
        asyncTask.execute(RequestString.getURL() + "/api/db/update");
    }

    private void retrieveFriendEquip(int id) {
        JSONObject postData = new JSONObject();
        try {
            postData.put("action", "user_data");
            postData.put("userid", id);
        } catch (JSONException e) {e.printStackTrace();}

        ShopRequest asyncTask = new ShopRequest(postData, null);
        asyncTask.delegate = this;
        asyncTask.execute(RequestString.getURL() + "/api/db/retrieve");
    }

    @Override
    public void processFinish(JSONObject returnObject) {
        try {
            JSONArray output = returnObject.getJSONArray("rows");
            String actionType = returnObject.getString("return_data");
            Log.d(TAG, output.toString());
            Log.d(TAG, "Action type: " + actionType);
            // Prevent user from clicking a different column and the asynchronous results
            // "appearing" under the wrong tab.
            if ((actionType.equals("friends") && currentList != CurrentList.FRIEND) ||
                (actionType.equals("pending_friends") && currentList != CurrentList.PENDING) ||
                (actionType.equals("search_user") && currentList != CurrentList.SEARCH)) { return; }
            for (int i = 0; i < output.length(); i++) {
                JSONObject q = output.getJSONObject(i);
                switch (actionType) {
                    case "friends": {
                        int friendID = q.getInt("friendid");
                        String friendName = q.getString("friendname");
                        Log.d(TAG, "Friend: " + friendID + " - " + friendName);
                        Friend newFriend = new Friend(friendName, friendID, Friend.FriendType
                                .CONFIRMED);
                        friendList.add(newFriend);
                        break;
                    }
                    case "pending_friends": {
                        int friendID = q.getInt("friendid");
                        String friendName = q.getString("friendname");
                        Log.d(TAG, "Pending friend: " + friendID + " - " + friendName);
                        Friend newFriend = new Friend(friendName, friendID, Friend.FriendType
                                .PENDING);
                        friendList.add(newFriend);
                        break;
                    }
                    case "search_user": {
                        int friendID = q.getInt("userid");
                        String friendName = q.getString("username");
                        Log.d(TAG, "Search friend: " + friendID + " - " + friendName);
                        Friend searchedFriend = new Friend(friendName, friendID, Friend.FriendType
                                .SEARCHED);
                        friendList.add(searchedFriend);
                        break;
                    }
                    case "remove_friend": {
                        int friendID = q.getInt("friendid");
                        Log.d(TAG, "Removed friend with id: " + friendID);
                        removeFriendFromList(friendID);
                        break;
                    }
                    case "accept_friend": {
                        if (q.getBoolean("success")) {
                            int friendID = q.getInt("friendid");
                            Log.d(TAG, "Accepted friend with id: " + friendID);
                            removeFriendFromList(friendID);
                        } else {
                            Log.d(TAG, "Could not add friend");
                            view.showMessage("You can not add yourself");
                        }
                        break;
                    }
                    case "deny_friend": {
                        int friendID = q.getInt("friendid");
                        removeFriendFromList(friendID);
                        break;
                    }
                    case "user_data": {
                        String gender = q.getString("gender");
                        int modelID = itemInteractor.getModel(gender);
                        view.setAvatarImagePart(AvatarImage.AvatarPart.MODEL, modelID);
                        view.animateAvatarImagePart(AvatarImage.AvatarPart.MODEL, true);
                        int hatid = q.getInt("hat");
                        int shirtid = q.getInt("shirt");
                        int pantsid = q.getInt("pants");
                        int shoesid = q.getInt("shoes");
                        int hatID = itemInteractor.getItem(hatid).second;
                        int shirtID = itemInteractor.getItem(shirtid).second;
                        int pantsID = itemInteractor.getItem(pantsid).second;
                        int shoesID = itemInteractor.getItem(shoesid).second;
                        view.setAvatarImagePart(AvatarImage.AvatarPart.HAT, hatID);
                        view.animateAvatarImagePart(AvatarImage.AvatarPart.HAT, true);
                        view.setAvatarImagePart(AvatarImage.AvatarPart.SHIRT, shirtID);
                        view.animateAvatarImagePart(AvatarImage.AvatarPart.SHIRT, true);
                        view.setAvatarImagePart(AvatarImage.AvatarPart.PANTS, pantsID);
                        view.animateAvatarImagePart(AvatarImage.AvatarPart.PANTS, true);
                        view.setAvatarImagePart(AvatarImage.AvatarPart.SHOES, shoesID);
                        view.animateAvatarImagePart(AvatarImage.AvatarPart.SHOES, true);
                        view.showAvatar(true);
                        break;
                    }
                }
            }
        } catch (JSONException ignored) {
            Log.d(TAG, ignored.getMessage());
        }
        view.reloadFriendsList();
    }

    private void removeFriendFromList(int id) {
        Iterator<Friend> iterator = friendList.iterator();
        while (iterator.hasNext()) {
            Friend friend = iterator.next();
            if (friend.getId() == id) {
                iterator.remove();
                return;
            }
        }
    }


    private enum CurrentList {
        SEARCH, FRIEND, PENDING
    }
}
