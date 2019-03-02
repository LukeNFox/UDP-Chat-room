
	//luke fox
	//16342861
	import java.awt.*;
	import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
// Java extension packages
	import javax.swing.*;
	import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;


	 public class Client extends JFrame {
	
		 private Color colors[] = {Color.blue,Color.green, Color.black, Color.orange, Color.red}; // array of colours
		 
		 //components of gui
		 private Container container;
		 
		 private JLabel picture;
		
		 private JPanel topPanel;
		 private JPanel chatPanel;
		 private JPanel messagePanel;
		 
		 private BufferedImage pic;
		 
		 private JButton send;
		 private JButton colour;
		 
		 private JButton profile;
		 private JButton upload;
		 
		 private JTextField inputMessage;
		 private Object messageReceived;
		 
		 private JTextArea chatWindow;
		 private JScrollPane scrollChat;
		 
		 private ActionListener buttonHandler;
		 
		 private int colourChange = 0;
		 
		 //sockets for sending and receiving 
		 private DatagramPacket sendPacket, receivePacket;
		 private DatagramSocket socket;


		private String username;


		private ImageIcon icon;
		 
	public Client() {
		//pop up box to set username
		username = JOptionPane.showInputDialog(null, null, "Please Enter a username", 
				JOptionPane.INFORMATION_MESSAGE);
		
		//create gui
		createGui();
		
		
		File filename = new File("" + System.getProperty("user.dir") + "\\anon.jpg");
		
		try {
			//read file from computer 
			BufferedImage image = ImageIO.read(filename);
			//method to scale image to smaller sixe 
			pic = getScaledImage(image,100,100);
			 icon = new ImageIcon(pic);
			picture.setIcon(icon);
			
		} catch (IOException e) {
		}
		
		 try {
			 //initialise socket
				socket = new DatagramSocket();
			} catch (SocketException e1) {
				
			}
	} 
	 // set up GUI
	 public void createGui()
	  {
		 
		 
		 container = getContentPane();
		 
		 picture = new JLabel();
		 colour = new JButton("Colour");
		 upload = new JButton("Upload");
		 profile = new JButton("Profile");
		 
		 buttonHandler = new ButtonHandler();
		 colour.addActionListener(buttonHandler);
		 upload.addActionListener(buttonHandler);
		 profile.addActionListener(buttonHandler);
		 
		 picture.setPreferredSize(new Dimension(100,100));	
		 
		 colour.setPreferredSize(new Dimension(125,40));
		 profile.setPreferredSize(new Dimension(125,40));
		 upload.setPreferredSize(new Dimension(125,40));
		 
		 topPanel = new JPanel();
		 topPanel.add(picture);
		 topPanel.add(colour);
		 topPanel.add(upload);
		 topPanel.add(profile);
		 
		 container.add(topPanel, BorderLayout.NORTH);
		 
		 
		 chatWindow = new JTextArea(40,50);
		 chatWindow.setEditable(false);
		 scrollChat = new JScrollPane(chatWindow);
		 scrollChat.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		 
		 chatPanel = new JPanel();
		 chatPanel.add(scrollChat);
		 container.add(chatPanel, BorderLayout.CENTER);
		 
		 send = new JButton("Send");
		 send.setPreferredSize(new Dimension(125,40));
		 inputMessage = new JTextField();
		 inputMessage.setPreferredSize(new Dimension(500,50));
		 
		 messagePanel = new JPanel();
		 messagePanel.add(inputMessage);
		 messagePanel.add(send);
		 container.add(messagePanel,BorderLayout.SOUTH);
		 
		 
		 //action lisetner added to send so message in text field will be sent
		 send.addActionListener(
				 
				 new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						
						try {
							//object output stream used to send byte array output stream 
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ObjectOutputStream oos = new ObjectOutputStream(bos);
						//allows for transmission of objects
						
						//adds username onto the end of the text field
						String message = inputMessage.getText() + "	-" + username;
						//writes object to output stream 
						oos.writeObject(message);
						// convert to byte array
						byte[] data = bos.toByteArray();
						
						// send String object as a packet to the server port 
							sendPacket = new DatagramPacket(
									data,data.length,
									InetAddress.getLocalHost(), 5001
									);
							socket.send(sendPacket);
							
						} catch (IOException e2) {
						}
						
					}
				 
				 }
				 
				 
				 );
		 
		 
		 
	 setSize( 750, 900 ); 
	 setVisible( true );
	 setTitle(username+"'s Chat Window" );
	 setResizable(true);
	 

	 
	 
	 }
	 
	 
	 public static void main( String args[] )
	 {
		 //create new client
	 Client application = new Client();
	 
	 application.setDefaultCloseOperation(
	 JFrame.EXIT_ON_CLOSE );
	 //start wait for packets infinite loop 
	 application.waitForPackets();
	 }
	 
	 class ButtonHandler implements ActionListener{  //Action listener to monitor button changes

		 public void actionPerformed(ActionEvent event) 
		 {

			 if(event.getActionCommand().equals("Colour")) {  //identify which button is pressed 

				 chatPanel.setBackground(colors[colourChange]); // set background of image panel to appropriate colour in colour array 
				 topPanel.setBackground(colors[colourChange]);
				 messagePanel.setBackground(colors[colourChange]);
				 colourChange++;
				 if(colourChange == colors.length) {
					 colourChange = 0;  		//loops through array of colours

				 }
			 }

			 if(event.getSource().equals(profile)) {

				 chooseProfilePicture();
				 

			 }

			 if(event.getSource().equals(upload)) {

				 try {
					uploadToServer();
				} catch (IOException e) {
				}
			 }
		 }

	
	 }
	 private void uploadToServer() throws IOException {
		
		 //creates output streams  
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
		//writes profile picture icon to the stream 
			oos.writeObject(icon);
		    
		   byte[] data = bos.toByteArray();
		 //sends icon packet to the server 
		    sendPacket = new DatagramPacket(
					data,data.length,
					InetAddress.getLocalHost(), 5001
					);
			socket.send(sendPacket);
			
		 
	}
	 

		private void chooseProfilePicture() {

			//file chooser to select profile picture from files on laptop 
			JFileChooser fileChooser = new JFileChooser();
			//filters out files that arent jpeg
			FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG Images", "jpg");
			fileChooser.setFileFilter(filter);
		
			int returnVal = fileChooser.showOpenDialog(null);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				//sets file to the selected file  
				File file = fileChooser.getSelectedFile();
				BufferedImage image;
				try {
					//read file from computer 
					image = ImageIO.read(file);
					//method to scale image to smaller sixe 
					pic = getScaledImage(image,100,100);
					 icon = new ImageIcon(pic);
					picture.setIcon(icon);
					
				} catch (IOException e) {
				}
				
		}
		}
		 private BufferedImage getScaledImage(BufferedImage image, int w, int h){  //method for scaling image 
			 //similar method to what i used in assignment 6
				
			    BufferedImage buffImage = new BufferedImage(w, h, BufferedImage.TRANSLUCENT);
			    Graphics2D graphic = (Graphics2D) buffImage.createGraphics();
			    graphic.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
			    graphic.drawImage(image, 0, 0, w, h, null);
			    graphic.dispose();
			    return buffImage;
			}
		
		
	public void waitForPackets()
	{
		
		while(true) {
			
			
			try {
				//loop to wait for packets 
				  byte[] data = new byte[65536];
				  receivePacket = new DatagramPacket(data, data.length);
				  socket.receive(receivePacket);
				  //receive packets as objects and display 
				 ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(data));
				 messageReceived = is.readObject();
				displayPacket();
			} catch (IOException | ClassNotFoundException e) {
				
			}
		}
		
		
	}
	private void displayPacket() {
		//appened received message to chat window 
		chatWindow.append("\n" + messageReceived);
		
	}
	 }
	 
	  
