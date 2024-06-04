package assPart3;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ServerWindow extends JFrame
{
	private static InetAddress _IP = null;
	private static String _stringIP = null;
	private static JButton _localLaunch;
	private static ArrayList<InetAddress> _clientIPs = new ArrayList<InetAddress>();
	private static int[] _inPorts = {6666, 7777};
	private static int[] _outPorts = {6667, 7778};
	private static Boolean _run = false;
	private static ServerWindow _me;
	
	/**
	 * Window that acts as the game's server
	 */
	public ServerWindow(String title)
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(400,150));
		setTitle(title);
		pack();
		setVisible(true);
		_me = this;
	}
	
	public static void main(String[] args) throws SocketException
	{
		try 
		{
			//Get local IP of server
			_IP = InetAddress.getLocalHost();
		} 
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		
		_stringIP = _IP.toString();
		String[] splitIP = _stringIP.split("/");
		_stringIP = splitIP[1];
		
		ServerWindow _server = new ServerWindow("Server " + _stringIP);
		_server.add(serverPanel());
		_server.setVisible(true);
	}

	/**
	 * Panel containing the server menu
	 */
	private static JPanel serverPanel()
	{
		JPanel serverPanel = new JPanel();
		serverPanel.setLayout(null);
		JLabel title = new JLabel("Server Local IP: " + _stringIP);
		title.setBounds(100, 25, 200, 15);
		serverPanel.add(title);
		_localLaunch = new JButton("Launch Server");
		_localLaunch.setBounds(80, 50, 200, 30);
		_localLaunch.setSize(200, 30);
		serverPanel.add(_localLaunch);
		
		_localLaunch.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				_localLaunch.setText("Waiting for clients...");
				launchServer();
			}
		});
		
		return serverPanel;
	}
	
	/**
	 * Launches the server's initial setup<br>
	 * Begins UDP loop once two players have "joined" (made their addresses known) via TCP
	 */
	private static void launchServer()
	{
		_run = true;
		setPlayers();
		Thread fListener = new Thread(new FlagListener(_me));
		fListener.start();
		
		if (_clientIPs.size() == 2)
		{
			while (_run)
			{
				exchangeUDP();
			}
		}
	}
	
	/**
	 * Sets the expected IPs and ports associated with first two players to connect using TCP
	 */
	private static void setPlayers()
	{
		int portTCP = 2000;
		ServerSocket sSocket = null;
		Socket clientSocket = null;
		BufferedReader inputStream;
		DataOutputStream outputStream;
		String input;

		while (_clientIPs.size() < 2)
		{	
			try 
			{
				sSocket = new ServerSocket(portTCP);
				System.out.println("Waiting for player " + (_clientIPs.size()+1) + " to connect.");
			} 
			catch(IOException e)
			{
				e.printStackTrace();
			}
								
			try 
			{
				clientSocket = sSocket.accept();
				inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				outputStream = new DataOutputStream(clientSocket.getOutputStream());
				
				//if message content exists
				if ((input = inputStream.readLine()) != null)
				{
					if (input.equals("REQ"))
					{
						int index = _clientIPs.size();
						//send acknowledgement to client, containing their player number, the port to reach the server on and the port
						//to listen for the server on
						outputStream.writeBytes("ACK:" + (index+1) + ":" + _inPorts[index] + ":" + _outPorts[index] + "\n");
						InetAddress clientIP = clientSocket.getInetAddress();
						//store IP address
						_clientIPs.add(clientIP);
						System.out.println("Player " + _clientIPs.size() + " connected with IP address: " + clientIP);
					}
					else //if message content not expected
					{
						outputStream.writeBytes("ERROR: Unexpected message received.\n");
						System.err.println("Unexpected message received from " + clientSocket.getInetAddress());
					}
				}
				sSocket.close();
				clientSocket.close();
				inputStream.close();
				outputStream.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				_run = false;
			}
		}
	}
	/**
	 * Sends and receives data between the two clients using UDP
	 */
	private static void exchangeUDP()
	{
		for (int i = 0; i < 2; i++)
		{
			try 
			{
				DatagramSocket socket = new DatagramSocket(_inPorts[i]);
				socket.setTrafficClass(0b11100000);
				byte[] buffer = new byte[10];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				Thread cHandler = new Thread(new ClientHandler(socket, packet, _clientIPs, _inPorts[i], _inPorts, _outPorts));
				cHandler.start();
			}
			catch(Exception e)
			{
				System.err.println("SERVER WINDOW");
				e.printStackTrace();
			}
		}
	}
	/**Attempts to send final location of player that triggered closeServer() to occur.<br>
	 * Mitigates issues whereby the game may not finish for both players
	 * @param X Final X location of player to trigger closeServer()
	 * @param Y Final Y location of player to trigger closeServer(
	 * @param playerNum Player number (1 or 2)
	 */
	private static void sendFinalUDP(int X, int Y, int rotationIndex, int playerNum)
	{
		Boolean valid = true;
		Integer sendPort = null;
		InetAddress sendIP = null;
		byte[] sendBytes = (X + "$" + Y + "$" + rotationIndex).getBytes();
		
		if (playerNum == 1)
		{
			//send to player 2
			sendPort = _outPorts[1];
			sendIP = _clientIPs.get(1);
		}
		else if (playerNum == 2)
		{
			//send to player 1
			sendPort = _outPorts[0];
			sendIP = _clientIPs.get(0);
		}
		else
		{
			System.err.println("ERROR: sendFinalUDP() called with erroneous playerNum.");
			valid = false;
		}
		
		if (valid && sendPort != null)
		{	
			for (int i = 0; i < 6; i++)
			{
				try 
				{
					DatagramSocket socket = new DatagramSocket();
					DatagramPacket sendPacket = new DatagramPacket(sendBytes, sendBytes.length, sendIP, sendPort);
					socket.send(sendPacket);
					socket.close();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * Stops main loop from executing
	 */
	public void stopRunning()
	{
		_run = false;
	}
	/**
	 * Stops server loop and forces ServerWindow to close
	 */
	public void closeServer(int finalX, int finalY, int finalRotationIndex, int finalPlayerNum)
	{
		sendFinalUDP(finalX, finalY, finalRotationIndex, finalPlayerNum);
		System.exit(1);
	}
}