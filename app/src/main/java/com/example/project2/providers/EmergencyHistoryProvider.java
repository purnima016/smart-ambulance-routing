package com.example.project2.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.example.project2.DatabaseHelper;

public class EmergencyHistoryProvider extends ContentProvider {
    private static final String TAG = "EmergencyHistoryProvider";

    private static final int HISTORY = 1;
    private static final int HISTORY_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(EmergencyHistoryContract.CONTENT_AUTHORITY, "history", HISTORY);
        uriMatcher.addURI(EmergencyHistoryContract.CONTENT_AUTHORITY, "history/#", HISTORY_ID);
    }

    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        Log.d(TAG, "✅ EmergencyHistoryProvider created successfully");
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        Log.d(TAG, "🔍 Query URI: " + uri);

        switch (uriMatcher.match(uri)) {
            case HISTORY:
                cursor = db.query(
                        EmergencyHistoryContract.HistoryEntry.TABLE_NAME,
                        projection, selection, selectionArgs,
                        null, null, sortOrder
                );
                break;
            case HISTORY_ID:
                String id = uri.getLastPathSegment();
                cursor = db.query(
                        EmergencyHistoryContract.HistoryEntry.TABLE_NAME,
                        projection,
                        EmergencyHistoryContract.HistoryEntry._ID + " = ?",
                        new String[]{id},
                        null, null, sortOrder
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        Log.d(TAG, "✅ Query executed, rows returned: " + cursor.getCount());
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;

        Log.d(TAG, "📝 Insert URI: " + uri);
        Log.d(TAG, "📝 Insert values: " + values.toString());

        if (uriMatcher.match(uri) != HISTORY) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // Validate required fields
        if (!values.containsKey(EmergencyHistoryContract.HistoryEntry.COLUMN_HOSPITAL_NAME)) {
            throw new IllegalArgumentException("Hospital name is required");
        }

        // Add timestamp if not provided
        if (!values.containsKey(EmergencyHistoryContract.HistoryEntry.COLUMN_TIMESTAMP)) {
            values.put(EmergencyHistoryContract.HistoryEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());
        }

        try {
            id = db.insertOrThrow(EmergencyHistoryContract.HistoryEntry.TABLE_NAME, null, values);
            Log.d(TAG, "✅ Insert successful, ID: " + id);
        } catch (Exception e) {
            Log.e(TAG, "❌ Insert failed: " + e.getMessage());
            throw new android.database.SQLException("Failed to insert row: " + e.getMessage());
        }

        if (id > 0) {
            Uri newUri = Uri.withAppendedPath(EmergencyHistoryContract.HistoryEntry.CONTENT_URI, String.valueOf(id));
            getContext().getContentResolver().notifyChange(newUri, null);
            getContext().getContentResolver().notifyChange(EmergencyHistoryContract.HistoryEntry.CONTENT_URI, null);
            Log.d(TAG, "✅ Notified content resolver of change");
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
            case HISTORY:
                count = db.update(
                        EmergencyHistoryContract.HistoryEntry.TABLE_NAME,
                        values, selection, selectionArgs
                );
                break;
            case HISTORY_ID:
                String id = uri.getLastPathSegment();
                count = db.update(
                        EmergencyHistoryContract.HistoryEntry.TABLE_NAME,
                        values,
                        EmergencyHistoryContract.HistoryEntry._ID + " = ?",
                        new String[]{id}
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        Log.d(TAG, "Update completed, rows affected: " + count);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;

        switch (uriMatcher.match(uri)) {
            case HISTORY:
                count = db.delete(
                        EmergencyHistoryContract.HistoryEntry.TABLE_NAME,
                        selection, selectionArgs
                );
                break;
            case HISTORY_ID:
                String id = uri.getLastPathSegment();
                count = db.delete(
                        EmergencyHistoryContract.HistoryEntry.TABLE_NAME,
                        EmergencyHistoryContract.HistoryEntry._ID + " = ?",
                        new String[]{id}
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        Log.d(TAG, "Delete completed, rows deleted: " + count);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case HISTORY:
                return EmergencyHistoryContract.HistoryEntry.CONTENT_TYPE;
            case HISTORY_ID:
                return EmergencyHistoryContract.HistoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
}