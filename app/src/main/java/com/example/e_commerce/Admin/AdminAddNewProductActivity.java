package com.example.e_commerce.Admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.e_commerce.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AdminAddNewProductActivity extends AppCompatActivity
{

    private String categoryName, description, price, name, date, time;
    private Button addNewProductButton;
    private ImageView productImage;
    private EditText productName, productDescription, productPrice;
    private static final int galleryPick = 1;
    private Uri imageURI;
    private String productRandKey, downloadImageUrl;
    private StorageReference productImagesRef;
    private DatabaseReference productsRef;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);

        categoryName = getIntent().getExtras().get("category").toString();
        productImagesRef = FirebaseStorage.getInstance().getReference().child("Product Images");
        productsRef = FirebaseDatabase.getInstance().getReference().child("Products");

        addNewProductButton = (Button) findViewById(R.id.add_new_product_button);
        productName = (EditText) findViewById(R.id.product_name);
        productDescription = (EditText) findViewById(R.id.product_description);
        productPrice = (EditText) findViewById(R.id.product_price);
        productImage = (ImageView) findViewById(R.id.select_product_image);
        loading = new ProgressDialog(this);


        Toast.makeText(AdminAddNewProductActivity.this, categoryName, Toast.LENGTH_SHORT).show();

        productImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                openGallery();
            }
        });


        addNewProductButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                validateProductData();
            }
        });

    }

    private void validateProductData()
    {
        description = productDescription.getText().toString();
        name = productName.getText().toString();
        price = productPrice.getText().toString();

        if (imageURI == null) {
            Toast.makeText(this, "Product image mandatory.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please write product description.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please write product name.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(price)) {
            Toast.makeText(this, "Please write product price.", Toast.LENGTH_SHORT).show();
        } else {
            storeProductInfo();
        }
    }

    private void storeProductInfo()
    {

        loading.setTitle("Add New Product");
        loading.setMessage("Please wait, we are adding the product.");
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat currDate = new SimpleDateFormat("MMM dd, yyyy");
        date = currDate.format(cal.getTime());

        SimpleDateFormat currTime = new SimpleDateFormat("HH:mm:ss a");
        time = currTime.format(cal.getTime());

        productRandKey = date + time;

        final StorageReference filePath =
                productImagesRef.child(imageURI.getLastPathSegment() + productRandKey + ".jpg");

        final UploadTask upTask = filePath.putFile(imageURI);


        upTask.addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String message = e.toString();
                Toast.makeText(AdminAddNewProductActivity.this, "Error: " + message,
                        Toast.LENGTH_SHORT).show();
                loading.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                Toast.makeText(AdminAddNewProductActivity.this, "Image uploaded successfully.",
                        Toast.LENGTH_SHORT).show();

                Task<Uri> urlTask =
                        upTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,
                                Task<Uri>>()
                {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                    {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        downloadImageUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if (task.isSuccessful()) {
                            downloadImageUrl = task.getResult().toString();
                            Toast.makeText(AdminAddNewProductActivity.this, "Product image saved" +
                                    ".", Toast.LENGTH_SHORT).show();
                            saveProductInfo();
                        }
                    }
                });
            }
        });

    }

    private void saveProductInfo()
    {
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid", productRandKey);
        productMap.put("date", date);
        productMap.put("time", time);
        productMap.put("description", description);
        productMap.put("name", name);
        productMap.put("price", price);
        productMap.put("category", categoryName);
        productMap.put("image", downloadImageUrl);

        productsRef.child(productRandKey).updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(AdminAddNewProductActivity.this,
                            AdminCategoryActivity.class);
                    startActivity(intent);

                    loading.dismiss();
                    Toast.makeText(AdminAddNewProductActivity.this, "Product saved successfully."
                            , Toast.LENGTH_SHORT).show();
                } else {
                    loading.dismiss();
                    String message = task.getException().toString();
                    Toast.makeText(AdminAddNewProductActivity.this, "Error: " + message,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void openGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, galleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null) {
            imageURI = data.getData();
            productImage.setImageURI(imageURI);
        }
    }
}
