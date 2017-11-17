package llc.imposterstudios.librarianandroid;

import android.app.DownloadManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Exchanger;

/**
 * Created by samdickson on 3/31/17.
 */

public class APIManager
{
    Context context;
    Handler RESTHandler;

    public APIManager(Context context, Handler RESTHandler)
    {
        this.context = context;
        this.RESTHandler = RESTHandler;
    }

    public void loadBooks()
    {
        new GetBookList().execute();
    }

    public void getBookCount()
    {
        new GetBookCount().execute();
    }

    public void fetchBookDetails(String isbn)
    {
        new FetchBookDetails().execute(isbn);
    }

    public void addBook(Book book)
    {
        new AddBook().execute(book);
    }

    public void editBook(Book book)
    {
        new EditBook().execute(book);
    }

    public void deleteBook(Book book)
    {
        new DeleteBook().execute(book);
    }

    private class GetBookList extends AsyncTask<Void, Void, Void>
    {
        protected Void doInBackground(Void... v)
        {
            HttpURLConnection urlConnection = null;

            try
            {
            URL url = new URL(context.getString(R.string.book_list_url));
            urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null)
                {
                    response.append(line);
                }

                MainActivity.books = new ArrayList<>();
                JSONArray data = new JSONArray(response.toString());
                for (int i = 0; i < data.length() ; i++)
                {
                    try {
                        Book book = new Book();
                        JSONObject obj = data.optJSONObject(i);

                        book.ISBN = obj.optString("isbn");
                        book.title = obj.optString("title");
                        book.edition = obj.optString("edition").replace("[", "").replace("]", "").replace("(", "").replace(")", "");
                        book.authors = obj.optString("authors").replace("&amp;", "&");
                        book.publication = obj.optString("publication").replace("&amp;", "&");
                        book.img_url = obj.optString("img_url");
                        book.tags = obj.optString("tags");
                        book.notes = obj.optString("notes");
                        book.date_created = obj.optString("date_created");
                        book.date_modified = obj.optString("date_modified");
                        book.ID = obj.optString("_id");

                        MainActivity.books.add(book);
                    }
                    catch(Exception e){}
                }

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                urlConnection.disconnect();
            }

            return null;
        }

