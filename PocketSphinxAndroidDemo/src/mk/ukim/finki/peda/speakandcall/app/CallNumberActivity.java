package mk.ukim.finki.peda.speakandcall.app;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import mk.ukim.finki.peda.speakandcall.R;
import mk.ukim.finki.peda.speakandcall.RecognitionListener;
import mk.ukim.finki.peda.speakandcall.RecognizerTask;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CallNumberActivity extends Activity implements RecognitionListener {
	static {
		System.loadLibrary("pocketsphinx_jni");
	}

	public static final String APP_NAME = "SpeakAndCall";
	public static final String NAMES_MODEL = "names";
	public static final String DIGITS_MODEL = "digits";

	/**
	 * Recognizer task, which runs in a worker thread.
	 */
	RecognizerTask rec;
	/**
	 * Thread in which the recognizer task runs.
	 */
	Thread rec_thread;
	/**
	 * Time at which current recognition started.
	 */
	Date start_date;
	/**
	 * Number of seconds of speech.
	 */
	float speech_dur;
	/**
	 * Are we listening?
	 */
	boolean listening;
	/**
	 * Progress dialog for final recognition.
	 */
	ProgressDialog rec_dialog;
	/**
	 * Performance counter view.
	 */
	TextView performance_text;
	/**
	 * Editable text view.
	 */
	EditText edit_text;

	TextView result_text;

	Button btnSpeak;

	Boolean flagSpeak;

	MediaPlayer player;

	public static HashMap<String, String> digits;

	/**
	 * Respond to touch events on the Speak button.
	 * 
	 * This allows the Speak button to function as a "push and hold" button, by
	 * triggering the start of recognition when it is first pushed, and the end
	 * of recognition when it is released.
	 * 
	 * @param v
	 *            View on which this event is called
	 * @param event
	 *            Event that was triggered.
	 */

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.call_layout);

		fillDigits();

		this.start_date = new Date();

		this.rec = new RecognizerTask();

		this.listening = false;

		flagSpeak = false;

		this.performance_text = (TextView) findViewById(R.id.CallPerformanceLabel);

		this.edit_text = (EditText) findViewById(R.id.CallEdit);

		this.rec.setRecognitionListener(this);

		this.rec_thread = new Thread(this.rec);

		this.rec_thread.start();

		new PlayCommandMenu().execute();
	}

	protected void onStop() {
		super.onStop();
		RecognizerControl(true);
		player.stop();
	}

	public void playCommandsMenu() {
		player = new MediaPlayer();

		try {
			player.setDataSource(Environment.getExternalStorageDirectory()
					.getPath() + "/" + APP_NAME + "/wav/save-contact.wav");
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

	public void RecognizerControl(boolean flagSpeak) {

		if (!flagSpeak) {
			start_date = new Date();
			listening = true;

			flagSpeak = true;
			edit_text.setText("");

			rec.start();
		} else {

			Date end_date = new Date();
			long nmsec = end_date.getTime() - start_date.getTime();
			speech_dur = (float) nmsec / 1000;
			if (listening) {
				Log.d(getClass().getName(), "Showing Dialog");
				/*
				 * rec_dialog = ProgressDialog.show(getParent(), "",
				 * "Процесира...", true); rec_dialog.setCancelable(false);
				 */
				listening = false;
				flagSpeak = false;
			}
			rec.stop();

		}
	}

	public static void fillDigits() {

		digits = new HashMap<String, String>();

		digits.put("NULA", "0");
		digits.put("EDEN", "1");
		digits.put("DVA", "2");
		digits.put("TRI", "3");
		digits.put("CHETIRI", "4");
		digits.put("PET", "5");
		digits.put("SHEST", "6");
		digits.put("SEDUM", "7");
		digits.put("OSUM", "8");
		digits.put("DEVET", "9");
	}

	@Override
	public void onPartialResults(Bundle b) {
		final CallNumberActivity that = this;
		final String hyp = b.getString("hyp");

		that.edit_text.post(new Runnable() {
			public void run() {

				if (hyp != null) {
					String tempRes = hyp;

					// removes words shorter than 3 letters
					tempRes = tempRes.replaceAll("\\b[\\w']{1,2}\\b", "");
					tempRes = tempRes.replaceAll("\\s{2,}", " ");

					String results[] = tempRes.split(" ");
					String finalRes = "";
					for (int i = 0; i < results.length; i++) {
						results[i] = digits.get(results[i]);
						if (results[i] != null)
							finalRes += results[i];
					}

					that.edit_text.setText(finalRes);
					if (results.length >= 9)
						RecognizerControl(true);
				}
			}
		});

	}

	@Override
	public void onResults(Bundle b) {
		String tempRes = b.getString("hyp");

		if (tempRes != null) {

			// removes words shorter than 3 letters tempRes =
			tempRes.replaceAll("\\b[\\w']{1,2}\\b", "");
			tempRes = tempRes.replaceAll("\\s{2,}", " ");

			String results[] = tempRes.split(" ");

			final String hyp;
			String telNum = "";

			for (int i = 0; i < results.length; i++) {
				if (digits.get(results[i]) != null)
					telNum += digits.get(results[i]);
			}

			hyp = telNum;

			final CallNumberActivity that = this;
			this.edit_text.post(new Runnable() {
				public void run() {

					that.edit_text.setText(hyp);
					Date end_date = new Date();
					long nmsec = end_date.getTime() - that.start_date.getTime();
					float rec_dur = (float) nmsec / 1000;
					that.performance_text.setText(String.format(
							"%.2f секунди %.2f xRT", that.speech_dur, rec_dur
									/ that.speech_dur));
					Log.d(getClass().getName(), "Hiding Dialog");

					if (that.rec_dialog != null)
						that.rec_dialog.dismiss();

					callAction(hyp);

				}
			});
		}

	}

	public void callAction(String telNumber) {

		if (telNumber != null) {

			rec.stop();
			flagSpeak = false;

			String speechResult = telNumber.replace(" ", "");

			Toast.makeText(getBaseContext(), speechResult, Toast.LENGTH_LONG)
					.show();

			try {
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse("tel:" + speechResult));
				startActivity(callIntent);
			} catch (ActivityNotFoundException activityException) {
				Log.e("Calling a Phone Number", "Call failed",
						activityException);
			}

		}

	}

	@Override
	public void onError(int err) {
		final CallNumberActivity that = this;
		that.edit_text.post(new Runnable() {
			public void run() {

			}
		});

	}

	private class PlayCommandMenu extends AsyncTask<URL, Integer, String> {

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
			RecognizerControl(false);
		}

	}

}
