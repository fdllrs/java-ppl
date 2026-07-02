package messaging;

public sealed interface Message permits Sample, Observe, Done, Fork { }

