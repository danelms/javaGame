package assPart3;

public class CollidableRect
{
	private int _xOrigin, _yOrigin, _height, _width;
	
	/**
	 * Used to instantiate rectangular objects used for tracking collisions (hard terrain or checkpoints)
	 * @param X X origin
	 * @param Y Y origin
	 * @param width Width
	 * @param height Height
	 */
	public CollidableRect(int X, int Y, int width, int height)
	{
		_xOrigin = X;
		_yOrigin = Y;
		_height = height;
		_width = width;
	}
	/**
	 * @return Boundary values of a rectangle surrounding object (X origin, Y origin, width, height)
	 */
	public int[] getBoundary()
	{
		int[] boundary = {_xOrigin, _yOrigin, _width, _height};
		return boundary;
	}
}
