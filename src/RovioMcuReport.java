/**
 * @author Peter Abeles
 */
public class RovioMcuReport {
 	int leftTicks;
	int rightTicks;
	int rearTicks;
	int headPosition;
	int batteryFull;
	boolean batteryVeryLow;
	boolean shutdownLevel;
	boolean headLight;
	boolean irPower;
	boolean irDetectBarrier;
	int chargeStatus;

	public void parse( byte data[] ) {
		leftTicks = RovioStatus.parseHex(data,6,2);
		if( RovioStatus.parseInt(data,4,2) == 1)
			leftTicks *= -1;
		rightTicks = RovioStatus.parseHex(data,12,2);
		if( RovioStatus.parseInt(data,10,2) == 1)
			rightTicks *= -1;
		rearTicks = RovioStatus.parseHex(data,18,2);
		if( RovioStatus.parseInt(data,16,2) == 1)
			rearTicks *= -1;
		headPosition = RovioStatus.parseHex(data,24,2);

		int value0 = RovioStatus.parseHex(data,26,2);
		int value1 = RovioStatus.parseHex(data,28,2);

		batteryFull = value0 >= 0x7F ? value0 - 0x7E : 0;
		batteryVeryLow = value0 == 0x6A;
		shutdownLevel = value0 == 0x64;

		headLight = (value1 & 0x01) != 0;
		irPower = (value1 & 0x02) != 0;
		irDetectBarrier = (value1 & 0x04) != 0;
		chargeStatus = ((value1 & 0x33) >> 3);
	}
}
