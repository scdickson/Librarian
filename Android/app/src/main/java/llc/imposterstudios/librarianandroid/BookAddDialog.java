package llc.imposterstudios.librarianandroid;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by samdickson on 4/3/17.
 */

public class BookAddDialog extends Dialog implements View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button ok, cancel;
    public Book book;
    public APIManager apiManager;
    public EditText isbn, title, edition, authors, publication, tags, notes;

    public BookAddDialog(Activity a, Book book, APIManager apiManager) {
        super(a);
        this.c = a;
        this.book = book;
        this.apiManager = apiManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.book_add_dialog);
        ok = (Button) findViewById(R.id.add_ok);
        cancel = (Button) findViewById(R.id.add_cancel);
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);

         isbn = (EditText) findViewById(R.id.add_isbn);
         title = (EditText) findViewById(R.id.add_title);
         edition = (EditText) findViewById(R.id.add_edition);
         authors = (EditText) findViewById(R.id.add_authors);
         publication = (EditText) findViewById(R.id.add_publication);
         tags = (EditText) findViewById(R.id.add_tags);
         notes = (EditText) findViewById(R.id.add_notes);

        if(book != null)
        {
            isbn.setText(book.ISBN);
            title.setText(book.title);
            edition.setText(book.edition);
            authors.setText(book.authors);
            publication.setText(book.publication);
            tags.setText("");
            notes.setText("");
        }

    }

    @Override
    public void onClick(View v)
    {
        if(v.equals(ok))
        {
            book.ISBN = isbn.getText().toString();
            book.title = title.getText().toString();
            book.edition = edition.getText().toString();
            book.authors = authors.getText().toString();
            book.publication = publication.getText().toString();
            book.tags = tags.getText().toString();
            book.notes = notes.getText().toString();
            book.date_created = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());

            if(book.title == null || book.title.isEmpty())
            {
                book.title = "Untitled";
            }

            apiManager.addBook(book);
        }

        dismiss();
    }
}
