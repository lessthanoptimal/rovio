/**
 * Manual controls for Rovio
 *
 * @author Peter Abeles
 */
public enum RovioManual {

	STOP(0),
	FORWARD(1),
	BACKWARD(2),
	STRAIGHT_LEFT(3),
	STRAIGHT_RIGHT(4),
	ROTATE_LEFT_SPEED(5),
	ROTATE_RIGHT_SPEED(6),
	DIAGONAL_FORWARD_LEFT(7),
	DIAGONAL_FORWARD_RIGHT(8),
	DIAGONAL_BACKWARD_LEFT(9),
	DIAGONAL_BACKWARD_RIGHT(10),
	HEAD_UP(11),
	HEAD_DOWN(12),
	HEAD_MIDDLE(13),
	ROTATE_LEFT_20(17),
	ROTATE_RIGHT_20(18);

	private RovioManual(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	int value;
}
