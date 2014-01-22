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
