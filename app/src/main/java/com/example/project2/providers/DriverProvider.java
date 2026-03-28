package com.example.project2.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.example.project2.DatabaseHelper;

public class DriverProvider extends ContentProvider {

    // Authority and base URI
    public static final String AUTHORITY = "com.example.project2.provider.driver";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/drivers");

    // URI matcher codes
    private static final int DRIVERS = 1;
    private static final int DRIVER_ID = 2;
    private static final int DRIVER_UID = 3;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, "drivers", DRIVERS);
        uriMatcher.addURI(AUTHORITY, "drivers/#", DRIVER_ID);
        uriMatcher.addURI(AUTHORITY, "drivers/uid/*", DRIVER_UID);
    }

    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case DRIVERS:
                cursor = db.query("drivers", projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case DRIVER_ID:
                String id = uri.getLastPathSegment();
                cursor = db.query("drivers", projection,
                        "id = ?", new String[]{id}, null, null, sortOrder);
                break;
            case DRIVER_UID:
                String uid = uri.getLastPathSegment();
                cursor = db.query("drivers", projection,
                        "firebase_uid = ?", new String[]{uid}, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;

        if (uriMatcher.match(uri) != DRIVERS) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        id = db.insert("drivers", null, values);

        if (id > 0) {
            Uri newUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(id));
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }

        throw new android.database.SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;

        switch (uriMatcher.match(uri)) {
            case DRIVERS:
                count = db.update("drivers", values, selection, selectionArgs);
                break;
            case DRIVER_ID:
                String id = uri.getLastPathSegment();
                count = db.update("drivers", values, "id = ?", new String[]{id});
                break;
            case DRIVER_UID:
                String uid = uri.getLastPathSegment();
                count = db.update("drivers", values, "firebase_uid = ?", new String[]{uid});
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;

        switch (uriMatcher.match(uri)) {
            case DRIVERS:
                count = db.delete("drivers", selection, selectionArgs);
                break;
            case DRIVER_ID:
                String id = uri.getLastPathSegment();
                count = db.delete("drivers", "id = ?", new String[]{id});
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case DRIVERS:
                return "vnd.android.cursor.dir/vnd.com.example.project2.driver";
            case DRIVER_ID:
            case DRIVER_UID:
                return "vnd.android.cursor.item/vnd.com.example.project2.driver";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
}