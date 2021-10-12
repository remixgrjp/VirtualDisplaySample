package asia.remix.virtualdisplay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class MainService extends Service{
	String sChannelId = "Channel ID";

	@Override
	public void onCreate(){
		super.onCreate();
	}

	@Override
	public int onStartCommand( Intent intent, int flags, int startId ){
		Context context = getApplicationContext();
		String sTitle = context.getString( R.string.app_name );

		if( android.os.Build.VERSION.SDK_INT >= 26 ){
			//API26 Android 8以上 通知を送信する前に通知チャネルを作成する必要がある
			NotificationChannel channel
			= new NotificationChannel( sChannelId, sTitle, NotificationManager.IMPORTANCE_DEFAULT );
			NotificationManager notificationManager
			= (NotificationManager)context.getSystemService( Context.NOTIFICATION_SERVICE );
			notificationManager.createNotificationChannel( channel );
		}

		NotificationCompat.Builder builder;
		if( android.os.Build.VERSION.SDK_INT >= 26 ){
			builder = new NotificationCompat.Builder( context, sChannelId );
		}else{
			builder = new NotificationCompat.Builder( context );
		}
		builder.setSmallIcon( android.R.drawable.btn_star );
		//通知はタップ応答する必要があり、ここではMainActivityを立ち上げる
		PendingIntent pendingIntent = PendingIntent.getActivity( context, 0, new Intent( this, MainActivity.class ), 0 );
		builder.setContentIntent( pendingIntent );

		Notification notification = builder.build();
		if( android.os.Build.VERSION.SDK_INT >= 29 ){
			startForeground( 1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION );
		}else{
			startForeground( 1, notification );//0は通知表示されない
		}

		return super.onStartCommand( intent, flags, startId );
	}

	@Override
	public IBinder onBind( Intent intent ){
		return null;//オーバーライド必須、バインド不要の場合はnullを返す。
	}
}