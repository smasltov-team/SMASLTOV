package tvla.jeannet.util;

/** This class can be used to add an underline to another string.
 * @author Roman Manevich.
 */
public class AddUnderline {
	public static char underlineCharacter = '-';
	
	public static String add(String source) {
		StringBuffer result = new StringBuffer();
		result.append(source + "\n");

		for (int index = 0; index < source.length(); ++index) {
			result.append(underlineCharacter);
		}
		
		return result.toString();
	}
	
}
