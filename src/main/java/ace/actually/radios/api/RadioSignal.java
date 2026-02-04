package ace.actually.radios.api;

import java.util.Random;

/// @param message  the message
/// @param strength from 0.0 to 1.0, how clear is the signal
public record RadioSignal(String message, double strength) {

    public static RadioSignal scrambled(String originalMessage, double strength) {
        return new RadioSignal(generateGarbledString(originalMessage.length()), strength);
    }

    private static String generateGarbledString(int length) {
        StringBuilder out = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            out.append(Character.toChars(random.nextInt(33, 127)));
        }
        return out.toString();
    }
}
