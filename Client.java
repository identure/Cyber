import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import javax.crypto.*;
import java.nio.file.*;

class Client {

	public static void main(String [] args) throws Exception {
	
		String host = args[0]; // hostname of server
		int port = Integer.parseInt(args[1]); // port of server
		String userid = args[2]; // userid of server
		String sender = userid;
		// Int postsNum = 0;
		//String message = null;

		if (args.length != 3) {
			System.err.println("Usage: java RSAKeyGen userid");
			System.exit(-1);
		}

		Socket s = new Socket(host, port);
		DataOutputStream dos = new DataOutputStream(s.getOutputStream());
		DataInputStream dis = new DataInputStream(s.getInputStream());

		// To generate the secret key
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		// kpg.initialize(2048);
		kpg.initialize(1024);
		KeyPair kp = kpg.genKeyPair();

		ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(args[2] + ".pub"));
		objOut.writeObject(kp.getPublic());
		PublicKey pubKey = kp.getPublic();
		objOut.close();
		objOut = new ObjectOutputStream(new FileOutputStream(args[2] + ".prv"));
		PrivateKey privKeys = kp.getPrivate();
		objOut.writeObject(kp.getPrivate());

		// decrypt
		// Cipher cipher = Cipher.getInstance("RSA");
		// cipher.init(Cipher.DECRYPT_MODE, privKeys);
		// byte[] stringBytes = cipher.doFinal(raw);
		// String result = new String(stringBytes, "UTF8");
		// System.out.println(result);

		// print all posts in server
		System.out.print(dis.readUTF());
	
		Scanner sc = new Scanner(System.in);
		String aLine = null;
		String newaLine = null;	
		String msgEncLine = null;		

		

		// client input
		BufferedReader resYNin = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Do you want to add a post? [y/n]");
		String resYN = resYNin.readLine();
		if(resYN.equals("y")||resYN.equals("Y")){
			System.out.println("Enter recipent (type 'all' for posting without encryption):");
			String resRecpt = resYNin.readLine();
			if(resRecpt.equals("all")){
				System.out.println("Enter your message:");
				Date date = new Date();
				while ((aLine = resYNin.readLine()) != null) {
					newaLine = "Sender: " + sender + "\n" + "Date: " + date + "\n" + "Messaage: " + aLine + "\n";
					// Get private key to create the signature
					ObjectInputStream keyIn = new ObjectInputStream(new FileInputStream(args[2]+".prv"));
					PrivateKey privateKey = (PrivateKey)keyIn.readObject();
					keyIn.close();
					// create signature
					Signature sig = Signature.getInstance("SHA1withRSA");
					sig.initSign(privateKey);
					sig.update(newaLine.getBytes());
					byte[] signature = sig.sign();
					dos.writeUTF("userid"+userid);
					dos.writeUTF("sign"+Arrays.toString(signature));
					dos.writeUTF(newaLine);
					System.exit(-1);
				}
			}else{
				// To someone privately, both the recipient and the message are encrypted
				System.out.println("Enter your message:");
				while ((aLine = resYNin.readLine()) != null) {
					String msgEncrypt = aLine;
					Date date = new Date();
					
					// encrypt
					Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
					cipher.init(Cipher.ENCRYPT_MODE, pubKey);
					byte[] raw = cipher.doFinal(msgEncrypt.getBytes("UTF8"));
					Base64.Encoder encoder = Base64.getEncoder();
					msgEncLine = encoder.encodeToString(raw);
					newaLine = "Sender: " + sender + "\n" + "Date: " + date + "\n" + "Messaage: " + msgEncLine + "\n";
					// Get private key to create the signature
					ObjectInputStream keyIn = new ObjectInputStream(new FileInputStream(args[2]+".prv"));
					PrivateKey privateKey = (PrivateKey)keyIn.readObject();
					keyIn.close();
					// create signature
					Signature sig = Signature.getInstance("SHA1withRSA");
					sig.initSign(privateKey);
					sig.update(newaLine.getBytes());
					byte[] signature = sig.sign();
					dos.writeUTF("userid"+userid);
					dos.writeUTF("sign"+Arrays.toString(signature));
					dos.writeUTF(newaLine);
					System.exit(-1);
				}
			}
			
		}else{
			System.exit(-1);
		}
		
		
	}
}

