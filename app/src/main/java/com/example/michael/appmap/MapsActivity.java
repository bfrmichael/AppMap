package com.example.michael.appmap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private Button btnAtualizar;
    private GoogleMap mMap;
    private TextView jsonOcorrencia;
    private JSONArray resultx;
    static final int REQUEST_IMAGE_CAPTURE = 0;
    static final int REQUEST_INCLUIR = 1;
    private Uri uriSaveImage;
    private JSONObject o;
    private HashMap<Marker, Ocorrencia> hashMapMarcadores = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        new ListOccurrenceTask().execute();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        btnAtualizar = (Button) findViewById(R.id.button_atualizar);

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

        prepararMapa();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String json = (String) jsonOcorrencia.getText();
                //visualizarOcorrencia(json);

                Intent ocorrenciaIntent = new Intent(OcorrenciaActivity.ACAO_OCORRENCIA);
                ocorrenciaIntent.addCategory(OcorrenciaActivity.CATEGORIA_OCORRENCIA);
                ocorrenciaIntent.putExtra("json_ocorrencia", json);
                startActivity(ocorrenciaIntent);
            }
        });

        //permite que o mapa seja recriado
        btnAtualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                atualizaOcorrencias();

            }
        });

    }


    public void listarUltimasOcorrencias(View v) {

        Intent ultimasIntent = new Intent(UltimasActivity.ACAO_ULTIMAS_OCORRENCIAS);
        ultimasIntent.addCategory(UltimasActivity.CATEGORIA_ULTIMAS_OCORRENCIAS);
        startActivity(ultimasIntent);

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
        //if(requisicaoDado == REQUEST_IMAGE_CAPTURE && resultadoDado == RESULT_OK) {
        switch(requisicaoDado) {
            case REQUEST_IMAGE_CAPTURE:
                Bundle extras = dado.getExtras();
                Bitmap imagemBitmap = (Bitmap) extras.get("data");
                irParaIncluirOcorrencia(imagemBitmap);
                break;
            case REQUEST_INCLUIR:
                atualizaOcorrencias();
                break;
        }
        //}
    }

    public void irParaIncluirOcorrencia (Bitmap imagemBitmap){

        //chama a activity responsável por fornecer interface para prenchimento das informações da ocorrência
        Intent intent = new Intent(IncluirOcorrenciaActivity.ACAO_INCLUIR_OCORRENCIA);
        intent.addCategory(IncluirOcorrenciaActivity.CATEGORIA_INCLUIR_OCORRENCIA);
        intent.putExtra("imagem", imagemBitmap);
        intent.putExtra("file_name", uriSaveImage);
        startActivityForResult(intent, REQUEST_INCLUIR);

    }


    private void atualizaOcorrencias(){
        mMap.clear();
        new ListOccurrenceTask().execute();

        prepararMapa();

    }

    public void prepararMapa() {

        try {

            if (resultx != null) {
                for (int i = 0; i < resultx.length(); i++) {

                    o = resultx.getJSONObject(i);

                    Ocorrencia ocorrencia = new Ocorrencia();
                    ocorrencia.setLatitude(Double.parseDouble(o.getString("latitude")));
                    ocorrencia.setLongitude(Double.parseDouble(o.getString("longitude")));
                    ocorrencia.setTitulo(o.getString("titulo"));
                    ocorrencia.setId(Integer.parseInt(o.getString("id")));
                    ocorrencia.setDescricao(o.getString("descricao"));
                    ocorrencia.setImagem(o.getString("foto"));
                    ocorrencia.setJSON(String.valueOf(o));

                    LatLng coordenadas = new LatLng(ocorrencia.getLatitude(), ocorrencia.getLongitude());

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(coordenadas);

                    Marker marcador = mMap.addMarker(markerOptions);

                    hashMapMarcadores.put(marcador, ocorrencia);

                    mMap.setInfoWindowAdapter(new MarkerInfoWindownAdapter());
                }
            } else {
                Toast.makeText(MapsActivity.this, "Não foi possível carregar o mapa.", Toast.LENGTH_LONG).show();
            }
            LatLng moveCamera = new LatLng(-16.0648249, -48.0525738);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moveCamera, 10));
        } catch (JSONException e) {
            Toast.makeText(MapsActivity.this, "Falha ao carregar os dados", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (NumberFormatException ne) {
            Toast.makeText(MapsActivity.this, "Falha ao carregar os dados", Toast.LENGTH_LONG).show();
        }

    }


    public class MarkerInfoWindownAdapter implements GoogleMap.InfoWindowAdapter {
        LayoutInflater inflater;

        public MarkerInfoWindownAdapter() {

        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            View v = getLayoutInflater().inflate(R.layout.informacoes_marcador, null);

            Ocorrencia ocorrencia = hashMapMarcadores.get(marker);

            TextView titulo = (TextView) v.findViewById(R.id.textoInfo);
            jsonOcorrencia = (TextView) v.findViewById(R.id.json_ocorrencia);
            ImageView imagemOcorrencia = (ImageView) v.findViewById(R.id.imagemInfoWindown);

            titulo.setText(ocorrencia.getTitulo());
            jsonOcorrencia.setText(ocorrencia.getJSON());

            String urlImagem = Uri.parse("http://michaelfelipe.com/app/uploads/" + ocorrencia.getImagem()).toString();

            //ImageLoader imageLoader = ImageLoader.getInstance();

            //imageLoader.displayImage(urlImagem, imagemOcorrencia);
            //id_ocorrencia.setText(ocorrencia.getId());

            return v;
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
                resultx = jsonObject.getJSONArray("content");

                return resultx;

            } catch(Exception e) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);
        }

    }

}
