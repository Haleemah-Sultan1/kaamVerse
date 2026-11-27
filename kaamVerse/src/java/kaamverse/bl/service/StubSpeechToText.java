package kaamverse.bl.service;

import java.nio.file.Path;

/** Placeholder until a real speech engine is added. */
public class StubSpeechToText implements SpeechToText {

    @Override
    public String transcribe(Path audio) {
        return "[transcript not available: speech-to-text stub]";
    }
}
