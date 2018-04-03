package projet.ift2905.budgetocracy;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


enum typeSort{
    sortByName,
    sortByDate,
    sortByAmount;
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
    //ADD==============
    ListView mListView;
    TextView mEmptyView;

    Cursor cursor;
    private CustomAdapter customAdapter;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DB_Expenses = new DBHelper_Expenses(this);
        DB_Budget = new DBHelper_Budget(this);

        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(getResources().getColor(R.color.colorMainBackground));

        showDBexpense = findViewById(R.id.displayDB_expense);
        showDBexpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage("Base de données - Dépenses :", DB_Expenses.getAllStringData());
            }
        });
        showDBbudget = findViewById(R.id.displayDB_budget);
        showDBbudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage("Base de données - Budget :", DB_Budget.getAllStringData());
            }
        });

        // Toolbar (en haut)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getCurrentMonth());

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
        }
    }

    /*****************
     *  PERMISSIONS  *
     *****************/
    // Gère la réponse d'un utilisateur à une requête de permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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

    /**
     *  Partie pas importante : gère l'affichage du Drawer (menu latéral)
            **/

    // Affiche le mois courant dans le Toolbar
    public String getCurrentMonth(){
        DateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();
        String mois = dateFormat.format(date);

        switch(mois) {
            case "01":
                return "Janvier";
            case "02":
                return "Février";
            case "03":
                return "Mars";
            case "04":
                return "Avril";
            case "05":
                return "Mai";
            case "06":
                return "Juin";
            case "07":
                return "Juillet";
            case "08":
                return "Août";
            case "09":
                return "Septembre";
            case "10":
                return "Octobre";
            case "11":
                return "Novembre";
            case "12":
                return "Décembre";
            default :
                return "Cinglinglin";
        }
    }

    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView mSearchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        mSearchView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        mSearchView.setQueryHint("Recherche");
        mEmptyView =  findViewById(R.id.txtName);
        mListView = findViewById(R.id.lstExpenses);


        //cursor = DB_Expenses.getAllData();
        customAdapter = new CustomAdapter(this,cursor);

        mListView.setAdapter((ListAdapter) customAdapter);


        mListView.setEmptyView(mEmptyView);

        mListView.setVisibility(View.GONE);

        final Button mButtonDate =  findViewById(R.id.buttonSortDate);
        final Button mButtonAmount = findViewById(R.id.buttonSortAmount);
        final Button mButtonName = findViewById(R.id.buttonSortName);


        mButtonDate.setVisibility(View.GONE);
        mButtonAmount.setVisibility(View.GONE);
        mButtonName.setVisibility(View.GONE);


        final EnumSort mSort = new EnumSort(typeSort.sortByName);

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
                        cursor= DB_Expenses.getExpenseListByKeyword(newText,mSort);
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
                showDBbudget.setVisibility(View.VISIBLE);
                showDBexpense.setVisibility(View.VISIBLE);
                return false;
            }
        });


        return true;
    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_settings:
                return true;

            case R.id.searchMenuItem:
                startActivity(new Intent(MainActivity.this, ResearchActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

*/
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
