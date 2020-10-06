/*
* Praise be to Ms. Kaufman and Computer Science A teachers.
* They spoke the truth when they spoke of handwritten code and BlueJ.
*/


package src;

import java.util.*;
import java.io.*;

public class LZW{

	//TODO:
	//Document all the things, including time complexity
	public static String fileToEncodeName;
	//Maximum size of table (1024 for 10 bits)
	private final int MAX_DICTIONARY_SIZE = 9999999;
	private final int DICTIONARY_SIZE = 256;
	
	/**
	 * Asks the user for the file they want to encode
	 * Then uses helper encoding methods to execute the encoding process
	 */
	public void Encode(){
		Scanner keyboard = new Scanner(System.in); 
		//Asks the user for the file name and saves it as a String
        	System.out.print("Enter filename here: ");
        	fileToEncodeName = keyboard.next();
		keyboard.close();

		generateCodestream(fileToEncodeName, initializeDictionaryForEncode());
	}

	/**
	 * Decodes the inputted the file
	 * Uses the decoding helper methods to execute the decoding process
	 */
	public void Decode(String fileName){
		decodeCodestream(fileName, initializeDictionaryForDecode(), catalogCodestream(fileName));
	}

	//Initializes dictionary (ArrayList) with ASCII table <-- for Lucas & Navid's encoder
	//O(1)
	/**
	 * Adds the first 256 (0-255) chars in the ascii table to the ArrayList dictionary
	 * Used for encoding
	 */
	public ArrayList<String> InitializeDictionaryForEncode(){
		//Size 1024 for 10 bits
		ArrayList<String> dictionary = new ArrayList<String>(MAX_DICTIONARY_SIZE);

		for (int i = 0; i<256;i++){
			dictionary.add (""+(char)i);
		}

		return dictionary;
	}

	//Initializes dictionary (HashMap) with ASCII table <-- for decoder
	//O(1)
	/**
	 * Adds the first 256 (0-255) chars in the ascii table to theh HashMap dictionary
	 * Used for decoding
	 */
	public HashMap<Integer, String> InitializeDictionaryForDecode(){
		HashMap<Integer, String> dictionary = new HashMap<Integer, String>();

		for(int i = 0; i < DICTIONARY_SIZE; i++){
			dictionary.put(i, "" + (char) i);
		}

		return dictionary;
	}

	//Generates codestream and prints it to 1st line of encoded .txt file
	//O(n)
	/**
	 * Takes in the text file name and the arraylist dictionary
	 * Encodes the text and outputs the codes onto a file called "lzwOutput.txt"
	 */
	public void GenerateCodestream(String fileName, ArrayList<String> dictionaryAsArrayList){
		ArrayList<Integer> codestream = new ArrayList<Integer>();
		String previousChar = "";
		HashMap<String, Integer> dictionary = arrayListToHashMap(dictionaryAsArrayList);
		//Tests code for errors while it's being executed
		try{
			BufferedReader bReader = new BufferedReader(new FileReader(new File(fileName)));
			//Creates output file called lzwOutput.txt
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(new File("lzwOutput.txt")));

			while (bReader.ready() && dictionary.size() < MAX_DICTIONARY_SIZE){
				char currentChar = (char) bReader.read();

				if (dictionary.containsKey(previousChar+currentChar)){
					previousChar=previousChar+currentChar;
				}
				else{
					bWriter.write (dictionary.get(previousChar) + " ");
					codestream.add(dictionary.get(previousChar));
					dictionary.put(previousChar+currentChar, (Integer)(dictionary.size()));
					previousChar=Character.toString(currentChar);
				}
			}

			//cover last read
			bWriter.write(dictionary.get(previousChar) + " ");
			codestream.add(dictionary.get(previousChar));

			//if dictionary reaches chosen bit limit
			if(dictionary.size() >= MAX_DICTIONARY_SIZE){
				System.out.println("Max dictionary size ("+dictionary.size()+") reached-stopping compression");

				while(bReader.ready()){
					int character = bReader.read();
					bWriter.write(character + " ");
					codestream.add(character);
				}
			}

			bWriter.close();
			//Close readers and writers to release system resources
			bReader.close();

		}
		catch (Exception e){
			//Executes exception if an error is found in the try block
			e.printStackTrace();
		}

