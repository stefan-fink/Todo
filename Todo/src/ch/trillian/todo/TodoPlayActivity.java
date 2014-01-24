package ch.trillian.todo;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import ch.trillian.todo.view.PlayView;

public class TodoPlayActivity extends Activity {

  PlayView playView;

  @Override
  protected void onCreate(Bundle bundle) {

    super.onCreate(bundle);
    setContentView(R.layout.activity_play);
    playView = (PlayView) findViewById(R.id.play_view);

    DownloadMapTask downloadMapTask = new DownloadMapTask();
    downloadMapTask.execute(0);
  }

  private class DownloadMapTask extends AsyncTask<Integer, Integer, Bitmap> {

    protected Bitmap doInBackground(Integer... ints) {

      // test http map download
      Bitmap bitmap = null;
      
      try {
        URL url = new URL("http://wmts.geo.admin.ch/1.0.0/ch.swisstopo.pixelkarte-farbe/default/20140106/21781/19/7/12.jpeg");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("referer", "http://map.geo.admin.ch/");
        InputStream inputStream = connection.getInputStream();

        //        byte[] buffer = new byte[1000];
        //        int count;
        //        while ((count = inputStream.read(buffer)) > 0) {
        //          Log.w("TODO", "Bitmap-size: " + size);
        //          size += count;
        //        }
        //        Log.w("TODO", "Bitmap-size: " + size);

        bitmap = BitmapFactory.decodeStream(inputStream);

        inputStream.close();
        connection.disconnect();
      } catch (Exception e) {
        Log.w("TODO", "Exception: " + e.getMessage(), e);
      }
      
      return bitmap;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(Bitmap result) {

      if (result == null) {
        Log.w("TODO", "Result: null");
      } else {
        Log.w("TODO", "Result: got a bitmap: (" + result.getWidth() + ", " + result.getHeight() + ")");
        playView.setBitmap(result);
      }
    }
  }

  @Override
  protected void onPause() {

    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    locationManager.removeUpdates(locationListener);

    super.onPause();
  }

  private void setLocationOnView(Location location) {

    playView.setLocation(location);
  }

  @Override
  protected void onResume() {

    super.onResume();

    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

    setLocationOnView(location);
  }

  private final LocationListener locationListener = new LocationListener() {

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String provider) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onLocationChanged(Location location) {

      setLocationOnView(location);
    }
  };
}
