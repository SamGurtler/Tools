package sg.cryptography;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
public class RSA{
    /** This is a use of a Textbook RSA Cryptography *without padding* */
	private static final ArrayList<Thread> THREADS=new ArrayList<Thread>();
    //Copied regex from stack overflow that partially works.
    private static final String COMMANDPATTERN=" (?=(?:[^\"]*\"([^\"]*)\")*[^\"]*$)";
    //{big prime,big prime,modulus,totient of modulus}
    private static final BigInteger[] NUMS = {new BigInteger("15805993460899067323"),new BigInteger("9382304296585360981"),
    new BigInteger("148296640359993439204927582798176323863"),new BigInteger("148296640359993439179739285040691895560")};
    //Main method
    public static void main(String[] args)throws InterruptedException{
        if(args.length<1){
        	getHelp();
            (new Thread(new menu(getInput("Option:").split(COMMANDPATTERN,-1)))).start();
        }else(new Thread(new menu(args[0].split(COMMANDPATTERN,-1)))).start();
    }
    private static class menu implements Runnable{
        private static final int BUFFER=2048;
        private static String[] args;
        public menu(String[] args){
                this.args=ghettofix(args);
        }
        @Override
        public void run(){
            boolean quit=false;
            while(!quit){
                 if(args[0].toLowerCase().contains("keys")){
                    THREADS.add(new Thread((new MakeKeys())));
                    THREADS.get(THREADS.size()-1).start();
                    try{
                        THREADS.get(THREADS.size()-1).join();
                        THREADS.remove(THREADS.size()-1);
                    }catch(InterruptedException e){
                        System.err.print(e);
                    }
                }else if(args[0].toLowerCase().contains("en")){
                    int y=Math.max(0,THREADS.size()-1);
                    File tmp;
                    ArrayList<File> toBeZipped= new ArrayList<>();
                    for(int x=2;x<args.length;x++){
                        THREADS.add(new Thread(new CryptoMethod(args,x,true)));
                        THREADS.get(THREADS.size()-1).run();
                        if((tmp=new File(args[x])).exists())toBeZipped.add(tmp);
                    }
                    for(int j=y;THREADS.size()-1>j;j++){
                        try{
                            THREADS.get(j).join();
                            THREADS.remove(j);
                        }catch(InterruptedException e){
                            System.err.println(e);
                        }
                    }
                    //If there wasn't any files found in arguments
                    if(!toBeZipped.isEmpty()) {
                    	BufferedInputStream origin = null;
                        byte data[] = new byte[BUFFER];
                    	//Finds a place to put encrypted files
                        for(int x=0;((tmp=new File("ENCRYPT_GROUP"+x+".zip")).exists());x++)System.out.print("\r"+x);
                        System.out.println("Created:"+tmp);
	                    try{
	                        CheckedOutputStream checksum = new CheckedOutputStream(new FileOutputStream(tmp), new Adler32());
	                        try(ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(checksum))){
	                            out.setLevel(9);
	                            for(int x=0;x<toBeZipped.size();x++){
	                                origin=new BufferedInputStream(new FileInputStream(toBeZipped.get(x)),BUFFER);
	                                ZipEntry entry = new ZipEntry(toBeZipped.get(x).getPath());
	                                out.putNextEntry(entry);
	                                int count;
	                                while((count = origin.read(data,0,BUFFER))!=-1){
	                                    out.write(data, 0, count);
	                                }
	                                origin.close();
	                               System.out.println("Zipped:"+toBeZipped.get(x));
	                            }
	                            for(File f:toBeZipped)f.delete();
	                        }
	                    }catch(IOException e){
	                        System.err.println(e);
	                    }
                    }
                }else if(args[0].toLowerCase().contains("de")){
                    //Need to make compatible with zip.
                    byte data[] = new byte[BUFFER];
                    BufferedOutputStream dest = null;
                    File tmp;
                    ArrayList<File> toBeUnzipped= new ArrayList<>();
                    ArrayList<String> editedArgs=new ArrayList<>(Arrays.asList(args));
                    for(int x=2;x<args.length;x++){if((tmp=new File(args[x])).exists()){
                        toBeUnzipped.add(tmp);
                        editedArgs.remove(x);
                    }}
                    for(File f:toBeUnzipped){
                        try{
                            FileInputStream fis = new FileInputStream(f);
                            CheckedInputStream checksum=new CheckedInputStream(fis,new Adler32());
                            ZipInputStream zis=new ZipInputStream(new BufferedInputStream(checksum));
                            ZipEntry entry;
                            while((entry=zis.getNextEntry()) != null){
                               int count;
                               // write the files to the disk
                               editedArgs.add(entry.getName());
                               FileOutputStream fos=new FileOutputStream(entry.getName());
                               dest=new BufferedOutputStream(fos,BUFFER);
                               while((count=zis.read(data,0,BUFFER))!=-1) {
                                  dest.write(data, 0, count);
                               }
                               dest.flush();
                               dest.close();
                            }
                            zis.close();
                            f.delete();
                        }catch(IOException e){
                            System.err.println(e);
                        }
                    }
                    args=editedArgs.toArray(new String[editedArgs.size()]);
                    for(int x=2;x<args.length;x++){
                        THREADS.add(new Thread(new CryptoMethod(args,x,false)));
                        THREADS.get(THREADS.size()-1).run();
                        try{
                            THREADS.get(THREADS.size()-1).join();
                        }catch(InterruptedException e){
                            System.err.println(e);
                        }
                    }
                }else if(args[0].toLowerCase().contains("quit"))quit=true;
                else getHelp();
                if(!quit)args=ghettofix(getInput("Option:").split(COMMANDPATTERN,-1));
            }
        }
        private static String[] ghettofix(String[] args){
            for(int count=0;count<args.length;count++){
                    if(args[count].charAt(0)=='"'&& args[count].charAt(args[count].length()-1)=='"'){
                            args[count]=args[count].substring(1,args[count].length()-1);
                    }
            }
            return args;
        }
    }
    protected static class CryptoMethod implements Runnable{
        private static String[] args;
        private static int x;
        private static boolean en;
        public CryptoMethod(String[]args,int x,boolean en){
            CryptoMethod.args=args;
            CryptoMethod.x=x;
            CryptoMethod.en=en;
        }   
        @Override
        public void run(){
            try{
                ArrayList<String> fileStorage=new ArrayList<>();
                String line;
                try (BufferedReader reader = new BufferedReader(new FileReader(args[x]))) {
                	System.out.println("Orignial "+args[x]);
                    while((line=reader.readLine())!=null){
                    	System.out.println(line);
                    	if(en)line+="\n";
                    	line= en?encrypt(line,args[1],NUMS[2].toString()):decrypt(line,args[1],NUMS[2].toString());
                    	fileStorage.add(line);
                    }
                }
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(args[x]))) {
                	System.out.println(en?"Encrypted":"Decrypted"+"file:"+args[x]);
                    for(String s:fileStorage) {
                    	writer.write(s);
                    	writer.newLine();
                    	System.out.println(s);
                    }                	
                }
            }catch(IOException e){
                System.err.println(e.getMessage());
                System.out.println("File not found. Switching to String mode.");
                if(en)System.out.println(encrypt(args[x],args[1],NUMS[2].toString()));  
                else System.out.println(decrypt(args[x],args[1],NUMS[2].toString()));
            }
       }
    }
    protected static class MakeKeys implements Runnable{
        private final String[] KEYS;
        public MakeKeys(){
        	KEYS=new String[2];
        }
        @Override
        public void run(){
            BigInteger publicKey=(new BigInteger(NUMS[3].bitLength(),new SecureRandom())).abs();
            while(check(publicKey))publicKey=(new BigInteger(NUMS[3].bitLength(),(new SecureRandom()))).abs();
            System.out.print("Public Key:"+(KEYS[0]=publicKey.toString()));
            BigInteger privateKey= publicKey.modInverse(NUMS[3]);
            while(check(privateKey))privateKey=publicKey.modInverse(NUMS[3]);
            System.out.print(" Private Key:"+(KEYS[1]=privateKey.toString())+"\n");
        }
        /**
         * @param num is intended to be a potential key for RSA algorithm.
         * @return true if num is not coprime and less than the totient of modulus.
         * Returns true if num is the opposite of what is required.
         */
        private static boolean check(BigInteger num){
        	return (num.compareTo(NUMS[3])>=0||num.compareTo(BigInteger.ONE)<=0)||!coPrime(NUMS[3],num);
        }   
        public String[] getKeys(){
            for(String key:KEYS)if(key==null)throw new NullPointerException("Keys not made yet."); 
        	return KEYS;
        }
    }
    //This is a fundamental method.
    private static boolean coPrime(BigInteger a,BigInteger b){
        return a.gcd(b).equals(BigInteger.ONE);
    }
    //This is a fundamental method
    public final static String encrypt(String msg,String publicKey,String modulus){
        return (new BigInteger(msg.getBytes())).modPow(new BigInteger(publicKey),new BigInteger(modulus)).toString();
    }
    //This is a fundamental method.
    public final static String decrypt(String cipher,String privateKey,String modulus){
        return new String(new BigInteger(cipher).modPow(new BigInteger(privateKey),new BigInteger(modulus)).toByteArray());
    }
    private static String getInput(String quote){
        System.out.print(quote);
        return (new Scanner(System.in)).nextLine();
    }
    private static void getHelp(){
        System.out.println("Separate all input arguments with spaces.");
        System.out.println("Enter any of the following:\nEn for to encrypt(with public key before files and or Strings),");
        System.out.print("De to decrypt(with private key before files and or Strings),\nOr Keys for new keys");
        System.out.print(";followed by String or File's path names that will be used.\nFor examples type ex or type quit to exit.\n");
    }
    static{System.out.println("@author Sam Gurtler");}
}