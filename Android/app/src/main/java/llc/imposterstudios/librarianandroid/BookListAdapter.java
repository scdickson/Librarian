package llc.imposterstudios.librarianandroid;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by samdickson on 3/31/17.
 */

public class BookListAdapter extends BaseAdapter
{
    ArrayList<Book> books;
    Context context;
    APIManager apiManager;

    public BookListAdapter(ArrayList<Book> books, Context context, APIManager apiManager)
    {
        this.books = books;
        this.context = context;
        this.apiManager = apiManager;
    }

    public int getCount()
    {
        return books.size();
    }

   public Object getItem(int position)
   {
       return books.get(position);
   }

   public long getItemId(int position)
   {
       return position;
   }

   public View getView(final int position, final View convertView, ViewGroup parent)
   {
       LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       View view = inflater.inflate(R.layout.book_list_row, parent, false);
       final Book book = (Book) getItem(position);

       LinearLayout bookRowLayout = (LinearLayout) view.findViewById(R.id.book_row_layout);
       bookRowLayout.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v)
           {
               BookEditDialog editDialog = new BookEditDialog((Activity) context, book, apiManager);
               editDialog.show();
           }
       });
       bookRowLayout.setOnLongClickListener(new View.OnLongClickListener() {
           @Override
           public boolean onLongClick(View v) {
               Toast.makeText(context, "_id: " + book.ID, Toast.LENGTH_LONG).show();
               return true;
           }
       });
       TextView title = (TextView) view.findViewById(R.id.book_title);
       TextView authorsPublisher = (TextView) view.findViewById(R.id.book_authors_publisher);
       TextView tags = (TextView) view.findViewById(R.id.book_tags);
       TextView notes = (TextView) view.findViewById(R.id.book_notes);

       if(!book.title.isEmpty())
       {
           title.setText(Html.fromHtml("<font color=\"#3F51B5\">" + book.title + "</font>"));
       }

       if(!book.edition.isEmpty())
       {
           title.setText(Html.fromHtml("<font color=\"#3F51B5\">" + book.title + "</font><font color=\"#000000\">" + " (" + book.edition + ")</font>"));
       }

       if(!book.authors.isEmpty())
       {
           authorsPublisher.setText(book.authors);
       }

       if(!book.publication.isEmpty())
       {
           authorsPublisher.setText(authorsPublisher.getText() + " \u2022 " + book.publication);
       }

       if(!book.tags.isEmpty())
       {
           tags.setText(Html.fromHtml("Tags: <b><font color=\"#E91E63\">" + book.tags + "</font></b>"));
       }

       if(!book.notes.isEmpty())
       {
           notes.setText(Html.fromHtml("Notes: <b><font color=\"#4CAF50\">" + book.notes + "</font></b>"));
       }

       return view;
   }
}
