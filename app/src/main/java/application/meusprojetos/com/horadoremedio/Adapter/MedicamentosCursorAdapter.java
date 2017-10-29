package application.meusprojetos.com.horadoremedio.Adapter;

import android.content.Context;
import android.database.Cursor;
import application.meusprojetos.com.horadoremedio.R;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static application.meusprojetos.com.horadoremedio.data.MedContract.MedEntry.COLUMN_DURACAO_MEDICAMENTO;
import static application.meusprojetos.com.horadoremedio.data.MedContract.MedEntry.COLUMN_HORA_MEDICAMENTO;
import static application.meusprojetos.com.horadoremedio.data.MedContract.MedEntry.COLUMN_NOME_MEDICAMENTO;

/**
 *
 * {@link MedicamentosCursorAdapter} é um adaptador para uma ListView ou GridView que utiliza um
 * {@link Cursor} de um determinado medicamento como fonte de dados. Esse adapter cria uma lista
 * de itens para cada entidade do banco de dados de medicamento no {@link Cursor}
 *
 * Created by LuisDaniel on 09/09/2017.
 */

public class MedicamentosCursorAdapter extends CursorAdapter {

    //Atributos da classe
    private Context context;
    private Cursor cursor;


    //Métodos especiais
    public MedicamentosCursorAdapter(Context cnt, Cursor csr) {
        super(cnt, csr, 0);
        this.context = cnt;
        this.cursor = csr;
    }

    /**
     * Cria um item view em branco. Nenhum dado é passado ainda nesse instante
     * @param context
     * @param cursor
     * @param parent
     * @return
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    /**
     * Esse método liga a entidade do medicamento (na respectiva linha apontada pelo cursor)
     * para um dado item do layout.
     * @param view
     * @param context
     * @param cursor
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //Recupera os itens da tela
        TextView nomeMedicamentoTextView = (TextView) view.findViewById(R.id.nome_med_textview);
        TextView horaMedicamentoTextView = (TextView) view.findViewById(R.id.hora_med_textview);
        TextView diaMedicamentoTextView = (TextView) view.findViewById(R.id.dia_med_textview);

        //Recupera as colunas do DB a serem exibidas
        int nomeMedicamentoColumnIndex = cursor.getColumnIndex(COLUMN_NOME_MEDICAMENTO);
        int horaMedicamentoColumnIndex = cursor.getColumnIndex(COLUMN_HORA_MEDICAMENTO);
        int diaMedicamentoColumnIndex = cursor.getColumnIndex(COLUMN_DURACAO_MEDICAMENTO);

        //Recupera os dados do cursor

        String nomeMedicamento = cursor.getString(nomeMedicamentoColumnIndex);
        String horaMedicamento = cursor.getString(horaMedicamentoColumnIndex);
        String diaMedicamento = cursor.getString(diaMedicamentoColumnIndex);

        //Formatacao da String horaMedicamento
        int posicaoSeparador = horaMedicamento.indexOf(":");
        horaMedicamento = horaMedicamento.substring(0,posicaoSeparador);

        //Exibindo os dados
        nomeMedicamentoTextView.setText(nomeMedicamento);
        String horaMedicamentoExibicao = context.getString(R.string.textview_hora_antecessor)
                + " " + horaMedicamento + " " + context.getString(R.string.textview_hora_sucessor);
        horaMedicamentoTextView.setText(horaMedicamentoExibicao);
        String diaMedicamentoExibicao = context.getString(R.string.textview_dia_antecessor) + " " +
                diaMedicamento + " " + context.getString(R.string.textview_dia_sucessor);
        diaMedicamentoTextView.setText(diaMedicamentoExibicao);

    }
}
