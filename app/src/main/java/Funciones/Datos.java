package Funciones;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Datos {

    private JsonArrayRequest jsonArrayRequestErrores;
    private RequestQueue request;
    private Context context;

    private void Iniciar(Context ctx){
        context = ctx;
        request = Volley.newRequestQueue(context);
    }

    private void ObtenerDatos(){

        jsonArrayRequestErrores = new JsonArrayRequest(Request.Method.POST, "http://sistemas.avxslv.com/lean/tr/" + "ObtenerErrores.php", null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JsonToStringError(response);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "No se pudo conectar al servidor" + error.toString(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<>();
                parametros.put("maquina","TPR881");
                return parametros;
            }
        };
        request.add(jsonArrayRequestErrores);
    }

    private void JsonToStringError(JSONArray jsonArray){

        try {
            JSONArray json = jsonArray;
            for(int i=0; i<json.length(); i++){
                JSONObject object = new JSONObject(json.getString(i));
                Log.d("Codigo",object.getString("Codigo"));
            }
        }catch (JSONException ex) {
            ex.printStackTrace();
        }
    }



}
