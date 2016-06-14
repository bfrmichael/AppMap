package com.example.michael.appmap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by Michael on 14/06/2016.
 */
public class UltimasActivity extends Activity {

    private ListView lista;
    private String[] jsonOcorrencia;
    public final static String ACAO_ULTIMAS_OCORRENCIAS = "appmap.ACAO_ULTIMAS_OCORRENCIAS";
    public final static String CATEGORIA_ULTIMAS_OCORRENCIAS = "appmap.CATEGORIA_ULTIMAS_OCORRENCIAS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        new ListOccurrenceTask().execute();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ultimas_ocorrencias);

        lista = (ListView) findViewById(R.id.lista);

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int i = (int) lista.getSelectedItemId();

                Intent ocorrenciaIntent = new Intent(OcorrenciaActivity.ACAO_OCORRENCIA);
                ocorrenciaIntent.addCategory(OcorrenciaActivity.CATEGORIA_OCORRENCIA);
                ocorrenciaIntent.putExtra("json_ocorrencia", jsonOcorrencia[position]);
                startActivity(ocorrenciaIntent);
            }
        });

    }


    private class ListOccurrenceTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String[] doInBackground(String... params) {

            try {

                String urlJson = "http://michaelfelipe.com/app/listar-ocorrencias.php";

                String url = Uri.parse(urlJson).toString();

                String content = HttpRequest.get(url).body();

                JSONObject jsonObject = new JSONObject(content);

                //recupera conteúdo json com atributo "content"
                JSONArray result = jsonObject.getJSONArray("content");

                String[] ocorrencias = new String[result.length()];
                jsonOcorrencia = new String[result.length()];

                for(int i = 0; i < result.length(); i++) {
                    JSONObject ocorrencia = result.getJSONObject(i);
                    String titulo = ocorrencia.getString("titulo");
                    String descricao = ocorrencia.getString("descricao");
                    String data = ocorrencia.getString("data");

                    ocorrencias[i] = titulo + " - " + descricao + "\n" + data;
                    jsonOcorrencia[i] = String.valueOf(ocorrencia);
                }

                return ocorrencias;

            } catch (Exception e) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(String[] result) {
            if(result != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, result);
                lista.setAdapter(adapter);
            }
        }

    }

}