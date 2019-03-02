
	//luke fox
	//16342861
	import java.awt.*;
	import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import javax.imageio.ImageIO;
// Java extension packages
	import javax.swing.*;
	import javax.swing.event.*;


public class Server extends JFrame {
	//set up gui 
	 private Container container;
	 
	 private JPanel chatPanel;

	 private JTextArea chatWindow;
	 private JScrollPane scrollChat;
	 
	 //socket for server 
	 private ServerSocket server;
	 
	 private DatagramPacket sendPacket, receivePacket, welcomePacket;
	 private DatagramSocket socket;
	
	 //list to hold client ports 
	 private ArrayList<Integer> clients;
	 //list to store history 
	 private static LinkedList history = new LinkedList();       
	
	 private boolean newClient = false;

	private int port;
	private static int imageNum = 0;

	 private Object messageReceived;
	 
	 public Server() {
		 //create gui, initlise client list and set up server sockets 
		 createGui();
		 clients = new ArrayList<Integer>(); 
		 
		 try {
			socket = new DatagramSocket(5001);
			server = new ServerSocket(5001,10);
		} catch (IOException e) {
		
		}
	 }
	 
	 
	 public void createGui()
	  {
		 container = getContentPane();


		 
		 chatWindow = new JTextArea(40,50);
		 chatWindow.setEditable(false);
		 scrollChat = new JScrollPane(chatWindow);
		 scrollChat.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		 
		 chatPanel = new JPanel();
		 chatPanel.add(scrollChat);
		 container.add(chatPanel, BorderLayout.CENTER);
		 
		 
		 
		 
	 setSize( 650, 700 ); 
	 setVisible( true );
	 setTitle("Server Window" );
	 setResizable(true);
	 
	 }
	 public static void main( String args[] )
	 {
	 Server application = new Server();
	 
	 application.setDefaultCloseOperation(
	 JFrame.EXIT_ON_CLOSE );
	 //start wait for packets
	 application.waitForPackets();
	 }
	 
	 public void waitForPackets() {
		 
		 while(true) {
			
			  try {
				  
				  //take in received packet and read to an object 
				  byte[] data = new byte[65536];
				  receivePacket = new DatagramPacket(data, data.length);
				  socket.receive(receivePacket);
				 ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(data));
				 messageReceived = is.readObject();
				
				 //check if object  received is a string
			   if(messageReceived instanceof String) {
				  if(clients.isEmpty()) {
				  //if client list empty, add new user to list 
				  newUser();
				  }
				  else {
					  // assume new user 
					  boolean newUser = true;
					  // check list for user  by comparing ports on list to that recived packet port
					 for(Object e:clients){
						 
					   port = (int)e;
					   
					  if(receivePacket.getPort() == port) {
						  // if port found on list set new user to false and break 
						  newUser = false;
						  break;
					  }
					 
				  }if(newUser == true) {
					  //if new user, send them the history and call new user method
					  sendHistory();
					  newUser();
				  }
						  
					  }
					
				  
				 //send packet received to all clients 
				  sendPacketToClients();
				  //add message to the history linked lis 
				  history.add(messageReceived);
				  //display message on server window 
				  displayPacket();
				}else {
					//if object recived is not a string it must be an image
					 chatWindow.append("\n image recieved");
					 //call imagehandler method to deal with image 
					 imageHandler();
					 
				 }
				  
			} catch (IOException | ClassNotFoundException e) {
				
			}
			 
			 
		 }
		 
		 
	 }


	private void sendPacketToClients() throws IOException {
//method to loop through client list sending message to each port.
		for(Object e:clients){
			//cast object e to an integer port 
			int port =  (int) e;
			
			try {
				//create streams
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				
				//write message and convert to byte array 
				oos.writeObject(messageReceived);
				byte[] data = bos.toByteArray();
				
				//send packet to each client 
					sendPacket = new DatagramPacket(
							data,data.length,
							InetAddress.getLocalHost(), port
							);
					socket.send(sendPacket);
				} catch (IOException e2) {
				}
			
		}
		
		
	}


	private void displayPacket() {
	//append message to server window 
		chatWindow.append("\n" + messageReceived);
		
	}
	 

	
	private void sendHistory() throws IOException {
		
		// loops through history list, sending all previous messages to the new user 
		for(Object e: history ) {
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				
				oos.writeObject(e);
				byte[] data = bos.toByteArray();
				
				sendPacket = new DatagramPacket(data,data.length,
						receivePacket.getAddress(),receivePacket.getPort());
				socket.send(sendPacket);
				} catch (IOException e2) {
				}
		}
	}

	 
	 private void newUser() {
		 
		 // new user port added to client list 
		 clients.add(receivePacket.getPort());
		 // takes packet received and splits it into message and username using split function
		 String s = new String(receivePacket.getData(), 0 , receivePacket.getLength());
		 String words[] = s.split("-");
		 String username = words[1];
		 // create string for welcome message 
		String message = "--------------- welcome to the chatroom " + username +" ---------------";
		
		try {
			//send welcome message to new user 
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			
			oos.writeObject(message);
			byte[] data = bos.toByteArray();
			
			sendPacket = new DatagramPacket(data,data.length,
					receivePacket.getAddress(),receivePacket.getPort());
			socket.send(sendPacket);
			} catch (IOException e2) {
			}
		// tell server window that new user has connected 
		 chatWindow.append("\n New User has connected : "
					+ username);
		 // string to inform other cleints that new client has joined
		 String m = username + " joined the chatroom!";
		 try {
		 ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
			
			
			oos.writeObject(m);
			byte[] data = bos.toByteArray();
			for(Object e:clients){
				// loop through client list 
				
				int port =  (int) e;
				//make sure connection message isnt sent to the new user 
				if(port != receivePacket.getPort()) {
				sendPacket = new DatagramPacket(data,data.length,InetAddress.getLocalHost(),port);
				
					socket.send(sendPacket);
				}
			}
				} catch (IOException e1) {
				}
				
				
				
			}
	
	 
	 private void imageHandler() throws IOException {
		 // class to handle uploaded image 
		 imageNum++;
		 //increment imagenum, unique image code 
		 // cast object received  to an image icon and then get the image 
		 ImageIcon rx = ((ImageIcon) messageReceived);
		 Image image = rx.getImage();
		 RenderedImage rendered = null;
		 // if else to ensure image is in correct format before svaing 
		 if (image instanceof RenderedImage)
		 {
		     rendered = (RenderedImage)image;
		 }
		 else
		 {
		     BufferedImage buffered = new BufferedImage(
		    		 rx.getIconWidth(),
		    		 rx.getIconHeight(),
		         BufferedImage.TYPE_INT_RGB
		     );
		     Graphics2D g = buffered.createGraphics();
		     g.drawImage(image, 0, 0, null);
		     g.dispose();
		     rendered = buffered;
		 }
		 // creates date string using local date 
		 LocalDate date = LocalDate.now();
		 String datestring = "" + date.getDayOfMonth() + " " + date.getMonthValue() + " " + date.getYear();
		 //creates file name using current directory, unique image number and the date string
		 String filename = "" + System.getProperty("user.dir") + "\\" + imageNum +  "_" + datestring + ".jpg";
		 // write the image to the computer at the selected location 
		 ImageIO.write(rendered, "jpg", new File(filename));

	 }
	 
}
