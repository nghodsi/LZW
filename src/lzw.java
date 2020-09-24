import java.util.*;
import java.io.*;

public class lzw {

	//TODO:
	//Document all the things, including time complexity
	//remove magic number 256
	private static String fileToEncodeName;
	//Maximum size of table (1024 for 10 bits)
	private final int maxDictionarySize = 9999999;

	public void encode () {
		Scanner keyboard = new Scanner(System.in); //Asks the user for the file name and saves it as a String
        System.out.print("Enter filename here: ");
        fileToEncodeName = keyboard.next();
		keyboard.close();

		generateCodestream(fileToEncodeName, initializeDictionaryForEncode());
	}

	public void decode(String fileName) {
		decodeCodestream(fileName, initializeDictionaryForDecode(), catalogCodestream(fileName));
	}

	//Initializes dictionary (ArrayList) with ASCII table <-- for Lucas & Navid's encoder
	//O(1)
	private ArrayList<String> initializeDictionaryForEncode() {
		//Size 1024 for 10 bits
		ArrayList<String> dictionary = new ArrayList<String>(maxDictionarySize);

		for (int i = 0; i<256;i++) {
			dictionary.add (""+(char)i);
		}

		return dictionary;
	}

	//Initializes dictionary (HashMap) with ASCII table <-- for decoder
	//O(1)
	private HashMap<Integer, String> initializeDictionaryForDecode() {
		HashMap<Integer, String> dictionary = new HashMap<Integer, String>();

		for(int i = 0; i < 256; i++) {
			dictionary.put(i, "" + (char) i);
		}

		return dictionary;
	}

	//Generates codestream and prints it to 1st line of encoded .txt file
	//O(n)
	private void generateCodestream(String fileName, ArrayList<String> dictionaryAsArrayList) {
		ArrayList<Integer> codestream = new ArrayList<Integer>();
		String previousChar = "";
		HashMap<String, Integer> dictionary = arrayListToHashMap(dictionaryAsArrayList);
		try { //Tests code for errors while it's being executed
			BufferedReader bReader = new BufferedReader(new FileReader(new File(fileName)));
			//Creates output file called lzwOutput.txt
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(new File("lzwOutput.txt")));

			while (bReader.ready() && dictionary.size() < maxDictionarySize) {
				char currentChar = (char) bReader.read();

				if (dictionary.containsKey(previousChar+currentChar)) {
					previousChar=previousChar+currentChar;
				}
				else {
					//something is wrong, fix
					bWriter.write (dictionary.get(previousChar) + " ");
					codestream.add(dictionary.get(previousChar));
					dictionary.put(previousChar+currentChar, (Integer)(dictionary.size()));
					previousChar=Character.toString(currentChar);
				}
			}

			//cover last read
			bWriter.write (dictionary.get(previousChar) + " ");
			codestream.add(dictionary.get(previousChar));

			//if dictionary reaches chosen bit limit
			if(dictionary.size() >= maxDictionarySize) {
				System.out.println("Maximum dictionary size (" +  dictionary.size() + ") reached - stopping compression");

				while(bReader.ready()) {
					int character = bReader.read();
					bWriter.write(character + " ");
					codestream.add(character);
				}
			}

			bWriter.close();//Close readers and writers to release system resources
			bReader.close();

		}
		catch (Exception exe) {//Executes exception if an error is found in the try block
			exe.printStackTrace();
		}

		//do not print codestream when testing really large files unless you have infinitely big dictionary/dictionary reset implemented
		//System.out.println(codestream);
		System.out.println("codestream size: " + codestream.size());
	}

	//converts an ArrayList aList to a HashMap with key-value pairs (aList[i], i).
	private HashMap<String, Integer> arrayListToHashMap(ArrayList<String> arrayList)
	{
		HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
		for(int i = 0; i < arrayList.size(); i++)
		{
			hashMap.put(arrayList.get(i), i);
		}
		return hashMap;
	}


	//Reads .txt file containing codestream and transfers all codes into an ArrayList
	//runs in O(characters in file)
	private ArrayList<Integer> catalogCodestream(String fileName) {
		ArrayList<Integer> codestreamList = new ArrayList<Integer>();

		try {
			BufferedReader codeReader = new BufferedReader(new FileReader(new File(fileName)));

			String line = "";
			if((line = codeReader.readLine()) != null) {
				for(String code : line.split(" ")) {
					//add character flush condition here

					//add EOF condition here

					codestreamList.add(Integer.parseInt(code));
				}
			}

			codeReader.close();
		} catch(IOException e) {
			e.printStackTrace();
		}

		System.out.println("CodestreamList size: " + codestreamList.size());

		return codestreamList;
	}

	//Decodes codestream and rebuilds dictionary
	private void decodeCodestream(String fileName, HashMap<Integer, String> dictionary, ArrayList<Integer> codestreamList) {
		int dictionarySize = 256;
		String w = "" + (char) (int) codestreamList.remove(0);
		StringBuffer decodedCodestream = new StringBuffer(w);

		for(int value : codestreamList) {
			String currentEntry;

			if(dictionary.containsKey(value)) {
				currentEntry = dictionary.get(value);
			}
			else if(value == dictionarySize) {
				currentEntry = w + w.charAt(0);
			}
			else {
				throw new IllegalArgumentException("Bad compressed value: " + value);
			}

			decodedCodestream.append(currentEntry);

			//rebuilds the dicitionary by adding the new string
			dictionary.put(dictionarySize++, w + currentEntry.charAt(0));
			w = currentEntry;
		}

		try {
			BufferedWriter decodedFileWriter = new BufferedWriter(new FileWriter(new File ("decodedFile.txt")));

			decodedFileWriter.write(decodedCodestream.toString());
			decodedFileWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	//Checks decoded file with file that was originally encoded (the unencoded version)
	private boolean checkDecodedFile(String unencodedFileName, String decodedFileName) {
		try {
			BufferedReader unencodedFileReader = new BufferedReader(new FileReader(new File(unencodedFileName)));
			BufferedReader decodedFileReader = new BufferedReader(new FileReader(new File(decodedFileName)));

			while(unencodedFileReader.ready() && decodedFileReader.ready()) {
				if(unencodedFileReader.read() != decodedFileReader.read()) {
					unencodedFileReader.close();
					decodedFileReader.close();

					return false;
				}
			}

			unencodedFileReader.close();
			decodedFileReader.close();
		} catch(IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	public static void main (String [] args) {
		lzw cipher = new lzw ();
		cipher.encode();
		cipher.decode("lzwOutput.txt");
		System.out.println(cipher.checkDecodedFile("decodedFile.txt", fileToEncodeName));
	}
}
