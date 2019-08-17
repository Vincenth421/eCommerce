package com.example.e_commerce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity
{

    private Button createAccount;
    private EditText name, number, password;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        createAccount = (Button) findViewById(R.id.register_btn);
        name = (EditText) findViewById(R.id.user_name_input);
        number = (EditText) findViewById(R.id.register_phone_number_input);
        password = (EditText) findViewById(R.id.register_password_input);
        loading = new ProgressDialog(this);


        createAccount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                createAccount();
            }
        });
    }

    private void createAccount()
    {
        String n = name.getText().toString();
        String p = number.getText().toString();
        String pass = password.getText().toString();

        if (TextUtils.isEmpty(n)) {
            Toast.makeText(this, "Please input your name.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(p)) {
            Toast.makeText(this, "Please input your phone number.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Please input your password.", Toast.LENGTH_SHORT).show();
        } else {
            loading.setTitle("Create Account");
            loading.setMessage("Please wait, we are checking credentials.");
            loading.setCanceledOnTouchOutside(false);
            loading.show();

            validatePhone(n, p, pass);
        }
    }

    private void validatePhone(final String n, final String p, final String pass)
    {
        final DatabaseReference rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (!(dataSnapshot.child("Users").child(p).exists())) {
                    HashMap<String, Object> userMap = new HashMap<>();
                    userMap.put("phone", p);
                    userMap.put("password", pass);
                    userMap.put("name", n);

                    rootRef.child("Users").child(p).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Your account has been " +
                                        "created.", Toast.LENGTH_SHORT).show();
                                loading.dismiss();

                                Intent intent = new Intent(RegisterActivity.this,
                                        LoginActivity.class);
                                startActivity(intent);

                            } else {
                                Toast.makeText(RegisterActivity.this, "Error. Please try again.",
                                        Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                            }
                        }
                    });

                } else {
                    Toast.makeText(RegisterActivity.this, "Phone number already exists",
                            Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                    Toast.makeText(RegisterActivity.this, "Try again with another phone number",
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
