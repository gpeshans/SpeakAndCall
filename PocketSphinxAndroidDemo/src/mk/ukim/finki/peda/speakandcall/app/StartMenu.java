package mk.ukim.finki.peda.speakandcall.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;

import mk.ukim.finki.peda.speakandcall.R;
import mk.ukim.finki.peda.speakandcall.RecognitionListener;
import mk.ukim.finki.peda.speakandcall.RecognizerTask;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class StartMenu extends Activity implements RecognitionListener {
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

	public static HashMap<String, String> digits;

	MediaPlayer player;

	boolean resumeFlag = false;

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
		setContentView(R.layout.start_menu_layout);

		File f = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/" + APP_NAME + "/model");
		if (!f.isDirectory())
			copyAssets();

		fillDigits();

		this.start_date = new Date();

		this.rec = new RecognizerTask();

		this.listening = false;
		flagSpeak = false;

		this.performance_text = (TextView) findViewById(R.id.PerformanceLabel);

		this.edit_text = (EditText) findViewById(R.id.StartMenuEdit);

		this.rec.setRecognitionListener(this);

		this.rec_thread = new Thread(this.rec);

		this.rec_thread.start();

		playCommandsMenu("menu.wav");

	}

	@Override
	protected void onPause() {
		super.onPause();
		RecognizerControl(true);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (resumeFlag) {
			this.rec = null;
			if (digits.isEmpty())
				fillDigits();

			this.start_date = new Date();

			this.rec = new RecognizerTask();

			this.listening = false;

			flagSpeak = false;

			this.performance_text = (TextView) findViewById(R.id.PerformanceLabel);

			this.edit_text = (EditText) findViewById(R.id.StartMenuEdit);

			this.rec.setRecognitionListener(this);

			this.rec_thread = new Thread(this.rec);

			this.rec_thread.start();

			resumeFlag = false;

			playCommandsMenu("choose-option.wav");
		}
	}

	public void playCommandsMenu(String fileName) {
		player = new MediaPlayer();
		player.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer mp) {
				RecognizerControl(false);
			}
		});

		try {
			player.setDataSource(Environment.getExternalStorageDirectory()
					.getPath() + "/" + APP_NAME + "/wav/" + fileName);
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

	private void copyAssets() {
		File digitsDir = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/" + APP_NAME + "/" + DIGITS_MODEL);

		digitsDir.mkdirs();

		File namesDir = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/" + APP_NAME + "/" + NAMES_MODEL);

		namesDir.mkdirs();

		File rawLogDir = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/" + APP_NAME + "/rawLogDir");
		rawLogDir.mkdirs();

		File wavDir = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/" + APP_NAME + "/wav");
		wavDir.mkdirs();

		AssetManager assetManager = getAssets();
		String[] digitsFiles = { "broevi.dic", "broevi.lm.DMP", "feat.params",
				"mdef", "means", "mixture_weights", "noisedict",
				"transition_matrices", "variances" };

		String[] namesFiles = { "names50.dic", "names50.lm.DMP", "feat.params",
				"mdef", "means", "mixture_weights", "noisedict",
				"transition_matrices", "variances" };

		String[] wavFiles = { "cell-phone-1-nr0.wav", "cell-phone-1-nr1.wav",
				"cell-phone-1-nr2.wav", "cell-phone-1-nr3.wav",
				"cell-phone-1-nr4.wav", "cell-phone-1-nr5.wav",
				"cell-phone-1-nr6.wav", "cell-phone-1-nr7.wav",
				"cell-phone-1-nr8.wav", "cell-phone-1-nr9.wav", "menu.wav",
				"phone-number.wav", "name.wav", "choose-option.wav", "beep.wav", "ringing.wav" };

		for (int i = 2; i < 9; i++) {
			String filenameDigits = "";
			InputStream inDigits = null;
			OutputStream outDigits = null;

			String filenameNames = "";
			InputStream inNames = null;
			OutputStream outNames = null;

			try {
				filenameDigits = digitsFiles[i];
				inDigits = assetManager.open(DIGITS_MODEL + "/"
						+ filenameDigits);
				outDigits = new FileOutputStream(Environment
						.getExternalStorageDirectory().getPath()
						+ "/"
						+ APP_NAME + "/" + DIGITS_MODEL + "/" + filenameDigits);
				copyFile(inDigits, outDigits);
				inDigits.close();
				inDigits = null;
				outDigits.flush();
				outDigits.close();
				outDigits = null;

				filenameNames = namesFiles[i];
				inNames = assetManager.open(NAMES_MODEL + "/" + filenameNames);
				outNames = new FileOutputStream(Environment
						.getExternalStorageDirectory().getPath()
						+ "/"
						+ APP_NAME + "/" + NAMES_MODEL + "/" + filenameNames);
				copyFile(inNames, outNames);
				inNames.close();
				inNames = null;
				outNames.flush();
				outNames.close();
				outNames = null;

			} catch (IOException e) {
				Log.e("tag", "Failed to copy asset file: " + filenameDigits
						+ " or " + filenameNames, e);
			}
		}

		for (int i = 0; i < 2; i++) {
			String filenameDigits = "";
			InputStream inDigits = null;
			OutputStream outDigits = null;

			String filenameNames = "";
			InputStream inNames = null;
			OutputStream outNames = null;

			try {
				filenameDigits = digitsFiles[i];
				inDigits = assetManager.open(filenameDigits);
				outDigits = new FileOutputStream(Environment
						.getExternalStorageDirectory().getPath()
						+ "/"
						+ APP_NAME + "/" + filenameDigits);
				copyFile(inDigits, outDigits);
				inDigits.close();
				inDigits = null;
				outDigits.flush();
				outDigits.close();
				outDigits = null;

				filenameNames = namesFiles[i];
				inNames = assetManager.open(filenameNames);
				outNames = new FileOutputStream(Environment
						.getExternalStorageDirectory().getPath()
						+ "/"
						+ APP_NAME + "/" + filenameNames);
				copyFile(inNames, outNames);
				inNames.close();
				inNames = null;
				outNames.flush();
				outNames.close();
				outNames = null;
			} catch (IOException e) {
				Log.e("tag", "Failed to copy asset file: " + filenameDigits
						+ " or " + filenameNames, e);
			}
		}

		for (int i = 0; i < 16; i++) {
			String filenameWav = "";
			InputStream inWav = null;
			OutputStream outWav = null;

			try {
				filenameWav = wavFiles[i];
				inWav = assetManager.open(filenameWav);
				outWav = new FileOutputStream(Environment
						.getExternalStorageDirectory().getPath()
						+ "/"
						+ APP_NAME + "/wav/" + filenameWav);
				copyFile(inWav, outWav);
				inWav.close();
				inWav = null;
				outWav.flush();
				outWav.close();
				outWav = null;

			} catch (IOException e) {
				Log.e("tag", "Failed to copy asset file: " + filenameWav
						+ " or " + filenameWav, e);
			}
		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	private void deleteRawLogDirFiles() {
		File rawLogDir = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/" + APP_NAME + "/rawLogDir");
		if (rawLogDir.isDirectory()) {
			String[] files = rawLogDir.list();
			for (String file : files) {
				File f = new File(Environment.getExternalStorageDirectory()
						.getPath() + "/" + APP_NAME + "/rawLogDir/" + file);
				f.delete();
			}
		}
	}

	private void deleteModel() {
		File modelDigits = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/" + APP_NAME + "/" + DIGITS_MODEL);
		if (modelDigits.isDirectory()) {
			String[] files = modelDigits.list();
			for (String file : files) {
				File f = new File(Environment.getExternalStorageDirectory()
						.getPath()
						+ "/"
						+ APP_NAME
						+ "/"
						+ DIGITS_MODEL
						+ "/"
						+ file);
				f.delete();
			}
		}

		File modelNames = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/" + APP_NAME + "/" + NAMES_MODEL);
		if (modelNames.isDirectory()) {
			String[] files = modelNames.list();
			for (String file : files) {
				File f = new File(Environment.getExternalStorageDirectory()
						.getPath()
						+ "/"
						+ APP_NAME
						+ "/"
						+ NAMES_MODEL
						+ "/"
						+ file);
				f.delete();
			}
		}
	}

	private void clearResources() {
		deleteRawLogDirFiles();
		deleteModel();

		File mainFolder = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/" + APP_NAME);
		if (mainFolder.isDirectory()) {
			String[] files = mainFolder.list();
			for (String file : files) {
				File f = new File(Environment.getExternalStorageDirectory()
						.getPath() + "/" + APP_NAME + "/" + file);
				f.delete();
			}
		}

		mainFolder.delete();
	}

	@Override
	protected void onStop() {
		super.onStop();
		deleteRawLogDirFiles();
		RecognizerControl(true);
		player.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		clearResources();
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
		final StartMenu that = this;
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
							finalRes += results[i] + " ";
					}

					that.edit_text.setText(finalRes);
					if (results.length > 0)
						if (results[results.length - 1] == "0"
								|| results[results.length - 1] == "1"
								|| results[results.length - 1] == "2"
								|| results[results.length - 1] == "3")
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
			String number = results[results.length - 1];

			hyp = digits.get(number);

			final StartMenu that = this;
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

					callActivity(hyp);

				}
			});
		}
	}

	public void callActivity(String number) {
		if (number == "0") {
			Intent intent = new Intent(this, CallNumberActivity.class);
			resumeFlag = true;
			startActivity(intent);
		} else if (number == "1") {
			Intent intent = new Intent(this, SaveContactActivity.class);
			resumeFlag = true;
			startActivity(intent);
		} else if (number == "2") {
			Intent intent = new Intent(this, SearchContactActivity.class);
			resumeFlag = true;
			startActivity(intent);
		} else if (number == "3") {
			// choose option
			playCommandsMenu("menu.wav");
		} else {
			RecognizerControl(false);
		}
	}

	@Override
	public void onError(int err) {
		final StartMenu that = this;
		that.edit_text.post(new Runnable() {
			public void run() {
				that.rec_dialog.dismiss();
			}
		});

	}

}
