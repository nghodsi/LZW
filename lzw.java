import java.util.*;
import java.io.*;

public class lzw {
	private ArrayList<String> dictionary = new ArrayList<String>(); //Dictionary of ascii symbols and any symbols added in the code
	private String inputFileName; //File name that the user enters
	
	public lzw () {
		for (int i = 0; i<256;i++) {
			dictionary.add (""+(char)i); //Adds each ascii symbol to the dictionary
		}
		Scanner kb = new Scanner(System.in); //Asks the user for the file name and saves it as a String
        System.out.print("Enter filename here: ");
        inputFileName = kb.next();
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
				bWriter.write (dictionary.indexOf(""+P)+"\n");
				dictionary.add(""+P+C);
				P=C;
			}
			int nextChar = bReader.read(); //Variable that holds the integer value of the current char we are dealing with in the .txt file
			if (nextChar>-1) {
				C = ""+(char)nextChar; //Need to set C to next symbol in file before going into the while loop
			}
			while (nextChar!=-1) {
				nextChar = bReader.read();
				if (dictionary.contains(""+P+C)) {
					P=P+C;
				}
				else {
					bWriter.write (dictionary.indexOf(""+P)+"\n");
					dictionary.add(""+P+C);
					P=C;
				}
				C = "" + (char)nextChar;
			}
			bWriter.write (""+dictionary.indexOf(""+P));
			bWriter.close();//Close readers and writers to release system resources
			fWriter.close();
			fReader.close();
			bReader.close();
		}
		catch (Exception exe) {//Executes exception if an error is found in the try block
			exe.printStackTrace();
		}
	}
	
	public static void main (String [] args) {
		lzw encoder = new lzw ();
		encoder.encode();
	}
}
