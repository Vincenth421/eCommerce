package com.example.e_commerce.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.e_commerce.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class AdminMaintainProductsActivity extends AppCompatActivity
{

    private Button applyChangesBtn, deleteBtn;
    private EditText name, price, description;
    private ImageView imageView;
    private String productID;
    private DatabaseReference productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_maintain_products);

        productID = getIntent().getStringExtra("pid");
        productsRef = FirebaseDatabase.getInstance().getReference().child("Products").child(productID);

        applyChangesBtn = findViewById(R.id.apply_changes_btn);
        deleteBtn = findViewById(R.id.delete_product_btn);
        name = findViewById(R.id.product_edit_name);
        price = findViewById(R.id.product_edit_price);
        description = findViewById(R.id.product_edit_description);
        imageView = findViewById(R.id.product_edit_image);

        displayProductInfo();

        applyChangesBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                applyChanges();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                deleteProduct();
            }
        });
    }

    private void deleteProduct()
    {
        productsRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                Intent intent = new Intent(AdminMaintainProductsActivity.this,
                        AdminCategoryActivity.class);
                startActivity(intent);
                finish();

                Toast.makeText(AdminMaintainProductsActivity.this, "The product has been deleted.", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void applyChanges()
    {
        String pName = name.getText().toString();
        String pPrice = price.getText().toString();
        String pDescription = description.getText().toString();

        if(pName.equals(""))
        {
            Toast.makeText(this, "Input product name.", Toast.LENGTH_SHORT).show();
        } else if(pPrice.equals("")) {
            Toast.makeText(this, "Input product price.", Toast.LENGTH_SHORT).show();
        } else if(pDescription.equals("")) {
            Toast.makeText(this, "Input product description.", Toast.LENGTH_SHORT).show();
        }

        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid", productID);
        productMap.put("description", pDescription);
        productMap.put("name", pName);
        productMap.put("price", pPrice);

        productsRef.updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(AdminMaintainProductsActivity.this, "Changes have been saved.", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(AdminMaintainProductsActivity.this,
                            AdminCategoryActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void displayProductInfo()
    {
        productsRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String pname = dataSnapshot.child("name").getValue().toString();
                    String pprice = dataSnapshot.child("price").getValue().toString();
                    String pdescription = dataSnapshot.child("description").getValue().toString();
                    String pimage = dataSnapshot.child("image").getValue().toString();

                    name.setText(pname);
                    price.setText(pprice);
                    description.setText(pdescription);
                    Picasso.get().load(pimage).into(imageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
