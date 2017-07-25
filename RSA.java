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
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
public class RSA{
	private static ArrayList<Thread> threads=new ArrayList<Thread>();
	private static final BigInteger[] nums={new BigInteger("15805993460899067323"),new BigInteger("9382304296585360981"),
			new BigInteger("148296640359993439204927582798176323863"),new BigInteger("148296640359993439179739285040691895560")};
	public static void main(String[] args)throws InterruptedException{
		if(args.length<1){
			getHelp();
			(new Thread(new menu(getInput("\nOption:").split(" (?=(?:[^\"]*\"([^\"]*)\")*[^\"]*$)",-1)))).start();
		}else(new Thread(new menu(args[0].split(" (?=(?:[^\"]*\"([^\"]*)\")*[^\"]*$)",-1)))).start();
	}
	private static class menu implements Runnable{
		private static String[] args;
		public menu(String[] args){
			this.args=ghettofix(args);
		}
		public void run(){
			boolean quit=false;
			while(!quit){
				if(args[0].toLowerCase().contains("keys")){
					threads.add((Thread)(new MakeKeys()));
					threads.get(threads.size()-1).start();
					try{
						threads.get(threads.size()-1).join();
					}catch(InterruptedException e){
						System.err.print(e);
					}
				}else if(args[0].toLowerCase().contains("en")){
					for(int x=2;x<args.length;x++){
						threads.add(new Thread(new CryptoMethod(args,x,true)));
						threads.get(threads.size()-1).run();
						try {
							threads.get(threads.size()-1).join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}else if(args[0].toLowerCase().contains("de")){
					for(int x=2;x<args.length;x++){
						threads.add(new Thread(new CryptoMethod(args,x,false)));
						threads.get(threads.size()-1).run();
						try {
							threads.get(threads.size()-1).join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}else if(args[0].toLowerCase().contains("quit"))quit=true;
				else getHelp();
				if(!quit)args=ghettofix(getInput("Option:").split(" (?=(?:[^\"]*\"([^\"]*)\")*[^\"]*$)",-1));
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
			this.args=args;
			this.x=x;
			this.en=en;
		}
		public void run(){
			try{
				ArrayList<String> fileStorage=new ArrayList<String>();
				String line;
				BufferedReader reader=new BufferedReader(/*(en)?*/new FileReader(args[x])/*:new InputStreamReader(new ZipInputStream(new BufferedInputStream(new FileInputStream(args[x]))))*/);
				while((line=reader.readLine())!=null) {
					String[] info = (en)?encrypt(line,args[1],nums[2].toString()):new String[]{decrypt(line.split("-"),args[1],nums[2].toString())};
					for(String data:info)fileStorage.add(data);
					fileStorage.add("ln");
				}
				reader.close();
				BufferedWriter writer=new BufferedWriter(/*(!en)?*/new FileWriter(args[x])/*:new OutputStreamWriter(new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(args[x]))))*/);
				for(int y=0;y<fileStorage.size();y++) {
					line=fileStorage.get(y);
					if(en&&y<fileStorage.size()-1&&!fileStorage.get(y).contains("ln"))writer.write(line+'-');
					else if(fileStorage.get(y).contains("ln"))writer.newLine();
					else writer.write(line);
				}
				writer. close();
			}catch(IOException e){
				System.err.println(e);
				System.out.println("No File found. Switching to String mode.");
				if(en){
					for(int count=0;count<args[x].length();count++){
						System.out.print(encrypt(args[x].charAt(count),args[1],nums[2].toString()));
						if(count<args[x].length()-1)System.out.print('-');
					}
				}else System.out.print(decrypt(args[x],args[1],nums[2].toString()));
			}
		}
	}
	private static class MakeKeys extends Thread{
		public void run(){
			BigInteger publicKey=(new BigInteger(nums[3].bitLength(),new SecureRandom())).abs();
			while(check(publicKey))publicKey=(new BigInteger(nums[3].bitLength(),(new SecureRandom()))).abs();
			System.out.print("Public Key:"+publicKey.toString());
			BigInteger privateKey= publicKey.modInverse(nums[3]);
			while(check(privateKey))publicKey.modInverse(nums[3]);
			System.out.print(" Private Key:"+privateKey.toString()+"\n");
		}
		private boolean check(BigInteger num){
			return (num.compareTo(nums[3])>=0||num.compareTo(BigInteger.ONE)<=0)||!coPrime(nums[3],num);
		}
	}
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
	private static char decrypt(String cipher,String privateKey,String modulus){
		return new String(new BigInteger(cipher).modPow(new BigInteger(privateKey),new BigInteger(modulus)).toByteArray()).charAt(0);
	}	
	private static String getInput(String quote){
		System.out.print(quote);
		return (new Scanner(System.in)).nextLine();
	}
	private static void getHelp(){
		System.out.println("Separate all input arguments with spaces.");
		System.out.println("Enter any of the following:\nEn for to encrypt(with public key first),");
		System.out.print("De to decrypt(with private key first),\nOr Keys for new keys");
		System.out.print(";followed by String or File's path names that will be used.");
	}
}