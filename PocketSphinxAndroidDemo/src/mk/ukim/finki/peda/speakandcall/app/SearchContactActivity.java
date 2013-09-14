package mk.ukim.finki.peda.speakandcall.app;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import mk.ukim.finki.peda.speakandcall.R;
import mk.ukim.finki.peda.speakandcall.RecognitionListenerNames;
import mk.ukim.finki.peda.speakandcall.RecognizerTaskNames;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SearchContactActivity extends Activity implements
		RecognitionListenerNames {
	static {
		System.loadLibrary("pocketsphinx_jni");
	}

	/**
	 * Recognizer task, which runs in a worker thread.
	 */
	RecognizerTaskNames names_rec;
	/**
	 * Thread in which the recognizer task runs.
	 */
	Thread names_rec_thread;
	/**
	 * Time at which current recognition started.
	 */
	Date names_start_date;
	/**
	 * Number of seconds of speech.
	 */
	float names_speech_dur;
	/**
	 * Are we listening?
	 */
	boolean names_listening;
	/**
	 * Progress dialog for final recognition.
	 */
	ProgressDialog names_rec_dialog;
	/**
	 * Performance counter view.
	 */
	TextView performance_text;
	/**
	 * Editable text view.
	 */
	EditText names_edit_text;

	TextView result_text;

	Button names_btnSpeak;

	Boolean names_flagSpeak;

	MediaPlayer player;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_layout);

		fillNames();

		this.names_start_date = new Date();

		this.names_rec = new RecognizerTaskNames();

		this.names_listening = false;

		names_flagSpeak = false;

		this.performance_text = (TextView) findViewById(R.id.SearchPerformanceLabel);

		this.names_edit_text = (EditText) findViewById(R.id.SearchEdit);

		this.names_rec.setRecognitionListenerNames(this);

		this.names_rec_thread = new Thread(this.names_rec);

		this.names_rec_thread.start();

		new PlaySearchCommandMenu().execute();
	}

	public static HashMap<String, String> names;

	public static void fillNames() {

		names = new HashMap<String, String>();

		names.put("ALEKSANDAR", "АЛЕКСАНДАР");
		names.put("ALEKSANDRA", "АЛЕКСАНДРА");
		names.put("BILJANA", "БИЛЈАНА");
		names.put("GORAN", "ГОРАН");
		names.put("DEJAN", "ДЕЈАН");
		names.put("DRAGAN", "ДРАГАН");
		names.put("ELENA", "ЕЛЕНА");
		names.put("FILIP", "ФИЛИП");
		names.put("IGOR", "ИГОР");
		names.put("ILIJA", "ИЛИЈА");
		names.put("MARIJA", "МАРИЈА");
		names.put("NIKOLA", "НИКОЛА");
		names.put("PETAR", "ПЕТАР");
		names.put("RISTO", "РИСТО");
		names.put("SNEZHANA", "СНЕЖАНА");
		names.put("STEFAN", "СТЕФАН");
		names.put("SUZANA", "СУЗАНА");
		names.put("VESNA", "ВЕСНА");
		names.put("VIOLETA", "ВИОЛЕТА");
		names.put("ZORAN", "ЗОРАН");
	}

	protected void onStop() {
		super.onStop();
		searchRecognizerControl(true);
		player.stop();
	}

	public void searchRecognizerControl(boolean flagSpeak) {

		if (!flagSpeak) {
			names_start_date = new Date();
			names_listening = true;

			flagSpeak = true;
			names_edit_text.setText("");

			names_rec.start();
		} else {

			Date end_date = new Date();
			long nmsec = end_date.getTime() - names_start_date.getTime();
			names_speech_dur = (float) nmsec / 1000;
			if (names_listening) {
				Log.d(getClass().getName(), "Showing Dialog");				
				names_listening = false;
				flagSpeak = false;
			}
			names_rec.stop();

		}
	}

	@Override
	public void onPartialResultsNames(Bundle b) {
		final SearchContactActivity that = this;
		final String hyp = b.getString("hyp");

		that.names_edit_text.post(new Runnable() {
			public void run() {

				if (hyp != null) {
					String tempRes = hyp;

					// removes words shorter than 3 letters
					tempRes = tempRes.replaceAll("\\b[\\w']{1,2}\\b", "");
					tempRes = tempRes.replaceAll("\\s{2,}", " ");

					String results[] = tempRes.split(" ");
					String finalRes = "";
					for (int i = 0; i < results.length; i++) {
						results[i] = names.get(results[i]);
						if (results[i] != null)
							finalRes += results[i] + " ";
					}

					that.names_edit_text.setText(finalRes);
					if (results.length > 0)
						searchRecognizerControl(true);
				}

			}
		});

	}

	@Override
	public void onResultsNames(Bundle b) {

		String tempRes = b.getString("hyp");

		if (tempRes != null) {

			// removes words shorter than 3 letters tempRes =
			tempRes.replaceAll("\\b[\\w']{1,2}\\b", "");
			tempRes = tempRes.replaceAll("\\s{2,}", " ");

			String results[] = tempRes.split(" ");

			final String hyp;
			String namesRcognized = "";

			for (int i = 0; i < results.length; i++) {
				if (names.get(results[i]) != null)
					namesRcognized += names.get(results[i]) + " ";
			}

			hyp = namesRcognized;

			final SearchContactActivity that = this;
			this.names_edit_text.post(new Runnable() {
				public void run() {

					that.names_edit_text.setText(hyp);
					Date end_date = new Date();
					long nmsec = end_date.getTime()
							- that.names_start_date.getTime();
					float rec_dur = (float) nmsec / 1000;
					that.performance_text.setText(String.format(
							"%.2f секунди %.2f xRT", that.names_speech_dur,
							rec_dur / that.names_speech_dur));
					Log.d(getClass().getName(), "Hiding Dialog");

					if (that.names_rec_dialog != null)
						that.names_rec_dialog.dismiss();

					String lastName = hyp.split(" ")[hyp.split(" ").length - 1];
					getPhoneNumber(lastName);

				}
			});

		}
	}

	@Override
	public void onErrorNames(int err) {
		final SearchContactActivity that = this;
		that.names_edit_text.post(new Runnable() {
			public void run() {

			}
		});

	}

	public void getPhoneNumber(String name) {

		if (name != null && names.containsValue(name)) {

			names_rec.stop();
			names_flagSpeak = false;

			Context context = getBaseContext();

			String phoneNumber = null;
			String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
					+ " like'" + name + "'";
			String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER };
			Cursor c = context.getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					projection, selection, null, null);
			if (c.moveToFirst()) {
				phoneNumber = c.getString(0);
			}
			c.close();
			if (phoneNumber == null) {
				phoneNumber = "Unsaved";
				searchRecognizerControl(false);
			} else {
				try {
					Intent callIntent = new Intent(Intent.ACTION_CALL);
					callIntent.setData(Uri.parse("tel:" + phoneNumber));
					startActivity(callIntent);
				} catch (ActivityNotFoundException activityException) {
					Log.e("Calling a Phone Number", "Call failed",
							activityException);
				}
			}

		} else {
			searchRecognizerControl(false);
		}
	}

	public void playCommandsMenu() {
		player = new MediaPlayer();

		try {
			player.setDataSource(Environment.getExternalStorageDirectory()
					.getPath()
					+ "/"
					+ StartMenu.APP_NAME
					+ "/wav/save-contact.wav");
			player.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		player.start();
		player.setLooping(false);
	}

	private class PlaySearchCommandMenu extends AsyncTask<URL, Integer, String> {

		@Override
		protected String doInBackground(URL... params) {

			try {
				playCommandsMenu();

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			searchRecognizerControl(false);
		}

	}

}
