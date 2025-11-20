package com.example.ac_api_18112025;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListaActivity extends Activity {
    ListView lvItems;
    ArrayList<Empresa> listaEmpresa = new ArrayList<>();
    EmpresaAdapter adapter;
    String apiUrl = "http://apijobs.somee.com/api/Empresas";
    EditText etNombre, etTelefono, etDomicilio, etObservaciones, etContacto, etAbreviatura;
    Spinner spEstado;
    TextView tvItemCount, tvEmptyList;
    Button btnAddItem;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        lvItems = findViewById(R.id.lvItems);
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etDomicilio = findViewById(R.id.etDomicilio);
        etObservaciones = findViewById(R.id.etObservaciones);
        etContacto = findViewById(R.id.etContacto);
        etAbreviatura = findViewById(R.id.etAbreviatura);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvEmptyList = findViewById(R.id.tvEmptyList);
        btnAddItem = findViewById(R.id.btnAddItem);

        adapter = new EmpresaAdapter(this, listaEmpresa);
        lvItems.setAdapter(adapter);
        lvItems.setEmptyView(tvEmptyList);

        cargarEmpresas();

        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarEmpresa();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }

    private void cargarEmpresas() {
        executorService.execute(() -> {
            HttpURLConnection con = null;
            List<Empresa> empresasDescargadas = new ArrayList<>();
            try{
                URL url = new URL(apiUrl);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                StringBuilder respuesta = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String linea;
                    while((linea = reader.readLine()) != null) {
                        respuesta.append(linea);
                    }
                }

                JSONArray array = new JSONArray(respuesta.toString());
                for (int i = 0; i < array.length(); i++){
                    JSONObject obj = array.getJSONObject(i);

                    Empresa p = new Empresa();

                    p.setIdEmpresa(obj.optInt("IdEmpresa", 0));
                    p.setNombre(obj.optString("Nombre", "Sin nombre"));
                    p.setTelefono(obj.optString("Telefonos", ""));
                    p.setDomicilio(obj.optString("Domicilio", ""));
                    p.setContacto(obj.optString("Contacto", ""));
                    p.setAbreviatura(obj.optString("Abreviatura", ""));
                    p.setObservaciones(obj.optString("Observaciones", ""));


                    empresasDescargadas.add(p);
                }

                runOnUiThread(() -> {
                    listaEmpresa.clear();
                    listaEmpresa.addAll(empresasDescargadas);
                    adapter.notifyDataSetChanged();
                    actualizarResumenLista();
                });
            } catch (Exception e){
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al cargar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        });
    }

    private void agregarEmpresa() {
        final String nombre = etNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            HttpURLConnection con = null;
            try{
                URL url = new URL(apiUrl);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                con.setDoOutput(true);

                JSONObject nuevo = new JSONObject();
                nuevo.put("IdEmpresa", 1);
                nuevo.put("Nombre", nombre);
                nuevo.put("Telefonos", etTelefono);
                nuevo.put("Domicilio", etDomicilio);
                nuevo.put("Contacto", etContacto);
                nuevo.put("Abreviatura", etAbreviatura);
                nuevo.put("Observaciones", etObservaciones);

                con.getOutputStream().write(nuevo.toString().getBytes(StandardCharsets.UTF_8));

                int respuesta = con.getResponseCode();
                if(respuesta == HttpURLConnection.HTTP_OK || respuesta == HttpURLConnection.HTTP_CREATED){
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show();
                        limpiarFormulario();
                    });
                    cargarEmpresas();
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error al agregar: " + respuesta, Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e){
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        });
    }

    private void limpiarFormulario() {
        etNombre.setText("");
        etTelefono.setText("");
        etAbreviatura.setText("");
        etContacto.setText("");
        etObservaciones.setText("");
        etDomicilio.setText("");
    }

    private void actualizarResumenLista() {
        if (tvItemCount != null) {
            tvItemCount.setText(String.format("%d items", ListaEmpresa.size()));
        }
    }

    private double obtenerDoubleDesdeCampo(EditText campo, double valorPorDefecto) {
        try {
            String texto = campo.getText().toString().trim();
            if (texto.isEmpty()) return valorPorDefecto;
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            return valorPorDefecto;
        }
    }

    private int obtenerEnteroDesdeCampo(EditText campo, int valorPorDefecto) {
        try {
            String texto = campo.getText().toString().trim();
            if (texto.isEmpty()) return valorPorDefecto;
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return valorPorDefecto;
        }
    }

    private String obtenerTextoODefecto(EditText campo, String valorPorDefecto) {
        String texto = campo.getText().toString().trim();
        return texto.isEmpty() ? valorPorDefecto : texto;
    }
}
