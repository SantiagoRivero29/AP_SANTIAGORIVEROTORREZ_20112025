package com.example.ac_api_18112025;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.List;

public class ProductAdapter extends ArrayAdapter<Producto> {

    public ProductAdapter(Context context, List<Producto> productos) {
        super(context, 0, productos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_list, parent, false);
        }

        Producto p = getItem(position);

        TextView txtProducto = convertView.findViewById(R.id.txtProducto);
        TextView txtCategoria = convertView.findViewById(R.id.txtCategoria);
        TextView txtPrecio = convertView.findViewById(R.id.txtPrecio);
        TextView txtStockUnidad = convertView.findViewById(R.id.txtStockUnidad);
        TextView txtFechaV = convertView.findViewById(R.id.txtFechaV);
        TextView txtDescripcion = convertView.findViewById(R.id.txtDescripcion);
        TextView txtEstado = convertView.findViewById(R.id.txtEstado);
        TextView txtIds = convertView.findViewById(R.id.txtIds);

        if (p != null) {
            txtProducto.setText(p.getProducto1());
            txtCategoria.setText(p.getCategoria());
            txtPrecio.setText(String.format("Bs. %.2f", p.getPrecio()));
            txtStockUnidad.setText(String.format("Stock: %d (%s)", p.getCantidadStock(), p.getUnidadMedida()));
            txtFechaV.setText(String.format("Vence: %s", p.getFechaVencimiento()));
            txtDescripcion.setText(p.getDescripcion());
            txtEstado.setText(p.getEstado());

            boolean disponible = "Disponible".equalsIgnoreCase(p.getEstado());
            txtEstado.setBackgroundResource(disponible ? R.drawable.bg_badge : R.drawable.bg_button_white);
            int color = ContextCompat.getColor(getContext(), disponible ? R.color.white : R.color.text_primary);
            txtEstado.setTextColor(color);

            txtIds.setText(String.format("ID: %d | Empresa: %d", p.getIdProducto(), p.getIdEmpresa()));
        }

        return convertView;
    }
}