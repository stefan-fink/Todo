package ch.trillian.todo;

import ch.trillian.todo.view.PlayView;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class TodoPlayActivity extends Activity {
  
  PlayView playView;
  
  @Override
  protected void onCreate(Bundle bundle) {
    
    super.onCreate(bundle);
    setContentView(R.layout.activity_play);
    playView = (PlayView) findViewById(R.id.play_view);
  }
  
  @Override
  protected void onPause() {
    
    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    locationManager.removeUpdates(locationListener);

    super.onPause();
  }

  private void setLocationOnView(Location location) {
    
    // calc swiss 1903 coordinates
    double p = (location.getLatitude()*3600d - 169028.66d) / 10000d;
    double l = (location.getLongitude()*3600d - 26782.5d) / 10000d;
    
    double x = 200147.07 + 308807.95*p + 3745.25*l*l + 76.63*p*p + 119.79*p*p*p - 194.56*l*l*p;
    double y = 600072.37 + 211455.93*l - 10938.51*l*p - 0.36*l*p*p - 44.54*l*l*l;
    
    String locationString = String.format("CH: (%3.0f, %3.0f)", x, y);
    Log.w("TODO", "CH: (" + x + ", " + y + ")");
    playView.setLabelText(locationString);
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
