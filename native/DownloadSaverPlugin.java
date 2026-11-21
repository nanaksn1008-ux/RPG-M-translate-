package com.nanak.rpgmtranslator;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@CapacitorPlugin(name = "DownloadSaver")
public class DownloadSaverPlugin extends Plugin {
  @PluginMethod
  public void save(PluginCall call) {
    String name = call.getString("name");
    String data = call.getString("data");
    String folder = call.getString("folder", "rpgmtranslate");
    if (name == null || data == null) {
      call.reject("name/data kosong");
      return;
    }
    try {
      byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
      if (Build.VERSION.SDK_INT >= 29) {
        ContentResolver r = getContext().getContentResolver();
        String rel = Environment.DIRECTORY_DOWNLOADS + "/" + folder;
        Uri coll = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        try {
          r.delete(coll,
            MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " + MediaStore.MediaColumns.RELATIVE_PATH + "=?",
            new String[]{name, rel + "/"});
        } catch (Exception ignored) {}
        ContentValues v = new ContentValues();
        v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        v.put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
        v.put(MediaStore.MediaColumns.RELATIVE_PATH, rel);
        Uri uri = r.insert(coll, v);
        if (uri == null) throw new Exception("insert MediaStore gagal");
        try (OutputStream os = r.openOutputStream(uri)) {
          os.write(bytes);
        }
      } else {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folder);
        dir.mkdirs();
        try (OutputStream os = new FileOutputStream(new File(dir, name))) {
          os.write(bytes);
        }
      }
      JSObject ret = new JSObject();
      ret.put("path", "Download/" + folder + "/" + name);
      call.resolve(ret);
    } catch (Exception e) {
      call.reject(String.valueOf(e.getMessage()));
    }
  }
}
