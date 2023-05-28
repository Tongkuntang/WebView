package com.azhamudev.kotlinproject

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.support.v4.content.ContextCompat.startActivity
import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class JavascriptInterface {
    var context: Context;

    constructor(context: Context) {
        this.context = context;
    }

    @JavascriptInterface
    fun processBase64Data(base64Data: String) {
        Log.i("JavascriptInterface/processBase64Data",  base64Data)

        var fileName = "";
        var bytes = "";

        if (base64Data.startsWith("data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            fileName = "foo.xlsx"
            bytes = base64Data.replaceFirst("data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64,","")
        }

        if (fileName.isNotEmpty() && bytes.isNotEmpty()) {
            val downloadPath = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )

            Log.i("JavascriptInterface/processBase64Data", "Download Path: ${downloadPath.absolutePath}")

            val decodedString = Base64.decode(bytes, Base64.DEFAULT)
            val os = FileOutputStream(downloadPath, false)
            os.write(decodedString)
            os.flush();
            os.close();
//            val file = File("file://",downloadPath.absolutePath);
            val contents = downloadPath.readText(); // Read file
//            if (downloadPath.exists()){
//                val intent = Intent()
//                intent.setAction(android.content.Intent.ACTION_VIEW);
//                val apkURI: Uri = FileProvider.getUriForFile(
//                    context,
//                    context.applicationContext.packageName + ".provider",
//                    downloadPath
//                )
////                intent.setDataAndType(apkURI, MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension));
////                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            }
//            Toast.makeText(context, "FILE DOWNLOADED!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method to convert blobUrl to Blob, then process Base64 data on native side
     *
     * 1. Download Blob URL as Blob object
     * 2. Convert Blob object to Base64 data
     * 3. Pass Base64 data to Android layer for processing
     */
    fun getBase64StringFromBlobUrl(blobUrl: String): String {
        Log.i("JavascriptInterface/getBase64StringFromBlobUrl", "Downloading $blobUrl ...")

        // Script to convert blob URL to Base64 data in Web layer, then process it in Android layer
        val script = "javascript: (() => {" +
                "async function getBase64StringFromBlobUrl() {" +
                "const xhr = new XMLHttpRequest();" +
                "xhr.open('GET', '${blobUrl}', true);" +
                "xhr.setRequestHeader('Content-type', 'image/png');" +
                "xhr.responseType = 'blob';" +
                "xhr.onload = () => {" +
                "if (xhr.status === 200) {" +
                "const blobResponse = xhr.response;" +
                "const fileReaderInstance = new FileReader();" +
                "fileReaderInstance.readAsDataURL(blobResponse);" +
                "fileReaderInstance.onloadend = () => {" +
                "console.log('Downloaded' + ' ' + '${blobUrl}' + ' ' + 'successfully!');" +
                "const base64data = fileReaderInstance.result;" +
                "Android.processBase64Data(base64data);" +
                "}" + // file reader on load end
                "}" + // if
                "};" + // xhr on load
                "xhr.send();" +
                "}" + // async function
                "getBase64StringFromBlobUrl();" +
                "}) ()"

        return script
    }
}