import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;

/**
 * @author Peter Abeles
 */
public class RovioControl {

	long COOL_DOWN_PERIOD = 10;

	String addr;

	byte inputBuff[] = new byte[ 1024 ];
	int inputSize = 0;

	Error error;

	long coolDownTime;

	DefaultHttpClient httpclient;

	public RovioControl(String addr) {
		this.addr = addr;

		httpclient = new DefaultHttpClient();

		HttpParams params = httpclient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 500);
		HttpConnectionParams.setSoTimeout(params, 500);

	}

	public void setRobotName() {

	}

	public boolean setHeadLights( int value ) {
		return sendCgi("rev.cgi?Cmd=nav&action=19&LIGHT="+value);
	}

	public boolean setIR( int value ) {
		return sendCgi("rev.cgi?Cmd=nav&action=19&IR="+value);
	}

	public RovioStatus getStatus( RovioStatus storage ) {
		if( sendCgi("GetStatus.cgi")) {
			if( inputSize != 95+11 ) {
				return null;
			}

			if( storage == null )
				storage = new RovioStatus();

			byte []adjusted = new byte[95];
			System.arraycopy(inputBuff,9,adjusted,0,95);
			storage.parse(adjusted);

			return storage;
		} else {
			return null;
		}
	}

	public RovioMcuReport getMcuReport( RovioMcuReport storage ) {
		if( sendCgi("rev.cgi?Cmd=nav&action=20")) {

			if( storage == null )
				storage = new RovioMcuReport();

			byte []adjusted = new byte[inputSize-22];
			System.arraycopy(inputBuff, 22, adjusted, 0, adjusted.length);
			storage.parse(adjusted);

			return storage;
		} else {
			return null;
		}
	}

	/**
	 *
	 * 0 = 176x144
	 * 1 = 352x288
	 * 2 = 320,240
	 * 3 = 640x480
	 *
	 * @param which
	 * @return
	 */
	public boolean setImageResolution( int which ) {
		if( which < 0 && which > 3 )
			throw new IllegalArgumentException("Resolution must be from 0 to 3");

		return sendCgi("ChangeResolution.cgi?ResType="+which);
	}

	/**
	 *
	 * 0 = low quality
	 * 1 = medium quality
	 * 2 = high quality
	 *
	 * @param which
	 * @return
	 */
	public boolean setImageCompression( int which ) {
		if( which < 0 && which > 2 )
			throw new IllegalArgumentException("Compression must be from 0 to 2");

		return sendCgi("ChangeCompressRatio.cgi?Ratio="+which);
	}

	public boolean setFrameRate( int fps ) {
		if( fps < 2 && fps > 32 )
			throw new IllegalArgumentException("FPS must be from 2 to 32");

		return sendCgi("ChangeFrameRate.cgi?Framerate="+fps);
	}

	public boolean setImageBrightness( int brightness ) {
		if( brightness < 0 && brightness > 6 )
			throw new IllegalArgumentException("brightness must be from 0 to 6");

		return sendCgi("ChangeBrightness.cgi?Brightness="+brightness);
	}

	public BufferedImage captureImage() {

		if( sendCgi("Jpeg/CamImg0000.jpg")) {
			try {
				if( inputSize == -1 )
					System.out.println("WTF");
				BufferedImage image = ImageIO.read(new ByteArrayInputStream(inputBuff,0,inputSize));
				return image;
			} catch (IOException e) {
				error = Error.IO_EXCEPTION;
			}
		}

		return null;
	}

	public boolean movement( RovioManual type, int speed ) {
		if( speed < 1 || speed > 10 )
			throw new IllegalArgumentException("Speed must be 1 <= speed <= 10");

		return sendCgi("/rev.cgi?Cmd=nav&action=18&drive="+type.getValue()+"&speed="+speed);
	}

	protected boolean sendCgi( String text ) {
		if( coolDownTime > System.currentTimeMillis() ) {
			error = Error.COOL_DOWN;
			return false;
		} else {
			coolDownTime = System.currentTimeMillis()+COOL_DOWN_PERIOD;
		}

		HttpGet httpGet = new HttpGet("http://"+addr+"/"+text);

		error = Error.NO_ERROR;

		try {
			HttpResponse response1 = httpclient.execute(httpGet);
			HttpEntity entity1 = response1.getEntity();
			readResponse(entity1.getContent(), entity1.getContentLength());
			EntityUtils.consume(entity1);
			httpGet.releaseConnection();
		} catch( ConnectTimeoutException e ) {
			System.out.println("Connection timed out");
			error = Error.TIMED_OUT;
			coolDownTime = System.currentTimeMillis()+ COOL_DOWN_PERIOD*10;
			return false;
		} catch( SocketTimeoutException e ) {
			System.out.println("Socket timed out");
			error = Error.TIMED_OUT;
			coolDownTime = System.currentTimeMillis()+ COOL_DOWN_PERIOD*10;
			return false;
		} catch (IOException e) {
			error = Error.IO_EXCEPTION;
			return false;
		}

		if( error != Error.NO_ERROR )
			return false;

		return true;
	}

	private void readResponse(InputStream stream , long length ) {
		inputSize = 0;

		if( length > inputBuff.length ) {
			inputBuff = new byte[(int)length+1024];
		}

		try {
			long TIME = System.currentTimeMillis()+1000;
			boolean timedOut = false;

			while( length > 0 && !timedOut) {
				int actualRead = stream.read(inputBuff, inputSize, (int) length);
				if( actualRead < 0 ) {
					throw new IllegalArgumentException("Premature EOF");
				} else {
					length -= actualRead;
					inputSize += actualRead;
				}
				timedOut = TIME < System.currentTimeMillis();
			}

			if( timedOut ) {
				error = Error.TIMED_OUT;
			}
		} catch (IOException e) {}
	}

	public Error getError() {
		return error;
	}

	public static enum Error {
		NO_ERROR,
		IO_EXCEPTION, COOL_DOWN, TIMED_OUT
	}
}
