package com.example.liraheta.utilizaciontr;

import android.Manifest;
import android.graphics.Color;
import android.graphics.PointF;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Pareto;
import com.anychart.charts.Pie;
import com.anychart.core.cartesian.series.Base;
import com.anychart.enums.Align;
import com.anychart.enums.Anchor;
import com.anychart.enums.LegendLayout;
import com.anychart.graphics.vector.StrokeLineCap;
import com.anychart.graphics.vector.StrokeLineJoin;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.levitnudi.legacytableview.LegacyTableView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Funciones.Descripcion;

public class MainActivity extends AppCompatActivity {

    private RequestQueue request;
    List<String> listErrores;
    List<Double> listCantidad;
    List<Double> listTiempo;
    List<Double> listPromedio;
    private double eficiencia;
    private String maquina = "";
    private ImageView imgBuscar;
    private Button btnBuscar;
    private TextView txtMaquina;

    private static final int MY_PERMISSION_REQUEST_CAMERA = 0;
    private AnyChartView anyChartViewPareto;
    private AnyChartView anyChartViewPie;
    private Pie pie;
    private Pareto pareto;

    int tiempoP = 0;
    int marchaP = 0;

    private boolean onePie = true;
    private boolean onePareto = true;
    private TableLayout tableLayout;
    private String[] header;
    private TableDynamic tableDynamic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final QRCodeReaderView qrCodeReaderView = findViewById(R.id.qrViewQr);
        imgBuscar = findViewById(R.id.imgBuscar);
        btnBuscar = findViewById(R.id.btnBuscar);
        txtMaquina = findViewById(R.id.txtMaquina);

        tableLayout = findViewById(R.id.table);

        anyChartViewPareto = findViewById(R.id.any_chart_view_pareto);
        anyChartViewPareto.setProgressBar(findViewById(R.id.progress_bar_pareto));

        anyChartViewPie = findViewById(R.id.any_chart_view_pie);
        anyChartViewPie.setProgressBar(findViewById(R.id.progress_bar_pie));

        request = Volley.newRequestQueue(getApplicationContext());

        qrCodeReaderView.setBackCamera();
        qrCodeReaderView.setQRDecodingEnabled(true);
        qrCodeReaderView.forceAutoFocus();
        qrCodeReaderView.stopCamera();

        requestCameraPermission();

