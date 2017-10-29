package application.meusprojetos.com.horadoremedio.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 *
 * Contrato para criar o banco de dados
 *
 * Created by LuisDaniel on 08/09/2017.
 */

public final class MedContract {

    //Atributos
    //Constante do Content Authority - mesmo do Android Manifest
    public final static String CONTENT_AUTHORITY = "application.meusprojetos.com.horadoremedio";

    //Parse que informa uma URI tipo string e retorna uma URI
    public final static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Nome da tabela que será concatenada a URI de conteudo
    public final static String PATH_MEDS = "medicamentos";

    //Métodos especiais
    private MedContract() {}

    //Métodos abstratos
    public static abstract class MedEntry implements BaseColumns {

        //URI completa
        public static final Uri CONTENT_URI =Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MEDS);

        //Definindo as constantes do nome da tabela de dados e nome das colunas da tabela
        //Nome da tabela
        public static final String TABLE_NAME = "medicamentos";
        //Chave primária
        public static final String _ID = BaseColumns._ID;
        //Nome do medicamento
        public static final String COLUMN_NOME_MEDICAMENTO = "nome";
        //Hora para tomar o medicamento
        public static final String COLUMN_HORA_MEDICAMENTO = "hora";
        //Duração em dias apra tomar o medicamento
        public static final String COLUMN_DURACAO_MEDICAMENTO = "duracao";
        //Hora do primeiro medicamento
        public static final String COLUMN_PRIMEIRO_MEDICAMENTO = "primeiro";


        /**
         * O tipo MIME do {@link #CONTENT_URI} para uma lista de medicamentos
         */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
            CONTENT_AUTHORITY + "/" + PATH_MEDS;

        /**
         * O tipo MIME do {@link #CONTENT_URI} para um unico medicamento
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + PATH_MEDS;

    }
}
