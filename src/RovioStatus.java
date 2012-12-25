import georegression.struct.point.Point2D_I32;

/**
 * Information of the state of the Rovio robot
 *
 * @author Peter Abeles
 */
public class RovioStatus {
	public boolean cameraState;
	public int modemState;
	public int PPoE;
	public int bright;
	public int contrast;
	public int resolution;
	public int compressionRatio;
	public int privilege;
	public int pictureIndex;
	public int emailState;
	public boolean userCheck;
	public int imageFileLength;
	public Point2D_I32 monitorRect[];
	public int ftpState;
	public int saturation;
	public int motionDetectedIndex;
	public int hue;
	public int sharpness;
	public int motionDetectWay;
	public int sensorFrequency;
	public int channelMode;
	public int channelValue;
	public int audioVolume;
	public int dynamicDNS;
	public int audioState;
	public int frameRate;
	public int speakerVolume;
	public int micVolume;
	public boolean showTime;
	public int wifiStrength;
	public int batteryLevel;

	public RovioStatus() {
		monitorRect = new Point2D_I32[4];
		for( int i = 0; i < 4; i++ )
			monitorRect[i] = new Point2D_I32();
	}

	public void parse( byte data[] ) {
		// Probably several of these are parsed incorrectly since I only use a few of the values

		cameraState = parseInt(data[1]) == 1;
		modemState = parseInt(data[3]);
		PPoE = parseInt(data,4,2);
		bright = parseInt(data,15,3);
		contrast = parseInt(data,18,3);
		resolution = parseInt(data[21]);
		compressionRatio = parseInt(data[22]);
		privilege = parseInt(data[23]);
		pictureIndex = parseHex(data,24,5);
		emailState = parseInt(data[30]);
		userCheck = parseInt(data[31]) != 0;
		imageFileLength = parseInt(data,32,3);
		monitorRect[0].set(parseInt(data, 40,2), parseInt(data, 42,2));
		monitorRect[1].set(parseInt(data, 44,2), parseInt(data, 46,2));
		monitorRect[2].set(parseInt(data, 48,2), parseInt(data, 50,2));
		monitorRect[3].set(parseInt(data, 52,2), parseInt(data, 54,2));
		ftpState = parseInt(data[56]);
		saturation = parseInt(data[57]);
		motionDetectedIndex = -1;
		hue = parseHex(data,66,3);
		sharpness = parseHex(data,69,3);
		motionDetectWay = parseInt(data[72]);
		sensorFrequency = parseInt(data[73]);
		channelMode = parseInt(data[74]);
		channelValue = parseHex(data,75,2);
		audioVolume = parseHex(data,77,3);
		dynamicDNS = parseInt(data[80]);
		audioState = parseInt(data[81]);
		frameRate = parseHex(data,82,3);
		speakerVolume = parseHex(data,85,3);
		micVolume = parseHex(data,88,3);
		showTime = parseInt(data[91]) != 0;
		wifiStrength = parseHex(data[92]);
		batteryLevel = parseHex(data,93,2);
	}

	public static int parseInt( byte value ) {
		return Integer.parseInt(Character.toString((char)value));
	}

	public static int parseHex( byte value ) {
		return Integer.parseInt(Character.toString((char)value),16);
	}

	public static int parseInt( byte data[] , int index , int length ) {
		String s = new String(data,index,length);
		return Integer.parseInt(s.replaceAll("\\s",""));
	}

	public static int parseHex( byte data[] , int index , int length ) {
		String s = new String(data,index,length).replaceAll("\\s","");
		return Integer.parseInt(s,16);
	}
}
