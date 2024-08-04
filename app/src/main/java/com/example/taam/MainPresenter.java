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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainPresenter {
    private final MainActivity mainActivity;
    private final FirebaseAuth auth;
    private static ExecutorService executorService = Executors.newFixedThreadPool(4);
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

            Canvas canvas = page.getCanvas();
            if (canvas == null) {
                Log.e(TAG, "Failed to create canvas");
            }

            canvas.drawText("Lot Number: " + item.getLotNumber(), 10, 25, paint);
            canvas.drawText("Name: " + item.getName(), 10, 50, paint);
            canvas.drawText("Category: " + item.getCategory(), 10, 75, paint);
            canvas.drawText("Period: " + item.getPeriod(), 10, 100, paint);

            // Multi-line description
            String[] desc = item.getDescription().split(" ");
            StringBuilder line = new StringBuilder();
            int y = 125;
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

            final int y_img = y + 25;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            String filename = item.getLotNumber() + ".png";
            Log.d(TAG, "image: " + filename);
            StorageReference imageRef = storageReference.child(filename);

            // This code is commented out because it is not working
            // Tried to check if file exists or not, then add it to the pdf
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

            // This code is commented out because it is also not working :D
            // Tried to make a function to get the image from the storage reference
            // Thought it was a synchronization issue, waited for each image to load
            // But the image won't load at all
            // Even with trivial cases; with 1.png
            /*
            Bitmap bitmap;
            try {
                bitmap = getBitmapFromStorageReference(imageRef);
                Paint paint_img = new Paint();
                canvas.drawBitmap(bitmap, 10, y_img, paint_img);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Failed to load image", e);
            }

             */

            // I don't understand why the image always fail to load
            // This is a simple implementation I tried but also not working
            // As I wrote previously, I think it is a sync issue
            // But I don't know how to fix it
            /*
            storageReference.getBytes(1024 * 1024)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Log.d(TAG, "Successfully loaded image");
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            Paint paint_img = new Paint();
                            canvas.drawBitmap(bitmap, 10, y_img, paint_img);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e(TAG, "Failed to load image", exception);
                        }
                    });

             */
            pdfDocument.finishPage(page);
        }

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
    }

    /*
    public static Bitmap getBitmapFromStorageReference(StorageReference storageReference) throws ExecutionException, InterruptedException {
        Log.d(TAG, "Loading image from storage reference");
        Callable<Bitmap> task = new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                final Bitmap[] result = new Bitmap[1];
                final Object lock = new Object();
                Log.d(TAG, "Getting image bytes");

                storageReference.getBytes(1024 * 1024)
                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Log.d(TAG, "Successfully loaded image");
                                result[0] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                synchronized (lock) {
                                    lock.notify();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.e(TAG, "Failed to load image", exception);
                                synchronized (lock) {
                                    lock.notify();
                                }
                            }
                        });

                synchronized (lock) {
                    Log.d(TAG, "Waiting");
                    lock.wait(); // Wait for the image to be loaded
                }
                return result[0];
            }
        };

        Future<Bitmap> future = executorService.submit(task);
        Log.d(TAG, "Waiting for image to load");
        return future.get(); // This will block until the task is complete
    }

     */
}
