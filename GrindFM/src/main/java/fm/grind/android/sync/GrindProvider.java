package fm.grind.android.sync;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import fm.grind.android.utils.SelectionBuilder;

import static android.provider.BaseColumns._ID;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_DESCRIPTION;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_ENTRY_ID;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_FORMATTED_DATE;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_IMAGE_URL;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_LINK;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_PUB_DATE;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_TITLE;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.TABLE_NAME;

/*
 * Define an implementation of ContentProvider that stubs out
 * all methods
 */
public class GrindProvider extends ContentProvider {

    public interface Contract {
        /**
         * Content provider authority.
         */
        String CONTENT_AUTHORITY = "com.dsvoronin.grindfm";

        /**
         * Base URI. (content://com.dsvoronin.grindfm)
         */
        Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

        /**
         * Path component for "entry"-type resources..
         */
        String PATH_ENTRIES = "entries";

        interface Entry extends BaseColumns {

            /**
             * MIME type for lists of entries.
             */
            String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.grindfm.entries";

            /**
             * MIME type for individual entries.
             */
            String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.grindfm.entry";

            /**
             * Fully qualified URI for "entry" resources.
             */
            Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ENTRIES).build();

            /**
             * Table name where records are stored for "entry" resources.
             */
            String TABLE_NAME = "entry";

            /**
             * rss entry ID. (Note: Not to be confused with the database primary key, which is _ID.
             */
            String COLUMN_NAME_ENTRY_ID = "entry_id";

            /**
             * Article title
             */
            String COLUMN_NAME_TITLE = "title";

            /**
             * Article hyperlink
             */
            String COLUMN_NAME_LINK = "link";

            /**
             * Date article was published.
             */
            String COLUMN_NAME_PUB_DATE = "pub_date";

            /**
             * full description html
             */
            String COLUMN_NAME_DESCRIPTION = "description";

            String COLUMN_NAME_IMAGE_URL = "image_url";

            String COLUMN_NAME_FORMATTED_DATE = "formatted_date";
        }
    }

    GrindFMDatabase database;

    /**
     * Content authority for this provider.
     */
    private static final String AUTHORITY = Contract.CONTENT_AUTHORITY;

    // The constants below represent individual URI routes, as IDs. Every URI pattern recognized by
    // this ContentProvider is defined using sUriMatcher.addURI(), and associated with one of these
    // IDs.
    //
    // When a incoming URI is run through sUriMatcher, it will be tested against the defined
    // URI patterns, and the corresponding route ID will be returned.
    /**
     * URI ID for route: /entries
     */
    public static final int ROUTE_ENTRIES = 1;

    /**
     * URI ID for route: /entries/{ID}
     */
    public static final int ROUTE_ENTRIES_ID = 2;

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "entries", ROUTE_ENTRIES);
        sUriMatcher.addURI(AUTHORITY, "entries/*", ROUTE_ENTRIES_ID);
    }

    @Override
    public boolean onCreate() {
        database = new GrindFMDatabase(getContext());
        return true;
    }

    /**
     * Determine the mime type for entries returned by a given URI.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_ENTRIES:
                return Contract.Entry.CONTENT_TYPE;
            case ROUTE_ENTRIES_ID:
                return Contract.Entry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Perform a database query by URI.
     * <p/>
     * <p>Currently supports returning all entries (/entries) and individual entries by ID
     * (/entries/{ID}).
     */
    @Override
    public Cursor query(
            @NonNull Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        SQLiteDatabase db = database.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case ROUTE_ENTRIES_ID:
                // Return a single entry, by ID.
                String id = uri.getLastPathSegment();
                builder.where(_ID + "=?", id);
            case ROUTE_ENTRIES:
                // Return all known entries.
                builder.table(TABLE_NAME)
                        .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                Context ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Insert a new entry into the database.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = database.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        switch (match) {
            case ROUTE_ENTRIES:
                long id = db.insertOrThrow(TABLE_NAME, null, values);
                result = Uri.parse(Contract.Entry.CONTENT_URI + "/" + id);
                break;
            case ROUTE_ENTRIES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    /**
     * Delete an entry by database by URI.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = database.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_ENTRIES:
                count = builder.table(TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(TABLE_NAME)
                        .where(_ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * Update an entry in the database by URI.
     */
    public int update(
            @NonNull Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {

        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = database.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_ENTRIES:
                count = builder.table(TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(TABLE_NAME)
                        .where(_ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * SQLite backend for @{link GrindProvider}.
     * <p/>
     * Provides access to an disk-backed, SQLite datastore which is utilized by GrindProvider.
     * This database should never be accessed by other parts of the application directly.
     */
    static class GrindFMDatabase extends SQLiteOpenHelper {

        /**
         * Schema version.
         */
        public static final int DATABASE_VERSION = 1;

        /**
         * Filename for SQLite file.
         */
        public static final String DATABASE_NAME = "grind.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_INTEGER = " INTEGER";
        private static final String TYPE_DATETIME = " DATETIME";
        private static final String COMMA_SEP = ",";

        /**
         * SQL statement to create "entry" table.
         */
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME_ENTRY_ID + TYPE_INTEGER + COMMA_SEP +
                        COLUMN_NAME_TITLE + TYPE_TEXT + COMMA_SEP +
                        COLUMN_NAME_LINK + TYPE_TEXT + COMMA_SEP +
                        COLUMN_NAME_PUB_DATE + TYPE_DATETIME + COMMA_SEP +
                        COLUMN_NAME_DESCRIPTION + TYPE_TEXT + COMMA_SEP +
                        COLUMN_NAME_IMAGE_URL + TYPE_TEXT + COMMA_SEP +
                        COLUMN_NAME_FORMATTED_DATE + TYPE_TEXT + ")";

        /**
         * SQL statement to drop "entry" table.
         */
        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        public GrindFMDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }
}
