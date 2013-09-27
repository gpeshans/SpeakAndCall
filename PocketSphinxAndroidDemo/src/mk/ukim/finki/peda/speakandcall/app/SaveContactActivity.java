package mk.ukim.finki.peda.speakandcall.app;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import mk.ukim.finki.peda.speakandcall.R;
import mk.ukim.finki.peda.speakandcall.RecognitionListener;
import mk.ukim.finki.peda.speakandcall.RecognitionListenerNames;
import mk.ukim.finki.peda.speakandcall.RecognizerTask;
import mk.ukim.finki.peda.speakandcall.RecognizerTaskNames;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class SaveContactActivity extends Activity implements
		RecognitionListenerNames, RecognitionListener {
	static {
		System.loadLibrary("pocketsphinx_jni");
	}

	public static final String APP_NAME = "SpeakAndCall";
	public static final String NAMES_MODEL = "names";
	public static final String DIGITS_MODEL = "digits";

	/**
	 * Recognizer task, which runs in a worker thread.
	 */
	RecognizerTaskNames names_rec;
	RecognizerTask digits_rec;
	/**
	 * Thread in which the recognizer task runs.
	 */
	Thread names_rec_thread;
	Thread digits_rec_thread;
	/**
	 * Time at which current recognition started.
	 */
	Date names_start_date;
	Date digits_start_date;
	/**
	 * Number of seconds of speech.
	 */
	float names_speech_dur;
	float digits_speech_dur;
	/**
	 * Are we listening?
	 */
	boolean names_listening;
	boolean digits_listening;
	/**
	 * Progress dialog for final recognition.
	 */
	ProgressDialog names_rec_dialog;
	ProgressDialog digits_rec_dialog;
	/**
	 * Editable text view.
	 */
	EditText names_edit_text;
	EditText digits_edit_text;

	TextView result_text;

	Button names_btnSpeak;
	Button digits_btnSpeak;

	Boolean names_flagSpeak;
	Boolean digits_flagSpeak;

	public static HashMap<String, String> names;
	public static HashMap<String, String> digits;

	/**
	 * Performance counter view.
	 */
	TextView performance_text;

	MediaPlayer digitsPlayer;
	MediaPlayer namesPlayer;
	MediaPlayer beepPlayer;

	String telephoneNumber = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.save_contact_layout);

		fillDigits();
		fillNames();

		this.performance_text = (TextView) findViewById(R.id.SavePerformanceLabel);

		this.digits_start_date = new Date();
		this.digits_rec = new RecognizerTask();
		this.digits_listening = false;
		this.digits_flagSpeak = false;
		this.digits_edit_text = (EditText) findViewById(R.id.SaveContactEditTel);
		this.digits_rec.setRecognitionListener(this);
		this.digits_rec_thread = new Thread(this.digits_rec);
		this.digits_rec_thread.start();

		this.names_start_date = new Date();
		this.names_rec = new RecognizerTaskNames();
		this.names_listening = false;
		this.names_flagSpeak = false;
		this.names_edit_text = (EditText) findViewById(R.id.SaveContactEditName);
		this.names_rec.setRecognitionListenerNames(this);
		this.names_rec_thread = new Thread(this.names_rec);
		this.names_rec_thread.start();

		playDigitsMenu("phone-number.wav");
	}

	protected void onStop() {
		super.onStop();
		digitsRecognizerControl(true);
		namesRecognizerControl(true);
		digitsPlayer.stop();
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

	public void playDigitsMenu(String wavFile) {
		digitsPlayer = new MediaPlayer();
		digitsPlayer.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer mp) {
				digitsRecognizerControl(false);
			}
		});

		try {
			digitsPlayer.setDataSource(Environment
					.getExternalStorageDirectory().getPath()
					+ "/"
					+ APP_NAME
					+ "/wav/" + wavFile);
			digitsPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		digitsPlayer.start();
		digitsPlayer.setLooping(false);
	}

	public void playNamesMenu(String wavFile) {
		namesPlayer = new MediaPlayer();
		namesPlayer.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer mp) {
				namesRecognizerControl(false);
			}
		});

		try {
			namesPlayer.setDataSource(Environment.getExternalStorageDirectory()
					.getPath() + "/" + APP_NAME + "/wav/" + wavFile);
			namesPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		namesPlayer.start();
		namesPlayer.setLooping(false);
	}

	public void playBeep(String wavFile) {
		beepPlayer = new MediaPlayer();

		try {
			beepPlayer.setDataSource(Environment.getExternalStorageDirectory()
					.getPath() + "/" + APP_NAME + "/wav/" + wavFile);
			beepPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		beepPlayer.start();
		digitsPlayer.setLooping(false);
	}

	public void digitsRecognizerControl(boolean flagSpeak) {

		if (!flagSpeak) {
			digits_start_date = new Date();
			digits_listening = true;

			flagSpeak = true;
			digits_edit_text.setText("");

			digits_rec.start();
		} else {

			Date end_date = new Date();
			long nmsec = end_date.getTime() - digits_start_date.getTime();
			digits_speech_dur = (float) nmsec / 1000;
			if (digits_listening) {
				Log.d(getClass().getName(), "Showing Dialog");
				digits_listening = false;
				flagSpeak = false;
			}
			digits_rec.stop();
		}
	}

	public void namesRecognizerControl(boolean flagSpeak) {

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

	public void savePhoneNumber(String number) {
		telephoneNumber = number;
		playBeep("beep.wav");
		playNamesMenu("name.wav");
	}

	public void saveContact(String cName, String cNumber) {
		try {
			Intent addPersonIntent = new Intent(Intent.ACTION_INSERT);
			addPersonIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);

			addPersonIntent.putExtra(ContactsContract.Intents.Insert.NAME,
					cName);
			addPersonIntent.putExtra(ContactsContract.Intents.Insert.PHONE,
					cNumber);

			startActivityForResult(addPersonIntent, 1);
		} catch (Exception ex) {
			Log.e(APP_NAME + ":", "Insert contact error!");
		}
	}

	private void addContact(String name, String phone) {
		try {
			ContentValues values = new ContentValues();
			values.put(People.NUMBER, phone);
			values.put(People.TYPE, Phone.TYPE_CUSTOM);
			values.put(People.LABEL, name);
			values.put(People.NAME, name);
			Uri dataUri = getContentResolver().insert(People.CONTENT_URI,
					values);
			Uri updateUri = Uri.withAppendedPath(dataUri,
					People.Phones.CONTENT_DIRECTORY);
			values.clear();
			values.put(People.Phones.TYPE, People.TYPE_MOBILE);
			values.put(People.NUMBER, phone);
			updateUri = getContentResolver().insert(updateUri, values);
		} catch (Exception ex) {
			Log.e(APP_NAME + ":", "Insert contact error!");
		}
		playBeep("beep.wav");
	}

	public void checkName(String name) {

		if (name != null && names.containsValue(name)) {
			namesRecognizerControl(true);
		}
	}

	@Override
	public void onPartialResults(Bundle b) {
		final SaveContactActivity that = this;
		final String hyp = b.getString("hyp");

		that.digits_edit_text.post(new Runnable() {
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

					that.digits_edit_text.setText(finalRes);
					if (results.length >= 9)
						digitsRecognizerControl(true);
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

			final SaveContactActivity that = this;
			this.digits_edit_text.post(new Runnable() {
				public void run() {

					that.digits_edit_text.setText(hyp);
					Date end_date = new Date();
					long nmsec = end_date.getTime()
							- that.digits_start_date.getTime();
					float rec_dur = (float) nmsec / 1000;
					that.performance_text.setText(String.format(
							"%.2f секунди %.2f xRT", that.digits_speech_dur,
							rec_dur / that.digits_speech_dur));
					Log.d(getClass().getName(), "Hiding Dialog");

					savePhoneNumber(hyp);
				}
			});
		}
	}

	@Override
	public void onError(int err) {
		final SaveContactActivity that = this;
		that.digits_edit_text.post(new Runnable() {
			public void run() {
			}
		});
	}

	@Override
	public void onPartialResultsNames(Bundle b) {
		final SaveContactActivity that = this;
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
					if (results.length > 0) {
						checkName(results[results.length - 1]);
					}
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

			final SaveContactActivity that = this;
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

					String contactName = hyp.split(" ")[hyp.split(" ").length - 1];
					addContact(contactName, telephoneNumber);

				}
			});
		}
	}

	@Override
	public void onErrorNames(int err) {
		final SaveContactActivity that = this;
		that.names_edit_text.post(new Runnable() {
			public void run() {

			}
		});

	}

}
