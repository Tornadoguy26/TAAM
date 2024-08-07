package com.example.taam;

import android.content.Context;
import android.widget.Toast;

import com.example.taam.structures.Item;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class DatabaseManager {
    static DatabaseManager databaseManager;
    private static FirebaseDatabase db;
    private static DatabaseReference dbRef;
    private static FirebaseStorage storage;
    private static StorageReference storageRef;
    private DatabaseManager() {

    }

    public static DatabaseManager getInstance() {
        if (databaseManager == null) {
            databaseManager = new DatabaseManager();
            db = FirebaseDatabase.getInstance("https://taam-1c732-default-rtdb.firebaseio.com/");
            dbRef = db.getReference();
            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference();
        }
        return databaseManager;
    }

    // public ArrayList<Item> readItems();
    public Task<Void> deleteItemInfoByLotNumber(int lotNumber) {
        return dbRef.child("Items").child(Integer.toString(lotNumber)).removeValue();
    }

    public Task<Void> deleteItemImageByLotNumber(int lotNumber) {
        StorageReference itemRef = storageRef.child(lotNumber + ".png");
        return itemRef.delete();
    }
    public void deleteItems(ArrayList<Item> items, Context context) {
        for (int i = 0; i < items.size(); i++) {
            deleteItemInfoByLotNumber(items.get(i).getLotNumber()).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Toast.makeText(context.getApplicationContext(), "An error has occurred " +
                            "when removing the object" , Toast.LENGTH_SHORT).show();
                }
            });
            deleteItemImageByLotNumber(items.get(i).getLotNumber()).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Toast.makeText(context.getApplicationContext(), "An error has occurred " +
                            "when removing the item's image" , Toast.LENGTH_SHORT).show();
                }
            });
            }
        }
    }






