package com.example.ac_api_18112025;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.List;

public class EmpresaAdapter extends ArrayAdapter<Empresa> {

    public EmpresaAdapter(Context context, List<Empresa> empresas) {
        super(context, 0, empresas);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_list, parent, false);
        }

        Empresa p = getItem(position);

        TextView txtNombre = convertView.findViewById(R.id.txtNombre);
        TextView txtTelefono = convertView.findViewById(R.id.txtTelefono);
        TextView txtDomicilio = convertView.findViewById(R.id.txtDomicilio);
        TextView txtObservaciones = convertView.findViewById(R.id.txtObservaciones);
        TextView txtContacto = convertView.findViewById(R.id.txtContacto);
        TextView txtAbreviatura = convertView.findViewById(R.id.txtAbreviatura);

        if (p != null) {
            txtNombre.setText(p.getNombre());
            txtTelefono.setText(p.getTelefono());
            txtDomicilio.setText(p.getDomicilio());
            txtObservaciones.setText(p.getObservaciones());
            txtContacto.setText(p.getContacto());
            txtAbreviatura.setText(p.getAbreviatura());

        }

        return convertView;
    }
}