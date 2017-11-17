package llc.imposterstudios.librarianandroid;

import java.io.Serializable;

/**
 * Created by samdickson on 3/31/17.
 */

public class Book implements Serializable
{
    public String ISBN;
    public String title;
    public String edition;
    public String authors;
    public String publication;
    public String tags;
    public String notes;
    public String img_url;
    public String date_created;
    public String date_modified;
    public String ID;

    public String toString()
    {
        return ISBN + ", " + title + ", " + edition + ", " + authors + ", " + publication;
    }
}
