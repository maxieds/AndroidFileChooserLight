/*
        This program (the AndroidFilePickerLight library) is free software written by
        Maxie Dion Schmidt: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        The complete license provided with source distributions of this library is
        available at the following link:
        https://github.com/maxieds/AndroidFilePickerLight
*/

package com.maxieds.androidfilepickerlightlibrary;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Point;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Set;

import static java.util.Locale.ROOT;

/*
 * Adapted from the source at:
 * https://github.com/android/storage-samples/tree/main/StorageProvider
 */
public class BasicFileProvider extends DocumentsProvider {

    private static String LOGTAG = BasicFileProvider.class.getSimpleName();

    private static final int MAX_ALLOWED_SEARCH_RESULTS = 20;
    private static final int MAX_ALLOWED_LAST_MODIFIED_FILES = 5;

    private static BasicFileProvider fileProviderStaticInst = null;
    public static  BasicFileProvider getInstance() { return fileProviderStaticInst; }

    private File baseDirPath;

    /*
     * Other storage related calls in the Context class still supported to look at later:
     * -> File[] getExternalMediaDirs()
     * -> getRootDirectory()
     * -> getNoBackupFilesDirectory()
     * -> getExternalStorageState() [Lengthy list of state inidcator constants]
     * -> isDeviceProtectedStorage()
     * -> isExternalStorageEmulated()
     * -> isExternalStorageLegacy()
     * -> isExternalStorageManager()
     * -> isExternalStorageRemovable()
     *
     * Also, may consider files flags:
     * Context.MODE_APPEND, Context.MODE_PRIVATE, Context.MODE_MULTI_PROCESS ;
     */
    private boolean setLegacyBaseFolderByName(String namedSubFolder) {
        String userPathSep = FileUtils.FILE_PATH_SEPARATOR;
        int pathSepCharCount = userPathSep.length();
        String storageRelPath = "/storage/self/primary" + userPathSep;
        //String storageRelPath = Environment.getExternalStorageDirectory() + userPathSep;
        String absFullFolderPath = String.format(Locale.getDefault(), "%s%s", storageRelPath, namedSubFolder);
        File nextFileByPath = new File(absFullFolderPath);
        if(nextFileByPath == null || !nextFileByPath.exists()) {
            Log.i(LOGTAG, "NOT Setting base dir path to \"" + absFullFolderPath + "\" -- file path DOES NOT exist");
            return false;
        }
        baseDirPath = nextFileByPath;
        return true;
    }

    public void selectBaseDirectoryByType(FileChooserBuilder.BaseFolderPathType baseFolderType) {
        Context appCtx = FileChooserActivity.getInstance();
        File nextFileByPath = null;
        switch(baseFolderType) {
            case BASE_PATH_TYPE_FILES_DIR:
            case BASE_PATH_DEFAULT:
            case BASE_PATH_SECONDARY_STORAGE:
                baseDirPath = appCtx.getFilesDir();
                break;
            case BASE_PATH_TYPE_CACHE_DIR:
                baseDirPath = appCtx.getCacheDir();
                break;
            case BASE_PATH_TYPE_EXTERNAL_FILES_DOWNLOADS:
                baseDirPath = appCtx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                setLegacyBaseFolderByName("Downloads");
                break;
            case BASE_PATH_TYPE_EXTERNAL_FILES_MOVIES:
                baseDirPath = appCtx.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                setLegacyBaseFolderByName("Movies");
                break;
            case BASE_PATH_TYPE_EXTERNAL_FILES_MUSIC:
                baseDirPath = appCtx.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                setLegacyBaseFolderByName("Music");
                break;
            case BASE_PATH_TYPE_EXTERNAL_FILES_DOCUMENTS:
                baseDirPath = appCtx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                setLegacyBaseFolderByName("Documents");
                break;
            case BASE_PATH_TYPE_EXTERNAL_FILES_DCIM:
                baseDirPath = appCtx.getExternalFilesDir(Environment.DIRECTORY_DCIM);
                setLegacyBaseFolderByName("DCIM");
                break;
            case BASE_PATH_TYPE_EXTERNAL_FILES_PICTURES:
                baseDirPath = appCtx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                setLegacyBaseFolderByName("Pictures");
                break;
            case BASE_PATH_TYPE_EXTERNAL_FILES_SCREENSHOTS:
                baseDirPath = appCtx.getExternalFilesDir(Environment.DIRECTORY_SCREENSHOTS);
                setLegacyBaseFolderByName("Pictures/Screenshots");
                break;
            case BASE_PATH_TYPE_EXTERNAL_CACHE_DIR:
            case BASE_PATH_TYPE_SDCARD:
                baseDirPath = appCtx.getExternalCacheDir();
                break;
            case BASE_PATH_TYPE_USER_DATA_DIR:
                baseDirPath = appCtx.getDataDir();
                break;
            case BASE_PATH_TYPE_MEDIA_STORE:
            case BASE_PATH_EXTERNAL_PROVIDER:
            default:
                return;
        }
    }