		//do not print codestream when testing really large files
		//System.out.println(codestream);
		System.out.println("codestream size: " + codestream.size());
	}

	/**
	 * converts an ArrayList arrayList to a HashMap with key-value pairs (aList[i], i).
	 */
	public HashMap<String, Integer> ArrayListToHashMap(ArrayList<String> arrayList){
		HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
		for(int i = 0; i < arrayList.size(); i++){
			hashMap.put(arrayList.get(i), i);
		}
		return hashMap;
	}

	//runs in O(characters in file)
	/**
	 * Reads .txt file containing codestream and transfers all codes into an ArrayList
	 */
	public ArrayList<Integer> CatalogCodestream(String fileName){
		ArrayList<Integer> codestreamList = new ArrayList<Integer>();
		try{
			BufferedReader codeReader = new BufferedReader(new FileReader(new File(fileName)));
			String line = "";
			if((line = codeReader.readLine()) != null){
				for(String code : line.split(" ")){
					codestreamList.add(Integer.parseInt(code));
				}
			}

			codeReader.close();
		} catch(IOException e){
			e.printStackTrace();
		}

		System.out.println("CodestreamList size: " + codestreamList.size());

		return codestreamList;
	}

	/**
	 * Rebuilds the dictionary using the arraylist of codes
	 */
	public void DecodeCodestream(String fileName,HashMap<Integer,String>dictionary,ArrayList<Integer>codestreamList){
		
		String w = "" + (char) (int) codestreamList.remove(0);
		StringBuffer decodedCodestream = new StringBuffer(w);
		
		for(int value : codestreamList){
			String currentEntry;

			if(dictionary.containsKey(value)){
				currentEntry = dictionary.get(value);
			}
			else if(value == dictionary.size()){
				currentEntry = w + w.charAt(0);
			}
			else{
				throw new IllegalArgumentException("Bad compressed value: " + value);
			}

			decodedCodestream.append(currentEntry);
			//rebuilds the dicitionary by adding the new string
			dictionary.put(dictionary.size(), w + currentEntry.charAt(0));
			
			w = currentEntry;
		}

		try{
			BufferedWriter decodedFileWriter=new BufferedWriter(new FileWriter(new File ("decodedFile.txt")));
			decodedFileWriter.write(decodedCodestream.toString());
			decodedFileWriter.close();
		} catch(IOException e){
			e.printStackTrace();
		}
	}


	/**
	 * Checks decoded file with file that was originally encoded (the unencoded version)
	 * Makes sure that everything did what it was supposed to do
	 */
	public boolean CheckDecodedFile(String unencodedFileName, String decodedFileName){
		try{
			BufferedReader unencodedFileReader=new BufferedReader(new FileReader(new File(unencodedFileName)));
			BufferedReader decodedFileReader=new BufferedReader(new FileReader(new File(decodedFileName)));

			while(unencodedFileReader.ready() && decodedFileReader.ready()){
				if(unencodedFileReader.read() != decodedFileReader.read()){
					unencodedFileReader.close();
					decodedFileReader.close();
					return false;
				}
			}

			unencodedFileReader.close();
			decodedFileReader.close();
		} catch(IOException e){
			e.printStackTrace();
		}

		return true;
	}

	
}
//. 　　　。　　　　•　 　ﾟ　　。 　　.
//
//.　　　 　　.　　　　　。　　 。　. 　
//
//.　　 。　　　　　 ඞ 。 . 　　 • 　　　　•
//
//ﾟ　　 Andrew was An Impostor.　 。　.
//
//'　　　 1 Impostor remains 　 　　。
//
//ﾟ　　　.　　　. ,　　　　.　 .


