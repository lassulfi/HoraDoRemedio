package application.meusprojetos.com.horadoremedio.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import static application.meusprojetos.com.horadoremedio.data.MedContract.PATH_MEDS;
import static application.meusprojetos.com.horadoremedio.data.MedContract.*;

/**
 * Provedor de acesso ao banco de dados
 * Created by LuisDaniel on 08/09/2017.
 * {@link ContentProvider} para o banco de dados de medicamentos
 */

public class MedProvider extends ContentProvider {

    //Atributos

    //Tag para mensagens de log
    private final static String LOG_TAG = MedProvider.class.getSimpleName();

    //Objeto para acesso ao banco de dados
    private MedDbHelper medDbHelper;

    //Código para verificar se a URI de content é a mesma do banco de dados
    private static final int MEDICAMENTOS = 100;

    //Código para verificar se o código de URI é o mesmo para um elemento da tabela
    private static  final int MEDICAMENTO_ID = 101;

    //Objeto UriMatcher para confrontar a content URI com o código informado.
    // O input passado no construtor representa o código para retornar a raiz da URI.
    // É comum nesses casos utilizar NO_MATCH como input

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //inicializador estático. É inicializando sempre que um processo é chamado a partir dessa classe
    static {
        //Chamada para AddUri(). Todos os padrões de URI devem ser reconhecidos aqui.
        // Todos os padrões informados pelo UriMatcher tem um código correspondente.
        // Retornam quando encontram correspondencia
        sUriMatcher.addURI("application.meusprojetos.com.horadoremedio",
                PATH_MEDS,MEDICAMENTOS);
        sUriMatcher.addURI("application.meusprojetos.com.horadoremedio",
                PATH_MEDS + "/#",MEDICAMENTO_ID);
    }

    //Métodos
    @Override
    public boolean onCreate() {

        ////Para acessar o banco de dados a subclasse do SQLiteOpenHelper é instanciada e o contexto
        // é passado para a ativdade corrente

        medDbHelper = new MedDbHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        //Recupera DB com permissao somente para leitura
        SQLiteDatabase database = medDbHelper.getReadableDatabase();

        //Instancia do cursor
        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match){
            case MEDICAMENTOS:
                cursor = database.query(MedEntry.TABLE_NAME,projection,selection,selectionArgs,
                        null,null,sortOrder);
                break;
            case MEDICAMENTO_ID:
                selection = MedEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(MedEntry.TABLE_NAME,projection,selection,selectionArgs,
                        null,null,sortOrder);
                break;
            default:
                throw new IllegalStateException("Não é possível realizar consulta na URI"  + uri);
        }

