package ru.ecoshming.ecoshaming;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ObjectStreamField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText emailEdit;
    private EditText passwordEdit;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText nameEdit;
    private EditText lastNameEdit;
    private EditText regEmailEdit;
    private EditText regPasswordEdit;
    private EditText regReenterPasswordEdit;
    private Boolean isRegistr = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        this.emailEdit = (EditText)findViewById(R.id.editTextTextEmailAddress);
        this.passwordEdit = (EditText)findViewById(R.id.editTextTextPassword);

        findViewById(R.id.signinButton).setOnClickListener(this);
        findViewById(R.id.regButton).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                updateUI(user);
            }
        };
    }

    @Override
    public void onBackPressed(){
        if (isRegistr){
            setContentView(R.layout.activity_signin);
            findViewById(R.id.signinButton).setOnClickListener(this);
            findViewById(R.id.regButton).setOnClickListener(this);
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
            this.isRegistr = false;
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(getApplicationContext(),
                    "Вы в аккаунте!", Toast.LENGTH_SHORT);
        } else {
            Toast.makeText(getApplicationContext(), "Пожалуйста авторизуйтесь!", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void onStop(){
        super.onStop();
        if(mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
                task -> {
            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

            if(!task.isSuccessful()){
                Toast.makeText(SignInActivity.this,
                        "Не удалось зарегистрировать аккаунт", Toast.LENGTH_SHORT).show();
            }
    });
}

    private boolean validateForm() {
        return (!TextUtils.isEmpty(emailEdit.getText().toString())) &&
                (!TextUtils.isEmpty(passwordEdit.getText().toString()));
    }

    private void signIn(String email, String password)
    {
        Log.d(TAG, "signinAccount:" + email);
        if (!validateForm()){
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this,
                task -> {
                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                    if(!task.isSuccessful()) {
                        Log.w(TAG, "signInWithEmail:failed", task.getException());
                        Toast.makeText(SignInActivity.this,
                                "Неправильный пароль или e-mail", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i)
        {
            case (R.id.regButton): {
                setContentView(R.layout.activity_register);
                this.nameEdit = (EditText) findViewById(R.id.editPersonName);
                this.lastNameEdit = (EditText) findViewById(R.id.editPersonLastName);
                this.regEmailEdit = (EditText) findViewById(R.id.editRegEmailAddress);
                this.regPasswordEdit = (EditText) findViewById(R.id.editRegPassword);
                this.regReenterPasswordEdit =
                        (EditText) findViewById(R.id.editRegReenterPassword);
                findViewById(R.id.regRegButton).setOnClickListener(this);
                this.isRegistr = true;
                break;
            }
            case (R.id.signinButton): {
                signIn(this.emailEdit.getText().toString(),
                        this.passwordEdit.getText().toString());
                if (mAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(this, IndexActivity.class);
                    startActivity(intent);
                }
            }
            break;
            case (R.id.regRegButton): {
                if(this.nameEdit.getText().toString().matches("") ||
                    this.lastNameEdit.getText().toString().matches("") ||
                    this.regEmailEdit.getText().toString().matches("") ||
                    this.regPasswordEdit.getText().toString().matches("")) {
                    Toast.makeText(SignInActivity.this,
                            "Пожалуйста, заполните все поля!",
                                Toast.LENGTH_SHORT).show();
                } else {
                    if (this.regReenterPasswordEdit.getText().toString().equals(
                            this.regPasswordEdit.getText().toString())
                    ) {
                        createAccount(this.regEmailEdit.getText().toString(),
                                this.regPasswordEdit.getText().toString());
                        if (mAuth.getCurrentUser() != null){
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            Map<String, Object> user = new HashMap<>();
                            user.put("name", this.nameEdit.getText().toString());
                            user.put("lastname", this.lastNameEdit.getText().toString());
                            user.put("email", this.regEmailEdit.getText().toString());
                            db.collection("users")
                                    .add(user)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error adding document", e);
                                        }
                                    });
                            Intent intent = new Intent(SignInActivity.this, IndexActivity.class);
                            startActivity(intent);
                        } else {
                            break;
                        }
                    } else {
                        Toast.makeText(SignInActivity.this, "Пароли не совпадают!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + i);
        }
    }
}