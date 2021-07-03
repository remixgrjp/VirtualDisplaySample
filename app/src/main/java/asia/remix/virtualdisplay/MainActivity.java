package asia.remix.virtualdisplay;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{

	MediaProjectionManager projectionManager;

	@Override
	protected void onCreate( Bundle savedInstanceState ){
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		projectionManager = (MediaProjectionManager)getSystemService( MEDIA_PROJECTION_SERVICE );
		Intent captureIntent = projectionManager.createScreenCaptureIntent();
		launcherStartActivityForResult.launch( captureIntent );
	}

	ActivityResultLauncher<Intent> launcherStartActivityForResult = registerForActivityResult(
		new ActivityResultContracts.StartActivityForResult(),
		new ActivityResultCallback<ActivityResult>(){
			@Override
			public void onActivityResult( ActivityResult result ){
				Log.d( "■", "launcherStartActivityForResult#onActivityResult()" );
				if( result.getResultCode() == Activity.RESULT_OK ){
					MediaProjection mediaProjection = projectionManager.getMediaProjection(
						result.getResultCode() // Activity.RESULT_OK
					,	result.getData() // Intent
					);

					DisplayMetrics metrics = getResources().getDisplayMetrics();
					SurfaceView surfaceView = (SurfaceView)findViewById( R.id.surfaceView );
					VirtualDisplay virtualDisplay = mediaProjection.createVirtualDisplay( "Virtual"
					,	surfaceView.getWidth()
					,	surfaceView.getHeight()
					,	metrics.densityDpi
					,	DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
					,	surfaceView.getHolder().getSurface() // Surface
					,	null // Callback
					,	null // Handler
					);
					Log.d( "■", String.format( "W%dxH%d", surfaceView.getWidth(), surfaceView.getHeight() ) );
				}else{
					Toast.makeText( MainActivity.this, "must Permission Screen Capture", Toast.LENGTH_SHORT ).show();
					finish();
				}
			}
		} 
	);
}