package com.example.michael.appmap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.jar.Manifest;

public class MarcaMapaActivity extends FragmentActivity implements OnMapReadyCallback {

    private Uri fileNameImage;
    private Bitmap imagemEnviada;
    private Double latitude;
    private Double longitude;
    private GoogleMap mMap;
    private Button botao_avancar;
    private Button botao_cancelar;
    private Button botao_localizar;
    LatLng coordenadas_marcadas;
    private  final int ENVIA_INFORMACOES = 1;
    public static final String ACAO_MARCAR_MAPA = "appmap.ACAO_MARCAR_MAPA";
    public static final String CATEGORIA_MARCAR_MAPA = "appmap.CATEGORIA_MARCAR_MAPA";
    private final int PERMISSAO_PARA_LOCALIZACAO = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marca_mapa);

        //captura imagem enviada
        Bundle imagem_enviada = getIntent().getExtras();
        imagemEnviada = (Bitmap) imagem_enviada.get("imagem");
        fileNameImage = (Uri) imagem_enviada.get("nome_imagem");


        //Vinculação dos botões
        botao_avancar = (Button) findViewById(R.id.btn_avancar);
        botao_cancelar = (Button) findViewById(R.id.btn_cancelar);
        botao_localizar = (Button) findViewById(R.id.btn_localizar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        int checa_permissao =
                ContextCompat.checkSelfPermission(MarcaMapaActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        if(PackageManager.PERMISSION_GRANTED == checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)) {

            GPSTracker gps = new GPSTracker(MarcaMapaActivity.this);
            if (gps.canGetLocation()) {

                latitude = gps.getLatitude();
                longitude = gps.getLongitude();

                LatLng coordenadas = new LatLng(latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(coordenadas));
                Toast.makeText(this, String.valueOf(latitude) + String.valueOf(longitude), Toast.LENGTH_LONG).show();
            } else {
                gps.showSettingsAlert();
            }
        }else{

            ActivityCompat.requestPermissions(MarcaMapaActivity.this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},PERMISSAO_PARA_LOCALIZACAO);
        }

     mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
         @Override
         public void onMapClick(LatLng latLng) {
             coordenadas_marcadas  = new LatLng(latLng.latitude,latLng.longitude);

             mMap.clear();
             MarkerOptions markerOptions = new MarkerOptions()
                     .position(coordenadas_marcadas);

             Marker marcador = mMap.addMarker(markerOptions);
             mMap.moveCamera(CameraUpdateFactory.newLatLng(coordenadas_marcadas));

         }
     });

        botao_avancar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (coordenadas_marcadas != null) {
                    latitude = coordenadas_marcadas.latitude;
                    longitude = coordenadas_marcadas.longitude;

                    exibirMensagemDeConfirmacao();
                } else {
                    Toast.makeText(MarcaMapaActivity.this, "Por favor, selecione o local da ocorrência.", Toast.LENGTH_LONG).show();
                }
            }
        });

        botao_localizar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                GPSTracker gps = new GPSTracker(MarcaMapaActivity.this);
                if (gps.canGetLocation()) {

                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();

                    LatLng coordenadas = new LatLng(latitude, longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(coordenadas));
                    Toast.makeText(MarcaMapaActivity.this, String.valueOf(latitude) + String.valueOf(longitude), Toast.LENGTH_LONG).show();
                } else {
                    gps.showSettingsAlert();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requisicaoDado, int resultadoDado, Intent dado) {
        switch(requisicaoDado) {
            case ENVIA_INFORMACOES:
                if(resultadoDado == Activity.RESULT_CANCELED) {

                }else if(resultadoDado == Activity.RESULT_OK){
                    Bundle dados_incluir = getIntent().getExtras();


                    String titulo = (String) dados_incluir.get("titulo");
                    String descricao = (String) dados_incluir.get("descricao");
                    Double latitude = (Double) dados_incluir.get("latitude");
                    Double longitude = (Double) dados_incluir.get("longitude");

                    Intent concluir_cadastro = new Intent();
                    concluir_cadastro.putExtra("titulo", titulo);
                    concluir_cadastro.putExtra("descricao",descricao);
                    concluir_cadastro.putExtra("latitude", latitude);
                    concluir_cadastro.putExtra("longitude", longitude);

                    setResult(Activity.RESULT_OK, concluir_cadastro);
                    finish();
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSAO_PARA_LOCALIZACAO:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    GPSTracker gps = new GPSTracker(MarcaMapaActivity.this);
                    if (gps.canGetLocation()) {

                        latitude = gps.getLatitude();
                        longitude = gps.getLongitude();

                        LatLng coordenadas = new LatLng(latitude, longitude);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordenadas));
                        Toast.makeText(this, String.valueOf(latitude) + String.valueOf(longitude), Toast.LENGTH_LONG).show();
                    } else {
                        gps.showSettingsAlert();
                    }
                }
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void exibirMensagemDeConfirmacao(){
        final AlertDialog.Builder alertaConfimacaoBuilder = new AlertDialog.Builder(this);

        alertaConfimacaoBuilder.setTitle("Marcação");
        alertaConfimacaoBuilder.setMessage("O local marcado está correto?");

        alertaConfimacaoBuilder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
             irParaIncluirOcorrencias();
            }
        });
        alertaConfimacaoBuilder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertaConfirmacao = alertaConfimacaoBuilder.create();
        alertaConfirmacao.show();
    }

    public void irParaIncluirOcorrencias(){
        Intent incluir_ocorrencia = new Intent(IncluirOcorrenciaActivity.ACAO_INCLUIR_OCORRENCIA);
        incluir_ocorrencia.addCategory(IncluirOcorrenciaActivity.CATEGORIA_INCLUIR_OCORRENCIA);
        incluir_ocorrencia.putExtra("imagem", imagemEnviada);
        incluir_ocorrencia.putExtra("nome_imagem", fileNameImage);
        incluir_ocorrencia.putExtra("latitude",latitude);
        incluir_ocorrencia.putExtra("longitude",longitude);
        startActivityForResult(incluir_ocorrencia, ENVIA_INFORMACOES);
    }
}
