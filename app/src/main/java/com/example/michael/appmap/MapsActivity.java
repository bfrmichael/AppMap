package com.example.michael.appmap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

//    private Toolbar toolbar;
    private GoogleMap mMap;
    private JSONArray result;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri uriSaveImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        new ListOccurrenceTask().execute();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupport

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu, menu);
//        return true;
//    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {

            if(result != null) {

                for (int i = 0; i < result.length(); i++) {

                    JSONObject o = result.getJSONObject(i);

                    LatLng ocorrencia = new LatLng(Double.parseDouble(o.getString("latitude")), Double.parseDouble(o.getString("longitude")));
                    mMap.addMarker(new MarkerOptions().position(ocorrencia).title(o.getString("titulo")).snippet(String.valueOf(o)));

                }

            } else {
                new ListOccurrenceTask().execute();
            }

            LatLng moveCamera = new LatLng(-16.0648249, -48.0525738);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moveCamera, 10));

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    String jsonOcorrencia = marker.getSnippet();
                    visualizarOcorrencia(jsonOcorrencia);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public void visualizarOcorrencia(String jsonOcorrencia) {

        Intent ocorrenciaIntent = new Intent(OcorrenciaActivity.ACAO_OCORRENCIA);
        ocorrenciaIntent.addCategory(OcorrenciaActivity.CATEGORIA_OCORRENCIA);
        ocorrenciaIntent.putExtra("json_ocorrencia", jsonOcorrencia);
        startActivity(ocorrenciaIntent);

    }

    public void incluirOcorrencia(View v) {

        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());

        File folder = new File(Environment.getExternalStorageDirectory(), "AppMap");
        folder.mkdirs();

        String imageUri = "app_map_" + timeStamp + ".jpg";
        File imageName = new File(folder, imageUri);
        //Uri uriSavemImagemHD = Uri.fromFile(imageName);
        uriSaveImage = Uri.parse(imageUri);

        if(camera.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(camera, REQUEST_IMAGE_CAPTURE);
        }

    }


    @Override
    protected void onActivityResult(int requisicaoDado, int resultadoDado, Intent dado) {
        if(requisicaoDado == REQUEST_IMAGE_CAPTURE && resultadoDado == RESULT_OK) {
            Bundle extras = dado.getExtras();
            Bitmap imagemBitmap = (Bitmap) extras.get("data");

            //chama a activity responsável por fornecer interface para prenchimento das informações da ocorrência
            Intent intent = new Intent(IncluirOcorrenciaActivity.ACAO_INCLUIR_OCORRENCIA);
            intent.addCategory(IncluirOcorrenciaActivity.CATEGORIA_INCLUIR_OCORRENCIA);
            intent.putExtra("imagem", imagemBitmap);
            intent.putExtra("file_name", uriSaveImage);
            startActivity(intent);
        }
    }


    private class ListOccurrenceTask extends AsyncTask<String, Void, JSONArray> {

        @Override
        protected void onPreExecute() {}

        @Override
        protected JSONArray doInBackground(String... params) {

            try{

                String urlJson = "http://michaelfelipe.com/app/listar-ocorrencias.php";

                String url = Uri.parse(urlJson).toString();

                String content = HttpRequest.get(url).body();

                JSONObject jsonObject = new JSONObject(content);

                //recupera conteúdo json com atributo "content"
                JSONArray ocorrencias = jsonObject.getJSONArray("content");

                if(ocorrencias.length() > 0) {
                    result = ocorrencias;
                } else {
                    result = null;
                }

                return result;

            } catch(Exception e) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Void... values){}

    }

}
