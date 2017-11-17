package llc.imposterstudios.librarianandroid;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private Context context;
    private Activity activity;
    private ListView bookList;
    private TextView bookCount;
    private Button scan, manual;

    private APIManager apiManager;
    private ProgressDialog bookLoadDialog;
    private ProgressDialog bookFetchDialog;
    public static ArrayList<Book> books;

    public static final int REQUEST_CAMERA_PERMISSIONS = 1;
    public static final int REQUEST_CODE_SCAN = 2;
    public static final String[] required_permissions = {"android.permission.CAMERA"};

    public static final int CODE_BOOK_LIST = 1;
    public static final int CODE_BOOK_COUNT = 2;
    public static final int CODE_BOOK_ADD = 3;
    public static final int CODE_BOOK_EDIT = 4;
    public static final int CODE_BOOK_DELETE = 5;
    public static final int CODE_BOOK_LOOKUP = 6;

    Handler RESTHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            Bundle bundle = msg.getData();
            int code = bundle.getInt("code");

            switch(code)
            {
                case CODE_BOOK_LIST:
                    Collections.reverse(books);
                    BookListAdapter adapter = new BookListAdapter(books, context, apiManager);
                    bookList.setAdapter(adapter);
                    if(bookLoadDialog.isShowing()) {
                        bookLoadDialog.hide();
                    }
                    break;
                case CODE_BOOK_COUNT:
                    int count = bundle.getInt("count");
                    if(count == 0)
                    {
                        bookCount.setText("No Books");
                    }
                    else if(count == 1)
                    {
                        bookCount.setText("1 Book");
                    }
                    else
                    {
                        bookCount.setText(count + " Books");
                    }
                    break;
                case CODE_BOOK_LOOKUP:
                    Book book = (Book) bundle.getSerializable("book");
                    bookFetchDialog.hide();
                    if(book == null)
                    {
                        Snackbar.make(bookList, "No books found for that ISBN.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                    else
                    {
                        BookAddDialog addDialog = new BookAddDialog(activity, book, apiManager);
                        addDialog.show();
                    }
                    break;
                case CODE_BOOK_ADD:
                    switch(bundle.getInt("response"))
                    {
                        case 200:
                            apiManager.getBookCount();
                            apiManager.loadBooks();
                            Snackbar.make(bookList, "Book added.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            break;
                        default:
                            Snackbar.make(bookList, "Error adding book.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            break;
                    }
                    break;
                case CODE_BOOK_EDIT:
                    switch(bundle.getInt("response"))
                    {
                        case 200:
                            apiManager.getBookCount();
                            apiManager.loadBooks();
                            Snackbar.make(bookList, "Book saved.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            break;
                        default:
                            Snackbar.make(bookList, "Error saving book.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            break;
                    }
                    break;
                case CODE_BOOK_DELETE:
                    switch(bundle.getInt("response"))
                    {
                        case 200:
                            apiManager.getBookCount();
                            apiManager.loadBooks();
                            Snackbar.make(bookList, "Book deleted.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            break;
                        default:
                            Snackbar.make(bookList, "Error deleting book.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            break;
                    }
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        this.context = this;
        this.activity = this;

        bookList = (ListView) findViewById(R.id.book_list);
        bookCount = (TextView) findViewById(R.id.book_count);
        scan = (Button) findViewById(R.id.scan);
        scan.setOnClickListener(this);
        manual = (Button) findViewById(R.id.manual);
        manual.setOnClickListener(this);

        bookLoadDialog = new ProgressDialog(this);
        bookLoadDialog.setIndeterminate(true);
        bookLoadDialog.setMessage("Loading Books");
        bookLoadDialog.setCancelable(false);
        bookLoadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        bookFetchDialog = new ProgressDialog(this);
        bookFetchDialog.setIndeterminate(true);
        bookFetchDialog.setMessage("Fetching Book Details");
        bookFetchDialog.setCancelable(false);
        bookFetchDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        apiManager = new APIManager(this, RESTHandler);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            }
            else
            {
                loadContent();
            }
        }
        else
        {
            loadContent();
        }

    }

    public void loadContent()
    {
        bookLoadDialog.show();
        apiManager.getBookCount();
        apiManager.loadBooks();
    }

    public void onClick(View v)
    {
        if(v.equals(scan))
        {
            Intent intent = new Intent(context, ScannerActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SCAN);
        }
        else if(v.equals(manual))
        {
            BookAddDialog addDialog = new BookAddDialog(this, new Book(), apiManager);
            addDialog.show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_SCAN)
        {
            if (resultCode == RESULT_OK)
            {
                bookFetchDialog.show();
                apiManager.fetchBookDetails(data.getStringExtra("result"));
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSIONS:
                {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        loadContent();
                    }
                    else
                    {
                        new AlertDialog.Builder(context)
                                .setTitle("Permissions Required")
                                .setMessage("Librarian needs to access your camera to scan barcodes. Please select 'Allow' on the following request.")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, 1);
                                    }
                                })
                                .show();
                    }
                    return;
                }

        }
    }
}
