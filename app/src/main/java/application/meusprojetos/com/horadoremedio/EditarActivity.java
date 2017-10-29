package application.meusprojetos.com.horadoremedio;

import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.icu.util.Calendar;
import application.meusprojetos.com.horadoremedio.alarm.Alarms;
import application.meusprojetos.com.horadoremedio.data.MedContract;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * Permite criar um novo medicamento ou editar um existente
 */

public class EditarActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    //Atributos
    private Button diminuirHorasButton;
    private Button aumentarHorasButton;
    private TextView horasTextView;
    private Button diminuirDiasButtton;
    private Button aumentarDiasButton;
    private TextView diasTextView;
    private EditText nomeMedicamentoEditText;
    int horaInicial = 0;
    int diaInicial = 0;
    private Button definirButton;
    private TextView primeiroMedicamentoTextView;

    //Variável que confirma caso o medicamento sofra alteração, em caso de edição
    private boolean medicamentoAlterado = false;

    //Constante utilizada pelo Loader
    private final static int MEDICAMENTO_EXISTENTE_LOADER = 0;

    private Uri mMedicamentoAtualUri;

    //Listener para verificar se houve alguma alteração de edição
    private View.OnTouchListener mTouchListener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            medicamentoAlterado = true;
            return false;
        }
    };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);

        //Examina a intent que foi utilizada para criar a Activity (Criar novo ou editar)
        Intent intent = getIntent();

        if (intent != null) {
            mMedicamentoAtualUri = intent.getData();
            //Se não existe uma Uri significa que será criado um novo medicamento
            if (mMedicamentoAtualUri == null) {
                //Altera o título para "Adicionar medicamento
                setTitle(R.string.titulo_activity_novo_medicamento);
                invalidateOptionsMenu();

            } else {
                setTitle(R.string.titulo_activity_editar_medicamento);
                getSupportLoaderManager().initLoader(MEDICAMENTO_EXISTENTE_LOADER, null, this);
            }
        }

        //Recupera objetos da tela
        nomeMedicamentoEditText = (EditText) findViewById(R.id.nome_med_edittext);
        diminuirHorasButton = (Button) findViewById(R.id.diminuir_horas_button);
        aumentarHorasButton = (Button) findViewById(R.id.aumentar_horas_button);
        horasTextView = (TextView) findViewById(R.id.horas_textview);
        diminuirDiasButtton = (Button) findViewById(R.id.diminuir_dias_button);
        aumentarDiasButton = (Button) findViewById(R.id.aumentar_dias_button);
        diasTextView = (TextView)findViewById(R.id.dias_textview);

        //Forçando exibir o valor zero para dia e hora ao iniciar o app
        horasTextView.setText(String.valueOf(horaInicial));
        diasTextView.setText(String.valueOf(diaInicial));

        nomeMedicamentoEditText.setOnTouchListener(mTouchListener);
        diminuirHorasButton.setOnTouchListener(mTouchListener);
        aumentarHorasButton.setOnTouchListener(mTouchListener);
        diminuirDiasButtton.setOnTouchListener(mTouchListener);
        aumentarDiasButton.setOnTouchListener(mTouchListener);


        diminuirHorasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                horaInicial = diminuirValor(horaInicial);
                horasTextView.setText(String.valueOf(horaInicial));
            }
        });

        aumentarHorasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                horaInicial = aumentarValor(horaInicial);
                horasTextView.setText(String.valueOf(horaInicial));
            }
        });

        diminuirDiasButtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diaInicial = diminuirValor(diaInicial);
                diasTextView.setText(String.valueOf(diaInicial));
            }
        });

        aumentarDiasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diaInicial = aumentarDias(diaInicial);
                diasTextView.setText(String.valueOf(diaInicial));
            }
        });

        //Instaniando o botão para definir a hora inicial e definindo o evento de onClick
        definirButton = (Button) findViewById(R.id.definir_button);
        definirButton.setOnTouchListener(mTouchListener);
        primeiroMedicamentoTextView = (TextView) findViewById(R.id.primeiro_medicamento_textview);

        definirButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                obterHoraAtual();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editar,menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuItem = menu.findItem(R.id.action_excluir);
        if(mMedicamentoAtualUri == null){
            menuItem.setVisible(false);
        } else {
            menuItem.setVisible(true);
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Usuário clica em algum item do menu
        switch (item.getItemId()){
            case R.id.action_salvar:
                //Salva o medicamento no banco de dados
                salvarMedicamento();
                //Cria os alarmes
                setAlarm();
                //Finaliza a activity
                finish();
                return true;
            case R.id.action_excluir:
                //Mostra uma dialog para confirmar a exclusão
                showDeleteConfirmDialog();
                return true;
            case R.id.home:
                //Retorna para a tela anterior
                //Se o medicamento não foi alterado retorna a activity pai que é {@link MainActivity}
                if (!medicamentoAlterado){
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                //Caso contrário se há alterações não salvas, configura um dialog para aletar
                // o usuário
                //Cria um click listener para lidar com o usuário confirmando que as mudanças
                // devem ser descartadas
                DialogInterface.OnClickListener descartarButtonOnClickListener =
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Usuário clicou no botão "Discard" retorna para a Activity Pai
                        NavUtils.navigateUpFromSameTask(EditarActivity.this);
                    }
                };

                //Mostra uma tela ao usuário informado que ele possui informações não salvas
                showUnsavedChangesDialog(descartarButtonOnClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //Cria um CursorLoader que irá criar um Loader responsável pela exibição dos dados
        String[] projecao = {MedContract.MedEntry._ID,
                MedContract.MedEntry.COLUMN_NOME_MEDICAMENTO,
                MedContract.MedEntry.COLUMN_HORA_MEDICAMENTO,
                MedContract.MedEntry.COLUMN_DURACAO_MEDICAMENTO,
                MedContract.MedEntry.COLUMN_PRIMEIRO_MEDICAMENTO};

        return new CursorLoader(this,
                mMedicamentoAtualUri,
                projecao,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        //Procede movendo o primeiro registro do cursor e lendo a data dele
        //(Este deveria ser o único registro do cursor)

        if (cursor.moveToFirst()){
            //Acha as colunas dos dados que estamos interessados
            int nomeMedColumnIndex = cursor.getColumnIndex(MedContract.MedEntry.COLUMN_NOME_MEDICAMENTO);
            int horaMedColumnIndex = cursor.getColumnIndex(MedContract.MedEntry.COLUMN_HORA_MEDICAMENTO);
            int diaMedColumnIndex = cursor.getColumnIndex(MedContract.MedEntry.COLUMN_DURACAO_MEDICAMENTO);
            int primeiroMedColumnIndex = cursor.getColumnIndex(MedContract.MedEntry.COLUMN_PRIMEIRO_MEDICAMENTO);

            //Recupera os valores do cursor
            String nomeMedicamento = cursor.getString(nomeMedColumnIndex);
            String horaMedicamento = cursor.getString(horaMedColumnIndex);
            int diaMedicamento = cursor.getInt(diaMedColumnIndex);
            String primeiroMedicamento = cursor.getString(primeiroMedColumnIndex);

            //Formatacao da String horaMedicamento
            int posicaoSeparador = horaMedicamento.indexOf(":");
            horaMedicamento = horaMedicamento.substring(0,posicaoSeparador);

            //Atualizando a listview com os valores do banco de dados
            nomeMedicamentoEditText.setText(nomeMedicamento);
            horasTextView.setText(horaMedicamento);
            diasTextView.setText(Integer.toString(diaMedicamento));
            primeiroMedicamentoTextView.setText(primeiroMedicamento);
        }
    }

    /**
     * Reseta os dados já que o Loader foi resetado
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        nomeMedicamentoEditText.setText("");
        horasTextView.setText("0");
        diasTextView.setText("0");
        primeiroMedicamentoTextView.setText("00:00");
    }

    @Override
    public void onBackPressed() {
        //Se o medicamento não foi alterado continyua lidando com o botão voltar
        if(!medicamentoAlterado){
            super.onBackPressed();
        }

        //Caso existam alteracoes a serem salvas configura um dialog para alertar o usuario
        //Cria um click listener para lidar com o usuario confirmando que as mudanças devem ser
        //descartadas

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Usuáruo clicou em Discard. fecha a activity atual
                finish();
            }
        };

        showUnsavedChangesDialog(discardButtonClickListener);

    }

    //Métodos
    private int diminuirValor(int valorInicial){
        int valorFinal = valorInicial;
        if (valorInicial > 0){
            valorFinal--;
        }
        return valorFinal;
    }

    private int aumentarValor(int valorInicial){
        int valorFinal = valorInicial;
        if (valorInicial < 24){
            valorFinal++;
        }
        return valorFinal;
    }

    private int aumentarDias(int valorInicial){
            valorInicial++;
        return valorInicial;
    }

    /**
     * Salva o medicamento
     */
    private void salvarMedicamento(){

        //String que recebe o nome do medicamento
        String nomeMedicamento = nomeMedicamentoEditText.getText().toString().trim();

        //Integer que recebe a hora do medicamento
        int horaMedicamentoInteger = Integer.parseInt(horasTextView.getText().toString());
        //Conversão para formato de hora
        String horaMedicamento = "";
        if (horaMedicamentoInteger < 10){
            horaMedicamento = "0" + horaMedicamentoInteger + ":00:00";
        } else {
            horaMedicamento = horaMedicamentoInteger + ":00:00";
        }

        //Integer que recebe a duracao de dias do medicamento
        String diaMedicamentoString = diasTextView.getText().toString().trim();
        int diaMedicamento = Integer.parseInt(diaMedicamentoString);

        //String que recebe a hora do primeiro medicamento
        String primeiroMedicamento = primeiroMedicamentoTextView.getText().toString();

        //Se não existem valores, não salva e sai do método
        if (mMedicamentoAtualUri == null && TextUtils.isEmpty(nomeMedicamento) &&
                TextUtils.isEmpty(horaMedicamento) && TextUtils.isEmpty(diaMedicamentoString) &&
                TextUtils.isEmpty(primeiroMedicamento)){
            return;
        }

        //Criação de mapas de valores onde os nomes das colunas sao as chaves
        ContentValues values = new ContentValues();
        values.put(MedContract.MedEntry.COLUMN_NOME_MEDICAMENTO,nomeMedicamento);
        values.put(MedContract.MedEntry.COLUMN_HORA_MEDICAMENTO,horaMedicamento);
        values.put(MedContract.MedEntry.COLUMN_DURACAO_MEDICAMENTO,diaMedicamento);
        values.put(MedContract.MedEntry.COLUMN_PRIMEIRO_MEDICAMENTO,primeiroMedicamento);

        if(mMedicamentoAtualUri == null) {
            Uri uri = getContentResolver().insert(MedContract.MedEntry.CONTENT_URI,values);
            if (uri == null){
                //Nenhuma linha criada
                Toast.makeText(this,R.string.toast_erro_salvar_medicamento,Toast.LENGTH_SHORT).show();
            } else {
                //Sucesso ao salvar
                Toast.makeText(this,R.string.toast_sucesso_salvar_medicamento,Toast.LENGTH_SHORT).show();
            }
        } else {
            //Caso contrário este é um medicamento EXISTENTE, então atualize o medicamento com URI de
            // conteúdo: mMedicamentoAtualUri e passar no novo ContentValues. Passe em null para os
            // selection e selectionArgs porque mMedicamentoAtualUri já identificará a linha correta do
            // banco de dados que queremos modificar.
            int linhasAtualizadas = getContentResolver().update(mMedicamentoAtualUri,values,null,null);
            //Mostra uma mensagem depedendo se a atualização foi bem sucessidade
            if (linhasAtualizadas == 0){
                //Nenhuma linha atualizada ou criada
                Toast.makeText(this,R.string.toast_erro_salvar_medicamento,Toast.LENGTH_SHORT).show();
            } else {
                //Sucesso ao salvar
                Toast.makeText(this,R.string.toast_sucesso_salvar_medicamento,Toast.LENGTH_SHORT).show();
            }
        }



    }

    /**
     * Exclui um medicamento do banco de dados
     */
    private void excluirMedicamento(){
        //Somente realiza a ação se existir algum medicamento
        if (mMedicamentoAtualUri != null){
            //Chama o contentResolver para deletar o medicamento no dado URI de conteúdo
            //Passa em nulo par ao selection e selection args porque o URI de conteudo
            // do mCurrentPetUri já identifica o medicamento que queremos
            int linhasExcluidas = getContentResolver().delete(mMedicamentoAtualUri,null,null);
            //Mostra um Toast dependendo de a exclusão ocorreu com sucesso ou não
            if (linhasExcluidas == 0){
                //Se nenhum elemento foi excluido exibe um toast de erro
                Toast.makeText(this,R.string.toast_erro_exluir_medicamento,Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,R.string.toast_sucesso_excluir_medicamento,Toast.LENGTH_SHORT).show();
            }
            //Fecha a activity
            finish();
        }
    }

    /**
     * Exibe uma mensagem de confirmação para deletar uma entrada
     */
    private void showDeleteConfirmDialog(){
        //Cria um AlertDialog, mensagens e clickListeners nos botões de confirmar e cancelar
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.excluir_medicamento_mensagem);
        builder.setPositiveButton(R.string.excluir_medicamento_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                excluirMedicamento();
            }
        });
        builder.setNegativeButton(R.string.excluir_medicamento_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Usuario clicou no botão "Continuir edição"
                //Fecha o dialog e continua a edição
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener descartarButtonOnClickListener){
        //Cria um AlertDialog, mensagens e clickListeners nos botões de confirmar e descartar
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.descartar_edicao_mensagem);
        builder.setPositiveButton(R.string.descartar_edicao_positive_button,
                descartarButtonOnClickListener);
        builder.setNegativeButton(R.string.descartar_edicao_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Usuario clicou no botão "Continuir edição"
                //Fecha o dialog e continua a edição
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        //Cria e mostra o Alerta
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void obterHoraAtual(){

        TimePicker timePicker = new TimePicker(this);

        //Obtendo a hora e minuto atual
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,timePicker.getHour());
        c.set(Calendar.MINUTE,timePicker.getMinute());
        int h = c.get(Calendar.HOUR_OF_DAY);
        int m = c.get(Calendar.MINUTE);

        //Criando o objeto listener
        TimePickerDialog.OnTimeSetListener mTimeSetListener =
                new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String hourString = String.valueOf(hourOfDay);
                String minuteString;
                if (minute < 10){
                    minuteString = "0" + String.valueOf(minute);
                } else {
                    minuteString = String.valueOf(minute);
                }
                primeiroMedicamentoTextView.setText(hourString + ":" + minuteString);
            }
        };

        TimePickerDialog dialog = new TimePickerDialog(EditarActivity.this,mTimeSetListener,h,m,true);

        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setAlarm(){

        //Recuperando os textos
        int acrescimo = Integer.parseInt(horasTextView.getText().toString());
        int repeticao = Integer.parseInt(diasTextView.getText().toString());
        String horaInicial = primeiroMedicamentoTextView.getText().toString();
        int textSize = horaInicial.length();
        int posicaoSeparador = horaInicial.indexOf(":");
        String horaString = horaInicial.substring(0,posicaoSeparador);
        int hora = Integer.parseInt(horaString);
        String minutoString = horaInicial.substring(posicaoSeparador + 1,textSize);
        int minuto = Integer.parseInt(minutoString);
        String nomeMedciamento = nomeMedicamentoEditText.getText().toString();

        //Instancia do objeto Alarm

        Alarms alarms = new Alarms(EditarActivity.this);
        alarms.getNomeMedicamento(nomeMedciamento);
        alarms.setAlarm(hora,minuto,acrescimo);
    }

}
