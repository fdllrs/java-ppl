package messaging;

public class Return implements Message {
	Object returnValue;

	public Return(Object returnValue) {
		this.returnValue = returnValue;
	}

	public Object getReturnValue() {
		return returnValue;
	}
}
