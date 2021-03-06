import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author Peter Abeles
 */
public class ManualControlApp implements KeyListener {

	final RovioControl control;
	final ImagePanel gui;
	boolean dotColor = false;

	char command;
	int imageNum;
	int captureFlag = 0;

	// how long after it captures an image will it try to grab another image
	int imageDelay = 50;

	public ManualControlApp(final String ip) {
		control = new RovioControl(ip);

		while( !control.setImageResolution(3) ){}
		while( !control.setImageCompression(2) ){}

		BufferedImage img = null;

		while( img == null ) {
			img = control.captureImage();
		}

		gui = ShowImages.showWindow(img,"Rovio");
		gui.grabFocus();
		gui.addKeyListener(this);
	}

	public void run() {

		long update = 0;

		while( true ) {
			if( update < System.currentTimeMillis() ) {
				updateImage();
				update = System.currentTimeMillis() + imageDelay;
			}

			if( command != 0 ) {
				performCommand(command);
				command = 0;
			}

			Thread.yield();
		}
	}

	private void updateImage() {

		final BufferedImage img = control.captureImage();

		if( img != null ) {
			escape:if( captureFlag != 0 ) {
				File dir = new File("images");
				if( dir.exists() ) {
					if( !dir.isDirectory() ) {
						System.err.println("images/ exists, but isn't a directory");
						captureFlag = 0;
						break escape;
					}
				} else {
					if( !dir.mkdir() ) {
						System.err.println("Couldn't create images/ directory");
						captureFlag = 0;
						break escape;
					}
				}

				System.out.println("Image saved "+imageNum);
				UtilImageIO.saveImage(img,String.format("images/image%05d.jpg",imageNum++));
				if( captureFlag == 1 ) {
					captureFlag = 0;
				}
			}

			Graphics2D g2 = img.createGraphics();

			RovioStatus status = control.getStatus(null);
			if( status != null ) {
				g2.setColor(Color.WHITE);
				g2.fillRect(5, 5, 80, 36);
				g2.setColor(Color.RED);
				g2.drawString("Wifi "+status.wifiStrength,10,20);
				g2.drawString("Battery "+status.batteryLevel,10,35);
			}

			if( dotColor ) {
				g2.setColor(Color.WHITE);
			} else {
				g2.setColor(Color.BLACK);
			}
			dotColor = !dotColor;
			g2.fillOval(img.getWidth()-10,img.getHeight()-10,10,10);

			if( captureFlag != 0 ) {
				g2.setColor(Color.RED);
				g2.fillOval(img.getWidth()-10,10,10,10);
			}

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					gui.setBufferedImage(img);
					gui.repaint();
				}
			});
		} else {
			System.out.println("****  null image");
		}
	}

	private void performCommand( char key ) {
		while( control.isCoolingDown() ) {
			Thread.yield();
		}

		boolean success = true;
		if( key == '8' ) {
			success = control.movement(RovioManualMotion.FORWARD,5);
		} else if( key == '5' ) {
			success = control.movement(RovioManualMotion.BACKWARD,5);
		} else if( key == '4' ) {
			success = control.movement(RovioManualMotion.STRAIGHT_LEFT,5);
		} else if( key == '6' ) {
			success = control.movement(RovioManualMotion.STRAIGHT_RIGHT,5);
		} else if( key == '7') {
			success = control.movement(RovioManualMotion.ROTATE_LEFT_20,5);
		} else if( key == '9') {
			success = control.movement(RovioManualMotion.ROTATE_RIGHT_20,5);
		} else if( key == 'l' ) {
			success = control.setHeadLights(1);
		} else if( key == 'L' ) {
			success = control.setHeadLights(0);
		} else if( key == '1' ) {
			success = control.movement(RovioManualMotion.HEAD_DOWN,1);
		} else if( key == '2' ) {
			success = control.movement(RovioManualMotion.HEAD_MIDDLE,1);
		} else if( key == '3' ) {
			success = control.movement(RovioManualMotion.HEAD_UP,1);
		} else if( key == 's' ) {
			RovioMcuReport r = control.getMcuReport(null);
			if( r != null ) {
				System.out.println("headlight = "+r.headLight);
				System.out.println("ir = "+r.irPower);
				System.out.println("ir barrier = "+r.irDetectBarrier);
				System.out.println("battery full = "+r.batteryFull);
				System.out.println("battery very low = "+r.batteryVeryLow);
				System.out.println("head position = "+r.headPosition);
				System.out.println("left ticks = "+r.leftTicks);
				System.out.println("right ticks = "+r.rightTicks);
				System.out.println("rear ticks = "+r.rearTicks);
				System.out.println("charge status = "+r.chargeStatus);
			} else {
				System.out.println("MCU report null");
			}
		} else if( key == 'p' ) {
			captureFlag = 1;
		} else if( key == 'm' ) {
			if( captureFlag == 0 )
				captureFlag = 2;
			else
				captureFlag = 0;
		} else if( key == 'a' ) {
			System.out.println("MAC Address = "+control.getMacAddress());
		} else if( key == 'd' ) {
			int response = control.action(RovioActions.GO_HOME_AND_DOCK);
			System.out.println("Action response = "+response);
		} else {
			success = control.movement(RovioManualMotion.STOP,5);
		}

		if( !success ) {
			System.out.println("command failed: "+control.error);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		command = e.getKeyChar();
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	public static void main( String args[] ) {
		ManualControlApp app = new ManualControlApp("192.168.1.31");
		app.run();
	}
}
