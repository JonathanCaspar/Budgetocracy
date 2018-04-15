package projet.ift2905.budgetocracy;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.Toast;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        try {
            String monthFixed = String.valueOf(month+1);
            if(monthFixed.length() == 1){
                monthFixed = "0"+ monthFixed;
            }
            ((NewExpensesActivity) getActivity() ).updateDate( year+ "-" + monthFixed + "-" + dayOfMonth);
        } catch(Exception e){
            Toast.makeText(getActivity().getApplicationContext(), "Erreur lors de l'ajout de date.", Toast.LENGTH_LONG).show();
        }
    }
}
