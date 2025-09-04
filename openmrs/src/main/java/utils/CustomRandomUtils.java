/**
 * 
 */
package utils;

import java.util.Random;

import org.testng.annotations.Test;

/**
 * 
 */
public class CustomRandomUtils {

	/**
	 * 
	 */
	public CustomRandomUtils() {
		// TODO Auto-generated constructor stub
	}


	public static String generateRandomString(int length) {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		Random rand = new Random();
		StringBuilder sb = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			sb.append(characters.charAt(rand.nextInt(characters.length())));
		}
		return sb.toString();
	}

	public static String generateRandomStringLength(int length) {

		StringBuilder sb = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			sb.append(generateRandomStringUsingAscii(length));
		}
		//System.out.println("Randon String : "+ sb.toString());
		return sb.toString();
	}

	@Test
	public static String generateRandomStringUsingAscii(int length) {
		//Lower chars as per Ascii
		Random rand = new Random();
		int minL = 97;
		int maxL = 122;
		char randomCharLower = (char) ( minL+rand.nextInt(maxL-minL+1));
		//System.out.println(" Lower Char Random : "+randomCharLower);

		//Upper chars as per Ascii
		int minU = 65;
		int maxU = 90;
		char randomCharUpper = (char) (minU+rand.nextInt(maxU-minU+1));
		//System.out.println(" Upper Char Random : "+randomCharUpper);
		String randonSting = String.valueOf(randomCharUpper) + String.valueOf(randomCharLower);

		return randonSting;
	}

	@Test
	public static String generateRandomAddress(int minLen, int maxLen) {
		//Lower chars as per Ascii
		Random rand = new Random();
		int minL = 97;
		int maxL = 122;
		char randomCharLower = (char) ( minL+rand.nextInt(maxL-minL+1));
		//	System.out.println(" Lower Char Random : "+randomCharLower);

		//Upper chars as per Ascii
		int minU = 65;
		int maxU = 90;
		char randomCharUpper = (char) (minU+rand.nextInt(maxU-minU+1));
		//System.out.println(" Upper Char Random : "+randomCharUpper);

		String randonSting = generateRandomNumber(minLen, maxLen)+" "+String.valueOf(randomCharUpper)+" MacArthur Blvd " + String.valueOf(randomCharLower);
		//System.out.println("Randon Address :: "+randonSting);
		return randonSting;
	}

	@Test
	public static int generateRandomNumber(int min, int max) {
		// Between min (inclusive) and max (exclusive)
		// min = 10000000;
		// max = 39999999;
		Random rand = new Random();
		int randomNum = rand.nextInt(max - min) + min;
		//System.out.println(" Random number between :  "+min+" and "+max+" :::: " + randomNum);
		return randomNum;
	}	

	public static String generateRandomSsn() {
		//302-38-2370
		String randomSsn = null;
		int ssn_p1 = generateRandomNumber(100,999);
		int ssn_p2 = generateRandomNumber(10,99);
		int ssn_p3 = generateRandomNumber(1000,9999);
		randomSsn = String.valueOf(ssn_p1)+"-"+String.valueOf(ssn_p2)+"-"+String.valueOf(ssn_p3);
		//	System.out.println(randomSsn);
		return randomSsn;
	}

	@Test
	public static void main(String[] args) {
		//generateRandomLicenceNumber();
		generateRandomStringLength(5);
		generateRandomSsn();
		Random rand = new Random();
		int randomNum = 10000000 + rand.nextInt(90000000); // 8-digit number
		//    System.out.println("8-digit random number: " + randomNum);
	}


}
