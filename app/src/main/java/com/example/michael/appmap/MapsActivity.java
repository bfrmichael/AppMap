package com.example.michael.appmap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private JSONArray result;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri uriSaveImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        new ListOccurrenceTask().execute();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


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

            for(int i = 0; i < result.length(); i++) {

                JSONObject o = result.getJSONObject(i);

                LatLng ocorrencia = new LatLng(Double.parseDouble(o.getString("latitude")), Double.parseDouble(o.getString("longitude")));
                mMap.addMarker(new MarkerOptions().position(ocorrencia).title(o.getString("titulo")));
            }

            LatLng moveCamera = new LatLng(-16.0648249, -48.0525738);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moveCamera, 10));

        } catch (JSONException e) {
            e.printStackTrace();
        }

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

//    public void getImageCamera() {
//        new AsyncTask<Void, Void, String>() {
//
//            @Override
//            protected String doInBackground(Void... params) {
//
//                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
//
//                File folder = new File(Environment.getExternalStorageDirectory(), "AppMap");
//                folder.mkdirs();
//
//                imageName = new File(folder, "QR_" + timeStamp + ".jpg");
//                Uri uriSaveImage = Uri.fromFile(imageName);
//
//                if(camera.resolveActivity(getPackageManager()) != null) {
//                    camera.putExtra(MediaStore.EXTRA_OUTPUT, uriSaveImage);
//                    startActivityForResult(camera, REQUEST_IMAGE_CAPTURE);
//                }
//
//                return "";
//            }
//
//            @Override
//            protected void onPostExecute(String result) {
//                super.onPostExecute(result);
//            }
//        }.execute(null, null, null);
//    }

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
                result = jsonObject.getJSONArray("content");

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
