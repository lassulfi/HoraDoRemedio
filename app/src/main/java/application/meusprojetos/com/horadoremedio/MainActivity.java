package application.meusprojetos.com.horadoremedio;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import application.meusprojetos.com.horadoremedio.Adapter.MedicamentosCursorAdapter;
import static application.meusprojetos.com.horadoremedio.data.MedContract.MedEntry.*;

import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    //Atributos

    //Constante usada pelo Loader
    private static final int MEDICAMENTO_LOADER = 0;

    //Adapter para a ListView
     MedicamentosCursorAdapter adapter;

    //Métodos especiais
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Configura o FAB para abrir a EditarActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,EditarActivity.class);
                startActivity(intent);
            }
        });

        //Configuração da listview
        //Encontra a listview
        ListView listView = (ListView) findViewById(R.id.medicamentos_list_view);

        //Carrega a Empty ListView
        View emptyView = findViewById(R.id.empty_view_layout);
        listView.setEmptyView(emptyView);

        adapter = new MedicamentosCursorAdapter(this,null);
        listView.setAdapter(adapter);

        //Evento de OnClick na ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Cria a intent para ir para a {@link EditarActivity}
                Intent intent = new Intent(MainActivity.this,EditarActivity.class);

                //Indexando a Uri com o indice da lista em que o usuario clica
                Uri medicamentoAtualUri = ContentUris.withAppendedId(CONTENT_URI,id);

                //Define a URI no campo de dados da intent
                intent.setData(medicamentoAtualUri);

                //Inicia a intent
                startActivity(intent);
            }
        });

        getSupportLoaderManager().initLoader(MEDICAMENTO_LOADER,null,this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Cria o menu
     * @param menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Usuario clica em algum item do menu
        switch (item.getItemId()){
            case R.id.action_inserir_dados_teste:
                //Insere dados de teste na tabela
                inserirDadosTeste();
                return true;
            case R.id.action_deletar_dados:
                //Deleta todos dos dados da tabela
                showDeletAllConfirmDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //Cria e retorna um CursorLoader que será responsável por exibir os dados que são criados
        String[] projecao = {_ID,
        COLUMN_NOME_MEDICAMENTO,
        COLUMN_HORA_MEDICAMENTO,
        COLUMN_DURACAO_MEDICAMENTO};

        return new CursorLoader(this,
                CONTENT_URI,
                projecao,
                null,
                null,
                null);
    }

    /**
     * Método chamando quando um loader anterior é finalizado
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Troca o cursor. O framework se encarrega de remover o cursor antigo
        adapter.swapCursor(data);
    }

    /**
     * Método chamando quando o cursor anterior é resetado.
     * Torna os dados indisponiveis
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Esse loader é chamando quando o ultimo cursor gerado no método onLoadFinished() está
        // prestes a ser fechado. É necessário garantir que não há nenhum dado no adapter
        adapter.swapCursor(null);
    }

    //Métodos

    /**
     * Deleta todos os dados do banco de dados
     */
    private void deletarDados(){
        int linhasExcluidas = getContentResolver().delete(CONTENT_URI,null,null);
        Log.v("MainActivity",linhasExcluidas + " linhas excluidas do banco de dados");

        //Atualizando a ListView
        adapter.swapCursor(null);
    }

    /**
     * Insere dados de teste na tabela
     */
    private void  inserirDadosTeste(){

        //Cria um mapa de valores onde os nomes das colunas são as chaves
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOME_MEDICAMENTO,"Medicanzol");
        values.put(COLUMN_HORA_MEDICAMENTO,"06:00:00");
        values.put(COLUMN_DURACAO_MEDICAMENTO,7);
        values.put(COLUMN_PRIMEIRO_MEDICAMENTO,"08:00:00");

        //Insere um novo registro para o Medicamento no provider usando ContentResolver
        //Use {@link PetEntry#CONTENT_URI} para indicar que queremos inserir
        // na tabela de banco de dados de medicamentos.
        //Recebe o novo URI de conteúdo que irá nos habilitar acessar todos os dados do Medicamento
        // no futuro

        Uri novaUri = getContentResolver().insert(CONTENT_URI,values);

    }

    //Exibe uma mensagem informado ao usario que todos os dados serão deletados
    private void showDeletAllConfirmDialog() {
        //Cria um AlertDialog, mensagens e clickListeners nos botões de confirmar e cancelar
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.excluir_todos_medicamentos);
        builder.setNegativeButton(R.string.excluir_tudo_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Fecha o dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        builder.setPositiveButton(R.string.excluir_tudo_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletarDados();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
