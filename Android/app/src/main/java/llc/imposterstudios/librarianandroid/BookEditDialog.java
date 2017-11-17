package llc.imposterstudios.librarianandroid;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by samdickson on 4/3/17.
 */

public class BookEditDialog extends Dialog implements View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button save, cancel, delete;
    public Book book;
    public APIManager apiManager;
    public EditText isbn, title, edition, authors, publication, tags, notes;

    public BookEditDialog(Activity a, Book book, APIManager apiManager) {
        super(a);
        this.c = a;
        this.book = book;
        this.apiManager = apiManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.book_edit_dialog);
        save = (Button) findViewById(R.id.edit_ok);
        cancel = (Button) findViewById(R.id.edit_cancel);
        delete = (Button) findViewById(R.id.edit_delete);
        delete.setOnClickListener(this);
        save.setOnClickListener(this);
        cancel.setOnClickListener(this);

        isbn = (EditText) findViewById(R.id.edit_isbn);
        title = (EditText) findViewById(R.id.edit_title);
        edition = (EditText) findViewById(R.id.edit_edition);
        authors = (EditText) findViewById(R.id.edit_authors);
        publication = (EditText) findViewById(R.id.edit_publication);
        tags = (EditText) findViewById(R.id.edit_tags);
        notes = (EditText) findViewById(R.id.edit_notes);

        if(book != null)
        {
            isbn.setText(book.ISBN);
            title.setText(book.title);
            edition.setText(book.edition);
            authors.setText(book.authors);
            publication.setText(book.publication);
            tags.setText(book.tags);
            notes.setText(book.notes);
        }

    }

    @Override
    public void onClick(View v)
    {
        if(v.equals(save))
        {
            book.ISBN = isbn.getText().toString();
            book.title = title.getText().toString();
            book.edition = edition.getText().toString();
            book.authors = authors.getText().toString();
            book.publication = publication.getText().toString();
            book.tags = tags.getText().toString();
            book.notes = notes.getText().toString();
            book.date_modified = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());

            if(book.title == null || book.title.isEmpty())
            {
                book.title = "Untitled";
            }

            apiManager.editBook(book);
        }
        else if(v.equals(delete))
        {
            new AlertDialog.Builder(c)
                    .setMessage("Are you sure you want to delete this book?")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            apiManager.deleteBook(book);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    })
                    .show();
        }

        dismiss();
    }
}
