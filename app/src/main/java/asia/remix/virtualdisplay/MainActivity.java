package asia.remix.virtualdisplay;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{

	MediaProjectionManager projectionManager;
	MediaProjection mediaProjection;
	ImageReader imageReader;
	android.os.Handler handlerBackground = new android.os.Handler();
	VirtualDisplay virtualDisplay;

	class Screen{
		int w = 0;
		int h = 0;
		void set( int w, int h ){
			this.w = w;
			this.h = h;
		}
	}
	Screen screen = new Screen();
	boolean isFinish = false;

	@Override
	protected void onCreate( Bundle savedInstanceState ){
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		isFinish = false;
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
					mediaProjection = projectionManager.getMediaProjection(
						result.getResultCode() // Activity.RESULT_OK
					,	result.getData() // Intent
					);

					DisplayMetrics metrics = getResources().getDisplayMetrics();
					screen.set( metrics.widthPixels, metrics.heightPixels );
					imageReader = ImageReader.newInstance(
						screen.w
					,	screen.h
					,	android.graphics.PixelFormat.RGBA_8888//×RGB_565,RGBA_4444
					,	1 //max images
					);
					imageReader.setOnImageAvailableListener( onImageAvailableListener1, handlerBackground );
					virtualDisplay = mediaProjection.createVirtualDisplay( "Virtual"
					,	screen.w
					,	screen.h
					,	metrics.densityDpi
					,	DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
					,	imageReader.getSurface() // Surface
					,	null // Callback
					,	null // Handler
					);
					Log.d( "■", String.format( "W%dxH%d,%d", metrics.widthPixels, metrics.heightPixels, metrics.densityDpi ) );
				}else{
					Toast.makeText( MainActivity.this, "must Permission Screen Capture", Toast.LENGTH_SHORT ).show();
					finish();
				}
			}
		} 
	);

	ImageReader.OnImageAvailableListener onImageAvailableListener1 = new ImageReader.OnImageAvailableListener(){
		@Override
		public void onImageAvailable( ImageReader ir ){// != this.imageReader -> Error
			Log.d( "▲", "onImageAvailable()" );

			if( isFinish ) return;
			Image image = null;
			try{
				image = ir.acquireLatestImage();//ir.acquireNextImage();
				if( image == null ) return;
				Image.Plane[] planes = image.getPlanes();
				Log.d( "■", String.format( "W%dxH%d,image.getFormat()=%d,planes=%d", image.getWidth(), image.getHeight(), image.getFormat(), planes.length ) );

				int rowStride = planes[0].getRowStride();//1ラインのバイト数
				int pixelStride = planes[0].getPixelStride();//1ピクセルのバイト数 例4
				Log.d( "■", String.format( "getRowStride()=%d,getPixelStride()=%d", rowStride, pixelStride ) );

				int w = rowStride/pixelStride;
				screen.set( w, image.getHeight() );
				virtualDisplay.release();
				isFinish = true;
				Log.d( "▼", "onImageAvailable()" );
				DisplayMetrics metrics = getResources().getDisplayMetrics();
				imageReader = ImageReader.newInstance(
					screen.w
				,	screen.h
				,	android.graphics.PixelFormat.RGBA_8888//×RGB_565,RGBA_4444
				,	5 //max images
				);
				imageReader.setOnImageAvailableListener( onImageAvailableListener2, handlerBackground );
				virtualDisplay = mediaProjection.createVirtualDisplay( "Virtual main"
				,	screen.w
				,	screen.h
				,	metrics.densityDpi
				,	DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
				,	imageReader.getSurface() // Surface
				,	null // Callback
				,	null // Handler
				);
			}finally{
				if( image != null ){
					image.close();//忘れるとW/ImageReader_JNI: Unable to acquire a buffer item, very likely client tried to acquire more than maxImages buffers
				}
			}

		}
	};

	ImageReader.OnImageAvailableListener onImageAvailableListener2 = new ImageReader.OnImageAvailableListener(){
		@Override
		public void onImageAvailable( ImageReader ir ){// != this.imageReader -> Error
			Image image = null;
			try{
				image = ir.acquireLatestImage();//ir.acquireNextImage();?
				if( image == null ) return;
				Image.Plane[] planes = image.getPlanes();

				int rowStride = planes[0].getRowStride();//1ラインのバイト数
				int pixelStride = planes[0].getPixelStride();//1ピクセルのバイト数 例4

				Bitmap bitmap = Bitmap.createBitmap(
					screen.w
				,	screen.h
				,	Bitmap.Config.ARGB_8888//deprecated in API level 29〇ARGB_4444,×RGB_565
				);
				bitmap.copyPixelsFromBuffer( /*java.nio.ByteBuffer*/ planes[0].getBuffer() );

				ImageView imageView = (ImageView)findViewById( R.id.imageView );
				imageView.setImageBitmap( bitmap );
			}finally{
				if( image != null ){
					image.close();// W/ImageReader_JNI: Unable to acquire a buffer item, very likely client tried to acquire more than maxImages buffers
				}
			}
		}
	};
}