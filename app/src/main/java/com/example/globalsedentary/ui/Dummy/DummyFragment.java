package com.example.globalsedentary.ui.Dummy;

import static java.text.DateFormat.getDateTimeInstance;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.globalsedentary.services.Dispositivo;
import com.example.globalsedentary.services.Registro;
import com.example.globalsedentary.services.RegistroDatabase;
import com.example.globalsedentary.databinding.FragmentDummyBinding;
import com.example.globalsedentary.utils.NetworkUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DummyFragment extends Fragment {
    private Handler handler = new Handler(Looper.getMainLooper());
    private FragmentDummyBinding binding;
    private RegistroDatabase registroDB;
    private List<Registro> registroList = new ArrayList<>(); // Initialize registroList

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DummyViewModel dummyViewModel = new ViewModelProvider(this).get(DummyViewModel.class);
        binding = FragmentDummyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.descriptionDummy;
        final Button activarBoton = binding.botonActivarDummy;
        final Button leerBoton = binding.botonLeerRegistroDummy;

        RoomDatabase.Callback myCallBack = new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
            }

            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                super.onOpen(db);
            }
        };

        registroDB = Room.databaseBuilder(requireContext(), RegistroDatabase.class, "registroDB")
                .addCallback(myCallBack).build();
        SyncCompleteListener syncCompleteListener = new SyncCompleteListener() {
            @Override
            public void onSyncSuccess(String message) {
                showToast(message);
            }

            @Override
            public void onSyncFailure(String error) {
                showToast("Error: " + error);
            }
        };

        activarBoton.setOnClickListener(v -> {
            String temperatura = -100 + (int) (Math.random() * (100 - -100 + 1)) + "C";
            String humedad = (int) (Math.random() * (100 + 1)) + "%";
            Calendar calendar = Calendar.getInstance();
            String fechaRegistro = getDateTimeInstance().format(calendar.getTime());

            if (isConnected()) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();

                if (currentUser != null) {
                    String uid = currentUser.getUid();
                    Registro registro = new Registro(fechaRegistro, temperatura, humedad, 999,"false");

                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(() -> {
                        Dispositivo existingDispositivo = registroDB.leerDispositivoDAO().leerDispositivo(uid);

                        if (existingDispositivo != null) {
                            agregarRegistroInBackground(registro, null); // Pass null for dispositivo
                        } else {
                            Dispositivo dispositivo = new Dispositivo(fechaRegistro, "Dispositivo Simulado", uid);
                            agregarRegistroInBackground(registro, dispositivo);
                        }
                    });
                }
            } else {
                Registro registro = new Registro(fechaRegistro, temperatura, humedad, 999,"false");
                Dispositivo dispositivo = new Dispositivo(fechaRegistro, "Dispositivo Simulado", "offline_user");
                agregarRegistroInBackground(registro, dispositivo);
            }
        });

        leerBoton.setOnClickListener(v -> leerRegistrosInBackground(syncCompleteListener));
        dummyViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void agregarRegistroInBackground(Registro registro, Dispositivo dispositivo) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            executorService.execute(() -> {
                registroDB.leerRegistroDAO().agregarRegistro(registro);

                if (NetworkUtil.isNetworkConnected(getContext())) {
                    syncRegistroWithFirebase(registro);
                }

                if (dispositivo != null) {
                    Dispositivo existingDispositivo = registroDB.leerDispositivoDAO().leerDispositivo(uid);
                    if (existingDispositivo == null) {
                        dispositivo.setIsUploaded("false");
                        registroDB.leerDispositivoDAO().agregarDispositivo(dispositivo);

                        // Sync dispositivo with Firebase if connected
                        if (NetworkUtil.isNetworkConnected(getContext())) {
                            syncDispositivoWithFirebase(dispositivo);
                        }
                    }
                }
            });
        }
    }
    private void syncRegistroWithFirebase(Registro registro) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference registrosRef = db.collection("registros");

        registrosRef.document(String.valueOf(registro.getId()))
                .set(registro)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        } else {
                        }
                    }
                });
    }

    private void syncDispositivoWithFirebase(Dispositivo dispositivo) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dispositivosRef = db.collection("dispositivos");

        dispositivosRef.document(String.valueOf(dispositivo.getId()))
                .set(dispositivo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Handle successful sync
                        } else {
                            // Handle sync failure
                        }
                    }
                });
    }

    public void leerRegistrosInBackground(SyncCompleteListener listener) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            registroList = registroDB.leerRegistroDAO().leerRegistros();
            if (registroList == null) {
                registroList = new ArrayList<>();
            }
            if (NetworkUtil.isNetworkConnected(getContext())) {
                syncDataWithFirebase(listener);
            }
            showToast("Registros leídos localmente");
        });
    }

    private void syncDataWithFirebase(SyncCompleteListener listener) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Sync registros
        DatabaseReference registrosRef = database.getReference("registros");
        for (Registro registro : registroList) {
            registrosRef.child(String.valueOf(registro.getDispositivo()))
                    .child(String.valueOf(registro.getId()))
                    .setValue(registro)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listener.onSyncSuccess("Registros sincronizados correctamente");
                        } else {
                            listener.onSyncFailure("Error al sincronizar registros: " + task.getException().getMessage());
                        }
                    });
        }

        // Sync dispositivos
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference dispositivosRef = database.getReference("dispositivos");
            List<Dispositivo> dispositivosList = registroDB.leerDispositivoDAO().leerDispositivos(currentUser.getUid(),"false");
            for (Dispositivo dispositivo : dispositivosList) {
                dispositivo.setIsUploaded("true");
                dispositivosRef.child(currentUser.getUid())
                        .child(String.valueOf(dispositivo.getId()))
                        .setValue(dispositivo)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                listener.onSyncSuccess("Dispositivos sincronizados correctamente");
                            } else {
                                listener.onSyncFailure("Error al sincronizar dispositivos: " + task.getException().getMessage());
                            }
                        });
            }
        }
    }

    public interface SyncCompleteListener {
        void onSyncSuccess(String message);
        void onSyncFailure(String error);
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private void showToast(String message) {
        handler.post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
    }
}