        qrCodeReaderView.setOnQRCodeReadListener(new QRCodeReaderView.OnQRCodeReadListener() {
            @Override
            public void onQRCodeRead(String text, PointF[] points) {
                imgBuscar.setVisibility(View.VISIBLE);
                qrCodeReaderView.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

                if(text.equals("TPR881") || text.equals("TPR995")){
                    if(!maquina.equals(text)){
                        maquina = text;
                        txtMaquina.setText(maquina);
                        ObtenerDatosPastel();
                        ObtenerDatosPareto();
                    }
                }
            }
        });

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgBuscar.setVisibility(View.INVISIBLE);
                qrCodeReaderView.setVisibility(View.VISIBLE);
                qrCodeReaderView.startCamera();
            }
        });

    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.CAMERA
            }, MY_PERMISSION_REQUEST_CAMERA);
        } else {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.CAMERA
            }, MY_PERMISSION_REQUEST_CAMERA);
        }
    }

    private void ObtenerDatosPastel(){

        StringRequest jsonArrayRequestErrores = new StringRequest(Request.Method.POST, "https://sistemas.avxslv.com/lean/tr/ObtenerEficiencia.php",  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonToStringEfi(response);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "No se pudo conectar al servidor" + error.toString(), Toast.LENGTH_LONG).show();
                Log.e("ErrorVolley", error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<>();
                parametros.put("maquina",maquina);
                return parametros;
            }
        };
        request.add(jsonArrayRequestErrores);

    }

    private void JsonToStringEfi(String jsonArray) {

        try {
            JSONArray json = new JSONArray(jsonArray);
            for(int i=0; i<json.length(); i++){
                JSONObject object = new JSONObject(json.getString(i));
                tiempoP = object.getInt("Tiempo");
                marchaP = object.getInt("Marcha");
            }
            if(onePie){
                CrearPastel(tiempoP, marchaP);
                onePie = false;
            }else{
                UpdatePie();
            }

        }catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void CrearPastel(int t, int m) {

        APIlib.getInstance().setActiveAnyChartView(anyChartViewPie);

        pie = AnyChart.pie();
        pie.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x", "value"}) {
            @Override
            public void onClick(Event event) {
                Toast.makeText(getApplicationContext(), event.getData().get("x") + ":" + event.getData().get("value"), Toast.LENGTH_SHORT).show();
            }
        });

        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("Marcha", m));
        data.add(new ValueDataEntry("Detenida", t-m));

        pie.data(data);

        pie.title("Eficiencia Media");

        pie.labels().position("outside");

        pie.legend().title().enabled(true);
        pie.legend().title()
                .text("Ultimas Ordenes")
                .padding(0d, 0d, 10d, 0d);

        pie.legend()
                .position("center-bottom")
                .itemsLayout(LegendLayout.HORIZONTAL)
                .align(Align.CENTER);

        anyChartViewPie.setChart(pie);

    }

    private void UpdatePie(){

        APIlib.getInstance().setActiveAnyChartView(anyChartViewPie);
        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("Marcha", marchaP));
        data.add(new ValueDataEntry("Detenida", tiempoP-marchaP));
        pie.data(data);
    }


    private void UpdatePareto(List<DataEntry> data_pareto){
        APIlib.getInstance().setActiveAnyChartView(anyChartViewPareto);
        pareto.data(data_pareto);
    }

    private void CrearPareto(List<DataEntry> data_pareto) {

        APIlib.getInstance().setActiveAnyChartView(anyChartViewPareto);

        pareto = AnyChart.pareto();

        pareto.data(data_pareto);

        pareto.title("Grafico de errores");

        pareto.yAxis(0d).title("Tiempo (min)");

        pareto.yAxis(1d).title("Porcentaje Acumulativo");

        pareto.animation(true);

        pareto.lineMarker(0)
                .value(80d)
                .axis(pareto.yAxis(1d))
                .stroke("#A5B3B3", 1d, "5 2", StrokeLineJoin.ROUND, StrokeLineCap.ROUND);

        pareto.getSeries(0d).tooltip().format("Porcentaje: {%CF}%");

        Base line = pareto.getSeries(1d);
        line.seriesType("spline")
                .markers(true);
        line.labels().enabled(false);
        line.labels()
                .anchor(Anchor.RIGHT_BOTTOM)
                .format("{%CF}%");
        line.tooltip().format("Porcentaje: {%CF}%");

        line.tooltip(false);

        pareto.crosshair().enabled(false);
        pareto.crosshair().xLabel(false);

        pareto.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x", "value"}) {
            @Override
            public void onClick(Event event) {
                Toast.makeText(getApplicationContext(), Descripcion.BKD(event.getData().get("x"), maquina), Toast.LENGTH_SHORT).show();
            }
        });

        anyChartViewPareto.setChart(pareto);
    }

    private void ObtenerDatosPareto(){

        StringRequest jsonArrayRequestErrores = new StringRequest(Request.Method.POST, "https://sistemas.avxslv.com/lean/tr/ObtenerErrores.php",  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonToStringError(response);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "No se pudo conectar al servidor" + error.toString(), Toast.LENGTH_LONG).show();
                Log.e("ErrorVolley", error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<>();
                parametros.put("maquina",maquina);
                return parametros;
            }
        };
        request.add(jsonArrayRequestErrores);
    }

    private void JsonToStringError(String jsonArray){

        List<DataEntry> data_pareto = new ArrayList<>();

        listErrores = new ArrayList<>();
        listCantidad = new ArrayList<>();
        listTiempo = new ArrayList<>();
        listPromedio = new ArrayList<>();

        try {
            JSONArray json = new JSONArray(jsonArray);
            for(int i=0; i<json.length(); i++){
                JSONObject object = new JSONObject(json.getString(i));
                Log.d("Data",object.getString("Codigo") + " : "+ String.valueOf(object.getDouble("Cantidad")));
                data_pareto.add(new ValueDataEntry(object.getString("Codigo"), object.getInt("Tiempo")/60));
                listErrores.add(object.getString("Codigo"));
                listCantidad.add(object.getDouble("Cantidad"));
                listTiempo.add(object.getDouble("Tiempo")/60);
                listPromedio.add(object.getDouble("Tiempo") / object.getDouble("Cantidad"));
            }

            if(onePareto){
                CrearPareto(data_pareto);
                onePareto = false;
            }else{
                UpdatePareto(data_pareto);
            }

            CrearTable();
        }catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void CrearTable(){

//        LegacyTableView legacyTableView;
//        legacyTableView = findViewById(R.id.legacy_table_view);
//
//        LegacyTableView.insertLegacyTitle("Error", "Tiempo (Min)", "Cantidad", "Promedio (Seg)");
//
//        for(int i=0; i<listErrores.size(); i++){
//            LegacyTableView.insertLegacyContent(
//                    listErrores.get(i),
//                    new DecimalFormat("##.#").format(listTiempo.get(i)),
//                    new DecimalFormat("##").format(listCantidad.get(i)),
//                    new DecimalFormat("##").format(listPromedio.get(i)));
//        }
//
//
//
//        legacyTableView.setTitle(LegacyTableView.readLegacyTitle());
//        legacyTableView.setContent(LegacyTableView.readLegacyContent());
//
//        legacyTableView.setTablePadding(7);
//
//        legacyTableView.setZoomEnabled(true);
//        legacyTableView.setShowZoomControls(true);
//
//        legacyTableView.build();

        header = new String[] {"Error","Tiempo (Min)","Cantidad","Promedio (Seg)"};

        ArrayList<String[]> rows = new ArrayList<>();

        for(int i=0; i<listErrores.size(); i++){

            rows.add(new String[] {listErrores.get(i),
                    new DecimalFormat("##.#").format(listTiempo.get(i)),
                    new DecimalFormat("##").format(listCantidad.get(i)),
                    new DecimalFormat("##").format(listPromedio.get(i))});

        }

        tableDynamic = new TableDynamic(tableLayout, getApplicationContext());
        tableDynamic.Clear();
        tableDynamic.addHeader(header);
        tableDynamic.addData(rows);
        tableDynamic.backgroundHeader(R.drawable.background_header);
        tableDynamic.backgroundData(Color.rgb(255, 255, 255), Color.rgb(255, 255, 255));
        tableDynamic.lineColor(Color.rgb(79, 173, 235));
        tableDynamic.textColorData(Color.BLACK);
        tableDynamic.textColorHeader(Color.rgb(255, 255, 255));

    }


}