        //Define uma notificação URI no cursor para que saibamos para que a content URI foi criada
        //Se os dados da URI mudar então sabemos que será necessário atualizar o cursor
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    /**
     * Retorna o tipo MIME de dados da content URI
     * @param uri
     * @return
     */

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match){
            case MEDICAMENTOS:
                return MedEntry.CONTENT_LIST_TYPE;
            case MEDICAMENTO_ID:
                return MedEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("URI desconhecida " + uri + " com correspondecia " + match);
        }
    }

    /**
     * Insere dados no provider com um dado ContentValues
     * @param uri
     * @param contentValues
     * @return
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case MEDICAMENTOS:
                return inserirMedicamentos(uri, contentValues);
            default:
                throw new IllegalStateException("A uri "  + uri + " não suporta inserção de dados");
        }
    }

    /**
     * Deleta os dados para uma entidade selecionada
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int delete( Uri uri, String selection, String[] selectionArgs) {

        //objeto banco de dados com permissão para alteração
        SQLiteDatabase database = medDbHelper.getWritableDatabase();

        //Uri referente ao item seleciona
        final int match = sUriMatcher.match(uri);

        switch (match){
            case MEDICAMENTOS:
                //Deleta todos os registros referente ao selection e selectionArgs
                return database.delete(MedEntry.TABLE_NAME,selection,selectionArgs);
            case MEDICAMENTO_ID:
                //Deleta um único registro dado pelo ID da URI
                selection = MedEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return database.delete(MedEntry.TABLE_NAME,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("A uri"  + uri + " não suporta exclusão de dados");
        }
    }

    /**
     * Atualiza os dados dada uma selection e uma selectionArgs com o novo ContentValues
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match){
            case MEDICAMENTOS:
                return atualizarMedicamento(uri,values,selection,selectionArgs);
            case MEDICAMENTO_ID:
                //Para o código MEDICAMENTO_ID, extrai a ID da URI, para que saibamos de qual URI
                // se trata. selection será "_id=?" e selectionArgs será um Array de String
                // contendo o atual ID
                selection = MedEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return atualizarMedicamento(uri,values,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("A uri"  + uri + " não suporta atualização de dados");
        }

    }

    /**
     * Insere no banco de dados os nodos dados a partir de uma Uri
     * @param uri
     * @param values
     * @return
     */
    private Uri inserirMedicamentos(Uri uri, ContentValues values){

        //Checando se o nome do medicamento é nulo
        String nomeMedicamento = values.getAsString(MedEntry.COLUMN_NOME_MEDICAMENTO);
        if (nomeMedicamento == null){
            throw new IllegalArgumentException("Necessário informar o nome do medicamento");
        }

        //Checa se existe uma hora para o medicamento e se o valor é maior que zero
        String horaMedicamento = values.getAsString(MedEntry.COLUMN_HORA_MEDICAMENTO);
        if (horaMedicamento == null){
            throw new IllegalArgumentException("Necessário informar um horário válido e maior que zero");
        }

        //Checa se existe uma hora para o medicamento e se o valor é maior que zero
        Integer duracaoMedicamento = values.getAsInteger(MedEntry.COLUMN_DURACAO_MEDICAMENTO);
        if (duracaoMedicamento == null || duracaoMedicamento == 0){
            throw new IllegalArgumentException("Necessário informar uma duração válida e maior que zero");
        }

        //Verifica se existe um horário para o primeiro medicamento
        String primeiroMedicamento = values.getAsString(MedEntry.COLUMN_PRIMEIRO_MEDICAMENTO);
        if (primeiroMedicamento == null){
            throw new IllegalArgumentException("Necessário informar a hora do primeiro medicamento");
        }

        //Obtendo banco de dados com permissao para escrita
        SQLiteDatabase database = medDbHelper.getWritableDatabase();

        //Insere um novo medicamento com os valores informados
        long id = database.insert(MedEntry.TABLE_NAME,null,values);

        //Exibe no log
        Log.v(LOG_TAG,"Nova linha id " + id);

        //se a id é -1, houve um erro e a inserção falhou.
        //Exibe log de erro e retorna nulo

        if (id == -1){
            Log.e(LOG_TAG,"Erro ao cadastrar linha na " + uri);
            return null;
        }

        //Notifica todos os listeners que os dados mudaram para o URI content de medicamentos
        //uri: application.meusprojetos.com.horadoremedio/medicamentos
        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri,id);
    }

    private int atualizarMedicamento(Uri uri, ContentValues values, String selection,
                                     String[] selectionArgs){
        /**
         * Se a chave {@link MedEntry.COLUMN_NOME_MEDICAMENTO} é presente, verificar se o valor não
         * é nulo
         */

        if (values.containsKey(MedEntry.COLUMN_NOME_MEDICAMENTO)){
            String nome = values.getAsString(MedEntry.COLUMN_NOME_MEDICAMENTO);
            if (nome == null){
                throw new IllegalArgumentException("Necessário informar um nome para o medicamento");
            }
        }

        /**
         * Se a chave {@link MedEntry.COLUMN_HORA_MEDICAMENTO} é presente, verificar se o valor não
         * é nulo e se não é zero
         */
        if (values.containsKey(MedEntry.COLUMN_HORA_MEDICAMENTO)){
            //Checa se existe uma hora para o medicamento e se o valor é maior que zero
            String horaMedicamento = values.getAsString(MedEntry.COLUMN_HORA_MEDICAMENTO);
            if (horaMedicamento == null){
                throw new IllegalArgumentException("Necessário informar um horário válido e " +
                        "maior que zero");
            }
        }

        /**
         * Se a chave {@link MedEntry.COLUMN_DURACAO_MEDICAMENTO} é presente, verificar se o valor não
         * é nulo e se não é zero
         */
        if (values.containsKey(MedEntry.COLUMN_DURACAO_MEDICAMENTO)){
            //Checa se existe uma hora para o medicamento e se o valor é maior que zero
            Integer duracaoMedicamento = values.getAsInteger(MedEntry.COLUMN_DURACAO_MEDICAMENTO);
            if (duracaoMedicamento == null || duracaoMedicamento == 0){
                throw new IllegalArgumentException("Necessário informar uma duração válida e maior que zero");
            }
        }

        /**
         * Se a chave {@link MedEntry.COLUMN_PRIMEIRO_MEDICAMENTO} é presente, verificar se o valor não
         * é nulo e se não é zero
         */
        if (values.containsKey(MedEntry.COLUMN_PRIMEIRO_MEDICAMENTO)){
            //Checa se existe uma hora para o primeiro medicamento
            String primeiroMedicamento = values.getAsString(MedEntry.COLUMN_PRIMEIRO_MEDICAMENTO);
            if (primeiroMedicamento == null){
                throw new IllegalArgumentException("Necessário informar a hora do primeiro medicamento");
            }
        }

        //Se não há valores para atualizar então não atualiza o banco de dados
        if (values.size() == 0){
            return 0;
        }

        //Caso contrário obtem um banco de dados com permissao escrita para acessar os dados
        SQLiteDatabase database = medDbHelper.getWritableDatabase();

        //Executa a atualização do banco de dados e recupera o número de linhas afetadas
        int linhasAtualizadas = database.update(MedEntry.TABLE_NAME,values,selection,selectionArgs);

        //Se uma ou mais linhas foram atualizadas notifica todos os listeners que os dados URI mudaram
        if (linhasAtualizadas != 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return linhasAtualizadas;
    }

}
