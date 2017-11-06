package Functions;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by spyridons on 10/7/2016.
 */
public class FileFunctions {

    public static String readInputFile(String filePath){
        try {
            String txtFileContent = new String(Files.readAllBytes(Paths.get(filePath)));
            // remove trailing character that may exist on utf-8 files
            txtFileContent = txtFileContent.replace("\uFEFF", "");
            return txtFileContent;

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean writeToFile(String filePath, String text){
        try {
            Files.write(Paths.get(filePath), text.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
