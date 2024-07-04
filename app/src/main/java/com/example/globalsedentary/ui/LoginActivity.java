package com.example.globalsedentary.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.globalsedentary.R;
import com.example.globalsedentary.services.RegistroDatabase;
import com.example.globalsedentary.services.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    Button btn_login, btn_register, btn_sinconexion;
    EditText email, password;
    TextView txt_sinconexion;
    RegistroDatabase registroDB;
    User user;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.correo);
        password = findViewById(R.id.contrasena);
        txt_sinconexion = findViewById(R.id.connection_status_text);
        btn_login = findViewById(R.id.btn_ingresar);
        btn_register = findViewById(R.id.btn_register);
        btn_sinconexion = findViewById(R.id.btn_sinconexion);

        registroDB = Room.databaseBuilder(this, RegistroDatabase.class, "registroDB")
                .fallbackToDestructiveMigration()
                .build();

        if (!isConnected(this)) {
            disableLoginButton();
        }

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailUser = email.getText().toString().trim();
                String passUser = password.getText().toString().trim();

                if (emailUser.isEmpty() && passUser.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Ingresar los datos", Toast.LENGTH_SHORT).show();
                } else if (isConnected(LoginActivity.this)) {
                    loginUser(emailUser, passUser);
                } else {
                    loginUserOffline(emailUser, passUser);
                }
            }
        });

        btn_sinconexion.setOnClickListener(v -> {
            String emailUser = email.getText().toString().trim();
            String passUser = password.getText().toString().trim();

            if (emailUser.isEmpty() && passUser.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Ingresar los datos", Toast.LENGTH_SHORT).show();
            } else {
                LiveData<User> userLiveData = registroDB.leerUserDAO().leerUserEmailPassword(emailUser, passUser);

                userLiveData.observe(LoginActivity.this, user -> {
                    if (user != null) {
                        updateLoginStatusInBackground(user);
                    } else {
                        Toast.makeText(LoginActivity.this, "Usuario o contraseña incorrectos o no registrado.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btn_register.setOnClickListener(v -> {
            String emailUser = email.getText().toString().trim();
            String passUser = password.getText().toString().trim();

            if (emailUser.isEmpty() && passUser.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Ingresar los datos", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(emailUser, passUser);
            }
        });
    }

    private void loginUser(String emailUser, String passUser) {
        mAuth.signInWithEmailAndPassword(emailUser, passUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    finish();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    Toast.makeText(LoginActivity.this, "Bienvenido", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Error al iniciar sesion", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser(String emailUser, String passUser) {
        if (isConnected(this)) {
            mAuth.createUserWithEmailAndPassword(emailUser, passUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Registrado exitosamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActivity.this, "Error al registrar", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            User userSinConexion = new User();
            userSinConexion.email = emailUser;
            userSinConexion.password = passUser;
            userSinConexion.isLoggedIn = false;
            registrarUserInBackground(userSinConexion);
            Toast.makeText(LoginActivity.this, "Registrado sin conexión", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private void updateLoginStatusInBackground(final User user) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            user.isLoggedIn = true;
            registroDB.leerUserDAO().update(user);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loginUserOffline(user.email, user.password);
                }
            });
        });
    }

    private void disableLoginButton() {
        btn_login.setEnabled(false);
        txt_sinconexion.setVisibility(View.VISIBLE);
        txt_sinconexion.setText("No hay conexión a internet. Intente nuevamente más tarde o ingrese sin conexión");
    }

    private void loginUserOffline(String emailUser, String passUser) {
        LiveData<User> userLiveData = registroDB.leerUserDAO().leerUserEmailPassword(emailUser, passUser);

        userLiveData.observe(LoginActivity.this, user -> {
            if (user != null && user.password.equals(passUser)) {
                updateLoginStatusInBackground(user);
                finish();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                Toast.makeText(LoginActivity.this, "Ingreso sin conexión", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "Usuario o contraseña incorrectos o no registrado.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void registrarUserInBackground(User user) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            if (registroDB.leerUserDAO().leerUserEmailPassword(user.email, user.password) == null) {
                registroDB.leerUserDAO().agregarUser(user);
            }
        });
    }
}
