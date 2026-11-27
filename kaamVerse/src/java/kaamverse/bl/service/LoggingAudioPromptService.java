package kaamverse.bl.service;

import java.util.logging.Logger;

/** Stub that only logs the prompt. */
public class LoggingAudioPromptService implements AudioPromptService {

    private static final Logger LOG = Logger.getLogger(LoggingAudioPromptService.class.getName());

    @Override
    public void play(String promptText) {
        LOG.info(() -> "Audio prompt: " + promptText);
    }
}
