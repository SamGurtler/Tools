import java.security.SecureRandom;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Scanner;
import java.math.BigInteger;
import java.lang.InterruptedException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
public class RSA{
	private static ArrayList<Thread> threads=new ArrayList<Thread>();
	private static final BigInteger[] nums={new BigInteger("15805993460899067323"),new BigInteger("9382304296585360981"),
			new BigInteger("148296640359993439204927582798176323863"),new BigInteger("148296640359993439179739285040691895560")};
	public static void main(String[] args)throws InterruptedException{
		if(args.length<1){
			getHelp();
			(new Thread(new menu(getInput("\nOption:").split(",")))).start();
		}else(new Thread(new menu(args[0].split(",")))).start();
	}
	private static class menu implements Runnable{
		private static String[] args;
		public menu(String[] args){
			this.args=args;
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
						threads.add(new Thread(new CryptoMethod(args,x)));
						threads.get(threads.size()-1).run();
						try {
							threads.get(threads.size()-1).join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}else if(args[0].toLowerCase().contains("de")){
					for(int x=2;x<args.length;x++){
						threads.add(new Thread(new CryptoMethod(args,x)));
						threads.get(threads.size()-1).run();
						try {
							threads.get(threads.size()-1).join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}else if(args[0].toLowerCase().contains("quit"))quit=true;
				else getHelp();
				if(!quit)args=getInput("\nOption:").split(",");
			}
		}
	}
	private static class CryptoMethod implements Runnable{
		private static String[] args;
		private static int x;
		public CryptoMethod(String[]args,int x){
			this.args=args;	
			this.x=x;
		}
		public void run(){
			try{
				ArrayList<String> fileStorage=new ArrayList<String>();
				String line;
				BufferedReader reader=new BufferedReader(new FileReader(args[x]));
				while((line=reader.readLine())!=null)fileStorage.add(encrypt(line,args[1],nums[2].toString()));
				reader.close();
				BufferedWriter writer=new BufferedWriter(new FileWriter(args[x]));
				for(int y=0;y<fileStorage.size();y++)writer.write(fileStorage.get(y));
				writer.close();
			}catch(IOException e){
				System.out.println(encrypt(args[x],args[1],nums[2].toString()));
			} 
		}
	}
	private static class MakeKeys extends Thread{
		public void run(){
			BigInteger publicKey=(new BigInteger(/*totient*/nums[3].bitLength(),new SecureRandom())).abs();
			while((publicKey.compareTo(/*totient*/nums[3])>=0||publicKey.compareTo(BigInteger.ONE)<=0)||!coPrime(/*totient*/nums[3],publicKey))publicKey=(new BigInteger(/*totient*/nums[3].bitLength(),(new SecureRandom()))).abs();
			System.out.print("Public Key:"+publicKey.toString());
			BigInteger privateKey= publicKey.modInverse(/*totient*/nums[3]);
			System.out.print(" Private Key:"+privateKey.toString()/*+" Modulus:"+modulus*/);
		}
	}
 	private static boolean coPrime(BigInteger a,BigInteger b){
    	return a.gcd(b).equals(BigInteger.ONE);
	}
	private static String encrypt(String msg,String publicKey,String modulus){
		return (new BigInteger(msg.getBytes())).modPow(new BigInteger(publicKey),new BigInteger(modulus)).toString();
	}
	private static String decrypt(String cipher,String privateKey,String modulus){
		return new String(new BigInteger(cipher).modPow(new BigInteger(privateKey),new BigInteger(modulus)).toByteArray());
	}	
	private static String getInput(String quote){
		System.out.print(quote);
		return (new Scanner(System.in)).nextLine();
	}
	private static void getHelp(){
		System.out.println("Enter any of the following:\nEn for to encrypt(with public key first),");
		System.out.print("De to decrypt(with private key first),\nOr Keys for new keys");
		System.out.print(";followed by String or File's path names that will be used.");
	}
}