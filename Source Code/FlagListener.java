package assPart3;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Listens on the TCP port for flags indicating the server neeeds to perform some action e.g., stop
 * UDP exchange
 * @author dan_9
 */
public class FlagListener implements Runnable
{
	private int _portTCP = 2000;
	private ServerSocket _sSocket = null;
	private Socket _clientSocket = null;
	private BufferedReader _inputStream;
	private DataOutputStream _outputStream;
	private String _input;
	private ServerWindow _parent;
	
	/**
	 * Listens on the TCP port for flags and alters a parent ServerWindow's behaviour based on them
	 * @param parent The parent ServerWindow 
	 */
	public FlagListener(ServerWindow parent)
	{
		_parent = parent;
	}
	
	@Override
	public void run() 
	{
		try 
		{
			_sSocket = new ServerSocket(_portTCP);
		} 
		catch(IOException e)
		{
			e.printStackTrace();
		}
							
		try 
		{
			_clientSocket = _sSocket.accept();
			_inputStream = new BufferedReader(new InputStreamReader(_clientSocket.getInputStream()));
			_outputStream = new DataOutputStream(_clientSocket.getOutputStream());
			
			try
			{
				//if message content exists
				if ((_input = _inputStream.readLine()) != null)
				{
					if (_input.contains("STOP"))
					{
						String[] splitInput = _input.split(":");
						int x = Integer.parseInt(splitInput[1]);
						int y = Integer.parseInt(splitInput[2]);
						int rot = Integer.parseInt(splitInput[3]);
						int player = Integer.parseInt(splitInput[4]);
						//tell server to inform other player of the ending player's final position
						_parent.closeServer(x, y, rot, player);
						//send acknowledgement to the ending player that their stop request has been acknowledged and executed
						_outputStream.writeBytes("ACKANDEX\n");
					}
					else if (_input.equals("REST"))
					{
						System.out.println("RECEIVED REQUEST TO RESTART");
						//Placeholder for reset logic
					}
					else
					{
						System.err.println("Unexpected flag received by FlagListener");
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			_clientSocket.close();
			_inputStream.close();
			_outputStream.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
