package com.example.taam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;
import android.graphics.Canvas;

import androidx.annotation.NonNull;

import com.example.taam.structures.Item;
import com.example.taam.structures.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class MainPresenter {
    private final MainActivity mainActivity;
    private final FirebaseAuth auth;

    public MainPresenter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.auth = FirebaseAuth.getInstance();
    }

    public void login(User user) {
        auth.signInWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mainActivity.onLoginSuccess();
                        mainActivity.switchAdminStatus(true);
                    } else {
                        mainActivity.onLoginFailure();
                    }
                });
    }

    public void generateReport(String searchBy, String searchValue, boolean descPic, ArrayList<Item> itemDataSet) {
        Toast.makeText(mainActivity, "not implemented yet", Toast.LENGTH_SHORT).show();

        ArrayList<Item> items = new ArrayList<>(itemDataSet);

        /*
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                items.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Item item = snapshot.getValue(Item.class);
                    if (item != null) {
                        items.add(item);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mainActivity, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
            }
        });
         */

        int pageHeight = 1120;
        int pageWidth = 792;

        // Check if file already exists and create a unique filename

        Toast.makeText(mainActivity, "Generating report", Toast.LENGTH_SHORT).show();
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();

        switch (searchBy) {
            case "Lot Number":
                items.removeIf(item -> item.getLotNumber() != Integer.parseInt(searchValue));
                break;
            case "Name":
                items.removeIf(item -> !item.getName().equals(searchValue));
                break;
            case "Category":
                items.removeIf(item -> !item.getCategory().equals(searchValue));
                break;
            case "Period":
                items.removeIf(item -> !item.getPeriod().equals(searchValue));
                break;
        }

        if (descPic) {
            ArrayList<Integer> indices = new ArrayList<>();
            for (final int[] i = {0}; i[0] < items.size(); i[0]++) {
                Item item = items.get(i[0]);
                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                StorageReference imageFile = storageReference.child(item.getLotNumber() + ".png");

                if (item.getDescription() == null) {
                    indices.add(i[0]);
                    continue;
                }

                imageFile.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata metadata) {
                        // File exists
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // File does not exist
                        indices.add(i[0]);
                    }
                });
            }
            for (Integer index : indices) {
                items.remove(items.get(index));
            }
        }

        if (items.isEmpty()) {
            Toast.makeText(mainActivity, "No items found", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i + 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Item item = items.get(i);
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference imageFile = storageReference.child(item.getLotNumber() + ".png");
            imageFile.getBytes(1024 * 1024).addOnSuccessListener(bytes -> {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Canvas canvas = page.getCanvas();
                canvas.drawBitmap(bmp, 10, 10, paint);
            }).addOnFailureListener(e -> {
                // Handle potential failures
            });
            Canvas canvas = page.getCanvas();
            canvas.drawText("Lot Number: " + item.getLotNumber(), 10, 25, paint);
            canvas.drawText("Name: " + item.getName(), 10, 50, paint);
            canvas.drawText("Category: " + item.getCategory(), 10, 75, paint);
            canvas.drawText("Period: " + item.getPeriod(), 10, 100, paint);
            canvas.drawText("Description: " + item.getDescription(), 10, 125, paint);
            pdfDocument.finishPage(page);
        }

        String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(pdfPath, "GFG.pdf");
        int counter = 0;
        while (file.exists()) {
            counter++;
            file = new File(pdfPath, "TAAM(" + counter + ").pdf");
        }

        try {
            // writing our PDF file to that location.
            pdfDocument.writeTo(Files.newOutputStream(file.toPath()));

            // printing toast message on completion of PDF generation.
            Toast.makeText(mainActivity, "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // handling error
            Toast.makeText(mainActivity, "Failed to generate PDF file.", Toast.LENGTH_SHORT).show();
        }

        // closing our PDF file.
        pdfDocument.close();
    }
}