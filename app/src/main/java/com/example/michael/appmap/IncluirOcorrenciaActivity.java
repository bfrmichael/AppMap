        package com.example.michael.appmap;

        import android.app.Activity;
        import android.app.AlertDialog;
        import android.app.Dialog;
        import android.app.DialogFragment;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.drawable.BitmapDrawable;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.text.Editable;
        import android.util.Base64;
        import android.view.View;
        import android.widget.ArrayAdapter;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.Spinner;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.io.ByteArrayOutputStream;
        import java.util.HashMap;
        import java.util.Map;

/**
 * Created by Michael on 25/05/2016.
 */
public class IncluirOcorrenciaActivity extends Activity {

    private ImageView imagem;
    private Bitmap bitmap;
    private Bitmap imagemEnviada;
    private String encodeImageString;
    private Uri fileNameImage;
    private EditText titulo;
    private EditText descricao;
    private EditText email;
    private Spinner categoria;
    private Double latitude = null;
    private Double longitude = null;
    private Ocorrencia o = new Ocorrencia();

    private Intent informacoes_mapa;
    private Map<String, String> ocorrencia = new HashMap<String, String>();
    public final int INFORMACOES_MAPA = 1;
    public static final String ACAO_INCLUIR_OCORRENCIA = "appmap.ACAO_INCLUIR_OCORRENCIA";
    public static final String CATEGORIA_INCLUIR_OCORRENCIA = "appmap.CATEGORIA_INCLUIR_OCORRENCIA";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incluir_ocorrencia);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.opcoes_categoria_ocorrencia, android.R.layout.simple_spinner_dropdown_item);
        categoria = (Spinner) findViewById(R.id.categoria_ocorrencia);
        categoria.setAdapter(adapter);

        //captura imagem enviada e os dados de localização
        Bundle dados = getIntent().getExtras();
        imagemEnviada = (Bitmap) dados.get("imagem");
        fileNameImage = (Uri) dados.get("nome_imagem");
        latitude = (Double) dados.get("latitude");
        longitude= (Double) dados.get("longitude");


        this.imagem = (ImageView) findViewById(R.id.imagem_ocorrencia);
        this.imagem.setImageBitmap(imagemEnviada);

        BitmapDrawable drawable = (BitmapDrawable) imagem.getDrawable();
        bitmap = drawable.getBitmap();

        this.titulo = (EditText) findViewById(R.id.titulo_ocorrencia);
        this.descricao = (EditText) findViewById(R.id.descricao_ocorrencia);
        this.email = (EditText) findViewById(R.id.email_usuario_ocorrencia);

        //converte imagem para binário
        encodeImageAsync();
    }


    public void salvarOcorrencia(View v) {

        Editable titulo  = this.titulo.getText();
        Editable descricao = this.descricao.getText();
        Editable email = this.email.getText();
        int retorno = 0;


        //pega id de item selecionado na Spinner
        long idCategoria = categoria.getSelectedItemId();

        //cria instância e seta valores de ocorrência

        o.setTitulo(titulo.toString());
        o.setDescricao(descricao.toString());
        o.setEmail(email.toString());
        o.setLatitude(latitude);
        o.setLongitude(longitude);

        if( o.getLongitude() != null && o.getLongitude() != null && o.getTitulo() != null && (!o.getTitulo().equals("")) && idCategoria != 0 ) {
            //prepara HashMap com parâmetros que serão enviados via requisição POST pelo AsyncTask
            ocorrencia.put("name", o.getTitulo());
            ocorrencia.put("description", o.getDescricao());
            ocorrencia.put("latitude", String.valueOf(o.getLatitude()));
            ocorrencia.put("longitude", String.valueOf(o.getLongitude()));
            ocorrencia.put("id_category", String.valueOf(idCategoria));
            ocorrencia.put("id_status", "1");
            ocorrencia.put("email", o.getEmail());
            ocorrencia.put("image", encodeImageString);
            ocorrencia.put("file_name", String.valueOf(fileNameImage));

            exibirMensagemDeConfirmacao();

        }if( (o.getTitulo() == null || o.getTitulo().equals("") && (o.getLatitude() != null || o.getLongitude() != null)) || idCategoria == 0)  {
            exibirMensagemDeRejeicao();
        }
    }


    public void onBackPressed(){
        Intent retorna_mapa = new Intent();
        setResult(Activity.RESULT_CANCELED);
        finish();
        super.onBackPressed();
    }


    public void exibirMensagemDeConfirmacao(){
        final AlertDialog.Builder alertaConfimacaoBuilder = new AlertDialog.Builder(this);

        alertaConfimacaoBuilder.setMessage("Deseja realmente realizar o cadastro?");

        alertaConfimacaoBuilder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new InsertOccurrenceAsync().execute();
                dialog.dismiss();
                exibirMensagemDeSucesso();
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

    public void exibirMensagemDeSucesso(){
        final AlertDialog.Builder alertaConfimacaoBuilder = new AlertDialog.Builder(this);

        alertaConfimacaoBuilder.setTitle("Sucesso");
        alertaConfimacaoBuilder.setMessage("Ocorrência incluída com sucesso.");

        alertaConfimacaoBuilder.setPositiveButton("Fechar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent concluir_cadastro  = new Intent();
                concluir_cadastro.putExtra("titulo", o.getTitulo());
                concluir_cadastro.putExtra("descricao", o.getDescricao());
                concluir_cadastro.putExtra("latitude", o.getLatitude());
                concluir_cadastro.putExtra("longitude", o.getLongitude());

                setResult(Activity.RESULT_OK,concluir_cadastro);
                finish();
            }
        });
        AlertDialog alertaConfirmacao = alertaConfimacaoBuilder.create();
        alertaConfirmacao.show();
    }

    public void exibirMensagemDeRejeicao(){
        final AlertDialog.Builder alertaRejeicaoBuilder = new AlertDialog.Builder(this);
        String titulo_invalido = "Por favor, preencha o título.";
        String categoria_invalida = "Por favor, selecione uma categoria.";

        alertaRejeicaoBuilder.setTitle("Aviso");

        if(o.getTitulo() == null || o.getTitulo().equals("")) {
            alertaRejeicaoBuilder.setMessage(titulo_invalido);
        }else if(categoria.getSelectedItemId() == 0){
            alertaRejeicaoBuilder.setMessage(categoria_invalida);
        }

        alertaRejeicaoBuilder.setPositiveButton("Fechar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertaRejeicao = alertaRejeicaoBuilder.create();
        alertaRejeicao.show();
    }

    //async para codificar imagem em formado base64
    public void encodeImageAsync() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground (Void...params){
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                byte[] imageByte = stream.toByteArray();
                encodeImageString = Base64.encodeToString(imageByte, 0);

                return "";
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
            }
        }.execute(null, null, null);
    }

    //async para enviar informações via protócolo http
    private class InsertOccurrenceAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {

                //passa url com arquivo JSON que receberá e interpretará os parâmetros passados
                String urlJson = "http://appmap.michaelfelipe.com/incluir-ocorrencia/";
                String url = Uri.parse(urlJson).toString();

                //realiza requisição HTTP
                HttpRequest.post(url).form(ocorrencia).created();

                return "Sucesso";

            } catch(Exception e) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(String result) {}
    }

}
