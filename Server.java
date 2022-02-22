import java.io.*;
import java.net.*;
import java.security.*;

class Server {

    public static void main(String [] args) throws Exception {
    
    	int port = Integer.parseInt(args[0]);
		int postNum = 0;
		String allposts = null;
		
    	ServerSocket ss = new ServerSocket(port);
    	System.out.println("Waiting incoming connection...");
		
    	while(true) {
			Socket s = ss.accept();
    		DataInputStream dis = new DataInputStream(s.getInputStream());
    		DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			
			String message = null;
    		String x = null;
			String fileName = null;
			String sign = null;
			message = "There are " + postNum + " posts.\n\n";
			dos.writeUTF(message+allposts);

    		try {
				while ((x = dis.readUTF()) != null) {
					if(x.contains("userid")){
						fileName = x.substring(6);
						//System.out.println(fileName);
					}else if(x.contains("sign")){
						sign = x.substring(4);
					}else{
						try{
							// Get private key to create the signature
							ObjectInputStream keyIn = new ObjectInputStream(new FileInputStream(fileName+".prv"));
							PrivateKey privateKey = (PrivateKey)keyIn.readObject();
							keyIn.close();
					
							// create signature
							Signature sig = Signature.getInstance("SHA1withRSA");
							sig.initSign(privateKey);
							sig.update(sign.getBytes());
							byte[] signature = sig.sign();
					
							//read public key to verify signature
							keyIn = new ObjectInputStream(new FileInputStream(fileName+".pub"));
							PublicKey publicKey = (PublicKey)keyIn.readObject();
							keyIn.close();
					
							// verify signature
							sig.initVerify(publicKey);
							sig.update(sign.getBytes());
							boolean b = sig.verify(signature);
							if (b) System.out.println("Signature verified");
							else System.out.println("Signature not verified");
							postNum++;
							allposts += x + "\n";
							System.out.println(x);
						}
						catch(IllegalArgumentException e){
							// IllegalArgumentException 
							// BadPaddingException
							System.err.println("Base64 conversion does not result in an IllegalArgumentException");
						}
						// catch(BadPaddingException e){
						// 	System.err.println("or the decryption does not result in a BadPaddingException");
						// }
					}
					
    			}
    		}
    		catch(IOException e) {
    			System.err.println("Client closed its connection.");
    		}
    	}
    }
}

