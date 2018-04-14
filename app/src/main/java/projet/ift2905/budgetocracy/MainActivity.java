package projet.ift2905.budgetocracy;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import info.hoang8f.android.segmented.SegmentedGroup;


enum typeSort{
    sortByName,
    sortByDate,
    sortByAmount
}

class EnumSort{
    typeSort sort;

    public EnumSort(typeSort sort){
        this.sort=sort;
    }

    public void changeSort(typeSort newSort){
        this.sort=newSort;
    }

    public typeSort getSort(){
        return sort;
    }

}

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ListView mListView;
    TextView mEmptyView;
    Cursor cursor;
    private PieChart pieChart;
    private CustomAdapter customAdapter;
    public SharedPreferences prefs;

    // INTERFACE
    private BottomNavigationViewEx mBottomBar; // Menu de l'écran principal
    private Button showDBexpense;
    private Button showDBbudget;
    private DBHelper_Expenses DB_Expenses;
    private DBHelper_Budget DB_Budget;

    final String[] PERMISSIONS = {Manifest.permission.CAMERA,
                                  Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                  Manifest.permission.INTERNET};
    final int PERMISSION_ALL = 101;
    final int REQUEST_IMAGE_CAPTURE = 102;
    final int REQUEST_EXPENSE_DATA = 103;
    final int REQUEST_MODIFICATION_EXPENSE_DATA = 104;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        DB_Expenses = new DBHelper_Expenses(this);
        DB_Budget = new DBHelper_Budget(this);

        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(getResources().getColor(R.color.colorMainBackground));


        // Toolbar (en haut)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getCurrentDate(getApplicationContext()));

        // Barre de navigation (en bas)
        mBottomBar = findViewById(R.id.menuBar);
        mBottomBar.setActivated(true);
        mBottomBar.enableItemShiftingMode(false);
        mBottomBar.enableAnimation(false);
        mBottomBar.enableShiftingMode(false);
        mBottomBar.setTextVisibility(false);

        // Liens d'écoute sur la barre de navigation
        mBottomBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.menu_scan :
                        // Si Permission accordée:
                        if(hasPermissions(getApplicationContext(), PERMISSIONS)){
                            Intent takePhotoIntent = new Intent(MainActivity.this, CameraActivity.class);
                            // Activité Caméra lancée dans l'attente d'une réponse (image)
                            startActivityForResult(takePhotoIntent ,REQUEST_IMAGE_CAPTURE);
                            return true;
                        } else {
                            // Sinon: les demander
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
                            return false;
                        }

                    case R.id.menu_categories:
                        startActivity(new Intent(MainActivity.this, NewCategoriesActivity.class));
                        return true;

                    case R.id.menu_add_expenses:
                        startActivityForResult(new Intent(MainActivity.this, NewExpensesActivity.class), REQUEST_EXPENSE_DATA);
                        return true;

                    case R.id.menu_graphiques:
                        startActivity(new Intent(MainActivity.this, GraphicActivity.class));
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Affichage du menu latéral gauche
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /**** MAIN GRAPH ****/
        pieChartSetup();

    }

    private void pieChartSetup (){
        pieChart = (PieChart) findViewById(R.id.piechart_1);
        pieChart.setRotationEnabled(true);
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(85.f);
        pieChart.setTransparentCircleRadius(90f);
        pieChart.setDrawCenterText(true);

        // Disable the legend
        Legend l = pieChart.getLegend();
        l.setEnabled(false);

        // Text from the middle of the piechart graph
        float budget = 0;
        float remaining = 0;
        float usedBudget;
        int nbDepenses;

        cursor = DB_Expenses.getAllData();
        nbDepenses = cursor.getCount();

        cursor = DB_Budget.getAllData();
        cursor.moveToFirst();
        if(cursor.getCount()>0){
            do{
                budget += Float.parseFloat (cursor.getString(2));
                remaining += Float.parseFloat (cursor.getString(3));
            }while(cursor.moveToNext());
        }


        usedBudget = budget-remaining + 0.0f;
        String currency = prefs.getString("currency","$");

        SpannableString s = new SpannableString(getString(R.string.epargne)+"\n"+remaining+currency);
        s.setSpan(new RelativeSizeSpan(1.5f),getString(R.string.epargne).length(), s.length(), 0);
        //s.setSpan(new ForegroundColorSpan(Color.rgb(16,176,115)),s.length()-1-Float.toString(remaining).length(), s.length(), 0);
        pieChart.setCenterText(s);

        // Add the values to the piechart
        ArrayList<PieEntry> yValues = new ArrayList<>();
        yValues.add(new PieEntry(usedBudget, ""));
        yValues.add(new PieEntry(remaining, ""));
        PieDataSet dataSet = new PieDataSet(yValues,"Budget");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueTextSize(0f);

        // Piechart bar colors
        ArrayList colors = new ArrayList();
        colors.add(0xFFFFFFFF);
        colors.add(0xFF10B073);
        dataSet.setColors(colors);

        PieData data = new PieData((dataSet));
        pieChart.setData(data);

        //Legend
        TextView legend = findViewById(R.id.textView6);
        String string = getString(R.string.totalbudget) +"\n"+Float.toString(budget)+currency
                                                        +"\n"+getString(R.string.used)
                                                        +"\n"+Float.toString(usedBudget)+currency
                                                        +"\n"+getString(R.string.nbBudget)
                                                        +"\n"+cursor.getCount()
                                                        +"\n"+getString(R.string.nbExpenses)
                                                        +"\n"+nbDepenses
                                                        ;
        int counter;
        int counter2;
        SpannableString string2 = new SpannableString(string);
        counter = getString(R.string.totalbudget).length();
        string2.setSpan(new StyleSpan(Typeface.BOLD), 0,counter , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        counter2 = counter + (Float.toString(budget)+currency).length() + 1;
        string2.setSpan(new RelativeSizeSpan(1.4f),counter,counter2, 0);
        counter = counter2 + getString(R.string.used).length()+2;
        string2.setSpan(new StyleSpan(Typeface.BOLD), counter2, counter , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        counter2 = counter + (Float.toString(usedBudget)+currency).length();
        string2.setSpan(new RelativeSizeSpan(1.4f),counter,counter2, 0);
        counter = counter2 + getString(R.string.nbBudget).length()+1;
        string2.setSpan(new StyleSpan(Typeface.BOLD), counter2,counter , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        counter2 = counter + Float.toString(cursor.getCount()).length();
        string2.setSpan(new RelativeSizeSpan(1.4f),counter,counter2, 0);
        counter = string.length()-Float.toString(nbDepenses).length()+1;
        string2.setSpan(new StyleSpan(Typeface.BOLD), counter2,counter , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        string2.setSpan(new RelativeSizeSpan(1.4f),string.length()-Float.toString(nbDepenses).length()+1,string.length(), 0);

        legend.setText(string2);
    }

    // Récupère les données attendues d'une activité selon le "requestCode"
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if(resultCode == RESULT_OK) {
                    String photoBase64 = data.getStringExtra("photoBase64");
                    Intent addExpenseWithData = new Intent(MainActivity.this, NewExpensesActivity.class);
                    addExpenseWithData.putExtra("requestDataToAPI", true);
                    addExpenseWithData.putExtra("photoBase64", photoBase64);
                    startActivity(addExpenseWithData);
                }
                break;

            case REQUEST_EXPENSE_DATA:
                if (resultCode == RESULT_OK && data != null) {
                    Toast.makeText(getApplicationContext(), R.string.successful_expense_add, Toast.LENGTH_SHORT).show();
                    String[] dataToAdd = data.getStringArrayExtra("dataToSave");

                    Integer budgetID = Integer.valueOf(dataToAdd[1]);
                    DB_Budget.updateRemainingAmount(budgetID, Float.valueOf(dataToAdd[2]));
                    DB_Expenses.insertDataName(dataToAdd[0], budgetID, Float.valueOf(dataToAdd[2]), dataToAdd[3]);
                }
                break;


            case REQUEST_MODIFICATION_EXPENSE_DATA:
                if (resultCode == RESULT_OK && data !=null){
                    Toast.makeText(getApplicationContext(), R.string.successful_expense_modification, Toast.LENGTH_SHORT).show();
                    String[] dataToModify = data.getStringArrayExtra("dataToModify");
                    String idExpense = data.getStringExtra("IdExpense");

                    Integer budgetID = Integer.valueOf(dataToModify[1]);
                    DB_Expenses.updateData(idExpense,dataToModify[0],budgetID,Float.valueOf(dataToModify[2]), dataToModify[3]);

                }

        }
    }

    /*****************
     *  PERMISSIONS  *
     *****************/
    // Gère la réponse d'un utilisateur à une requête de permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && isAllGranted(grantResults)) {
                    // Permission accordée : accès à l'appareil photo
                    startActivity(new Intent(MainActivity.this, CameraActivity.class));
                } else {
                    // Permission refusée: impossible de prendre de photo
                    Toast.makeText(getApplicationContext(), "Permission refusée: impossible d'accéder à l'appareil photo.", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    // Vérification si toutes les permissions demandées ont été acceptés
    public boolean isAllGranted(int[] grantResults) {
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    //Vérification si les permissions actuelles sont suffisantes
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    // Affiche la date courante dans le Toolbar
    public static String getCurrentDate(Context ctx){
        DateFormat dateFormat = new SimpleDateFormat("d/MM/yyyy");
        Date date = new Date();
        String[] dateStr = dateFormat.format(date).split("/");

        return dateStr[0] + getMonthFromNb(dateStr[1], ctx) + dateStr[2];
    }

    public static String getMonthFromNb(String nb, Context ctx){
        switch(nb) {
            case "01":
                return " " + ctx.getString(R.string.janvier) + " ";
            case "02":
                return " " + ctx.getString(R.string.fevrier) + " ";
            case "03":
                return " " + ctx.getString(R.string.mars) + " ";
            case "04":
                return " " + ctx.getString(R.string.avril) + " ";
            case "05":
                return " " + ctx.getString(R.string.mai) + " ";
            case "06":
                return " " + ctx.getString(R.string.juin) + " ";
            case "07":
                return " " + ctx.getString(R.string.juillet) + " ";
            case "08":
                return " " + ctx.getString(R.string.aout) + " ";
            case "09":
                return " " + ctx.getString(R.string.septembre) + " ";
            case "10":
                return " " + ctx.getString(R.string.octobre) + " ";
            case "11":
                return " " + ctx.getString(R.string.novembre) + " ";
            case "12":
                return " " + ctx.getString(R.string.decembre) + " ";
            default:
                return " Cinglinglin ";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView mSearchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        // Retire la barre en dessous de la recherche
        int searchPlateId = mSearchView.getContext().getResources()
                .getIdentifier("android:id/search_plate", null, null);
        View searchPlateView = mSearchView.findViewById(searchPlateId);

        SegmentedGroup mSegGroup = findViewById(R.id.segGroupResearch);
        mSegGroup.setTintColor(getResources().getColor(R.color.colorPrimary));

        mSearchView.setQueryHint("Recherche");
        if (searchPlateView != null) {
            searchPlateView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        mSearchView.setQueryHint(getResources().getString(R.string.research));
        mEmptyView =  findViewById(R.id.txtName);
        mListView = findViewById(R.id.lstExpenses);


        //cursor = DB_Expenses.getAllData();
        customAdapter = new CustomAdapter(this,cursor);

        mListView.setAdapter((ListAdapter) customAdapter);
        mListView.setEmptyView(mEmptyView);
        mListView.setVisibility(View.GONE);

        final RadioButton mButtonDate =  findViewById(R.id.buttonSortDate);
        final RadioButton mButtonAmount = findViewById(R.id.buttonSortAmount);
        final RadioButton mButtonName = findViewById(R.id.buttonSortName);
        final String currentSortSetting;

        mButtonDate.setVisibility(View.GONE);
        mButtonAmount.setVisibility(View.GONE);
        mButtonName.setVisibility(View.GONE);

        final EnumSort mSort = new EnumSort(typeSort.sortByName);
        mButtonName.setChecked(true);

        // On entre dans la recherche : Gestion recherche + Trie selon le bouton choisi
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                cursor = DB_Expenses.getExpenseListByKeyword(query,mSort);

                if (cursor==null){
                    Toast.makeText(MainActivity.this,"Pas de dépense trouvée!",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(MainActivity.this, cursor.getCount() + " dépenses trouvée.s!",Toast.LENGTH_LONG).show();
                }
                customAdapter.changeCursor(cursor);

                mButtonAmount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSort.changeSort(typeSort.sortByAmount);
                        cursor= DB_Expenses.getExpenseListByKeyword(query,mSort);
                        customAdapter.changeCursor(cursor);
                    }
                });

                mButtonDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSort.changeSort(typeSort.sortByDate);
                        cursor= DB_Expenses.getExpenseListByKeyword(query,mSort);
                        customAdapter.changeCursor(cursor);
                    }
                });

                mButtonName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSort.changeSort(typeSort.sortByName);
                        cursor= DB_Expenses.getExpenseListByKeyword(query,mSort);
                        customAdapter.changeCursor(cursor);
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                mListView.setVisibility(View.VISIBLE);
                mListView.setDivider(null);
                mListView.setDividerHeight(2);
                mButtonDate.setVisibility(View.VISIBLE);
                mButtonAmount.setVisibility(View.VISIBLE);
                mButtonName.setVisibility(View.VISIBLE);

                mBottomBar.setVisibility(View.GONE);
                showDBbudget.setVisibility(View.GONE);
                showDBexpense.setVisibility(View.GONE);

                cursor = DB_Expenses.getExpenseListByKeyword(newText,mSort);

                customAdapter.changeCursor(cursor);
                if (cursor !=null){
                    customAdapter.changeCursor(cursor);
                }

                mButtonAmount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSort.changeSort(typeSort.sortByAmount);
                        cursor = DB_Expenses.getExpenseListByKeyword(newText,mSort);
                        customAdapter.changeCursor(cursor);
                    }
                });

                mButtonDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSort.changeSort(typeSort.sortByDate);
                        cursor= DB_Expenses.getExpenseListByKeyword(newText,mSort);
                        customAdapter.changeCursor(cursor);
                    }
                });

                mButtonName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSort.changeSort(typeSort.sortByName);
                        cursor= DB_Expenses.getExpenseListByKeyword(newText,mSort);
                        customAdapter.changeCursor(cursor);
                    }
                });
                return false;
            }
        });
        // Quand on quitte la recherche on fait disparaitre le ListView
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mListView.setVisibility(View.GONE);
                mButtonDate.setVisibility(View.GONE);
                mButtonAmount.setVisibility(View.GONE);
                mButtonName.setVisibility(View.GONE);

                // Réactivation ecran principal
                mBottomBar.setVisibility(View.VISIBLE);
                return false;
            }
        });

        // Quand on clique sur une des dépenses de la liste, on redirige vers la page pour la modifier
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String strId = String.valueOf(id);
                String[] list = {getResources().getString(R.string.modify), getResources().getString(R.string.delete)};

                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setCancelable(true);
                builder.setItems(list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            case 0: // Modification
                                // On appelle l'activité pour modifier la dépense (qui est en soit l'activité de création de dépense, auxquelle on fourni un extra particulier)
                                Intent modifyExpense = new Intent(MainActivity.this, NewExpensesActivity.class);
                                modifyExpense.putExtra("requestModifyData", true);
                                modifyExpense.putExtra("idExpenseToModify",strId);
                                startActivityForResult(modifyExpense,REQUEST_MODIFICATION_EXPENSE_DATA);

                                // Une fois qu'on a modifié la dépense, on ferme la recherche précédente pour l'actualiser
                                mSearchView.setQuery("", false);
                                mSearchView.setIconified(true);
                                break;

                            case 1: // Suppression
                                DB_Expenses.deleteData(strId);
                                Snackbar.make(findViewById(R.id.myCoordinatorLayout),R.string.successful_expense_deleted, Snackbar.LENGTH_SHORT).show();
                                cursor = DB_Expenses.getExpenseListByKeyword("",mSort);
                                customAdapter.changeCursor(cursor);
                                break;

                            default:
                                break;
                        }
                    }
                }) ;
                builder.show();
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.action_settings:
                final String[] listCurrencies = { "$ - " + getString(R.string.CAD),
                        "€ - " + getString(R.string.EUR),
                        "$ - " + getString(R.string.USD),
                        "$ - " + getString(R.string.AUD),
                        "£ - " + getString(R.string.GBP),
                        "Fr. - "+getString(R.string.CHF),
                        "¥ - " + getString(R.string.JPY),
                        "R - " + getString(R.string.ZAR),
                };

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true);
                builder.setTitle(R.string.pick_currency);
                builder.setItems(listCurrencies, null);

                int currentChoice = prefs.getInt("currencyID", 0);

                builder.setSingleChoiceItems(listCurrencies, currentChoice, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String chosenCurrency = listCurrencies[which].split("-")[0].trim();
                        SharedPreferences.Editor editor = prefs.edit();

                        editor.putString("currency", chosenCurrency);
                        editor.putInt("currencyID", which); // Modification du paramètre "devise"
                        editor.apply();
                        Toast.makeText(getApplicationContext(), "Devise choisie : "+ chosenCurrency, Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(R.string.cancel,null);
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch(id){
            case R.id.nav_erase_data:
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true);
                builder.setTitle(R.string.erase_all_data);
                builder.setMessage(R.string.erase_all_data_message);
                builder.setNegativeButton(R.string.cancel, null);
                builder.setPositiveButton(R.string.erase_all, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DB_Expenses.deleteDataBase();
                        DB_Budget.deleteDataBase();
                        Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.successful_erased_data, Snackbar.LENGTH_LONG).show();
                    }
                });
                builder.show();
                break;

            case R.id.nav_communicate:
                Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Pas implémenté car pas nécéssaire", Snackbar.LENGTH_LONG).show();
                break;

            case R.id.nav_donation:
                Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Pas implémenté, nous ne sommes pas des escrocs", Snackbar.LENGTH_LONG).show();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
