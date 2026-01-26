package ace.actually.radios;

/// @param message  the message
/// @param strength from 0.0 to 1.0, how clear is the signal
public record RadioSignal(String message, double strength) {
}
