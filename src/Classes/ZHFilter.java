package Classes;

import java.io.File;
import java.io.FilenameFilter;

public class ZHFilter {

    public String findFileName(String dirName, String extension){
        File dir = new File(dirName);

        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename)
            { return filename.endsWith(extension); }
        } );
        if (files.length > 0) {
            File f = files[0];
            return f.getName();
        }
        return null;
    }
}