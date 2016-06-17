package com.example.michael.appmap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONObject;

/**
 * Created by Michael on 03/06/2016.
 */
public class OcorrenciaActivity extends Activity {

    private TextView titulo;
    private TextView descricao;
    private TextView categoria;
    private TextView data;
    private TextView latitude;
    private TextView longitude;
    private ImageView imagem;
    public static final String ACAO_OCORRENCIA = "appmap.ACAO_OCORRENCIA";
    public static final String CATEGORIA_OCORRENCIA = "appmap.CATEGORIA_OCORRENCIA";

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocorrencia);

        //Confira biblioteca Universal Image Loader, para exibir imagens no aplicativo
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        this.titulo = (TextView) findViewById(R.id.titulo_activity_ocorrencia);
        this.descricao = (TextView) findViewById(R.id.descricao_activity_ocorrencia);
        this.categoria = (TextView) findViewById(R.id.categoria_activity_ocorrencia);
        this.data = (TextView) findViewById(R.id.data_activity_ocorrencia);
        this.latitude = (TextView) findViewById(R.id.latitude_activity_ocorrencia);
        this.longitude = (TextView) findViewById(R.id.longitude_activity_ocorrencia);
        this.imagem = (ImageView) findViewById(R.id.imagem_activity_ocorrencia);

        exibirDetalhesOcorrencia();

    }

    public void compartilharOcorrencia(View v) {

        Double latUsuario = null;
        Double lonUsuario = null;

        //pega localização atual do usuário
        GPSTracker gps = new GPSTracker(OcorrenciaActivity.this);
        if( gps.canGetLocation() ) {
            latUsuario = gps.getLatitude();
            lonUsuario = gps.getLongitude();
        } else {
            gps.showSettingsAlert();
        }

        //pega localidade da ocorrencia selecionada
        String latOcorrencia = (String) this.latitude.getText();
        String lonOcorrencia = (String) this.longitude.getText();

        String url = Uri.parse("https://www.google.com.br/maps?saddr=" + latUsuario + "," + lonUsuario + "&daddr=" + latOcorrencia + "," + lonOcorrencia).toString();
        //String url = Uri.parse("geo:" + latitude + "," + longitude).toString();

        Intent compartilhar = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(compartilhar);

    }

    public void exibirDetalhesOcorrencia() {

        try {

            JSONObject o = new JSONObject(getIntent().getStringExtra("json_ocorrencia"));

            String urlImagem = Uri.parse("http://michaelfelipe.com/app/uploads/" + o.getString("foto")).toString();

            //carrega imagem passada via Url
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(urlImagem, imagem);

            //converte formato da data
//            String data = "2016-06-16 09:23:43";
//            SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");
//            String dataBR = formatoData.format(data);

            this.titulo.setText(o.getString("titulo"));
            this.descricao.setText(o.getString("descricao"));
            this.categoria.setText(o.getString("categoria"));
            this.data.setText(o.getString("data"));
            this.latitude.setText(o.getString("latitude"));
            this.longitude.setText(o.getString("longitude"));

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

}
