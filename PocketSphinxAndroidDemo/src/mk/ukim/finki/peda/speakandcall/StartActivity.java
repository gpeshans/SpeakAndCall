package mk.ukim.finki.peda.speakandcall;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StartActivity extends FragmentActivity implements
		ActionBar.TabListener {

	public static final String APP_NAME = "SpeakAndCall";
	public static final String NAMES_MODEL = "names";
	public static final String DIGITS_MODEL = "digits";
	

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the three primary sections of the app. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	AppSectionsPagerAdapter mAppSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will display the three primary sections of the
	 * app, one at a time.
	 */
	ViewPager mViewPager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		File f = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/" + APP_NAME + "/model");
		if (!f.isDirectory())
			copyAssets();

		// Create the adapter that will return a fragment for each of the three
		// primary sections
		// of the app.
		mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();

		// Specify that we will be displaying tabs in the action bar.
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Set up the ViewPager, attaching the adapter and setting up a listener
		// for when the
		// user swipes between sections.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mAppSectionsPagerAdapter);
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						// When swiping between different app sections, select
						// the corresponding tab.
						// We can also use ActionBar.Tab#select() to do this if
						// we have a reference to the
						// Tab.
						actionBar.setSelectedNavigationItem(position);
					}
				});

		actionBar.addTab(actionBar.newTab().setText("Телефонирај")
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Зачувај")
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Пребарај")
				.setTabListener(this));

		/*
		 * // For each of the sections in the app, add a tab to the action bar.
		 * for (int i = 2; i < mAppSectionsPagerAdapter.getCount(); i++) { //
		 * Create a tab with text corresponding to the page title defined by //
		 * the adapter. // Also specify this Activity object, which implements
		 * the // TabListener interface, as the // listener for when this tab is
		 * selected. actionBar.addTab(actionBar.newTab()
		 * .setText(mAppSectionsPagerAdapter.getPageTitle(i))
		 * .setTabListener(this)); }
		 */
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

		AssetManager assetManager = getAssets();
		String[] digitsFiles = { "broevi.dic", "broevi.lm.DMP", "feat.params",
				"mdef", "means", "mixture_weights", "noisedict",
				"transition_matrices", "variances" };

		String[] namesFiles = { "digitsNames.dic", "digitsNames.lm.DMP", "feat.params",
				"mdef", "means", "mixture_weights", "noisedict",
				"transition_matrices", "variances" };

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
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		clearResources();
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the primary sections of the app.
	 */
	public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

		public AppSectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0:
				// The first section of the app is the most interesting -- it
				// offers
				// a launchpad into the other demonstrations in this example
				// application.
				return new LaunchpadSectionFragment();
			case 1:
				return new NamesFragment();
			case 2: 
				return new SearchNumberFragment();

			default:
				// The other sections of the app are dummy placeholders.
				Fragment fragment = new DummySectionFragment();
				Bundle args = new Bundle();
				args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
				fragment.setArguments(args);
				return fragment;
			}
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "Section " + (position + 1);
		}
	}

	/**
	 * A fragment that launches other parts of the demo application.
	 */
	public static class LaunchpadSectionFragment extends Fragment implements
			RecognitionListener {
		static {
			System.loadLibrary("pocketsphinx_jni");
		}

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

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_section_launchpad, container, false);

			this.btnSpeak = (Button) rootView.findViewById(R.id.BtnSpeak);
			this.btnSpeak.setOnClickListener(btnSpeakListener);

			fillDigits();

			this.start_date = new Date();

			this.rec = new RecognizerTask();

			this.listening = false;
			flagSpeak = false;

			this.performance_text = (TextView) rootView
					.findViewById(R.id.PerformanceText);

			this.edit_text = (EditText) rootView.findViewById(R.id.ResultEdit);

			this.rec.setRecognitionListener(this);

			this.rec_thread = new Thread(this.rec);

			this.rec_thread.start();

			return rootView;
		}

		public static HashMap<String, String> digits;

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
		public void onStop() {
			super.onStop();
			rec.stop();
		}

		public void onPause() {
			super.onPause();
			rec.stop();
		};

		OnClickListener btnSpeakListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

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
						rec_dialog = ProgressDialog.show(
								getView().getContext(), "", "Процесира...",
								true);
						rec_dialog.setCancelable(false);
						listening = false;
						flagSpeak = false;
					}
					rec.stop();

				}
			}
		};

		@Override
		public void onPartialResults(Bundle b) {
			final LaunchpadSectionFragment that = this;
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
			}

			String results[] = tempRes.split(" ");

			final String hyp;
			String telNum = "";

			for (int i = 0; i < results.length; i++) {
				if (digits.get(results[i]) != null)
					telNum += digits.get(results[i]);
			}

			hyp = telNum;

			final LaunchpadSectionFragment that = this;
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

		@Override
		public void onError(int err) {
			final LaunchpadSectionFragment that = this;
			that.edit_text.post(new Runnable() {
				public void run() {
					that.rec_dialog.dismiss();
				}
			});

		}

		public void callAction(String telNumber) {

			if (telNumber != null) {
								
				rec.stop();
				flagSpeak = false;

				String speechResult = telNumber.replace(" ", "");

				Toast.makeText(getActivity().getBaseContext(), speechResult,
						Toast.LENGTH_LONG).show();

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
	}

	public static class NamesFragment extends Fragment implements
			RecognitionListener {
		static {
			System.loadLibrary("pocketsphinx_jni");
		}

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
		 * Performance counter view.
		 */
		TextView performance_text;
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

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_names,
					container, false);

			/*this.names_btnSpeak = (Button) rootView
					.findViewById(R.id.BtnSpeak1);
			this.names_btnSpeak.setOnClickListener(btnSpeakListener);

			fillDigits();

			this.names_start_date = new Date();

			this.names_rec = new RecognizerTaskNames();

			this.names_listening = false;
			names_flagSpeak = false;

			this.performance_text = (TextView) rootView
					.findViewById(R.id.PerformanceText1);

			this.names_edit_text = (EditText) rootView
					.findViewById(R.id.ResultEdit1);

			this.names_rec.setRecognitionListenerNames(this);

			this.names_rec_thread = new Thread(this.names_rec);

			this.names_rec_thread.start();
*/
			return rootView;
		}

		public static HashMap<String, String> names;

		public static void fillDigits() {

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
			names.put("NULA", "0");
			names.put("EDEN", "1");
			names.put("DVA", "2");
			names.put("TRI", "3");
			names.put("CHETIRI", "4");
			names.put("PET", "5");
			names.put("SHEST", "6");
			names.put("SEDUM", "7");
			names.put("OSUM", "8");
			names.put("DEVET", "9");
		}

		@Override
		public void onStop() {
			super.onStop();
			names_rec.stop();
		}

		public void onPause() {
			super.onPause();
			names_rec.stop();
		};

		OnClickListener btnSpeakListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!names_flagSpeak) {
					names_start_date = new Date();
					names_listening = true;

					names_flagSpeak = true;
					names_edit_text.setText("");

					names_rec.start();
				} else {

					Date end_date = new Date();
					long nmsec = end_date.getTime()
							- names_start_date.getTime();
					names_speech_dur = (float) nmsec / 1000;
					if (names_listening) {
						Log.d(getClass().getName(), "Showing Dialog");
						names_rec_dialog = ProgressDialog.show(getView()
								.getContext(), "", "Процесира...", true);
						names_rec_dialog.setCancelable(false);
						names_listening = false;
						names_flagSpeak = false;
					}
					names_rec.stop();

				}
			}
		};

		@Override
		public void onPartialResults(Bundle b) {
			final NamesFragment that = this;
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
			}

			String results[] = tempRes.split(" ");

			final String hyp;
			String namesRcognized = "";

			for (int i = 0; i < results.length; i++) {
				if (names.get(results[i]) != null)
					namesRcognized += names.get(results[i]) + " ";
			}

			hyp = namesRcognized;

			final NamesFragment that = this;
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
				}
			});

			String lastName = hyp.split(" ")[hyp.split(" ").length - 1];
			insertContact(lastName, "077872222");			

		}		

		@Override
		public void onError(int err) {
			final NamesFragment that = this;
			that.names_edit_text.post(new Runnable() {
				public void run() {
					that.names_rec_dialog.dismiss();
				}
			});

		}

		public void insertContact(String name, String number) {
			try {
				Intent addPersonIntent = new Intent(Intent.ACTION_INSERT);
				addPersonIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);

				addPersonIntent.putExtra(ContactsContract.Intents.Insert.NAME,
						name);
				addPersonIntent.putExtra(ContactsContract.Intents.Insert.PHONE,
						number);

				startActivity(addPersonIntent);
			} catch (Exception ex) {
				Log.e(APP_NAME + ":", "Insert contact error!");
			}
		}			

	}

	public static class SearchNumberFragment extends Fragment implements
			RecognitionListener {
		static {
			System.loadLibrary("pocketsphinx_jni");
		}

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
		 * Performance counter view.
		 */
		TextView performance_text;
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

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_search,
					container, false);

			/*this.names_btnSpeak = (Button) rootView
					.findViewById(R.id.BtnSpeak2);
			this.names_btnSpeak.setOnClickListener(btnSpeakListener);

			fillDigits();

			this.names_start_date = new Date();

			this.names_rec = new RecognizerTaskNames();

			this.names_listening = false;
			names_flagSpeak = false;

			this.performance_text = (TextView) rootView
					.findViewById(R.id.PerformanceText1);

			this.names_edit_text = (EditText) rootView
					.findViewById(R.id.ResultEdit2);

			this.names_rec.setRecognitionListenerNames(this);

			this.names_rec_thread = new Thread(this.names_rec);

			this.names_rec_thread.start();
*/
			return rootView;
		}

		public static HashMap<String, String> names;

		public static void fillDigits() {

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

		@Override
		public void onStop() {
			super.onStop();
			names_rec.stop();
		}

		public void onPause() {
			super.onPause();
			names_rec.stop();
		};

		OnClickListener btnSpeakListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!names_flagSpeak) {
					names_start_date = new Date();
					names_listening = true;

					names_flagSpeak = true;
					names_edit_text.setText("");

					names_rec.start();
				} else {

					Date end_date = new Date();
					long nmsec = end_date.getTime()
							- names_start_date.getTime();
					names_speech_dur = (float) nmsec / 1000;
					if (names_listening) {
						Log.d(getClass().getName(), "Showing Dialog");
						names_rec_dialog = ProgressDialog.show(getView()
								.getContext(), "", "Процесира...", true);
						names_rec_dialog.setCancelable(false);
						names_listening = false;
						names_flagSpeak = false;
					}
					names_rec.stop();

				}
			}
		};

		@Override
		public void onPartialResults(Bundle b) {
			final SearchNumberFragment that = this;
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
			}

			String results[] = tempRes.split(" ");

			final String hyp;
			String namesRcognized = "";

			for (int i = 0; i < results.length; i++) {
				if (names.get(results[i]) != null)
					namesRcognized += names.get(results[i]) + " ";
			}

			hyp = namesRcognized;

			final SearchNumberFragment that = this;
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
				}
			});

			String lastName = hyp.split(" ")[hyp.split(" ").length - 1];			
			getPhoneNumber(lastName);

		}	

		@Override
		public void onError(int err) {
			final SearchNumberFragment that = this;
			that.names_edit_text.post(new Runnable() {
				public void run() {
					that.names_rec_dialog.dismiss();
				}
			});

		}

		public void getPhoneNumber(String name) {

			if (name != null) {

				names_rec.stop();
				names_flagSpeak = false;

				Context context = getActivity().getBaseContext();

				String phoneNumber = null;
				String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
						+ " like'%" + name + "%'";
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

			}
		}

	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {

		public static final String ARG_SECTION_NUMBER = "section_number";

		Button btnInsert;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_section_dummy,
					container, false);
			Bundle args = getArguments();
			((TextView) rootView.findViewById(android.R.id.text1))
					.setText(getString(R.string.dummy_section_text,
							args.getInt(ARG_SECTION_NUMBER)));

			btnInsert = (Button) rootView.findViewById(R.id.BtnInsert);
			btnInsert.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						Intent addPersonIntent = new Intent(
								Intent.ACTION_INSERT);
						addPersonIntent
								.setType(ContactsContract.Contacts.CONTENT_TYPE);

						addPersonIntent.putExtra(
								ContactsContract.Intents.Insert.NAME,
								"Goran Peshaski");
						addPersonIntent.putExtra(
								ContactsContract.Intents.Insert.PHONE,
								"077872222");

						startActivityForResult(addPersonIntent, 1);
					} catch (Exception ex) {
						Log.e(APP_NAME + ":", "Insert contact error!");
					}
				}
			});

			return rootView;
		}
	}

}
