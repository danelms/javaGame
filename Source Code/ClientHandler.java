package assPart3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class ClientHandler implements Runnable 
{
	final private DatagramSocket _sendSocket;
	final private DatagramPacket _incomingPacket;
	private ArrayList<InetAddress> _clientIPs = new ArrayList<InetAddress>();
	final private int _receivingPort;
	final private int[] _clientPorts;
	final private int[] _outPorts;

	/**
	 * Handles the distribution of packets between two known clients, identified by their combined IP and port number
	 * @param packet Inbound packet to handle
	 * @param clientIPs The IP addresses of the clients
	 * @param clientPorts The port number allocated to the client by the server
	 */
    public ClientHandler(DatagramSocket sendSocket, DatagramPacket packet, ArrayList<InetAddress> clientIPs, int receivingPort, int clientPorts[], int outPorts[])
    {
        _sendSocket = sendSocket;
    	_incomingPacket = packet;
        _clientIPs = clientIPs;
        _clientPorts = clientPorts;
        _receivingPort = receivingPort;
        _outPorts = outPorts;
    }
	
	@Override
	public void run() 
	{
		try
		{
			//Get IP address of sender
			InetAddress clientIP = _incomingPacket.getAddress();
			Integer destination = null; //placeholder value
			
			//if received from expected address & port for player 1
			if (clientIP.equals(_clientIPs.get(0)) && _receivingPort == _clientPorts[0] )
			{
				//set player 2 as destination
				destination = 1;
			}
			//if received from expected address & port for player 2
			else if (clientIP.equals(_clientIPs.get(1)) && _receivingPort == _clientPorts[1])
			{
				//set player 1 as destination
				destination = 0;
			}
			
			//DatagramSocket sendSocket = new DatagramSocket(_clientPorts[destination]);
			if (destination != null)
			{
				DatagramPacket sendPacket = new DatagramPacket(_incomingPacket.getData(), _incomingPacket.getData().length, _clientIPs.get(destination), _outPorts[destination]);
				_sendSocket.setReuseAddress(true);
				_sendSocket.send(sendPacket);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			_sendSocket.close();
		}
	}
}
