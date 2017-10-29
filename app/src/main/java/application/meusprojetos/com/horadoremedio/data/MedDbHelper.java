package application.meusprojetos.com.horadoremedio.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static application.meusprojetos.com.horadoremedio.data.MedContract.*;



/**
 * Created by LuisDaniel on 08/09/2017.
 */

public class MedDbHelper extends SQLiteOpenHelper {

    //Atributos

    //Variável para armazenar a versão do banco de dados
    public static final int DATABASE_VERSION = 1;

    //Variavel para armazenar o nome do banco de dados
    public static final String DATABASE_NAME = "medicamentos.db";


    //Métodos especiais
    public MedDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Criando uma String que contém o statement do banco de dados SQL para a tabela medicamentos
        String SQL_CREATE_MEDS_TABLE = "CREATE TABLE IF NOT EXISTS " + MedEntry.TABLE_NAME + " (" +
                MedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MedEntry.COLUMN_NOME_MEDICAMENTO + " TEXT NOT NULL, " +
                MedEntry.COLUMN_HORA_MEDICAMENTO + " TEXT NOT NULL, " +
                MedEntry.COLUMN_DURACAO_MEDICAMENTO + " INTEGER NOT NULL, " +
                MedEntry.COLUMN_PRIMEIRO_MEDICAMENTO + " TEXT NOT NULL);";

        //Cria o bando de dados
        db.execSQL(SQL_CREATE_MEDS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Tabela em versão 1 - sem atualizacoes
        switch (oldVersion) {
            case 1:
                String SQL_DROP_MEDS_TABLE = "DROP TABLE IF EXISTS " + DATABASE_NAME;
                db.execSQL(SQL_DROP_MEDS_TABLE);
            case 2:
                onCreate(db);
        }
    }
}