    @Override
    public boolean onCreate() {
        if(fileProviderStaticInst == null) {
            fileProviderStaticInst = this;
        }
        if(FileChooserActivity.getInstance() != null) {
            selectBaseDirectoryByType(FileChooserBuilder.BaseFolderPathType.BASE_PATH_DEFAULT);
        }
        return true;
    }

    public static final String[] DEFAULT_ROOT_PROJECTION = new String[] {
            DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_MIME_TYPES,
            DocumentsContract.Root.COLUMN_FLAGS,
            DocumentsContract.Root.COLUMN_ICON,
            DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_SUMMARY,
            DocumentsContract.Root.COLUMN_DOCUMENT_ID,
            DocumentsContract.Root.COLUMN_AVAILABLE_BYTES
    };

    public static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[] {
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_FLAGS,
            DocumentsContract.Document.COLUMN_SIZE
    };

    public static final int ROOT_PROJ_ROOTID_COLUMN_INDEX = 6;
    public static final int ROOT_PROJ_DOCID_COLUMN_INDEX = 6;
    public static final int DOCS_PROJ_PARENTID_COLUMN_INDEX = 0;

    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {

        final MatrixCursor mcResult = new MatrixCursor(resolveRootProjection(projection));
        final MatrixCursor.RowBuilder row = mcResult.newRow();

        row.add(DocumentsContract.Root.COLUMN_ROOT_ID, ROOT);
        row.add(DocumentsContract.Root.COLUMN_SUMMARY, getContext().getString(R.string.fileProviderRootSummayDesc));
        row.add(DocumentsContract.Root.COLUMN_FLAGS, DocumentsContract.Root.FLAG_SUPPORTS_CREATE |
                DocumentsContract.Root.FLAG_SUPPORTS_RECENTS |
                DocumentsContract.Root.FLAG_SUPPORTS_SEARCH);
        row.add(DocumentsContract.Root.COLUMN_TITLE,
                String.format(Locale.getDefault(), "%s(%s)",
                        getContext().getString(R.string.libraryName).replaceAll(" ", ""),
                        baseDirPath.getAbsolutePath()));
        row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, getDocIdForFile(baseDirPath));
        row.add(DocumentsContract.Root.COLUMN_MIME_TYPES, getChildMimeTypes(baseDirPath));
        row.add(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES, baseDirPath != null ? baseDirPath.getFreeSpace() : 0);
        row.add(DocumentsContract.Root.COLUMN_ICON, R.drawable.library_profile_icon_round_background);

