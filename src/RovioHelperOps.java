import java.io.*;

/**
 * @author Peter Abeles
 */
public class RovioHelperOps {

	/**
	 * Searches for the robot's config directory using its MAC address.
	 *
	 * @param macaddr MAC address in string format
	 * @param verbose if true it will print out all the robots it finds
	 * @return Path to the robot directory
	 * @throws IOException
	 */
	public static String findRobotDirectory( String macaddr , boolean verbose ) throws IOException {
		File robotDir = new File("robots");
		if( !robotDir.exists() )
			throw new IOException("robots directory does not exist");

		File[] files = robotDir.listFiles();

		for( File f : files ) {
			if( !f.isDirectory() )
				continue;

			File macFile = new File(f,"mac");
			if( !macFile.exists() )
				continue;

			BufferedReader stream = new BufferedReader(new FileReader(macFile));
			String found = stream.readLine();
			stream.close();

			if( found.equals(macaddr ) ) {
				return f.getPath();
			} else if( verbose ) {
				System.out.println("found mac "+found);
			}
		}

		throw new IOException("Robot not found.");
	}
}