        protected void onPostExecute(Void v)
        {
            Message msg = RESTHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putInt("code", MainActivity.CODE_BOOK_LIST);
            msg.setData(bundle);
            RESTHandler.sendMessage(msg);
        }
    }

    private class GetBookCount extends AsyncTask<Void, Void, Void>
    {
        int count = 0;

        protected Void doInBackground(Void... v)
        {
            HttpURLConnection urlConnection = null;

            try
            {
                URL url = new URL(context.getString(R.string.book_count_url));
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null)
                {
                    response.append(line);
                }

                JSONObject data = new JSONObject(response.toString());
                count = data.optInt("count");

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                urlConnection.disconnect();
            }

            return null;
        }

        protected void onPostExecute(Void v)
        {
            Message msg = RESTHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putInt("count", count);
            bundle.putInt("code", MainActivity.CODE_BOOK_COUNT);
            msg.setData(bundle);
            RESTHandler.sendMessage(msg);
        }
    }

    private class FetchBookDetails extends AsyncTask<String, Void, Void>
    {
        Book book = null;

        protected Void doInBackground(String... args)
        {
            HttpURLConnection urlConnection = null;

            try
            {
                URL url = new URL("https://openlibrary.org/api/books?bibkeys=ISBN:" + args[0] + "&jscmd=details&format=json");
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null)
                {
                    response.append(line);
                }

                JSONObject data = new JSONObject(response.toString()).getJSONObject("ISBN:" + args[0]);

                if(data.length() > 0)
                {
                    book = new Book();
                    book.ISBN = args[0];

                    try {
                        if (data.has("thumbnail_url")) {
                            book.img_url = data.optString("thumbnail_url").replace("-S", "-M");
                        }
                    }
                    catch(Exception e){}

                    data = data.getJSONObject("details");

                    try {
                        if (data.has("full_title")) {
                            book.title = data.optString("full_title");
                        } else if (data.has("title")) {
                            book.title = data.optString("title");
                        }
                    }
                    catch(Exception e){}

                    try {
                        if (data.has("edition_name")) {
                            book.edition = data.optString("edition_name");
                        }
                    }catch(Exception e){}

                    try {
                        if (data.has("authors")) {
                            String authors = "";
                            JSONArray tmp = data.optJSONArray("authors");
                            for (int i = 0; i < tmp.length(); i++) {
                                JSONObject obj = tmp.optJSONObject(i);
                                authors += obj.optString("name");

                                if (i < tmp.length() - 1) {
                                    authors += ", ";
                                }
                            }
                            book.authors = authors;
                        }
                    }catch(Exception e){}

                    try {
                        if (data.has("publishers")) {
                            String publication = data.optString("publishers");
                            book.publication = publication.substring(2, publication.length() - 2);
                        }
                    }catch(Exception e){}

                    try {
                        if (data.has("publish_date")) {
                            book.publication += ", " + data.optString("publish_date");
                        }
                    }catch(Exception e){}
                }

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                urlConnection.disconnect();
            }

            return null;
        }

        protected void onPostExecute(Void v)
        {
            Message msg = RESTHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putSerializable("book", book);
            bundle.putInt("code", MainActivity.CODE_BOOK_LOOKUP);
            msg.setData(bundle);
            RESTHandler.sendMessage(msg);
        }
    }

    private class AddBook extends AsyncTask<Book, Void, Void>
    {
        int code = -1;

        protected Void doInBackground(Book... args)
        {
            Book book = (Book) args[0];
            try
            {
                URL url = new URL(context.getString(R.string.book_add_url));
                HttpURLConnection uc = (HttpURLConnection) url.openConnection();
                String line;
                StringBuffer jsonString = new StringBuffer();

                JSONObject bookData = new JSONObject();
                bookData.put("isbn", book.ISBN);
                bookData.put("title", book.title);
                bookData.put("edition", book.edition);
                bookData.put("authors", book.authors);
                bookData.put("publication", book.publication);
                bookData.put("img_url", book.img_url);
                bookData.put("tags", book.tags);
                bookData.put("notes", book.notes);
                bookData.put("date_created", book.date_created);

                uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                uc.setRequestMethod("POST");
                uc.setDoInput(true);
                uc.setInstanceFollowRedirects(false);
                uc.connect();
                OutputStreamWriter writer = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
                writer.write(bookData.toString());
                writer.close();
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        jsonString.append(line);
                    }
                    br.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                uc.disconnect();
                JSONObject data = new JSONObject(jsonString.toString());
                code = data.optInt("code");
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void v)
        {
            Message msg = RESTHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putInt("code", MainActivity.CODE_BOOK_ADD);
            bundle.putInt("response", code);
            msg.setData(bundle);
            RESTHandler.sendMessage(msg);
        }
    }

    private class EditBook extends AsyncTask<Book, Void, Void>
    {
        int code = -1;

        protected Void doInBackground(Book... args)
        {
            Book book = (Book) args[0];
            try
            {
                URL url = new URL(context.getString(R.string.book_edit_url) + "/" + book.ID);
                HttpURLConnection uc = (HttpURLConnection) url.openConnection();
                String line;
                StringBuffer jsonString = new StringBuffer();

                JSONObject bookData = new JSONObject();
                bookData.put("isbn", book.ISBN);
                bookData.put("title", book.title);
                bookData.put("edition", book.edition);
                bookData.put("authors", book.authors);
                bookData.put("publication", book.publication);
                bookData.put("img_url", book.img_url);
                bookData.put("tags", book.tags);
                bookData.put("notes", book.notes);
                bookData.put("date_created", book.date_created);
                bookData.put("date_modified", book.date_modified);

                uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                uc.setRequestMethod("POST");
                uc.setDoInput(true);
                uc.setInstanceFollowRedirects(false);
                uc.connect();
                OutputStreamWriter writer = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
                writer.write(bookData.toString());
                writer.close();
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        jsonString.append(line);
                    }
                    br.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                uc.disconnect();
                JSONObject data = new JSONObject(jsonString.toString());
                code = data.optInt("code");
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void v)
        {
            Message msg = RESTHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putInt("code", MainActivity.CODE_BOOK_EDIT);
            bundle.putInt("response", code);
            msg.setData(bundle);
            RESTHandler.sendMessage(msg);
        }
    }

    private class DeleteBook extends AsyncTask<Book, Void, Void>
    {
        int code = -1;

        protected Void doInBackground(Book... args)
        {
            Book book = (Book) args[0];
            HttpURLConnection urlConnection = null;

            try
            {
                URL url = new URL(context.getString(R.string.book_delete_url) + "/" + book.ID);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null)
                {
                    response.append(line);
                }

                JSONObject data = new JSONObject(response.toString());
                code = data.optInt("code");

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                urlConnection.disconnect();
            }
            return null;
        }

        protected void onPostExecute(Void v)
        {
            Message msg = RESTHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putInt("code", MainActivity.CODE_BOOK_DELETE);
            bundle.putInt("response", code);
            msg.setData(bundle);
            RESTHandler.sendMessage(msg);
        }
    }

}
