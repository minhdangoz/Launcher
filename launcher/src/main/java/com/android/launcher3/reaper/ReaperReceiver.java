package com.android.launcher3.reaper;

import java.io.Serializable;
import java.util.HashMap;




import com.android.launcher3.settings.SettingsProvider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

/* RK_ID: RK_LELAUNCHER_REAPER. AUT: zhangdxa DATE: 2013-01-11 */
public class ReaperReceiver extends BroadcastReceiver {

	private final static String TAG = "Reaper";
	
	private static boolean bReaperInitTransferData = false;

	/* If SettingsValue.PREF_REAPER is true ,then it must reaper initialize.
	 * If SettingsValue.PREF_REAPER is false, then it will reaper initialize 
	 *     when it has on of the three following conditions;
	 *   one condition: WIFI network is connected or network enabler of launcher is enabled 
	 *     when launcher created;
	 *   two condition: when the network broadcast received 
	 *     when launcher not reaper initialized;
	 *   three condition: when the network enabler of launcher has been changed enabled 
	 *     when launcher not reaper initialized.
	 */
	
	/*
	 *Reaper initialize need calling:
	 *  Reaper.reaperOn( context );(Reaper initialize configuration)
	 *  Reaper.scheduleReaperInit(context);  
	 *  (  Because Reaper.reaperOn need some time to configure, 
	 *     so usually this function can't transfer data to server successfully. 
	 *     So if must execute this function to transfer data to server.)
	 *  Reaper.scheduleReaperInitAgain(context);
	 *  (  It set a timer to raper initialize again. And it usually is at 8pm next day.)
	 */
         //modified by yumina for the sonar
         private void doReaperAction(Intent intent,Context context){
	        String category = intent.getStringExtra("category");
		String act = intent.getStringExtra("action");
		String label = intent.getStringExtra("label");
		int value = intent.getIntExtra("value", Reaper.REAPER_NO_INT_VALUE);
		Reaper.processReaperEvent(context, category, act, label, value);
         }
         private void doReaperMapAction(Intent intent,Context context){
			String category = intent.getStringExtra("category");
			String act = intent.getStringExtra("action");
			Serializable extra =intent.getSerializableExtra("map");
			HashMap<String,String> map = (HashMap<String,String>)extra;
			int value = intent.getIntExtra("value", Reaper.REAPER_NO_INT_VALUE);
			Reaper.processReaperEventMap(context, category, act, map, value);
         }
         private void doReaperInitAction(Context context){
			if( Reaper.ISNetworkAvailable(context)){
	    		Log.i("Reaper","***ReaperInit, call Reaper init.....");
	    		reaperInitAgain(context);
        	}else{
	    		setReaperInitTransferData(false);
            }
         }
         private void doReaperConnectAction(boolean bTagReaper,Intent intent,Context context){
		if( bTagReaper ||Reaper.bReaperInitCMCC ){
			    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			    if (info != null && info.isConnected() ) {
			        Log.i("Reaper","***ReaperReceiver.onReceiver(), network is connectted, call Reaper init again...");
			        reaperInitAgain(context);
				}
	   }else {
			    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
		        if ((info != null )&& 
		    	    (info.isConnected()) && 
		    	    (info.getType() == ConnectivityManager.TYPE_WIFI)) {
		        	Log.i("Reaper","***ReaperReceiver.onReceiver(), network is connectted, call Reaper init ...");
		    	    Reaper.setReaperInitCMCC(true);
	           	    Reaper.reaperOn( context );
	           	    Reaper.scheduleReaperInit(context);
		        }
		    }
        }
	@Override	
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Log.i("Reaper","***************ReaperReceiver.onReceiver(), action:"+ action );
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean bTagReaper = mSharedPreferences.getBoolean(Reaper.PREF_REAPER, false);

		if( bTagReaper && action.equals(Reaper.ACTION_REAPER)){
			if( !Reaper.bReaperInitForce ){
				Reaper.setReaperInitForce(true);
				Reaper.reaperOn( context );
			}
                        doReaperAction(intent, context);
		}else if( bTagReaper && action.equals(Reaper.ACTION_REAPER_MAP)){
			if( !Reaper.bReaperInitForce ){
				Reaper.setReaperInitForce(true);
				Reaper.reaperOn( context );
			}
                        doReaperMapAction(intent,context);
		}
		else if( action.equals(Reaper.ACTION_REAPER_INIT) ||
				 action.equals(Reaper.ACTION_REAPER_INIT_AGAIN)){
                        doReaperInitAction(context);
		}else if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){ 
			if( bReaperInitTransferData ){
				return;
			}
                 doReaperConnectAction( bTagReaper,intent,context);
        }else if( !bTagReaper && action.equals( SettingsProvider.ACTION_NETWORK_ENABLER_CHANGED)){
        	boolean networkEnabled = intent.getBooleanExtra(SettingsProvider.EXTRA_NETWORK_ENABLED, false);
                networkReaper(networkEnabled,context);
        	
        }else if ( action.equals(Reaper.ACTION_REAPER_INIT_FORCE) ||
        		   action.equals(Reaper.ACTION_REAPER_INIT_CMCC_FORCE)){
        	Log.i("Reaper","***ReaperReceiver.onReceiver(), Reaper.ACTION_REAPER_INIT_, call Reaper init...");
                scheduleReaper(context);
        }
	}
        private void networkReaper(boolean flag ,Context context){
       	    if( bReaperInitTransferData || Reaper.bReaperInitCMCC){
	        return;
            }
            deepReaperOn(flag ,context);

        }
        private void deepReaperOn(boolean flag ,Context context){
            if( flag){
        	Log.i("Reaper","***ReaperReceiver.onReceiver(), SettingsValue.EXTRA_NETWORK_ENABLED true, call Reaper init...");
        	Reaper.setReaperInitCMCC(true);
            	Reaper.reaperOn( context );
            	Reaper.scheduleReaperInit(context);
       	    }
        }
        private void scheduleReaper(Context context){
       	    Reaper.reaperOn( context );
       	    Reaper.scheduleReaperInit(context);
        }

	private void setReaperInitTransferData(boolean b) {
		// TODO Auto-generated method stub
		bReaperInitTransferData = b;

	}
	
    private void deepThreadStart(final Context context){
				setReaperInitTransferData(true);
        	    Reaper.scheduleReaperInitAgain(context);
        	    Reaper.reaperTrackInit(context);
    }
	private void reaperInitAgain(final Context context){
		new Thread(new Runnable() {
			public void run() {
                            deepThreadStart(context);
			}

		}).start();
	}
}
