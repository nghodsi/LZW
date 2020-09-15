import java.util.*;
import java.io.*;

public class lzw {
	private ArrayList<Integer> codestream = new ArrayList<Integer>();
	private ArrayList<String> dictionary = new ArrayList<String>(); //Dictionary of ascii symbols and any symbols added in the code
	private String inputFileName; //File name that the user enters
	private int max = 1024; //Maximum size of table
	
	public lzw () {
		for (int i = 0; i<256;i++) {
			dictionary.add (""+(char)i); //Adds each ascii symbol to the dictionary
		}
		Scanner keyboard = new Scanner(System.in); //Asks the user for the file name and saves it as a String
        System.out.print("Enter filename here: ");
        inputFileName = keyboard.next();
	}
	
	public void encode () {
			try { //Tests code for errors while it's being executed
				FileReader fReader = new FileReader (inputFileName);
				BufferedReader bReader = new BufferedReader (fReader);
				File file = new File ("lzwOutput.txt"); //Creates output file called lzwOutput.txt
				if(!file.exists()) {
					file.createNewFile();
				}
				FileWriter fWriter = new FileWriter (file);
				BufferedWriter bWriter = new BufferedWriter (fWriter);
				String P = null; 
				String C = ""+(char)bReader.read(); //Initializes P & C to what they should be (P as null, C as first letter)
				if (dictionary.contains(C)) { //Since P starts out as null, P+C is the same as just C (same logic repeated below)
					P=C;
				}
				else {
					bWriter.write (dictionary.indexOf(""+P)+" ");
					codestream.add(dictionary.indexOf(""+P));
					dictionary.add(""+P+C);
					P=C;

				}
				int nextChar = bReader.read(); //Variable that holds the integer value of the current char we are dealing with in the .txt file
				if (nextChar>-1) {
					C = ""+(char)nextChar; //Need to set C to next symbol in file before going into the while loop
				}
				while (bReader.ready() && nextChar!=-1 && dictionary.size() < max) {
					nextChar = bReader.read();
					if (dictionary.contains(""+P+C)) {
						P=P+C;
					}
					else {
						bWriter.write (dictionary.indexOf(""+P)+" ");
						codestream.add(dictionary.indexOf(""+P));
						dictionary.add(""+P+C);
						P=C;
					}
					C = "" + (char)nextChar;
				}
				
				System.out.println("Maximum dictionary size reached - stopping compression");
				while(bReader.ready() && dictionary.size() >= max) {
					int c = bReader.read();
					bWriter.write(c + " ");
					codestream.add(c);
				}
				bWriter.write (""+dictionary.indexOf(""+P));
				codestream.add(dictionary.indexOf(""+P));
				bWriter.close();//Close readers and writers to release system resources
				fWriter.close();
				fReader.close();
				bReader.close();
			}
			catch (Exception exe) {//Executes exception if an error is found in the try block
				exe.printStackTrace();
			}
			
			//just for testing - do not use if testing really large files
			//System.out.println(codestream);
		}
	
	
	public void decode(String fileName) {
		decodeCodestream(fileName, initializeDictionary(), catalogCodestream(fileName));
	}
	
	private HashMap<Integer, String> initializeDictionary() {
		//ASK LUCAS/NAVID HOW MANY BITS THEY'RE ENCODING (they put 1000 for max size???)
		HashMap<Integer, String> dictionary = new HashMap<Integer, String>();
		
		//adds ASCII table (all characters w/ decimal values from 0-255)
		for(int i = 0; i < 256; i++) {
			dictionary.put(i, Character.toString((char) i));
		}
				
		return dictionary;
	}
	
	//reads .txt file where codestream was outputted to and
	//transfers all codes into an ArrayList<Integer>
	private ArrayList<Integer> catalogCodestream(String fileName) {
		ArrayList<Integer> codestreamList = new ArrayList<Integer>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
			
			String line = "";
			while((line = br.readLine()) != null) {
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
		
		//just for testing
		System.out.println(codestreamList.size());
		
		return codestreamList;
	}
	
	private void decodeCodestream(String fileName, HashMap<Integer, String> dictionary, ArrayList<Integer> codestreamList) {
		int dictSize = 256;
		String w = "" + (char) (int) codestreamList.remove(0);
		StringBuffer decodedCodestream = new StringBuffer(w);
		
		for(int k : codestreamList) {
			String entry;
			
			if(dictionary.containsKey(k)) {
				entry = dictionary.get(k);
			}
			else if(k == dictSize) {
				entry = w + w.charAt(0);
			}
			else {
				throw new IllegalArgumentException("Bad compressed k: " + k);
			}
			
			decodedCodestream.append(entry);
			dictionary.put(dictSize++, w + entry.charAt(0));
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
	
	public static void main (String [] args) {
		lzw encoder = new lzw ();
		encoder.encode();
		encoder.decode("lzwOutput.txt");
	}
}
