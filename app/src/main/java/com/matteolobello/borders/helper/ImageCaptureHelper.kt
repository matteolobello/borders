package com.matteolobello.borders.helper

// MIT License
// Copyright (c) 2015 Karol Wrótniak, Droids On Roids

import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.provider.MediaStore.Images.ImageColumns
import android.provider.MediaStore.MediaColumns
import java.io.File
import java.util.regex.Pattern

/**
 * Helper for sending ACTION_IMAGE_CAPTURE intent and retrieve its results. Handles all low level operations
 * <br></br>
 * Usage:<br></br>
 *
 *  1. Launch camera app by calling [.launchCameraApp], save resulting Uri, handle (or ignore) exceptions.
 *  1. In your onActivityResult(int requestCode, int resultCode, Intent data) call [) ][.retrievePhotoResult]. You may want to inform user if photo cannot be read despite the result is RESULT_OK.
 *
 * @author koral--
 */
object ImageCaptureHelper {

    private val PROJECTION = arrayOf(MediaColumns.DATA)
    private val CONTENT_VALUES = ContentValues(1)
    private const val PHOTO_SHARED_PREFS_NAME = "photo_shared"
    private const val PHOTO_URI = "photo_uri"

    /**
     * Description text inserted into @link{ImageColumns.DESCRIPTION} column
     */
    private const val DESCRIPTION = "Photo taken with example application"

    init {
        CONTENT_VALUES.put(ImageColumns.DESCRIPTION, DESCRIPTION)
    }

    /**
     * Tries to obtain File containing taken photo. Perform cleanups if photo was not taken or it is empty.
     * @param caller caller Activity
     * @param photoKey key in Shared Preferences for taken image, can be null
     * @return File containing photo or null if no or empty photo was saved by camera app.
     */
    fun retrievePhotoResult(caller: Activity, photoKey: String?): File? {
        var photoKey = photoKey
        try {
            if (photoKey == null) {
                photoKey = PHOTO_URI
            }
            val prefs = caller.getSharedPreferences(PHOTO_SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            val takenPhotoUriString = prefs.getString(photoKey, null)
            prefs.edit().remove(photoKey).apply()

            if (takenPhotoUriString == null) {
                return null
            }

            val takenPhotoUri = Uri.parse(takenPhotoUriString)
            val cr = caller.contentResolver
            val out = File(getPhotoFilePath(takenPhotoUri, cr)!!)
            if (!out.isFile || out.length() == 0L) {
                cr.delete(takenPhotoUri, null, null)
            } else {
                return out
            }
        } catch (ex: Exception) {
            // no-op
        }

        return null
    }

    /**
     * Tries to create photo placeholder and launch camera app
     * @param requestCode your unique code that will be returned in onActivityResult
     * @param caller caller Activity
     * @param photoKey key in Shared Preferences for taken image, can be null
     * @throws ActivityNotFoundException if no camera app was found
     * @throws Exception if there is a problem with create photo eg. SDcard is not mounted. It may be eg. [IllegalStateException] or [UnsupportedOperationException] depending on [ContentResolver].
     */
    @Throws(ActivityNotFoundException::class, Exception::class)
    fun launchCameraApp(requestCode: Int, caller: Activity, photoKey: String?) {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        var photoKey = photoKey
        if (photoKey == null) {
            photoKey = PHOTO_URI
        }

        val cr = caller.contentResolver
        val takenPhotoUri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, CONTENT_VALUES)
                ?: throw IllegalStateException("Photo insertion failed")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getIntentUri(takenPhotoUri, cr))
        caller.startActivityForResult(intent, requestCode)

        val prefs = caller.getSharedPreferences(PHOTO_SHARED_PREFS_NAME, Context.MODE_PRIVATE)

        prefs.edit().putString(photoKey, takenPhotoUri.toString()).apply()
    }

    private fun getIntentUri(takenPhotoUri: Uri, cr: ContentResolver): Uri {
        val path = getPhotoFilePath(takenPhotoUri, cr)
                ?: throw IllegalStateException("Photo resolution failed")
        return Uri.fromFile(File(path))
    }

    private fun getPhotoFilePath(takenPhotoUri: Uri, cr: ContentResolver): String? {
        val cursor = cr.query(takenPhotoUri, PROJECTION, null, null, null)
        var res: String? = null
        if (cursor != null) {
            val dataIdx = cursor.getColumnIndex(MediaColumns.DATA)
            if (dataIdx >= 0 && cursor.moveToFirst())
                res = cursor.getString(dataIdx)
            cursor.close()
        }
        return res
    }

    /**
     * Dirty hack for API level <8 to get a top-level public external storage directory where Camera photos should be placed.<br></br>
     * Empty photo is inserted, path of its parent directory is retrieved and then photo is deleted.<br></br>
     * If photo cannot be inserted eg. external storage is not mounted, then "DCIM" folder in root of the external storage is used as a fallback.
     * @param cr [ContentResolver] used to resolve image Uris
     * @return path to directory where camera app places photos (may be fallback)
     */
    fun getPhotoDirPath(cr: ContentResolver): String {
        val fallback = Environment.getExternalStorageDirectory().toString() + "/DCIM"
        var takenPhotoUri: Uri? = null
        var photoFilePath: String? = null
        try {
            takenPhotoUri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, CONTENT_VALUES)
            if (takenPhotoUri == null)
                return fallback
            photoFilePath = getPhotoFilePath(takenPhotoUri, cr)
            cr.delete(takenPhotoUri, null, null)
        } catch (ex: Exception) {
            //igonred
        }

        if (photoFilePath == null)
            return fallback
        var parent = File(photoFilePath).parent
        val m = Pattern.compile("/DCIM(/|$)").matcher(parent)
        if (m.find())
            parent = parent.substring(0, m.end())

        return parent
    }
}