package com.example.taam;

import android.content.Context;
import android.widget.Toast;

import com.example.taam.structures.Item;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatabaseManager {

    static DatabaseManager databaseManager;
    private static FirebaseDatabase db;
    private static DatabaseReference dbRef;
    private static FirebaseStorage storage;
    private static StorageReference storageRef;
    private static FirebaseAuth auth;

    public final static String[] categories, periods;
    static {
        categories = new String[]{"Clear", "Jade", "Paintings", "Calligraphy", "Rubbings", "Bronze",
                "Brass and Copper", "Gold and Silvers", "Lacquer", "Enamels"};
        periods = new String[]{"Clear", "Xia", "Shang", "Zhou", "Chuanqiu", "Zhanggou", "Qin", "Han",
                "Shangou", "Ji", "South and North", "Shui", "Tang", "Liao", "Song", "Jin",
                "Yuan", "Ming", "Qing", "Modern"};
    }

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (databaseManager == null) {
            databaseManager = new DatabaseManager();
            db = FirebaseDatabase.getInstance("https://taam-1c732-default-rtdb.firebaseio.com/");
            dbRef = db.getReference();
            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference();
            auth = FirebaseAuth.getInstance();
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
    public void deleteItems(ArrayList<Item> items, Context applicationContext) {
        List<Task <Void>> tasks = new ArrayList<Task<Void>>();

        for (int i = 0; i < items.size(); i++) {
            Task<Void> deleteItemTask = deleteItemInfoByLotNumber(items.get(i).getLotNumber());
            deleteItemTask.addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Toast.makeText(applicationContext, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            tasks.add(deleteItemTask);
            deleteItemImageByLotNumber(items.get(i).getLotNumber());
        }

        Tasks.whenAll(tasks).addOnCompleteListener(task -> {
            Toast.makeText(applicationContext, "Selected items have been removed", Toast.LENGTH_SHORT).show();
        });
    }

    public Task<AuthResult> loginQuery(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

}






