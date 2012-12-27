/**
 * List of high-level actions that the Rovio can perform
 *
 * @author Peter Abeles
 */
public enum RovioActions {

	GO_HOME(12),
	GO_HOME_AND_DOCK(13),
	UPDATE_HOME_POSITION(14),
	SET_TUNING_PARAMETERS(15);

	private RovioActions(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	int value;
}
