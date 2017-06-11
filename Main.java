 import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main{
  public static void main(String[] paramArrayOfString) throws java.io.IOException{
    if (paramArrayOfString.length < 3) {
      System.out.println("Find pattern format at:");
      System.out.println("https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html#matcher(java.lang.CharSequence)");
      paramArrayOfString = new String[] { getInput("Pattern:"), getInput("Replacement:"), getInput("File Path:") };
    }
    File[] arrayOfFile = new File[paramArrayOfString.length - 2];
    try {
      for (int i = 2; i < paramArrayOfString.length; i++) {
        arrayOfFile[(i - 2)] = new File(paramArrayOfString[i]);
      }
      System.out.println(ToolRC(paramArrayOfString[0], paramArrayOfString[1], arrayOfFile));
    }
    catch (java.io.FileNotFoundException localFileNotFoundException) {
      System.out.println(ToolRC(paramArrayOfString[0], paramArrayOfString[1], paramArrayOfString[2]));
    }
  }
  public static int ToolRC(String paramString1, String paramString2, File[] paramArrayOfFile) throws java.io.IOException, java.io.FileNotFoundException { BufferedReader localBufferedReader = new BufferedReader(new java.io.FileReader(paramArrayOfFile[0]));
    java.util.ArrayList<String> localArrayList = new java.util.ArrayList<String>();
    String str1 = "";
    Pattern localPattern = Pattern.compile(paramString1);
    int i = 0;
    Object localObject; while ((str1 = localBufferedReader.readLine()) != null) {
      localObject = localPattern.matcher(str1);
      i += ((Matcher)localObject).groupCount();
      localArrayList.add(((Matcher)localObject).replaceAll(paramString2));
    }
    localBufferedReader.close();
    
    if ((paramArrayOfFile.length > 1) && (paramArrayOfFile.length < 3)) localObject = new BufferedWriter(new java.io.FileWriter(paramArrayOfFile[(paramArrayOfFile.length - 1)])); else
      localObject = new BufferedWriter(new java.io.FileWriter(paramArrayOfFile[0]));
    for (String str2 : localArrayList) {
      System.out.println(str2);
      ((BufferedWriter)localObject).write(str2 + "\n");
    }
    ((BufferedWriter)localObject).flush();
    ((BufferedWriter)localObject).close();
    return i;
  }
  public static String ToolRC(String paramString1, String paramString2, String paramString3) { Pattern localPattern = Pattern.compile(paramString1);
    Matcher localMatcher = localPattern.matcher(paramString3);
    return localMatcher.replaceAll(paramString2);
  }
  private static String getInput(String paramString) { System.out.print(paramString);
    return new java.util.Scanner(System.in).nextLine();
  }
}