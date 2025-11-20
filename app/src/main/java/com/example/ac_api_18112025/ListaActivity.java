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
    ArrayList<Producto> listaProducto = new ArrayList<>();
    ProductAdapter adapter;
    String apiUrl = "http://demoapi.somee.com/api/productos";
    EditText etNombre, etDescripcion, etPrecio, etCantidadStock, etUnidadMedida, etFechaVencimiento, etCategoria;
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
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);
        etCantidadStock = findViewById(R.id.etCantidadStock);
        etUnidadMedida = findViewById(R.id.etUnidadMedida);
        etFechaVencimiento = findViewById(R.id.etFechaVencimiento);
        etCategoria = findViewById(R.id.etCategoria);
        spEstado = findViewById(R.id.spEstado);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvEmptyList = findViewById(R.id.tvEmptyList);
        btnAddItem = findViewById(R.id.btnAddItem);

        adapter = new ProductAdapter(this, listaProducto);
        lvItems.setAdapter(adapter);
        lvItems.setEmptyView(tvEmptyList);

        cargarProductos();

        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarProducto();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }

    private void cargarProductos() {
        executorService.execute(() -> {
            HttpURLConnection con = null;
            List<Producto> productosDescargados = new ArrayList<>();
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

                    Producto p = new Producto();

                    p.setIdProducto(obj.optInt("idProducto", 0));
                    p.setIdEmpresa(obj.optInt("idEmpresa", 0));
                    p.setProducto1(obj.optString("producto1", "Sin nombre"));
                    p.setDescripcion(obj.optString("descripcion", "Sin descripción"));
                    p.setPrecio(obj.optDouble("precio", 0.0));
                    p.setCantidadStock(obj.optInt("cantidadStock", 0));
                    p.setUnidadMedida(obj.optString("unidadMedida", "Unidad"));
                    p.setFechaVencimiento(obj.optString("fechaVencimiento", "Sin fecha"));
                    p.setEstado(obj.optString("estado", "Disponible"));
                    p.setCategoria(obj.optString("categoria", "Sin categoría"));

                    productosDescargados.add(p);
                }

                runOnUiThread(() -> {
                    listaProducto.clear();
                    listaProducto.addAll(productosDescargados);
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

    private void agregarProducto() {
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
                nuevo.put("idEmpresa", 1);
                nuevo.put("producto1", nombre);
                nuevo.put("descripcion", etDescripcion.getText().toString().trim());
                nuevo.put("precio", obtenerDoubleDesdeCampo(etPrecio, 0.0));
                nuevo.put("cantidadStock", obtenerEnteroDesdeCampo(etCantidadStock, 0));
                nuevo.put("unidadMedida", obtenerTextoODefecto(etUnidadMedida, "unidad"));
                nuevo.put("fechaVencimiento", obtenerTextoODefecto(etFechaVencimiento, ""));
                nuevo.put("estado", spEstado.getSelectedItem().toString());
                nuevo.put("categoria", etCategoria.getText().toString().trim());

                con.getOutputStream().write(nuevo.toString().getBytes(StandardCharsets.UTF_8));

                int respuesta = con.getResponseCode();
                if(respuesta == HttpURLConnection.HTTP_OK || respuesta == HttpURLConnection.HTTP_CREATED){
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show();
                        limpiarFormulario();
                    });
                    cargarProductos();
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
        etDescripcion.setText("");
        etPrecio.setText("");
        etCantidadStock.setText("");
        etUnidadMedida.setText("");
        etFechaVencimiento.setText("");
        etCategoria.setText("");
        spEstado.setSelection(0);
    }

    private void actualizarResumenLista() {
        if (tvItemCount != null) {
            tvItemCount.setText(String.format("%d items", listaProducto.size()));
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
