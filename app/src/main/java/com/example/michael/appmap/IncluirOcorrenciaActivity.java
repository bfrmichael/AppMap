package com.example.michael.appmap;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michael on 25/05/2016.
 */
public class IncluirOcorrenciaActivity extends Activity {

    private EditText titulo;
    private EditText descricao;
    private TextView exibir;
    private Spinner categoria;
    private Map<String, String> ocorrencia = new HashMap<String, String>();
    public static final String ACAO_INCLUIR_OCORRENCIA = "appmap.ACAO_INCLUIR_OCORRENCIA";
    public static final String CATEGORIA_INCLUIR_OCORRENCIA = "appmap.CATEGORIA_INCLUIR_OCORRENCIA";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incluir_ocorrencia);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.opcoes_categoria_ocorrencia, android.R.layout.simple_spinner_item);
        categoria = (Spinner) findViewById(R.id.categoria_ocorrencia);
        categoria.setAdapter(adapter);

        this.titulo = (EditText) findViewById(R.id.titulo_ocorrencia);
        this.descricao = (EditText) findViewById(R.id.descricao_ocorrencia);
        this.exibir = (TextView) findViewById(R.id.exibir_titulo);
    }

    public void salvarOcorrencia(View v) {

        Editable titulo  = this.titulo.getText();
        Editable descricao = this.descricao.getText();
        this.exibir.setText(titulo);

        Double latitude = null;
        Double longitude = null;

        //captura coordenadas do usuário assim que ele envia ocorrência
        GPSTracker gps = new GPSTracker(IncluirOcorrenciaActivity.this);
        if( gps.canGetLocation() ) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        } else {
            gps.showSettingsAlert();
        }

        //cria instância e seta valores de ocorrência
        Ocorrencia o = new Ocorrencia();
        o.setTitulo(titulo.toString());
        o.setDescricao(descricao.toString());
        o.setLatitude(latitude);
        o.setLongitude(longitude);

        //prepara HashMap com parâmetros que serão enviados via requisição POST pelo AsyncTask
        ocorrencia.put("name", o.getTitulo());
        ocorrencia.put("description", o.getDescricao());
        ocorrencia.put("latitude", String.valueOf(o.getLatitude()));
        ocorrencia.put("longitude", String.valueOf(o.getLongitude()));
        ocorrencia.put("id_category", "1");
        ocorrencia.put("id_status", "1");
        ocorrencia.put("id_user", "1");

        new InsertOccurrenceAsync().execute();
    }

    private class InsertOccurrenceAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {

                //passa url com arquivo JSON que receberá e interpretará os parâmetros passados
                String urlJson = "http://michaelfelipe.com/app/incluir-ocorrencia.php";
                String url = Uri.parse(urlJson).toString();

                //realiza requisição HTTP
                HttpRequest.post(url).form(ocorrencia).created();

                return "Sucesso";

            } catch(Exception e) {
                return null;
            }

        }
    }

}
