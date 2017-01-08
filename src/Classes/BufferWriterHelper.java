package Classes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BufferWriterHelper {

	private final static boolean showSystemPrintWhenOutput = false;
	private BufferedWriter bw;

	public BufferWriterHelper(String fileName) throws IOException {

		String folderName = "result";
		File theDir = new File(folderName);
		if (!theDir.exists())
			theDir.mkdir();
		bw = new BufferedWriter(new FileWriter(folderName + "//" + fileName
				+ ".txt"));
	}

	public void writeAndLog() {
		writeAndLog(null);
	}

	public void writeAndLog(boolean mustPrint) {
		writeAndLog(null, mustPrint);
	}

	public void writeAndLog(String s) {
		writeAndLog(s, showSystemPrintWhenOutput);
	}

	public void writeAndLog(String s, boolean mustPrint) {
		try {
			if (s != null) {
				bw.write(s);
				if (mustPrint)
					System.out.println(s);
			} else {
				if (mustPrint)
					System.out.println();
			}
			bw.write("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeWriterBuffers() {
		try {
			this.bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
