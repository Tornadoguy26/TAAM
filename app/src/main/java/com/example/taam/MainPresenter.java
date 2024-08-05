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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MainPresenter {
    private final MainActivity mainActivity;
    private final FirebaseAuth auth;
    private static final String TAG = "MainPresenter";

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
        ArrayList<Item> items = new ArrayList<>(itemDataSet);

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

        final Bitmap[] bitmaps = new Bitmap[items.size()];
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference imageRef = storageReference.child(item.getLotNumber() + ".png");
            final int finalI = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> getBitmap(bitmaps, finalI, imageRef));
            futures.add(future);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.thenRun(() -> {
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
                if (description != null) {
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
        });
    }

    public static void getBitmap(final Bitmap[] bitmaps, final int finalI, StorageReference imageRef) {
        imageRef.getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                bitmaps[finalI] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                bitmaps[finalI] = null;
                Log.e(TAG, "No image", e);
            }
        });
    }
}


/*
imageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata metadata) {
                    Log.d("MainPresenter", "File exists");

                    try {
                        File localFile = File.createTempFile("images", "jpg");
                        Log.d("MainPresenter", "Temp file created");
                        imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // File exists
                                Log.d("MainPresenter", "Download success");
                                // Decode the file into a Bitmap
                                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                if (bitmap == null) {
                                    Log.d("MainPresenter", "Failed to decode image");
                                }
                                // Generate the PDF with the downloaded image
                                Paint paint_img = new Paint();
                                canvas.drawBitmap(bitmap, 10, y_img, paint_img);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception exception) {
                                Log.e("MainPresenter", "Image download failed", exception);
                            }
                        });
                    } catch (IOException e) {
                        Log.e("MainPresenter", "Error creating temp file", e);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    if (exception instanceof StorageException) {
                        StorageException se = (StorageException) exception;
                        if (se.getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                            Log.e("MainPresenter", "File not found at specified path");
                            // Handle file not found case
                        } else {
                            Log.e("MainPresenter", "Failed to get metadata", exception);
                            // Handle other errors
                        }
                    }
                }
            });
 */