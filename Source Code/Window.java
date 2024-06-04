package assPart3;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/*
 * Defines the viewable window within which the application displays the game and menu
 */
@SuppressWarnings("serial")
public class Window extends JFrame 
{
	private JPanel _menu = null;
	private Level _level = null;
	private BufferedImage _bg;
	private SoundEffect _menuMusic;
	int _playerNumber;
	Boolean _lastLaunchedNetworked;
	InetAddress _serverIP = null;
	Integer _sendPort = null;
	Integer _receivePort = null;
	
	public Window(String title)
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(875,675));
		setTitle(title);
		
		try
		{
			_menuMusic = new SoundEffect("/menu.wav");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		openMainMenu();
	}
	
	public static void main(String[] args) 
	{
		@SuppressWarnings("unused")
		Window win = new Window("Assignment Part 3");
	}
	/**
	 * @return A main menu panel
	 */
	private JPanel mainMenu()
	{
		try
		{
			_bg = ImageIO.read(getClass().getResource("/menuBG.png"));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
				
		JPanel menu = new JPanel();
		menu.setSize(875,675);
		menu.setLayout(null);
		
		JButton buttonLMP = new JButton("Local Multiplayer");
		JButton buttonNMP = new JButton("Network Multiplayer");
		JButton buttonQuit = new JButton("Exit");
		buttonLMP.setBounds(10,10,180,40);
		buttonNMP.setBounds(10,60,180,40);
		buttonQuit.setBounds(10,585,180,40);
		menu.add(buttonLMP);
		menu.add(buttonNMP);
		menu.add(buttonQuit);
		
		JLabel bg = new JLabel(new ImageIcon(_bg));
		bg.setBounds(0,0,875,675);
		menu.add(bg);
		
		buttonLMP.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				launchLevel(false, _playerNumber, _serverIP, _sendPort, _receivePort);
				_lastLaunchedNetworked = false;
			}
		});
		
		buttonNMP.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String serverIP = JOptionPane.showInputDialog("Enter IP address of server");
				
				if (serverIP != null)
				{
					try
					{
						requestJoinFromServer(serverIP);
					}
					catch (Exception s)
					{
						System.err.println("Failed to join server " + serverIP);
					}
					try 
					{
						_serverIP = InetAddress.getByName(serverIP);
					} 
					catch (UnknownHostException e1) 
					{
						System.err.println("Unknown host: " + serverIP);
						e1.printStackTrace();
					}
				}
				else
				{
					return;
				}
				
				launchLevel(true, _playerNumber, _serverIP, _sendPort, _receivePort);
				_lastLaunchedNetworked = true;
				_serverIP = null;
			}
		});
		
		buttonQuit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.exit(1);
			}
		});
		
		return menu;
	}
	
	public void launchLevel(Boolean multiplayer, Integer netPlayerNo, InetAddress serverIP, Integer sendPort, Integer receivePort)
	{
		if (_menu != null)
		{
			_menu.setVisible(false);
			remove(_menu);
			_menu = null;
			_menuMusic.stop();
		}
		if (_level != null)
		{
			_level.setVisible(false);
			remove(_level);
			_level = null;
		}
		_level = new Level(multiplayer, this, netPlayerNo, serverIP, sendPort, receivePort);
		add(_level);
		_level.setFocusable(true);
		_level.requestFocus();
	}
	
	public void openMainMenu()
	{
		if (_level != null)
		{
			_level.setVisible(false);
			remove(_level);
			_level = null;
		}
		
		if (_menu == null)
		{
			_menu = mainMenu();
		}
		
		add(_menu);
		pack();
		_menu.setFocusable(true);
		_menu.requestFocus();
		_menu.setVisible(true);
		setVisible(true);
		_menuMusic.loop();
	}
	
	@SuppressWarnings("resource")
	private void requestJoinFromServer(String serverIP)
	{
		Socket clientSocket = new Socket();
		int portTCP = 2000;
		
		//output stream and string to send to server 
		DataOutputStream outputStream = null;
		String request;
						
		//input stream from server and string to store input received from server
		BufferedReader inputStream = null;
		String responseLine;
		
		//replace "localhost" with the remote server address, if needed
		//5000 is the server port
		String serverHost = serverIP;
		
		try
		{
			clientSocket = new Socket(serverIP, portTCP);
			
			outputStream = new DataOutputStream(
				clientSocket.getOutputStream()
			);
			
			inputStream = new BufferedReader(
				new InputStreamReader(
					clientSocket.getInputStream()
				)
			);
		}
		catch (UnknownHostException e)
		{
			System.err.println("Can't find host at " + serverHost);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		if (clientSocket != null && outputStream != null && inputStream != null)
		{
			try
			{
				request = "REQ\n";
				outputStream.writeBytes(request);
								
				if((responseLine = inputStream.readLine()) != null)
				{
					if (responseLine.contains("ACK:"))
					{
						String[] splitString = responseLine.split(":");
						_playerNumber = Integer.parseInt(splitString[1]);
						_sendPort = Integer.parseInt(splitString[2]);
						_receivePort = Integer.parseInt(splitString[3]);
					}
					else
					{
						System.err.println("Received unexpected message \"" + responseLine + "\" from " + serverIP);
					}
				}
				outputStream.close();
				inputStream.close();
				clientSocket.close();
			}
			catch (UnknownHostException e)
			{
				System.err.println("Trying to connect to unknown host: " + e);
			}
			catch (IOException e)
			{
				System.err.println("IOException:  " + e);
			}
			
		}	
	}
}