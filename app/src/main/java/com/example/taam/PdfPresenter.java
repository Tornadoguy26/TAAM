package com.example.taam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import android.graphics.Canvas;

import androidx.annotation.NonNull;

import com.example.taam.structures.Item;
import com.example.taam.structures.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PdfPresenter {
    private final MainActivity mainActivity;
    private static final String TAG = "MainPresenter";

    public PdfPresenter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    public void generateReport(String searchBy, String searchValue, boolean descPic, ArrayList<Item> itemDataSet) {
        ArrayList<Item> items = new ArrayList<>(itemDataSet);

        // Check if file already exists and create a unique filename

        Toast.makeText(mainActivity, "Generating report", Toast.LENGTH_SHORT).show();

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
            List<Task<Void>> tasks = new ArrayList<>();
            ArrayList<Integer> indices = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                StorageReference imageFile = storageReference.child(item.getLotNumber() + ".png");
                Task<Void> task = removeIfNoImage(indices, i, imageFile);
                tasks.add(task);
            }
            Tasks.whenAll(tasks).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "All async functions are done.");
                    for (Integer index : indices) {
                        Log.d(TAG, "Removing item at index " + index);
                        items.remove(items.get(index));
                    }
                    writePdf(items);
                } else {
                    Log.e(TAG, "Some async functions failed.", task.getException());
                }
            });
        }

        writePdf(items);
    }

    public void writePdf(ArrayList<Item> items) {
        if (items.isEmpty()) {
            Toast.makeText(mainActivity, "No items found", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Number of items: " + items.size());

        final Bitmap[] bitmaps = new Bitmap[items.size()];
        List<Task<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference imageRef = storageReference.child(item.getLotNumber() + ".png");
            Task<Void> task = getBitmap(bitmaps, i, imageRef);
            tasks.add(task);
        }

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();

        int pageHeight = 1120;
        int pageWidth = 792;

        Tasks.whenAll(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // All tasks completed successfully
                Log.d("MainPresenter", "All downloads completed successfully.");
                Log.d(TAG, "All async functions are done.");
                for (int i = 0; i < items.size(); i++) {
                    Log.d(TAG, "Creating page " + (i + 1));
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i + 1).create();
                    PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                    Item item = items.get(i);

                    Canvas canvas = page.getCanvas();
                    if (canvas == null) {
                        Log.e(TAG, "Failed to create canvas");
                    }

                    canvas.drawText("Lot Number: " + item.getLotNumber(), 10, 25, paint);
                    canvas.drawText("Name: " + item.getName(), 10, 50, paint);
                    canvas.drawText("Category: " + item.getCategory(), 10, 75, paint);
                    canvas.drawText("Period: " + item.getPeriod(), 10, 100, paint);

                    // Multi-line description
                    int y = 125;
                    String description = item.getDescription();
                    String[] desc = item.getDescription().split(" ");
                    StringBuilder line = new StringBuilder();
                    for (String word : desc) {
                        float textWidth = paint.measureText(line + word + " ");
                        if (textWidth <= pageWidth - 10) {
                            line.append(word).append(" ");
                        } else {
                            canvas.drawText(line.toString(), 10, y, paint);
                            line = new StringBuilder(word).append(" ");
                            y += 25;
                        }
                    }
                    if (!line.toString().isEmpty()) {
                        canvas.drawText(line.toString(), 10, y, paint);
                    }

                    if (bitmaps[i] != null) {
                        canvas.drawBitmap(bitmaps[i], 10, y + 25, paint);
                    } else {
                        Log.d(TAG, "No image available");
                        canvas.drawText("No image available", 10, y + 25, paint);
                    }

                    pdfDocument.finishPage(page);
                    Log.d(TAG, "Page " + (i + 1) + " created");
                }
                Log.d(TAG, "all pages done");

                String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                File file = new File(pdfPath, "TAAM.pdf");
                int counter = 0;
                while (file.exists()) {
                    counter++;
                    file = new File(pdfPath, "TAAM(" + counter + ").pdf");
                }

                try {
                    // writing our PDF file to that location.
                    pdfDocument.writeTo(Files.newOutputStream(file.toPath()));
                    Log.d(TAG, "PDF file written");
                    // printing toast message on completion of PDF generation.
                    Toast.makeText(mainActivity, "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    // handling error
                    Toast.makeText(mainActivity, "Failed to generate PDF file.", Toast.LENGTH_SHORT).show();
                }

                // closing our PDF file.
                pdfDocument.close();
                Log.d(TAG, "PDF file closed");
            } else {
                // Handle failures
                Log.e("MainPresenter", "Some downloads failed.", task.getException());
            }
        });
    }

    public static Task<Void> getBitmap(final Bitmap[] bitmaps, final int finalI, StorageReference imageRef) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        try {
            File localFile = File.createTempFile("images", "jpg");
            Log.d("MainPresenter", "Temp file created");
            imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // File exists
                    Log.d("MainPresenter", "Download success");
                    // Decode the file into a Bitmap
                    bitmaps[finalI] = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    taskCompletionSource.setResult(null); // Mark the task as complete
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception exception) {
                    bitmaps[finalI] = null;
                    Log.e(TAG, "No image", exception);
                    taskCompletionSource.setResult(null); // Mark the task as complete
                }
            });
        } catch (IOException e) {
            bitmaps[finalI] = null;
            Log.e("MainPresenter", "Error creating temp file", e);
            taskCompletionSource.setException(e); // Mark the task as failed
        }
        return taskCompletionSource.getTask();
    }

    public static Task<Void> removeIfNoImage(ArrayList<Integer> indices, final int finalI, StorageReference imageRef) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        imageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata metadata) {
                // File exists
                taskCompletionSource.setResult(null); // Mark the task as complete
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // File does not exist
                indices.add(finalI);
                taskCompletionSource.setResult(null); // Mark the task as complete
            }
        });
        return taskCompletionSource.getTask();
    }
}