        return mcResult;

    }

    @Override
    public Cursor queryRecentDocuments(String rootId, String[] projection) throws FileNotFoundException {

        // Create a cursor with the requested projection, or the default projection.
        final MatrixCursor mcResult = new MatrixCursor(resolveDocumentProjection(projection));
        final File parent = getFileForDocId(rootId);

        // Create a queue to store the most recent documents, which orders by last modified.
        PriorityQueue<File> lastModifiedFiles = new PriorityQueue<File>(5, new Comparator<File>() {
            public int compare(File i, File j) {
                return Long.compare(i.lastModified(), j.lastModified());
            }
        });

        // Iterate through all files and directories in the file structure under the root.  If
        // the file is more recent than the least recently modified, add it to the queue,
        // limiting the number of results.
        final LinkedList<File> pending = new LinkedList<File>();

        // Start by adding the parent to the list of files to be processed
        pending.add(parent);

        // Do while we still have unexamined files
        while (!pending.isEmpty()) {
            // Take a file from the list of unprocessed files
            final File file = pending.removeFirst();
            if (file.isDirectory()) {
                // If it's a directory, add all its children to the unprocessed list
                Collections.addAll(pending, file.listFiles());
            } else {
                // If it's a file, add it to the ordered queue.
                lastModifiedFiles.add(file);
            }
        }

        // Add the most recent files to the cursor, not exceeding the max number of results.
        int includedCount = 0;
        while (includedCount < MAX_ALLOWED_LAST_MODIFIED_FILES + 1 && !lastModifiedFiles.isEmpty()) {
            final File file = lastModifiedFiles.remove();
            includeFile(mcResult, null, file);
            includedCount++;
        }
        return mcResult;
    }

    @Override
    public Cursor querySearchDocuments(String rootId, String query, String[] projection) throws FileNotFoundException {

        // Create a cursor with the requested projection, or the default projection.
        final MatrixCursor mcResult = new MatrixCursor(resolveDocumentProjection(projection));
        final File parent = getFileForDocId(rootId);

        // Iterate through all files in the file structure under the root until we reach the
        // desired number of matches.
        final LinkedList<File> pending = new LinkedList<File>();

        // Start by adding the parent to the list of files to be processed
        pending.add(parent);

        // Do while we still have unexamined files, and fewer than the max search results
        while (!pending.isEmpty() && mcResult.getCount() < MAX_ALLOWED_SEARCH_RESULTS) {
            // Take a file from the list of unprocessed files
            final File file = pending.removeFirst();
            if (file.isDirectory()) {
                // If it's a directory, add all its children to the unprocessed list
                Collections.addAll(pending, file.listFiles());
            } else {
                // If it's a file and it matches, add it to the result cursor.
                if (file.getName().toLowerCase().contains(query)) {
                    includeFile(mcResult, null, file);
                }
            }
        }
        return mcResult;
    }

    @Override
    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint,
                                                     CancellationSignal signal) throws FileNotFoundException {

        final File file = getFileForDocId(documentId);
        final ParcelFileDescriptor pfd =
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        return new AssetFileDescriptor(pfd, 0, AssetFileDescriptor.UNKNOWN_LENGTH);
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {

        // Create a cursor with the requested projection, or the default projection.
        final MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));
        includeFile(result, documentId, null);
        return result;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection,
                                      String sortOrder) throws FileNotFoundException {
        Log.v(LOGTAG, "queryChildDocuments, parentDocumentId: " +
                parentDocumentId + " sortOrder: " + sortOrder);

        final MatrixCursor mcResult = new MatrixCursor(resolveDocumentProjection(projection));
        final File parent = getFileForDocId(parentDocumentId);
        for (File file : parent.listFiles()) {
            includeFile(mcResult, null, file);
        }
        return mcResult;
    }

    @Override
    public ParcelFileDescriptor openDocument(final String documentId, final String mode,
                                             CancellationSignal signal) throws FileNotFoundException {

        final File file = getFileForDocId(documentId);
        final int accessMode = ParcelFileDescriptor.parseMode(mode);

        final boolean isWrite = (mode.indexOf('w') != -1);
        if (isWrite) {
            // Attach a close listener if the document is opened in write mode.
            try {
                Handler handler = new Handler(getContext().getMainLooper());
                return ParcelFileDescriptor.open(file, accessMode, handler,
                        new ParcelFileDescriptor.OnCloseListener() {
                            @Override
                            public void onClose(IOException e) {}
                        });
            } catch (IOException e) {
                throw new FileNotFoundException("Failed to open document with id " + documentId + " and mode " + mode);
            }
        } else {
            return ParcelFileDescriptor.open(file, accessMode);
        }
    }

    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        File file = getFileForDocId(documentId);
        if (file.delete()) {
            Log.i(LOGTAG, "Deleted file with id " + documentId);
        } else {
            throw new FileNotFoundException("Failed to delete document with id " + documentId);
        }
    }

    @Override
    public String getDocumentType(String documentId) throws FileNotFoundException {
        File file = getFileForDocId(documentId);
        return getTypeForFile(file);
    }

    private static String[] resolveRootProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_ROOT_PROJECTION;
    }

    private static String[] resolveDocumentProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION;
    }

    private static String getTypeForFile(File file) {
        if (file.isDirectory()) {
            return DocumentsContract.Document.MIME_TYPE_DIR;
        } else {
            return getTypeForName(file.getName());
        }
    }

    private static String getTypeForName(String name) {
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = name.substring(lastDot + 1);
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }
        return "application/octet-stream";
    }

    private String getChildMimeTypes(File parent) {
        Set<String> mimeTypes = new HashSet<String>();
        mimeTypes.add("image/*");
        mimeTypes.add("text/*");
        mimeTypes.add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        // Flatten the list into a string and insert newlines between the MIME type strings.
        StringBuilder mimeTypesString = new StringBuilder();
        for (String mimeType : mimeTypes) {
            mimeTypesString.append(mimeType).append("\n");
        }

        return mimeTypesString.toString();
    }

    private String getDocIdForFile(File file) {
        if(file == null) {
            return null;
        }
        String path = file.getAbsolutePath();

        // Start at first char of path under root
        final String rootPath = baseDirPath.getPath();
        if (rootPath.equals(path)) {
            path = "";
        } else if (rootPath.endsWith("/")) {
            path = path.substring(rootPath.length());
        } else {
            path = path.substring(rootPath.length() + 1);
        }

        return "root" + ':' + path;
    }

    private void includeFile(MatrixCursor result, String docId, File file) throws FileNotFoundException {
        if (docId == null) {
            docId = getDocIdForFile(file);
        } else {
            file = getFileForDocId(docId);
        }

        int flags = 0;

        if (file.isDirectory()) {
            // Request the folder to lay out as a grid rather than a list. This also allows a larger
            // thumbnail to be displayed for each image.
            //            flags |= Document.FLAG_DIR_PREFERS_GRID;

            // Add FLAG_DIR_SUPPORTS_CREATE if the file is a writable directory.
            if (file.isDirectory() && file.canWrite()) {
                flags |= DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE;
            }
        } else if (file.canWrite()) {
            // If the file is writable set FLAG_SUPPORTS_WRITE and
            // FLAG_SUPPORTS_DELETE
            flags |= DocumentsContract.Document.FLAG_SUPPORTS_WRITE;
            flags |= DocumentsContract.Document.FLAG_SUPPORTS_DELETE;
        }

        final String displayName = file.getName();
        final String mimeType = getTypeForFile(file);

        if (mimeType.startsWith("image/")) {
            // Allow the image to be represented by a thumbnail rather than an icon
            flags |= DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL;
        }

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, docId);
        row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, displayName);
        row.add(DocumentsContract.Document.COLUMN_SIZE, file.length());
        row.add(DocumentsContract.Document.COLUMN_MIME_TYPE, mimeType);
        row.add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, file.lastModified());
        row.add(DocumentsContract.Document.COLUMN_FLAGS, flags);
        row.add(DocumentsContract.Document.COLUMN_ICON, R.drawable.library_profile_icon_round_background);
    }

    private File getFileForDocId(String docId) throws FileNotFoundException {
        File target = baseDirPath;
        if (docId.equals(ROOT)) {
            return target;
        }
        final int splitIndex = docId.indexOf(':', 1);
        if (splitIndex < 0) {
            throw new FileNotFoundException("Missing root for " + docId);
        } else {
            final String path = docId.substring(splitIndex + 1);
            target = new File(target, path);
            Log.i(LOGTAG, "DOCID effective path -> " + target.getAbsolutePath());
            if (!target.exists()) {
                throw new FileNotFoundException("Missing file for " + docId + " at " + target);
            }
            return target;
        }
    }

    public static final boolean CURSOR_TYPE_IS_ROOT = true;
    public static final boolean CURSOR_TYPE_IS_DOCUMENT = false;

    public static String getDocumentIdForCursorType(MatrixCursor mcResult, boolean cursorType) {
        if(mcResult.getCount() == 0) {
            return null;
        }
        String docId = "";
        if(cursorType == CURSOR_TYPE_IS_ROOT) {
            docId = mcResult.getString(ROOT_PROJ_ROOTID_COLUMN_INDEX);
        }
        else {
            docId = mcResult.getString(DOCS_PROJ_PARENTID_COLUMN_INDEX);
        }
        return docId;
    }

    public File getFileAtCurrentRow(MatrixCursor mcResult, boolean cursorType) {
        if(mcResult.getCount() == 0) {
            return null;
        }
        String docId = getDocumentIdForCursorType(mcResult, cursorType);
        try {
            return getFileForDocId(docId);
        } catch(IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    public String getAbsPathAtCurrentRow(MatrixCursor mcResult, boolean cursorType) {
        if(mcResult.getCount() == 0) {
            return null;
        }
        String docId = getDocumentIdForCursorType(mcResult, cursorType);
        try {
            File curWorkingFile = getFileForDocId(docId);
            return curWorkingFile.getAbsolutePath();
        } catch(IOException ioe) {
            ioe.printStackTrace();
            return "";
        }
    }

    public String getBaseNameAtCurrentRow(MatrixCursor mcResult, boolean cursorType) {
        if(mcResult.getCount() == 0) {
            return null;
        }
        String docId = getDocumentIdForCursorType(mcResult, cursorType);
        try {
            File curWorkingFile = getFileForDocId(docId);
            return curWorkingFile.getName();
        } catch(IOException ioe) {
            ioe.printStackTrace();
            return "";
        }
    }

    public static final int PROPERTY_ABSPATH = 0;
    public static final int PROPERTY_BASENAME = 1;
    public static final int PROPERTY_FILE_SIZE = 2;
    public static final int PROPERTY_POSIX_PERMS = 3;
    public static final int PROPERTY_ISDIR = 4;
    public static final int PROPERTY_ISHIDDEN = 5;

    public String[] getPropertiesOfCurrentRow(MatrixCursor mcResult, boolean cursorType) {
        if(mcResult.getCount() == 0) {
            return null;
        }
        String docId = getDocumentIdForCursorType(mcResult, cursorType);
        try {
            File curWorkingFile = getFileForDocId(docId);
            return new String[] {
                    curWorkingFile.getAbsolutePath(),
                    curWorkingFile.getName(),
                    FileUtils.getFileSizeString(curWorkingFile),
                    FileUtils.getFilePosixPermissionsString(curWorkingFile),
                    String.format(Locale.getDefault(), "%s", curWorkingFile.isDirectory() ? "true" : "false"),
                    String.format(Locale.getDefault(), "%s", curWorkingFile.isHidden() ? "true" : "false")
            };
        } catch(IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

}
