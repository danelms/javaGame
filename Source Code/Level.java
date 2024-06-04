package assPart3;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Rectangle;

/**
 * Behaves as a game "level".<br>
 * <br>
 * Defines the level visually and dictates gameplay via an ActionListener
 */
@SuppressWarnings("serial")
public class Level extends JPanel implements ActionListener, KeyListener
{
	//standard level variables
	private final int FRAMERATE = 60;
	private Timer _timer = new Timer(1000/FRAMERATE, this);
	private Player _playerOne = new Player(1, 390, 525);
	private Player _playerTwo = new Player(2, 390, 575);
	private BufferedImage _biOne, _biTwo, _biCentre, _biP1Win, _biP2Win, _biGameOver, _biConWaiting;
	private Set<Integer> _pressedKeys = new HashSet<>(); 
	private int _laps[] = new int[2];
	private JLabel _p1LapsLbl = new JLabel(), _p2LapsLbl = new JLabel();
	private final CollidableRect _trackCentre = new CollidableRect(150, 200, 550, 300);
	private final CollidableRect _trackUpperWall = new CollidableRect(50, 100, 750, 1);
	private final CollidableRect _trackBottomWall = new CollidableRect(50, 600, 750, 1);
	private final CollidableRect _trackLeftWall = new CollidableRect(50, 100, 1, 500);
	private final CollidableRect _trackRightWall = new CollidableRect(800, 100, 1, 500);
	private final CollidableRect[] _trackCollidables = {_trackCentre, _trackUpperWall, _trackBottomWall, _trackLeftWall, _trackRightWall};
	private final CollidableRect _checkpoint1 = new CollidableRect(700, 500, 100, 100);
	private final CollidableRect _checkpoint2 = new CollidableRect(700, 100, 100, 100);
	private final CollidableRect _checkpoint3 = new CollidableRect(50, 100, 100, 100);
	private final CollidableRect _checkpoint4 = new CollidableRect(50, 500, 100, 100);
	private final CollidableRect _checkpoint5 = new CollidableRect(425, 500, 1, 100);
	private final CollidableRect _checkpoints[] = {_checkpoint1, _checkpoint2, _checkpoint3, _checkpoint4, _checkpoint5};
	private Boolean[] _p1Checkpoints = {false, false, false, false, false};
	private Boolean[] _p2Checkpoints = {false, false, false, false, false};
	private Boolean _p1Win = false, _p2Win = false, _gameOver = false;
	private Boolean _networked;
	private SoundEffect _musicBackground, _musicGameOver, _sfxImpact;
	private final Window _win;
	//networked level variables
	private Integer _playerToControl = null;
	private Integer _networkedPlayer = null;
	private final Player[] _players = new Player[2];
	private byte[] _bufferSend = null, _bufferReceive  = null;
	private Integer _sendPort = null;
	private Integer _receivePort = null;
	private InetAddress _serverIP = null;
	private DatagramSocket _sendSocket = null;
	private DatagramSocket _receiveSocket = null;
	private Boolean _canPlay = true;
	
