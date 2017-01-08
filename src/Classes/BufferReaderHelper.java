package Classes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class BufferReaderHelper {

	private String lastLine;
	private BufferedReader br;
	private String fileName;
	
	public String getLastLine(){
		return lastLine;
	}
	
	public String readOneLine() throws IOException{
		lastLine = br.readLine();
		return lastLine;
	}
	
	public BufferReaderHelper(String fileName) throws FileNotFoundException{
		
		br = new BufferedReader(new FileReader(fileName));
		this.fileName = fileName;
	}
	
	// read line once only
	public boolean getLine(String source) throws IOException {
		lastLine = br.readLine();
		if (lastLine.contains(source))
			return true;
		return false;
	}
	
	// read lines until reaching the end or you find it
	public boolean findLine(String source, boolean continueFromLastTime)
			throws IOException {
		if (!continueFromLastTime) {
			this.br.close();
			this.br = new BufferedReader(new FileReader(this.fileName));
		}
		lastLine = br.readLine();
		while (lastLine != null) {
			if (lastLine.contains(source)) {
				return true;
			}
			lastLine = br.readLine();
		}
		return false;
	}
	
	public void closeReaderBuffers() {
		try {
			this.br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
