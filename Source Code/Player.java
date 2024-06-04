package assPart3;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Class representing a "Player"<br>
 * <br>
 * Allows the instantiation and manipulation of a player-controlled car in the game
 */
public class Player 
{
	private float _potentialRotations[] = {0f, 22.5f, 45f, 67.5f, 90f,
											112.5f, 135f, 157.5f, 180f,
											202.5f, 225f, 247.f, 270f,
											292.5f, 312f, 337.5f};
	private BufferedImage _sprites[] = new BufferedImage[16];
	private int _xLocation, _yLocation, _rotationIndex, _speed, _previousX, _previousY;
	private int[][] _collisionMask;
	
	/**
	 * Constructs a new instance of the Player class
	 * @param playerID Relates to directly to player no. for multiplayer e.g. playerID is "1" for player 1
	 * @param X X coordinate to spawn at
	 * @param Y Y coordinate to spawn at
	 */
	public Player(int playerID, int X, int Y)
	{
		//If player causing instantiation is player 1
		if (playerID == 1)
		{
			try
			{
				_sprites[0] = ImageIO.read(getClass().getResource("/orangeMk2.png"));
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		//If player causing instantiation is player 2
		else if (playerID == 2)
		{
			try
			{
				_sprites[0] = ImageIO.read(getClass().getResource("/redMk2.png"));
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		_xLocation = X;
		_yLocation = Y;
		_rotationIndex = 4;
		_speed = 0;
		
		for (int i=1; i<16; i++)
		{
			_sprites[i] = rotateSprite(_sprites[0], _potentialRotations[i]);
		}
		
	}
	/**
	 * @param sprite The original sprite as BufferedImage
	 * @param angle The desired angle of adjustment
	 * @return The rotated sprite as BufferedImage
	 */
	private BufferedImage rotateSprite(BufferedImage sprite, double angle)
	{
		BufferedImage rotatedSprite = null;
		double radians = Math.toRadians(angle);
		double sin = Math.abs(Math.sin(radians)), cos = Math.abs(Math.cos(radians));
		double width = sprite.getWidth(), height = sprite.getHeight();
		int newWidth = (int)Math.floor(width * cos + height * sin);
		int newHeight = (int)Math.floor(height * cos + width * sin);
		
		rotatedSprite = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = rotatedSprite.createGraphics();
		AffineTransform affTran = new AffineTransform();
		affTran.translate((newWidth - width) / 2, (newHeight - height) / 2);
		
		double x = width / 2;
		double y = height / 2;
		
		affTran.rotate(radians, x, y);
		graphics.setTransform(affTran);
		graphics.drawImage(sprite, 0, 0, null);
		graphics.dispose();
		
		return rotatedSprite;
	}
	/**
	 * Turns the car 22.5 degrees to the right
	 */
	public void turnRight()
	{
		if (_rotationIndex < 15)
		{
			_rotationIndex++;
		}
		else
		{
			_rotationIndex = 0;
		}
	}
	/**
	 * Turns the car 22.5 degrees to the left
	 */
	public void turnLeft()
	{
		if (_rotationIndex > 0)
		{
			_rotationIndex--;
		}
		else
		{
			_rotationIndex = 15;
		}
	}
	/**
	 * Increments speed by 0.1 (up to a max of 1)
	 */
	public void increaseSpeed()
	{
		if (_speed < 10)
		{
			_speed += 1;
		}
	}
	/**
	 * Decrements speed by 0.1 (down to a minimum of 0)
	 */
	public void decreaseSpeed()
	{
		if (_speed > 0)
		{
			_speed -= 1;
		}
	}
	/**
	 * Updates the car's coordinates based on its rotation index and speed
	 */
	public void updatePosition()
	{
		_previousX = _xLocation;
		_previousY = _yLocation;
		
		switch (_rotationIndex)
		{
			case 0: 
				_yLocation = _yLocation - (2 *_speed);
				break;
			case 1: 
				_yLocation = _yLocation - (2 * _speed);
				_xLocation = _xLocation + _speed;
				break;
			case 2:
				_yLocation = _yLocation - (2 * _speed);
				_xLocation = _xLocation + (2 * _speed);
				break;
			case 3: 
				_yLocation = _yLocation - _speed;
				_xLocation = _xLocation + (2 * _speed);
				break;
			case 4:
				_xLocation = (_xLocation + (2 * _speed));
				break;
			case 5:
				_xLocation = _xLocation + (2 * _speed);
				_yLocation = _yLocation + _speed;
				break;
			case 6:
				_xLocation = _xLocation + (2 * _speed);
				_yLocation = _yLocation + (2 *_speed);
				break;
			case 7:
				_xLocation = _xLocation + _speed;
				_yLocation = _yLocation + (2 *_speed);
				break;
			case 8:
				_yLocation = (_yLocation + (2 * _speed));
				break;
			case 9:
				_xLocation = _xLocation - _speed;
				_yLocation = _yLocation + (2 *_speed);
				break;
			case 10:
				_xLocation = _xLocation - (2 *_speed);
				_yLocation = _yLocation + (2 *_speed);
				break;
			case 11:
				_xLocation = _xLocation - (2 *_speed);
				_yLocation = _yLocation + _speed;
				break;
			case 12:
				_xLocation = (_xLocation - (2 * _speed));
				break;
			case 13:
				_xLocation = _xLocation - (2 *_speed);
				_yLocation = _yLocation - _speed;
				break;
			case 14:
				_xLocation = _xLocation - (2 *_speed);
				_yLocation = _yLocation - (2 *_speed);
				break;
			case 15:
				_xLocation = _xLocation - _speed;
				_yLocation = _yLocation - (2 * _speed);
				break;
		}
	}
	/**
	 * Overloaded updatePosition. Forces player position to dictated X and Y coordinates
	 * @param X Dictated X position
	 * @param Y Dictated Y position
	 */
	public void updatePosition(int X, int Y)
	{
		_xLocation = X;
		_yLocation = Y;
	}
	/**
	 * Overloaded updatePosition. Forces player position to dictated X and Y coordinates and rotation
	 * index to dictated rotation index
	 * @param X Dictated X position
	 * @param Y Dictated Y position
	 * @param rotationIndex Dictated rotation index
	 */
	public void updatePosition(int X, int Y, int rotationIndex)
	{
		_previousX = _xLocation;
		_previousY = _yLocation;
		
		_xLocation = X;
		_yLocation = Y;
		_rotationIndex = rotationIndex;
	}
	/**
	 * Updates the cars collision mask array to a series of 0s and 1s<br><br>
	 * 0s represent entirely transparent pixels, 1s represent all others
	 */
	private void updateCollisionMask()
	{
		BufferedImage sprite = _sprites[_rotationIndex];
		int width = sprite.getWidth(), height = sprite.getHeight(), alpha;
		int[][] spritePixels = new int[height][width];
		//Populate 2D array with corresponding pixel RGBA values
		for (int i = 0; i < height; i++) 
		{
		    for (int j = 0; j < width; j++) {
		        spritePixels[i][j] = sprite.getRGB(j, i);
		    }
		}
		
		_collisionMask = new int[height][width];
		
		//Iterate over all pixels in current sprite
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				alpha = (spritePixels[i][j] >> 24) & 0xff;
				//if pixel is not transparent (therefore part of the car)
				if (alpha > 0)
				{
					_collisionMask[i][j] = 1;
				}
				else
				{
					_collisionMask[i][j] = 0;
				}
			}
		}
	}
	public void crashRebound(int collX, int collY, int collWidth)
	{
		int newX = _xLocation, newY = _yLocation;
		
		//travelling north
		if (_xLocation == _previousX && _yLocation < _previousY)
		{
			newY = _yLocation + (3 * _speed);
			_speed = 0;
		}
		//travelling north-east
		else if (_xLocation > _previousX && _yLocation < _previousY)
		{
			//Car collided with left-hand side of rect
			if (collX > _xLocation)
			{
				newX = _xLocation - _speed;
				turnLeft();
			}
			else //car collided with underside of rect
			{
				newY = _yLocation + _speed;
				turnRight();
			}
			_speed = _speed / 2;
		}
		//travelling east
		else if (_xLocation > _previousX && _yLocation == _previousY)
		{
			newX = _xLocation - (3 * _speed);
			_speed = 0;
		}
		//travelling south-east
		else if (_xLocation > _previousX && _yLocation > _previousY)
		{
			//Car collided with topside of rect
			if (collY > _yLocation)
			{
				newY = _yLocation - _speed;
				turnLeft();
			}
			else //car collided with left-hand side of rect
			{
				newX = _xLocation - _speed;
				turnRight();
			}
			_speed = _speed / 2;
		}
		//travelling south
		else if (_xLocation == _previousX && _yLocation > _previousY)
		{
			newY = _yLocation - (3 * _speed);
			_speed = 0;
		}
		//travelling south-west
		else if (_xLocation < _previousX && _yLocation > _previousY)
		{
			//Car collided with topside of rect
			if (collY > _yLocation)
			{
				newY = _yLocation - _speed;
				turnRight();
			}
			else //car collided with right-hand side of rect
			{
				newX = _xLocation + _speed;
				turnLeft();
			}
			_speed = _speed / 2;
		}
		//travelling west
		else if (_xLocation  < _previousX && _yLocation == _previousY)
		{
			newX = _xLocation + (3 * _speed);
			_speed = 0;
		}
		//travelling north-west
		else if (_xLocation < _previousX && _yLocation < _previousY)
		{
			//Car collided with right-hand side
			if ((collX + collWidth) < _xLocation)
			{
				newX = _xLocation + _speed;
				turnRight();
			}
			else //car collided with underside
			{
				newY = _yLocation + _speed;
				turnLeft();
			}
			_speed = _speed / 2;
		}
		_xLocation = newX;
		_yLocation = newY;
	}
	
	/**
	 * Updates and returns a collision mask for the car, based on the current sprite in use
	 * @return An up to date collision mask
	 */
	public int[][] getCollisionMask()
	{
		updateCollisionMask();
		return _collisionMask;
	}
	/** 
	 * @return The current sprite
	 */
	public BufferedImage getSprite()
	{
		return _sprites[_rotationIndex];
	}
	/**
	 * @return Boundary values of a rectangle surrounding object (X origin, Y origin, width, height)
	 */
	public int[] getBoundary()
	{
		int height = getSprite().getHeight(), width = getSprite().getWidth();
		int[] boundary = {_xLocation - (height/2), _yLocation - (width/2), width, height};
		return boundary;
	}
	public int getXPos()
	{
		return _xLocation;
	}
	public int getYPos()
	{
		return _yLocation;
	}
	public int getRotationIndex()
	{
		return _rotationIndex;
	}
}