	/**
	 * Instantiates a level
	 * @param networked Dictates whether or not the level should behave as local (same machine)
	 * or networked multiplayer
	 * @param win The parent window
	 * @param netPlayerNo The player number used for networked play. Should be null for local game
	 * @param serverIP The IP address of the game server. Should be null for local game
	 * @param sendPort The port number used to send data to the server
	 * @param receivePort The port number used to receive data from the server
	 * and acquired from an active server
	 */
	public Level(Boolean networked, Window win, Integer netPlayerNo, InetAddress serverIP, Integer sendPort, Integer receivePort)
	{
		_networked = networked;
		_win = win;
		if (_networked)
		{
			//Limit ability to play until receiving data from opponent
			_canPlay = false;
			_bufferSend = new byte[10]; _bufferReceive  = new byte[10];
		}
		if (netPlayerNo != null)
		{
			_playerToControl = (netPlayerNo - 1);
			//if player to be controlled's index is 0, set networked player's index to 1. Otherwise, set it to 0
			_networkedPlayer = (_playerToControl == 0) ? 1 : 0;
			_players[0] = _playerOne; _players[1] = _playerTwo;
		}
		else
		{
			_players[0] = null; _players[1] = null;
		}
		if (serverIP != null)
		{
			_serverIP = serverIP;
		}
		if (sendPort != null)
		{
			_sendPort = sendPort;
			try 
			{
				_sendSocket = new DatagramSocket();
				_sendSocket.setTrafficClass(0b11100000); // Set traffic to high priority
			}
			catch (SocketException e)
			{
				e.printStackTrace();
			}
		}
		if (receivePort != null)
		{
			_receivePort = receivePort;
			try
			{
				_receiveSocket = new DatagramSocket(_receivePort);
				_receiveSocket.setSoTimeout(1);
			}
			catch (SocketException e)
			{
				e.printStackTrace();
			}
		}
		
		//Get graphical resources
		try
		{
			_biCentre = ImageIO.read(getClass().getResource("/trackCentre.png"));
			_biP1Win = ImageIO.read(getClass().getResource("/p1Win.png"));
			_biP2Win = ImageIO.read(getClass().getResource("/p2Win.png"));
			_biGameOver = ImageIO.read(getClass().getResource("/gameOver.png"));
			if (_networked)
			{
				_biConWaiting = ImageIO.read(getClass().getResource("/connectionWaiting.png"));
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		//get audio resources
		try
		{
			_musicBackground = new SoundEffect("/slapBass.wav");
			_musicGameOver = new SoundEffect("/gameOver.wav");
			_sfxImpact = new SoundEffect("/impact.wav");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		addKeyListener(this);
		setFocusable(true);
		setLayout(null);
		_laps[0] = 0; _laps[1] = 0;
		Font gameFont = new Font("Arial", Font.BOLD, 15);
		_p1LapsLbl.setText("P1 Laps: " + _laps[0] + "/5");
		_p1LapsLbl.setFont(gameFont);
		_p2LapsLbl.setText("P2 Laps: " + _laps[1] + "/5");
		_p2LapsLbl.setFont(gameFont);
		_p1LapsLbl.setBounds(50,25,100,25);
		_p2LapsLbl.setBounds(50,50,100,25);
		add(_p1LapsLbl);
		add(_p2LapsLbl);
		_musicBackground.loop();
		_timer.start();
	}
	/**
	 * Triggered by the timer (once per frame)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (_networked && _canPlay)
		{
			handlePlayerInputs(_players[_playerToControl], KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S);
			_players[_playerToControl].updatePosition();
		}
		if (_networked)
		{
			sendAndReceiveServerData(_players[_playerToControl], _players[_networkedPlayer]);
		}
		else
		{
			handlePlayerInputs(_playerOne, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S);
			handlePlayerInputs(_playerTwo, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN);
			//Update the cars' positions
	        _playerOne.updatePosition();
	        _playerTwo.updatePosition();
		}

        //Check for collisions and update behaviour if there are any
        checkForCollisions();
        //Draw current frame
		repaint();
	}
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		paintTrack(g);
		_biOne = _playerOne.getSprite();
		_biTwo = _playerTwo.getSprite();
		if (_p1Win)
		{
			g.drawImage(_biP1Win, 225, 325, null);
		}
		if (_p2Win)
		{
			g.drawImage(_biP2Win, 225, 325, null);
		}
		if (_gameOver)
		{
			g.drawImage(_biGameOver, 225, 325, null);
		}
		if (!_canPlay)
		{
			g.drawImage(_biConWaiting, 225, 325, null);
		}
		g.drawImage(_biOne, _playerOne.getXPos() - _biOne.getWidth()/2, _playerOne.getYPos() - _biOne.getHeight()/2, null);
		g.drawImage(_biTwo, _playerTwo.getXPos() - _biTwo.getWidth()/2, _playerTwo.getYPos() - _biTwo.getHeight()/2, null);
	}
	/**
	 * Draws the track using a Graphics object
	 * @param g Graphics object
	 */
	private void paintTrack(Graphics g)
	{
		Color c = Color.DARK_GRAY;
		g.setColor(c);
		g.fillRect(50, 100, 750, 500); //tarmac
		g.drawImage(_biCentre, 150, 200, null); //grass 
		Color c2 = Color.black;
		g.setColor( c2 ); 
		g.drawRect( 50, 100, 750, 500 ); //outer edge 
		g.drawRect( 150, 200, 550, 300 ); //inner edge 
		Color c3 = Color.yellow; 
		g.setColor( c3 ); 
		g.drawRect( 100, 150, 650, 400 ); //mid-lane marker 
		Color c4 = Color.white; 
		g.setColor( c4 ); 
		g.drawLine( 425, 500, 425, 600 ); //start line		
	}
	@Override
	public void keyPressed(KeyEvent e)
	{
		int key = e.getKeyCode();
        _pressedKeys.add(key);
	}
	@Override
	public void keyReleased(KeyEvent e) 
	{
		int key = e.getKeyCode();
        _pressedKeys.remove(key);
	}
	@Override
	public void keyTyped(KeyEvent e) 
	{
		// Unused but needed for KeyListener	
	}
	/**
	 * Checks for inputs tied to the control of a Player and executes control logic if they're found
	 * @param p Player to be controller
	 * @param left Keyevent code tied to turning left e.g. left arrow
	 * @param right Keyevent code tied to turning right e.g. right arrow
	 * @param up Keyevent code tied to increasing speed e.g. up arrow
	 * @param down Keyevent code tied to decreasing speed e.g. down arrow
	 */
	private void handlePlayerInputs(Player p, int left, int right, int up, int down)
	{
		if (_pressedKeys.contains(left))
        {p.turnLeft();}
		if (_pressedKeys.contains(right))
        {p.turnRight();}
		if (_pressedKeys.contains(up))
    	{p.increaseSpeed();}
		if (_pressedKeys.contains(down))
    	{p.decreaseSpeed();}
	}
	/**
	 * Checks for collisions between collidable objects
	 */
	private void checkForCollisions()
	{
		int[] p1Bound = _playerOne.getBoundary(), p2Bound = _playerTwo.getBoundary();
		Rectangle p1Box = new Rectangle(p1Bound[0], p1Bound[1], p1Bound[2], p1Bound[3]);
		Rectangle p2Box = new Rectangle(p2Bound[0], p2Bound[1], p2Bound[2], p2Bound[3]);
		
		//Check if player 1 and player 2's boundaries intersect (if cars are close)
		if (p1Box.intersects(p2Box))
		{
			//Check for pixel-perfect collision between players' collision masks
			if (objectsCollided(_playerOne.getCollisionMask(), p1Bound[0], p1Bound[1],_playerTwo.getCollisionMask(), p2Bound[0], p2Bound[1]))
			{
				_sfxImpact.play();
				_gameOver = true;
				_musicBackground.stop();
				_musicGameOver.play();
				endGame();
			}
		}
		//For both players if local, for locally controlled player if networked
		for (int p = 1; p<=2; p++)
		{
			Player player = null;
			Rectangle playerBox = null;
			int[] playerBound = new int[4];
			
			if (p == 1)
			{
				player = _playerOne;
				playerBox = p1Box;
			}
			if (p == 2)
			{
				player = _playerTwo;
				playerBox = p2Box;
			}
			//For CollidableRect objects relating to solid collisions
			for (int i = 0; i < _trackCollidables.length; i++)
			{
				//Get bounding box from CollidableRect
				int[] collBound = _trackCollidables[i].getBoundary();
				Rectangle collBox = new Rectangle(collBound[0], collBound[1], collBound[2], collBound[3]);
				
				//If CollidableRect and player are intersecting
				if (collBox.intersects(playerBox))
				{	
					//Generate 1 filled-mask the size of CollidableRect to represent solid object
					int[][] tcMask = new int[collBound[3]][collBound[2]];
					for (int[] row : tcMask) 
					{
					    Arrays.fill(row, 1);//Fill row of mask with 1s for solid rectangle
					}
					
					if (objectsCollided(player.getCollisionMask(), playerBox.x, playerBox.y, tcMask, collBound[0], collBound[1]))
					{
						player.crashRebound(collBound[0], collBound[1], collBound[2]);
						//Additional adjustment if needed to eliminate cars being able to overlap sides of track post-collision
						if (objectsCollided(player.getCollisionMask(), playerBox.x, playerBox.y, tcMask, collBound[0], collBound[1]))
						{
							forLoop : for (int j = 0; j<10; j++)
							{
								correctPosition(player, i);
								playerBound = player.getBoundary();
								//if no longer colliding
								if (!objectsCollided(player.getCollisionMask(), playerBound[0], playerBound[1], tcMask, collBound[0], collBound[1]))
								{
									break forLoop;
								}
							}
						}
						_sfxImpact.play();
					}
				}
			}
		}
		//For CollidableRect objects representing checkpoints
		for (int i = 0; i < _checkpoints.length; i++)
		{
			//Get bounding box from CollidableRect
			int[] collBound = _checkpoints[i].getBoundary();
			Rectangle collBox = new Rectangle(collBound[0], collBound[1], collBound[2], collBound[3]);
			//If checkpoint not already acquired by player
			if (!_p1Checkpoints[i])
			{
				//If CollidableRect and player are intersecting
				if (collBox.intersects(p1Box))
				{	
					//Generate 1 filled-mask the size of CollidableRect to represent solid object
					int[][] tcMask = new int[collBound[3]][collBound[2]];
					for (int[] row : tcMask) 
					{
					    Arrays.fill(row, 1);//Fill row of mask with 1s for solid rectangle
					}
					//If collided
					if (objectsCollided(_playerOne.getCollisionMask(), p1Bound[0], p1Bound[1], tcMask, collBound[0], collBound[1]))
					{
						if (i != 0)
						{
							if (_p1Checkpoints[i-1])
							{
								_p1Checkpoints[i] = true;
								checkLaps();
							}
						}
						else
						{
							_p1Checkpoints[i] = true;
						}
					}
				}
			}
			if (!_p2Checkpoints[i])
			{
				//If CollidableRect and player are intersecting
				if (collBox.intersects(p2Box))
				{	
					//Generate 1 filled-mask the size of CollidableRect to represent solid object
					int[][] tcMask = new int[collBound[3]][collBound[2]];
					for (int[] row : tcMask) 
					{
					    Arrays.fill(row, 1);//Fill row of mask with 1s for solid rectangle
					}
					//If collided
					if (objectsCollided(_playerTwo.getCollisionMask(), p2Bound[0], p2Bound[1], tcMask, collBound[0], collBound[1]))
					{
						if (i != 0)
						{
							if (_p2Checkpoints[i-1])
							{
								_p2Checkpoints[i] = true;
								checkLaps();
							}
						}
						else
						{
							_p2Checkpoints[i] = true;
						}
					}
				}
			}
		}
	}
	/**
	 * Corrects the Player's position to eliminate instances where a Player can overlap a CollidableRect after
	 * rebounding off of it
	 * @param p The Player to adjust
	 * @param collIndex Index of CollidableRect in array of CollidableRects
	 */
	private void correctPosition(Player p, int collIndex)
	{
		int oldX = p.getXPos(), oldY = p.getYPos(), newX = oldX, newY = oldY;
		
		//if the car is colliding with the centre of the course
		if (collIndex == 0)
		{
			int[] collBound = _trackCentre.getBoundary();
			//if the car hit the top of the centre
			if (oldY < collBound[1])
			{
				newY = oldY - 1;
			}
			//if the car hit the bottom of the centre
			if (oldY > collBound[1] + collBound[3])
			{
				newY = oldY + 1;
			}
			//if the car hit the left of the centre
			if (oldX < collBound[0])
			{
				newX = oldX - 1;
			}
			//if the car hit the right of the centre
			if (oldX > collBound[0] + collBound[2])
			{
				newX = oldX + 1;
			}
		}
		//if the car is colliding with the northern-most side of the course
		if (collIndex == 1)
		{
			newY = oldY + 1;
		}
		//if the car is colliding with the southern-most side of the course
		if (collIndex == 2)
		{
			newY = oldY - 1;
		}
		//if car is colliding with the western-most side of the course
		if (collIndex == 3)
		{
			newX = oldX + 1;
		}
		//if the car is colliding with the eastern-most side of the course
		if (collIndex == 4)
		{
			newX = oldX - 1;
		}
		p.updatePosition(newX, newY);
	}
	/**
	 * Updates laps if either player has acquired all checkpoints.
	 */
	private void checkLaps()
	{
		if (_p1Checkpoints[4])
		{
			_laps[0] += 1;
			_p1LapsLbl.setText("P1 Laps: " + _laps[0] + "/5");
			Arrays.fill(_p1Checkpoints, false);
			checkForWinner();
		}
		if (_p2Checkpoints[4])
		{
			_laps[1] += 1;
			_p2LapsLbl.setText("P2 Laps: " + _laps[1] + "/5");
			Arrays.fill(_p2Checkpoints, false);
			checkForWinner();
		}
	}
	/**
	 * If either player has reached 5 laps, ends the game and declares a winner
	 */
	private void checkForWinner()
	{
		if (_laps[0] == 5)
		{
			_p1Win = true;
			endGame();
		}
		if (_laps[1] == 5)
		{
			_p2Win = true;
			endGame();
		}
	}
	/**
	 * Displays end-game options and stops the timer (stops the gameplay loop)
	 */
	private void endGame()
	{
		JButton buttonQuit = new JButton("Quit to main menu");
		JButton buttonRestart = new JButton("Restart race");
		buttonQuit.setBounds(180,420,240,50);
		buttonRestart.setBounds(430,420,240,50);
		
		buttonQuit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				_musicBackground.stop();
				_musicGameOver.stop();
				_win.openMainMenu();
				closeSockets();
			}
		});
		
		buttonRestart.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				_musicBackground.stop();
				_musicGameOver.stop();
				_win.launchLevel(_networked, _playerToControl, _serverIP, _sendPort, _receivePort);
			}
		});
		
		add(buttonQuit);
		if (!_networked)
		{
			add(buttonRestart);
		}
		if (_networked)
		{
			sendTCPFlag("STOP" + ":" + _players[_playerToControl].getXPos() + ":" + _players[_playerToControl].getYPos() + ":" + _players[_playerToControl].getRotationIndex() + ":" + (_playerToControl + 1));
		}
		_timer.stop();
	}
	/**
	 * Sends data necessary for networked play to the server<br>
	 * Receives data necessary necessary for networked play from the server<br>
	 * Sets opponent's position using data received from server
	 * @param local the Player being controlled from this instance of the game
	 * @param networked the Player being controlled in the opponent's instance of the game
	 */
	private void sendAndReceiveServerData(Player local, Player networked)
	{
		try 
		{
			_sendSocket = new DatagramSocket();
			//SENDING
			//add data required by opponent's program to buffer
			_bufferSend = (local.getXPos() + "$" + local.getYPos() + "$" + local.getRotationIndex()).getBytes();
			DatagramPacket packet = new DatagramPacket(_bufferSend, _bufferSend.length, _serverIP, _sendPort);
			_sendSocket.send(packet);
			_sendSocket.close();
			
			//RECEIVING
			DatagramPacket receivePacket = new DatagramPacket(_bufferReceive, _bufferReceive.length);
			_receiveSocket.receive(receivePacket);
			//Convert received data to Strings
			String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
			String[] receivedDataSplit = receivedData.split("\\$");
			//Update position of opponent
			networked.updatePosition(Integer.parseInt(receivedDataSplit[0]), Integer.parseInt(receivedDataSplit[1]), Integer.parseInt(receivedDataSplit[2]));
			//First packet has been received by opponent, so allow gameplay to commence
			if (!_canPlay)
			{
				_canPlay = true;
			}
		}
		catch (SocketTimeoutException t)
		{
			System.err.println("Player at index " + _playerToControl + " timed out waiting on data from server");
		}
		catch (BindException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			System.err.println("Error sending and receiving positional data from server.");
			e.printStackTrace();
		}
	}
	@SuppressWarnings("resource")
	private void sendTCPFlag(String flag)
	{
		Socket clientSocket = new Socket();
		DataOutputStream outputStream = null;
		BufferedReader inputStream = null;
		String responseLine;
		
		try
		{
			clientSocket = new Socket(_serverIP, 2000);
			
			outputStream = new DataOutputStream(
				clientSocket.getOutputStream());
			
			inputStream = new BufferedReader(
				new InputStreamReader(
					clientSocket.getInputStream()));
		}
		catch (UnknownHostException e)
		{
			System.err.println("Can't find server at " + _serverIP);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		if (clientSocket != null && outputStream != null && inputStream != null)
		{
			try 
			{
				clientSocket.setSoTimeout(20);
			} 
			catch (SocketException e1) 
			{
				e1.printStackTrace();
			}
			
			try
			{
				outputStream.writeBytes(flag + "\n");
								
				if((responseLine = inputStream.readLine()) != null)
				{
					if (!responseLine.equals("ACKANDEX"))
					{
						System.err.println("Server failed to respond with acknowledgement of execution");
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
			catch (SocketTimeoutException e)
			{
				System.err.println("No response from server, assumed closed.");
			}
			catch (IOException e)
			{
				System.err.println("IOException:  " + e);
			}
		}
	}
	//Closes the sending and receiving DatagramSockets
	private void closeSockets()
	{
		if (_sendSocket != null)
		{
			_sendSocket.close();
		}
		if (_receiveSocket != null)
		{
			_receiveSocket.close();
		}
	}
	/**
	 * Compares two collision masks to check if a collision has occured between the objects they represent 
	 * @param mask1 Collision mask of the first object
	 * @param mask2 Collision mask of the second object
	 * @return true if a collision has occured, otherwise false
	 */
	private boolean objectsCollided(int[][] mask1, int x1, int y1, int[][] mask2, int x2, int y2)
	{
		boolean collision = false;
		//Define psoition of first mask
		int left1 = x1, right1 = left1 + mask1[0].length;
		int top1 = y1, bottom1 = top1 + mask1.length;
		//Define position of second mask
		int left2 = x2, right2 = left2 + mask2[0].length;
		int top2 = y2, bottom2 = top2 + mask2.length;
		//Define area of overlap
		int left = Math.max(left1, left2), right = Math.min(right1,right2), bottom = Math.min(bottom1, bottom2), top = Math.max(top1, top2);
		//Determine no. of columns and rows to be checked in masks
		int columns = right - left + 1;
		int rows = bottom - top + 1;
		
		// Iterate over the intersecting region and check for matching 1s in collision masks
	    outerLoop: for (int i = 0; i < rows; i++) 
	    {
	        for (int j = 0; j < columns; j++) 
	        {
	            int mask1X = left + j - x1;
	            int mask1Y = top + i - y1;
	            int mask2X = left + j - x2;
	            int mask2Y = top + i - y2;

	            // Check if the indices are within the bounds of the masks
	            if (mask1X >= 0 && mask1X < mask1[0].length && mask1Y >= 0 && mask1Y < mask1.length &&
	                mask2X >= 0 && mask2X < mask2[0].length && mask2Y >= 0 && mask2Y < mask2.length) 
	            {
	                // Check for collision at the corresponding positions in the masks
	                if (mask1[mask1Y][mask1X] == 1 && mask2[mask2Y][mask2X] == 1) 
	                {
	                    collision = true;
	                    break outerLoop;
	                }
	            }
	        }
	    }
	    return collision;
	}
}

