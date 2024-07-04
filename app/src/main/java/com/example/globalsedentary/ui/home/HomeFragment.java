package com.example.globalsedentary.ui.home;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.globalsedentary.R;
import com.example.globalsedentary.activities.adapter;
import com.example.globalsedentary.databinding.FragmentHomeBinding;
import com.example.globalsedentary.services.Dispositivo;
import com.example.globalsedentary.services.DispositivoDAO;
import com.example.globalsedentary.services.Registro;
import com.example.globalsedentary.services.RegistroDatabase;
import com.example.globalsedentary.utils.NetworkUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {
    private Handler handler = new Handler(Looper.getMainLooper());
    private RecyclerView recyclerView;
    private Spinner spinner;
    RegistroDatabase registroDB;
    private ArrayAdapter adapterDispositivos;
    private adapter adapterRegistros;
    private List<Dispositivo> dispositivosList;
    private List<Registro> registrosList;

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        spinner = root.findViewById(R.id.spinner);
        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

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

        dispositivosList = new ArrayList<>();
        registrosList = new ArrayList<>();

        leerRegistrosInBackground();

        adapterDispositivos = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, dispositivosList);
        adapterDispositivos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterRegistros = new adapter(registrosList);
        recyclerView.setAdapter(adapterRegistros);
        spinner.setAdapter(adapterDispositivos);

        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public void leerRegistrosInBackground() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            Handler handler = new Handler(Looper.getMainLooper());
            executorService.execute(() -> {
                downloadRegistrosFromFirebase(uid);
                downloadDispositivosFromFirebase(uid);

                List<Registro> newRegistros = registroDB.leerRegistroDAO().leerRegistroBool("false");
                List<Dispositivo> newDispositivos = registroDB.leerDispositivoDAO().leerDispositivos(uid,"false");

                List<Registro> syncRegistros = registroDB.leerRegistroDAO().leerRegistroBool("true");
                List<Dispositivo> syncDispositivos = registroDB.leerDispositivoDAO().leerDispositivos(uid,"true");

                if (NetworkUtil.isNetworkConnected(getContext())) {
                    syncDataWithFirebase(newDispositivos,newRegistros);
                }

                handler.post(() -> {
                    dispositivosList.clear();
                    dispositivosList.addAll(syncDispositivos);
                    registrosList.clear();
                    registrosList.addAll(syncRegistros);
                    adapterRegistros.notifyDataSetChanged();
                    adapterDispositivos.notifyDataSetChanged();
                });
            });
        }

    }
    public void downloadRegistrosFromFirebase(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference registrosRef = db.collection("registros");

        Query query = registrosRef.whereEqualTo("isUploaded", true);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Registro> downloadedRegistros = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Registro registro = document.toObject(Registro.class);
                        downloadedRegistros.add(registro);

                        registroDB.leerRegistroDAO().agregarRegistro(registro);
                    }
                    registrosList.addAll(downloadedRegistros);
                    adapterRegistros.notifyDataSetChanged();
                }
            }
        });
    }
    public void downloadDispositivosFromFirebase(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dispositivosRef = db.collection("dispositivos");

        Query query = dispositivosRef.whereEqualTo("isUploaded", true);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Dispositivo> downloadedDispositivos = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Dispositivo dispositivo = document.toObject(Dispositivo.class);
                        downloadedDispositivos.add(dispositivo);

                        registroDB.leerDispositivoDAO().agregarDispositivo(dispositivo);
                    }
                    dispositivosList.addAll(downloadedDispositivos);

                    // Notify adapter about data change
                    adapterRegistros.notifyDataSetChanged();
                } else {
                    // Handle errors
                }
            }
        });
    }

    private void syncDataWithFirebase(List<Dispositivo> dispositivos, List<Registro> registros) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference registrosRef = db.collection("registros");

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        for (Registro registro : registros) {
            registrosRef.document(String.valueOf(registro.getId()))
                    .set(registro)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                executorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        registro.setIsUploaded("true");
                                        registroDB.leerRegistroDAO().actualizarRegistro(registro);
                                    }
                                });
                            }
                        }
                    });
        }
        CollectionReference dispositivosRef = db.collection("dispositivos");
        for (Dispositivo dispositivo : dispositivos) {
            dispositivosRef.document(String.valueOf(dispositivo.getId()))
                    .set(dispositivo)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                executorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        dispositivo.setIsUploaded("true");
                                        registroDB.leerDispositivoDAO().actualizarDispositivo(dispositivo);
                                    }
                                });
                            } else {
                                // Handle error
                            }
                        }
                    });
        }
    }
}
