import java.util.*;
import java.io.*;

public class lzw { 
	private static String fileToEncodeName;
	//Maximum size of table (1024 for 10 bits)
	private final int maxDictionarySize = 1024;
	
	public void encode () {
		Scanner keyboard = new Scanner(System.in); //Asks the user for the file name and saves it as a String
        System.out.print("Enter filename here: ");
        fileToEncodeName = keyboard.next();
		keyboard.close();
        
		generateCodestream(fileToEncodeName, initializeDictionary());
	}
	
	public void decode(String fileName) {
		decodeCodestream(fileName, initializeDictionary2(), catalogCodestream(fileName));
	}
	
	//Initializes dictionary (ArrayList) with ASCII table <-- for Lucas & Navid's encoder
	private ArrayList<String> initializeDictionary() {
		//Size 1024 for 10 bits
		ArrayList<String> dictionary = new ArrayList<String>(maxDictionarySize);
			
		for (int i = 0; i<256;i++) {
			dictionary.add (""+(char)i);
		}
			
		return dictionary;
	}
	
	//Initializes dictionary (HashMap) with ASCII table <-- for decoder
	private HashMap<Integer, String> initializeDictionary2() {
		HashMap<Integer, String> dictionary = new HashMap<Integer, String>();
			
		for(int i = 0; i < 256; i++) {
			dictionary.put(i, "" + (char) i);
		}
			
		return dictionary;
	}
	
	//Generates codestream and prints it to 1st line of encoded .txt file
	private void generateCodestream(String fileName, ArrayList<String> dictionary) {
		ArrayList<Integer> codestream = new ArrayList<Integer>();
		String P = "";
		
		try { //Tests code for errors while it's being executed
			BufferedReader bReader = new BufferedReader(new FileReader(new File(fileName)));
			//Creates output file called lzwOutput.txt
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(new File("lzwOutput.txt")));
			
			while (bReader.ready() && dictionary.size() < maxDictionarySize) {
				char C = (char) bReader.read();
				
				if (dictionary.contains(P+C)) {
					P=P+C;
				}
				else {
					bWriter.write (dictionary.indexOf(P) + " ");
					codestream.add(dictionary.indexOf(P));
					dictionary.add(P+C);
					P=Character.toString(C);
				}
			}
			
			//cover last read
			bWriter.write (dictionary.indexOf(P) + " ");
			codestream.add(dictionary.indexOf(P));
			
			//if dictionary reaches chosen bit limit
			if(dictionary.size() >= maxDictionarySize) {
				System.out.println("Maximum dictionary size (" +  dictionary.size() + ") reached - stopping compression");
				
				while(bReader.ready()) {
					int c = bReader.read();
					bWriter.write(c + " ");
					codestream.add(c);
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
	
	//Reads .txt file containing codestream and transfers all codes into an ArrayList
	private ArrayList<Integer> catalogCodestream(String fileName) {
		ArrayList<Integer> codestreamList = new ArrayList<Integer>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
			
			String line = "";
			if((line = br.readLine()) != null) {
				for(String code : line.split(" ")) {
					//add character flush condition here
					
					//add EOF condition here
					
					codestreamList.add(Integer.parseInt(code));
				}
			}
			
			br.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		//System.out.println("CodestreamList size: " + codestreamList.size());
		
		return codestreamList;
	}
	
	//Decodes codestream and rebuilds dictionary
	private void decodeCodestream(String fileName, HashMap<Integer, String> dictionary, ArrayList<Integer> codestreamList) {
		int dictionarySize = 256;
		String w = "" + (char) (int) codestreamList.remove(0);
		StringBuffer decodedCodestream = new StringBuffer(w);
		
		for(int k : codestreamList) {
			String entry;
			
			if(dictionary.containsKey(k)) {
				entry = dictionary.get(k);
			}
			else if(k == dictionarySize) {
				entry = w + w.charAt(0);
			}
			else {
				throw new IllegalArgumentException("Bad compressed k: " + k);
			}
			
			decodedCodestream.append(entry);
			dictionary.put(dictionarySize++, w + entry.charAt(0));
			w = entry;
		}
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File ("decodedFile.txt")));
			
			bw.write(decodedCodestream.toString());
			bw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	//Checks decoded file with file that was originally encoded
	private boolean checkDecodedFile(String fileName1, String fileName2) {
		try {
			BufferedReader br1 = new BufferedReader(new FileReader(new File(fileName1)));
			BufferedReader br2 = new BufferedReader(new FileReader(new File(fileName2)));
			
			while(br1.ready() && br2.ready()) {
				if(br1.read() != br2.read()) {
					br1.close();
					br2.close();
					
					return false;
				}
			}
			
			br1.close();
			br2.close();
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
