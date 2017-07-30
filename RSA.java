import java.security.SecureRandom;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Scanner;
import java.math.BigInteger;
import java.lang.InterruptedException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
public class RSA{
    private static final ArrayList<Thread> THREADS=new ArrayList<Thread>();
    //Copied regex from stack overflow that partially works.
    private static final String COMMANDPATTERN=" (?=(?:[^\"]*\"([^\"]*)\")*[^\"]*$)";
    //{big prime,big prime,modulus,totient of modulus}
    private static final BigInteger[] NUMS = {new BigInteger("15805993460899067323"),new BigInteger("9382304296585360981"),
    new BigInteger("148296640359993439204927582798176323863"),new BigInteger("148296640359993439179739285040691895560")};
    
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
                    THREADS.add((Thread)(new MakeKeys()));
                    THREADS.get(THREADS.size()-1).start();
                    try{
                        THREADS.get(THREADS.size()-1).join();
                        THREADS.remove(THREADS.size()-1);
                    }catch(InterruptedException e){
                        System.err.print(e);
                    }
                }else if(args[0].toLowerCase().contains("en")){
                    int y=THREADS.size()-1;
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
                    BufferedInputStream origin = null;
                    byte data[] = new byte[BUFFER];
                    for(int x=0;((tmp=new File("ENCRYPT_GROUP"+x+".zip")).exists());x++)System.out.print("\r"+x);
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
                            }
                            for(File f:toBeZipped)f.delete();
                        }
                    }catch(IOException e){
                        System.err.println(e);
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
                    args=(String[])editedArgs.toArray();
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
    
    private static class CryptoMethod implements Runnable{
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
                    while((line=reader.readLine())!=null){
                        String[] info = (en)?encrypt(line,args[1],NUMS[2].toString()):new String[]{decrypt(line.split("-"),args[1],NUMS[2].toString())};
                        fileStorage.addAll(Arrays.asList(info));
                        fileStorage.add("ln");
                    }
                }
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(args[x]))) {
                    for(int y=0;y<fileStorage.size();y++) {
                        line=fileStorage.get(y);
                        if(en&&y<fileStorage.size()-1&&!fileStorage.get(y).contains("ln"))writer.write(line+'-');
                        else if(fileStorage.get(y).contains("ln"))writer.newLine();
                        else writer.write(line);
                    }
                }
            }catch(IOException e){
                System.err.println(e);
                System.out.println("No File found. Switching to String mode.");
                if(en){
                    for(int count=0;count<args[x].length();count++){
                        System.out.print(encrypt(args[x].charAt(count),args[1],NUMS[2].toString()));
                        if(count<args[x].length()-1)System.out.print('-');
                    }
                }else System.out.print(decrypt(args[x],args[1],NUMS[2].toString()));
            }
       }
    }
    
    private static class MakeKeys extends Thread{
        @Override
        public void run(){
            BigInteger publicKey=(new BigInteger(NUMS[3].bitLength(),new SecureRandom())).abs();
            while(check(publicKey))publicKey=(new BigInteger(NUMS[3].bitLength(),(new SecureRandom()))).abs();
            System.out.print("Public Key:"+publicKey.toString());
            BigInteger privateKey= publicKey.modInverse(NUMS[3]);
            while(check(privateKey))publicKey.modInverse(NUMS[3]);
            System.out.print(" Private Key:"+privateKey.toString()+"\n");
        }
        
        private boolean check(BigInteger num){
            return (num.compareTo(NUMS[3])>=0||num.compareTo(BigInteger.ONE)<=0)||!coPrime(NUMS[3],num);
        }
    }
    //Main method
    public static void main(String[] args)throws InterruptedException{
        if(args.length<1){
            getHelp();
            (new Thread(new menu(getInput("Option:").split(COMMANDPATTERN,-1)))).start();
        }else(new Thread(new menu(args[0].split(COMMANDPATTERN,-1)))).start();
    }
    //This is a fundamental method.
    private static boolean coPrime(BigInteger a,BigInteger b){
        return a.gcd(b).equals(BigInteger.ONE);
    }
    
    private static String[] encrypt(String msg,String publicKey,String modulus){
        String[] cipher=new String[msg.length()];
        for(int x=0;msg.length()>x;x++){
            cipher[x] = encrypt(msg.charAt(x),publicKey,modulus);
        }
        return cipher;
    }
    //This is a fundamental method
    private static String encrypt(char msg,String publicKey,String modulus){
        return (new BigInteger(String.valueOf(msg).getBytes())).modPow(new BigInteger(publicKey),new BigInteger(modulus)).toString();
    }
    
    private static String decrypt(String[]cipher,String privateKey,String modulus){
        for(int x=0;cipher.length>x;x++){
            if(x>0)cipher[0]+=decrypt(cipher[x],privateKey,modulus);
            else cipher[0]=""+decrypt(cipher[x],privateKey,modulus);
        }
        return cipher[0];
    }
    //This is a fundamental method.
    private static char decrypt(String cipher,String privateKey,String modulus){
        return new String(new BigInteger(cipher).modPow(new BigInteger(privateKey),new BigInteger(modulus)).toByteArray()).charAt(0);
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
}