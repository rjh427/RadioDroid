package net.programmierecke.radiodroid2;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import net.programmierecke.radiodroid2.data.DataRadioStation;

import java.util.Locale;
import java.util.Map;

public class ActivityPlayerInfo extends AppCompatActivity {
	ProgressDialog itsProgressLoading;
	TextView aTextViewName;
	ImageButton buttonStop;
	ImageButton buttonAddTimeout;
	ImageButton buttonClearTimeout;
	private TextView textViewCountdown;
	private BroadcastReceiver updateUIReciver;
	private TextView textViewLiveInfo;
	private TextView textViewExtraInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_player_status);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);

		Bundle anExtras = getIntent().getExtras();

		PlayerServiceUtil.bind(this);

		InitControls();
		UpdateOutput();

		IntentFilter filter = new IntentFilter();

		filter.addAction(PlayerService.PLAYER_SERVICE_TIMER_UPDATE);
		filter.addAction(PlayerService.PLAYER_SERVICE_STATUS_UPDATE);
		filter.addAction(PlayerService.PLAYER_SERVICE_META_UPDATE);

		updateUIReciver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				UpdateOutput();
			}
		};
		registerReceiver(updateUIReciver,filter);
	}

	private void InitControls() {
		aTextViewName = (TextView) findViewById(R.id.detail_station_name_value);
		textViewCountdown = (TextView) findViewById(R.id.textViewCountdown);
		if (textViewCountdown != null){
			textViewCountdown.setText("");
		}
		textViewLiveInfo = (TextView) findViewById(R.id.textViewLiveInfo);
		textViewExtraInfo = (TextView) findViewById(R.id.textViewExtraStreamInfo);

		buttonStop = (ImageButton) findViewById(R.id.buttonStop);
		if (buttonStop != null){
			buttonStop.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					PlayerServiceUtil.stop();
					finish();
				}
			});
		}

		buttonClearTimeout = (ImageButton) findViewById(R.id.buttonCancelCountdown);
		if (buttonClearTimeout != null){
			buttonClearTimeout.setVisibility(View.GONE);
			buttonClearTimeout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					clearTime();
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_player_info, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_set_alarm:
				addTime();
				return true;

			default:
				// If we got here, the user's action was not recognized.
				// Invoke the superclass to handle it.
				return super.onOptionsItemSelected(item);
		}
	}

	private void clearTime() {
		PlayerServiceUtil.clearTimer();
	}

	private void addTime() {
		PlayerServiceUtil.addTimer(10 * 60);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		PlayerServiceUtil.unBind(this);
		if (updateUIReciver != null) {
			unregisterReceiver(updateUIReciver);
			updateUIReciver = null;
		}
	}

	private void UpdateOutput() {
		Log.w("ARR","UpdateOutput()");

		if (aTextViewName != null) {
			String stationName = PlayerServiceUtil.getStationName();
			String streamName = PlayerServiceUtil.getStreamName();
			if (!TextUtils.isEmpty(streamName)) {
				aTextViewName.setText(streamName);
			}else{
				aTextViewName.setText(stationName);
			}
		}

		long seconds = PlayerServiceUtil.getTimerSeconds();

		if (seconds <= 0){
			buttonClearTimeout.setVisibility(View.GONE);
			textViewCountdown.setText("");
		}else{
			buttonClearTimeout.setVisibility(View.VISIBLE);
			textViewCountdown.setText(getResources().getString(R.string.sleep_timer,seconds / 60, seconds % 60));
		}

		Map<String,String> liveInfo = PlayerServiceUtil.getMetadataLive();
		if (liveInfo != null){
			String streamTitle = liveInfo.get("StreamTitle");
			if (!TextUtils.isEmpty(streamTitle)) {
				textViewLiveInfo.setVisibility(View.VISIBLE);
				textViewLiveInfo.setText(streamTitle);
			}else {
				textViewLiveInfo.setVisibility(View.GONE);
			}
		}else{
			textViewLiveInfo.setVisibility(View.GONE);
		}

		textViewExtraInfo.setText(String.format("%d kbps\n%s\n%s",PlayerServiceUtil.getMetadataBitrate(),PlayerServiceUtil.getMetadataGenre(),PlayerServiceUtil.getMetadataHomepage()));

		if (!PlayerServiceUtil.isPlaying()){
			Log.i("ARR","exit..");
			finish();
		}
	}
}