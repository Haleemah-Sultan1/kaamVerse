package kaamverse.bl.service;

import java.nio.file.Path;

/** GRASP Protected Variations: hides the transcription provider. */
public interface SpeechToText {

    String transcribe(Path audio);
}
