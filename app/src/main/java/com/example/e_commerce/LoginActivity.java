package com.example.e_commerce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.e_commerce.Admin.AdminCategoryActivity;
import com.example.e_commerce.Model.Users;
import com.example.e_commerce.Prevalent.Prevalent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity
{

    private EditText phoneNum, pass;
    private Button login;
    private ProgressDialog loading;
    private String parentDBName = "Users";
    private CheckBox rememberMe;
    private TextView adminLink, notAdminLink, forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneNum = findViewById(R.id.login_phone_number_input);
        pass = findViewById(R.id.login_password_input);
        login = findViewById(R.id.login_btn);
        loading = new ProgressDialog(this);
        rememberMe = findViewById(R.id.remember_me_checkbox);
        adminLink = findViewById(R.id.admin_panel_link);
        notAdminLink = findViewById(R.id.not_admin_panel_link);
        forgotPassword = findViewById(R.id.forget_password_link);

        Paper.init(this);

        login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                loginUser();
            }

        });

        forgotPassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent (LoginActivity.this, ResetPasswordActivity.class);
                intent.putExtra("check", "login");
                startActivity(intent);
            }
        });

        adminLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                login.setText("Login Admin");
                adminLink.setVisibility(View.INVISIBLE);
                notAdminLink.setVisibility(View.VISIBLE);
                parentDBName = "Admins";
            }
        });

        notAdminLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                login.setText("Login");
                adminLink.setVisibility(View.VISIBLE);
                notAdminLink.setVisibility(View.INVISIBLE);
                parentDBName = "Users";
            }
        });

    }

    private void loginUser()
    {
        String p = phoneNum.getText().toString();
        String pas = pass.getText().toString();

        if (TextUtils.isEmpty(p)) {
            Toast.makeText(this, "Please input your phone number.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(pas)) {
            Toast.makeText(this, "Please input your password.", Toast.LENGTH_SHORT).show();
        } else {
            loading.setTitle("Login Account");
            loading.setMessage("Please wait, we are checking credentials.");
            loading.setCanceledOnTouchOutside(false);
            loading.show();

            loginToAccount(p, pas);
        }
    }

    private void loginToAccount(final String p, final String pas)
    {
        if (rememberMe.isChecked()) {
            Paper.book().write(Prevalent.phoneKey, p);
            Paper.book().write(Prevalent.passKey, pas);
        }

        final DatabaseReference rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.child(parentDBName).child(p).exists()) {
                    Users data = dataSnapshot.child(parentDBName).child(p).getValue(Users.class);

                    if (data.getPhone().equals(p)) {
                        if (data.getPassword().equals(pas)) {
                            if (parentDBName.equals("Admins")) {
                                Toast.makeText(LoginActivity.this, "Login successful, welcome " +
                                        "admin.", Toast.LENGTH_SHORT).show();
                                loading.dismiss();

                                Intent intent = new Intent(LoginActivity.this,
                                        AdminCategoryActivity.class);
                                startActivity(intent);
                            } else if (parentDBName.equals("Users")) {
                                Toast.makeText(LoginActivity.this, "Login successful.",
                                        Toast.LENGTH_SHORT).show();
                                loading.dismiss();

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                Prevalent.currOnlineUser = data;
                                startActivity(intent);
                            }
                        } else {
                            loading.dismiss();
                            Toast.makeText(LoginActivity.this, "Password is incorrect.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                } else {
                    Toast.makeText(LoginActivity.this, "Account with " + p + " doesn't exist.",
                            Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
