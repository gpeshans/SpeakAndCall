package mk.ukim.finki.peda.speakandcall;

import android.os.Bundle;

public interface RecognitionListenerNames {
	/**
	 * Called on the recognition thread when partial results are available.
	 * 
	 * Note: This is not like android.speech.RecognitionListener in that it does
	 * not get called on the main thread.
	 * 
	 * @param b
	 *            Bundle containing the partial result string under the "hyp"
	 *            key.
	 */
	abstract void onPartialResultsNames(Bundle b);

	/**
	 * Called when final results are available.
	 * 
	 * Note: This is not like android.speech.RecognitionListener in that it does
	 * not get called on the main thread.
	 * 
	 * @param b
	 *            Bundle containing the final result string under the "hyp" key.
	 */
	abstract void onResultsNames(Bundle b);

	/**
	 * Called if a recognition error occurred.
	 * 
	 * Note: This will only ever be passed -1 for the moment, which corresponds
	 * to a recognition failure (null result).
	 * 
	 * @param err
	 *            Code representing the error that occurred.
	 */
	abstract void onErrorNames(int err);
}
