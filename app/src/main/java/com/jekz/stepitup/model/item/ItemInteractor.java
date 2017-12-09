package com.jekz.stepitup.model.item;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;
import android.util.Pair;

import com.jekz.stepitup.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by evanalmonte on 12/1/17.
 */

public class ItemInteractor {
    private static ItemInteractor instance;
    private HashMap<Integer, Pair<Item, Integer>> itemList;
    private HashMap<String, Integer> modelList;

    private ItemInteractor(LinkedHashMap<Integer, Pair<Item, Integer>> items,
                           HashMap<String, Integer> modelList) {
        this.itemList = items;
        this.modelList = modelList;
    }


    public static ItemInteractor getInstance(Resources resources) {
        if (instance != null) {
            return instance;
        }

        HashMap<String, Integer> modelList = extractModels(resources);
        LinkedHashMap<Integer, Pair<Item, Integer>> itemList = new LinkedHashMap<>();
        populateMiscItems(itemList, Item.Item_Type.MISC, resources, R.array.misc);
        populateItemList(itemList, resources, R.array.items);
        instance = new ItemInteractor(itemList, modelList);
        return instance;
    }

    @SuppressLint("ResourceType")
    private static void populateMiscItems(LinkedHashMap<Integer, Pair<Item, Integer>> itemList,
                                          Item.Item_Type type, Resources res, int arrayID) {
        int numOfAttributes = 5;
        TypedArray typedArray = res.obtainTypedArray(arrayID);
        for (int i = 0; i < typedArray.length(); i += numOfAttributes) {
            Item item = new Item(
                    typedArray.getInt(i, 0),
                    typedArray.getString(i + 2),
                    type,
                    typedArray.getBoolean(i + 3, false),
                    typedArray.getInteger(i + 4, -1));

            itemList.put(item.getId(), new Pair<>(item, typedArray.getResourceId(i + 1, 0)));
        }
        typedArray.recycle();
    }

    @SuppressLint("ResourceType")
    private static void populateItemList(HashMap<Integer, Pair<Item, Integer>> itemList,
                                         Resources res, int arrayID) {
        int numOfAttributes = res.getInteger(R.integer.num_item_attributes);
        TypedArray typedArray = res.obtainTypedArray(arrayID);
        int arrayLength = typedArray.length();
        for (int i = 0; i < arrayLength; i += numOfAttributes) {
            String name = typedArray.getString(i + 1);
            Log.d("Name", name);
            boolean isAnimate = typedArray.getBoolean(i + 3, false);
            int price = typedArray.getInteger(i + 4, -1);
            Item.Item_Type type = getTypeFromString(typedArray.getString(i + 2));
            Item item = new Item(name, type, isAnimate, price);
            itemList.put(item.getId(), new Pair<>(item, typedArray.getResourceId(i, 0)));
        }
        typedArray.recycle();
    }

    private static Item.Item_Type getTypeFromString(String string) {
        switch (string.toLowerCase()) {
            case "hat":
                return Item.Item_Type.HAT;
            case "shirt":
                return Item.Item_Type.SHIRT;
            case "pants":
                return Item.Item_Type.PANTS;
            case "shoes":
                return Item.Item_Type.SHOES;
        }
        return null;
    }

    private static HashMap<String, Integer> extractModels(Resources res) {
        HashMap<String, Integer> modelList = new HashMap<>();
        TypedArray typedArray = res.obtainTypedArray(R.array.models);
        for (int i = 0; i < typedArray.length(); i += 2) {
            modelList.put(typedArray.getString(i + 1), typedArray.getResourceId(i, 0));
        }
        typedArray.recycle();
        return modelList;
    }


    public List<Item> getItems(Item.Item_Type type) {
        List<Item> itemsByType = new ArrayList<>();
        for (Pair<Item, Integer> item : itemList.values()) {
            if (item.first.getType() == type) {
                itemsByType.add(item.first);
            }
        }
        return itemsByType;
    }

    public Pair<Item, Integer> getItem(int id) {
        return itemList.get(id);
    }

    public int getModel(String name) {
        return modelList.get(name);
    }
}
