package minesweeper;
import java.io.*;
import java.util.*;
import java.awt.Toolkit;

public class Minesweeper
{
	//Main Method
	public static void main(String[] args) throws FileNotFoundException
	{
		Scanner stdin = new Scanner(System.in);
		boolean gameOver = false;
		char[] alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
		double startTime = 0;
		double curentTime = 0;
		int blanks = 0;
		int turnNum = 0;
		int[] chooseExpose = new int[2];
		int[] options = new int[3]; //[0]=y-coor [1]=x-coor [2]=#bombs
		int[] time = new int[2];
		String command = " ";
		String record = "";
		String tempRecord ="";
		String timeString = "0:00";
		drawHeader(50, "Welcome to MineSweeper!", '#');
		askOptions(options, stdin);
		int[][] location = new int[options[1]][options[0]];
		boolean[][] expose = new boolean[options[1]][options[0]];
		boolean[][] allExposed = new boolean[options[1]][options[0]];
		exposeAll(options[1], options[0], allExposed);
		initializeBoard(location, options[2], options[1], options[0]);
		//MAKE RECORD
		record = initializeRecord(record, options);
		record += "\n\n## ANSWER KEY ##\n";
		record += drawBoardRecord(location, allExposed, options[1], options[0], alphabet);
		record += "\n\n#TURN#\t#TIME#\t#COOR#";
		record += "\n--\t--\t--";
		startTime = System.currentTimeMillis();
		while (gameOver!=true) //continue until win, lose, or user quit
		{
			blanks = checkBlanks(options[1], options[0], expose);
			if (blanks==options[2])
			{
				gameOver=true;
				break;
			}
			//Draw the Board
			++turnNum;
			drawBoardHeader("### TURN "+turnNum+", TIME "+getTime(startTime, time)+" ###", options);
			drawBoard(location, expose, options[1], options[0], alphabet);
			//GET THE NEXT ACTION TO PERFORM
			command = askCommand(stdin, options, expose, chooseExpose, alphabet, startTime, time);
			if (command.length()<4 && command.length()>1 && Character.isLetter(command.charAt(0))==true && Character.isDigit(command.charAt(1))==true && Character.isDigit(command.charAt(command.length()-1))==true)
			{
				gameOver=exposeSquare(chooseExpose[1], chooseExpose[0], location, options[1], options[0], expose, gameOver);
				System.out.println();
				record +="\n"+turnNum+"\t"+getTime(startTime, time)+"\t"+command;
			}
			else if (command.equalsIgnoreCase("quit"))
			{
				System.out.println("Quitting...");
				System.out.println();
				gameOver=true;
			}
			else if (command.equalsIgnoreCase("redraw"))
			{
				--turnNum;
			}
		}
		//DRAW FINAL BOARD
		record += ("\n\n## FINAl BOARD ##\n");
		record += drawBoardRecord(location, expose, options[1], options[0], alphabet);
		exposeAll(options[1], options[0], expose);
		drawBoardHeader("### GAME-OVER, TIME "+getTime(startTime, time)+" ###", options);
		drawBoard(location, expose, options[1], options[0], alphabet);
		if (blanks==options[2]) //check if user won
		{
			System.out.println("You win!");
			record += "\nNUMBER OF SQUARES LEFT: 0";
			record += "\n## RESULT: WIN ##";
		}
		else //if there are more blanks than bombs
		{
			System.out.println("You lose...");
			record += "\nNUMBER OF SQUARES LEFT: "+(blanks-options[2]);
			record += "\n## RESULT: LOSE ##";
		}
		//Check if user wants to save the record
		if (yesNo("Would you like to save a record of this game?", stdin))
		{
			saveRecord(stdin, record);
		}
		System.out.println("GAME-OVER, THANKS FOR PLAYING!");
	}
	
	/*
	*	purpose:
	*		-prompt player for command
	*	input:
	*		-from scanner stdin
	*	output:
	*		-prompts, reminds invalid, changes values of chooseExpose
	*	returns:
	*		-command
	*/
	public static String askCommand(Scanner stdin, int[] options, boolean[][] expose, int[] chooseExpose, char[]alphabet, double startTime, int[] time)
	{
		boolean valid = false;
		String command = " ";
		int[] checkExpose = new int[2];
		while(valid == false)
		{
			System.out.print("Enter a coordinate or other command: ");
			command = stdin.next();
			if (command.equals("?"))
			{
				drawLine(55, '-');
				System.out.println("To uncover a square, enter the coordinate (ex. \"A2\").");
				System.out.println("To check current game time, type \"time\".");
				System.out.println("To redraw the screen, type \"redraw\".");
				System.out.println("To announce number of bombs, type \"bombs\".");
				System.out.println("To quit the game, type \"quit\".");
				drawLine(55, '-');
			}
			else if (command.equalsIgnoreCase("time"))
			{
				System.out.println("Current game time is "+getTime(startTime, time));
			}
			else if (command.equalsIgnoreCase("quit"))
			{
				valid=true;
			}
			else if (command.length()<4 && command.length()>1 && Character.isLetter(command.charAt(0))==true && Character.isDigit(command.charAt(1))==true && Character.isDigit(command.charAt(command.length()-1))==true)
			{
				checkExpose = getCoor(command, alphabet, checkExpose);
				if (checkExpose[1]<options[1] && checkExpose[0]<options[0] && expose[checkExpose[1]][checkExpose[0]]==true)
				{
					System.out.println("That square has already been uncovered!");
				}
				else if (checkExpose[1]<options[1] && checkExpose[0]<options[0])
				{
					chooseExpose[1]=checkExpose[1];
					chooseExpose[0]=checkExpose[0];
					valid=true;
				}
				else
				{
					System.out.println("Invalid coordinate (out of bounds). Try again.");
				}
			}
			else if (command.equalsIgnoreCase("redraw"))
			{
				System.out.println("Redrawing grid...");
				System.out.println();
				valid=true;
			}
			else if (command.equalsIgnoreCase("bombs"))
			{
				System.out.println("Number of hidden bombs - "+options[2]);
			}
			else 
			{
				System.out.println("Invalid command! Try again, or type \"?\" for help.");
			}
		}
		command = command.toUpperCase();
		return command;
	}
	
	/*Gets the size values for the grid, and the number of bombs from the user input
	 * input:-Scanner stdin
	 * output:
	 * prompts for number of bombs

	*/
	public static void askOptions(int[] options, Scanner stdin)
	{
		//ask the user to choose the board height
		do
		{
			System.out.print("How tall should the board be (1-26)?: ");
			options[0] = stdin.nextInt();
			if (options[0]<1 || options[0]>26)
			{
				System.out.println("Invalid size! Try again...");
			}
		}
		while (options[0]<1 || options[0]>26);
		//ask the user to choose the board width
		do
		{
			System.out.print("How wide should the board be (1-26)?: ");
			options[1] = stdin.nextInt();
			if (options[1]<1 || options[1]>26)
			{
				System.out.println("Invalid size! Try again...");
			}
		}
		while (options[1]<1 || options[1]>26);
		//Ask the user for number of bombs
		do
		{
			
			System.out.print("How many bombs should there be (0-"+options[0]*options[1]+")?: ");
			options[2] = stdin.nextInt();
			if (options[2]<0 || options[2]>options[0]*options[1])
			{
				System.out.println("Invalid number of bombs! Try again...");
			}
		}
		while (options[2]<0 || options[2]>options[0]*options[1]);
		System.out.println();
	}
	
	//Check the grid for uncovered squares
	public static int checkBlanks(int xmax, int ymax, boolean[][] expose)
	{
		int blanks = 0;
		for (int y=0; y<ymax; ++y)
		{
			for (int x=0; x<xmax; ++x)
			{
				if (expose[x][y]==false)
				{
					++blanks;
				}
			}
		}
		return blanks;
	}
	
	public static int[] getCoor(String coor, char[] alphabet, int[] result)
	{
		coor = coor.toUpperCase();
		for (int i=0; i<(alphabet.length)-1; ++i)
		{
			if (alphabet[i]==(coor.charAt(0)))
			{
				result[1]=i;
			}
		}
		result[0]=Integer.parseInt(coor.substring(1, coor.length()))-1;
		return result;
	}
				
	/*
		Makes a header with centered text and border
	uses the drawLine method
	outputs the bordered header to console
	
	
	*/
	public static void drawHeader(int length, String text, char symbol)
	{
		int textLength = text.length();
		if (textLength>length-2)
		{
			length=textLength+2;
		}
		drawLine(length, symbol);
		System.out.print(symbol);
		for (int i=0; i<(length-textLength-2)+1; ++i)
		{
			if (i==(length-textLength-2)/2)
			{
				System.out.print(text);
			}
			else
			{
				System.out.print(" ");
			}
		}
		System.out.println(symbol);
		drawLine(length, symbol);
	}
	
	//draws a line of characters of a passed length
	public static void drawLine(int length, char symbol)
	{
		for (int i=0; i<length-1; ++i)
		{
			System.out.print(symbol);
		}
		System.out.println(symbol);
	}
	
	public static void exposeAll(int xmax, int ymax, boolean[][] toExpose)
	{
		for (int y=0; y<ymax; ++y)
		{
			for (int x=0; x<xmax; ++x)
			{
				toExpose[x][y]=true;
			}
		}
	}
	
	/*Sets space on grid to uncovered
	 * if said space was - triggers a cascade effect by calling this method recursively
	 * if the space was a bomb sets gameOver to TRUE
	
	*/
	public static boolean exposeSquare(int row, int col, int[][] location, int xmax, int ymax, boolean[][] expose, boolean gameOver)
	{
		if (location[row][col]==-1)
		{
			location[row][col]=-2;
			expose[row][col]=true;
			gameOver=true;
		}
		else if (location[row][col]==0)
		{	
			/*insert code that checks all around zero and for each check, runs this method*/
			expose[row][col]=true;
			for (int b=col-1; b<=col+1; ++b)
			{
				if (b<ymax && b>=0)
				{
					for (int a=row-1; a<=row+1; ++a)
					{
						if (a<xmax && a>=0 && expose[a][b]!=true)
						{
							gameOver=exposeSquare(a, b, location, xmax, ymax, expose, gameOver);
						}
					}
				}
			}
					
		}
		else //normal square - consider to be combined with bomb reveal
		{
			expose[row][col]=true;
		}
		return gameOver;		
	}
	
	
	public static void initializeBoard(int[][] location, int bombs, int xmax, int ymax)
	{
		int x = 0;
		int y = 0;
		Random r = new Random();
		for (int i=0; i<bombs; ++i)
		{
			do
			{
				x=r.nextInt(xmax);
				y=r.nextInt(ymax);
			}
			while (location[x][y]==-1);
			location[x][y]=-1;
			for (int b = y-1; b<=y+1; ++b)
			{
				if (b<ymax && b>=0)
				{
					for (int a=x-1; a<=x+1; ++a)
					{
						if (a<xmax && a>=0)
						{
							if (location[a][b]!=-1)
							{
								location[a][b]+=1;
							}
						}
					}
				}
			}
		}
	}
	
	public static String initializeRecord(String record, int[] options)
	{
		record += "### MINESWEEPER - GAME RECORD ###";
		record += "\nSIZE (WxH): "+options[1]+"x"+options[0];
		record += "\nNUMBER OF BOMBS: "+options[2];
		return record;
	}
	
	//prints current grid to console while hiding covered squares from player
	
	public static void drawBoard(int[][] location, boolean[][] expose, int xmax, int ymax, char[] alphabet)
	{
		System.out.print("   |");
		for (int xaxis=0; xaxis<xmax; ++xaxis)
		{
			System.out.print(" "+alphabet[xaxis]+" ");
		}
		System.out.println("|");
		System.out.print(" ");
		for (int top=0; top<(xmax*3)+6; ++top)
		{
			System.out.print("-");
		}
		System.out.println();
		for (int y=0; y<ymax; ++y)
		{
			if (y+1<10)
			{
				System.out.print("  "+(y+1)+"|");
			}
			else 
			{
				System.out.print(" "+(y+1)+"|");
			}
			for (int x=0; x<xmax; ++x)
			{
				if (expose[x][y]==true && location[x][y]>0)
				{
					System.out.print("["+location[x][y]+"]");
				}
				else if (expose[x][y]==true && location[x][y]==-1)
				{
					System.out.print(" @ ");
				}
				else if (expose[x][y]==true && location[x][y]==-2)
				{
					System.out.print(" X ");
				}
				else if (expose[x][y]==true && location[x][y]==0)
				{
					System.out.print("   ");
				}
				else 
				{
					System.out.print(" ? ");
				}
			}
			System.out.print("|"+(y+1));
			System.out.println();
		}
		System.out.print(" ");
		for (int bottom=0; bottom<(xmax*3)+6; ++bottom)
		{
			System.out.print("-");
		}
		System.out.println();
		System.out.print("   |");
		for (int xaxis=0; xaxis<xmax; ++xaxis)
		{
			System.out.print(" "+alphabet[xaxis]+" ");
		}
		System.out.println("|");
		System.out.println();
		
	}
	
	//prints the current grid to record,hiding covered squares from the player
	
	public static String drawBoardRecord(int[][] location, boolean[][] expose, int xmax, int ymax, char[] alphabet)
	{
		String record ="";
		record += ("\n   |");
		for (int xaxis=0; xaxis<xmax; ++xaxis)
		{
			record += (" "+alphabet[xaxis]+" ");
		}
		record += ("|\n");
		record += (" ");
		for (int top=0; top<(xmax*3)+6; ++top)
		{
			record += ("-");
		}
		record += ("\n");
		for (int y=0; y<ymax; ++y)
		{
			if (y+1<10)
			{
				record += ("  "+(y+1)+"|");
			}
			else //y+1>=10
			{
				record += (" "+(y+1)+"|");
			}
			for (int x=0; x<xmax; ++x)
			{
				if (expose[x][y]==true && location[x][y]>0)
				{
					record += ("["+location[x][y]+"]");
				}
				else if (expose[x][y]==true && location[x][y]==-1)
				{
					record += (" @ ");
				}
				else if (expose[x][y]==true && location[x][y]==-2)
				{
					record += (" X ");
				}
				else if (expose[x][y]==true && location[x][y]==0)
				{
					record += ("   ");
				}
				else 
				{
					record += (" ? ");
				}
			}
			record += ("|"+(y+1));
			record += ("\n");
		}
		record += (" ");
		for (int bottom=0; bottom<(xmax*3)+6; ++bottom)
		{
			record += ("-");
		}
		record += ("\n");
		record += ("   |");
		for (int xaxis=0; xaxis<xmax; ++xaxis)
		{
			record += (" "+alphabet[xaxis]+" ");
		}
		record += ("|\n");
		return record;
	}
	
	//draws a cebtered header over the game grid
	public static void drawBoardHeader(String text, int[] options)
	{
		int textLength = text.length();
		int length = (options[1]*3)+10;
		for (int i=0; i<(length-textLength-2)+1; ++i)
		{
			if (i==(length-textLength-2)/2)
			{
				System.out.print(text);
			}
			else
			{
				System.out.print(" ");
			}
		}
		System.out.println();
	}
	
	//Gets current time and makes a string for it. startTime is found from system at the start of the program
	
	public static String getTime(double startTime, int[] time)
	{
		String timeString = "0:00";
		double currentAccumTime = System.currentTimeMillis()-startTime;
		time[0]=(int)(currentAccumTime%60000)/1000;
		time[1]=(int)(currentAccumTime/60000);
		if (time[0]<10)
		{
			timeString=time[1]+":0"+time[0];
		}
		else //time>=10
		{
			timeString=time[1]+":"+time[0];
		}
		return timeString;
	}
	
	//Saves the process,  asks the user for name of the save and warns eventual overwrite
	
	public static void saveRecord(Scanner stdin, String record) throws FileNotFoundException
	{
		boolean fileExists = false;
		boolean saved = false;
		String fileName = " ";
		while (saved == false)
		{
			System.out.println("What would you like to name this game record?: ");
			fileName = stdin.next();
			fileName +=".txt";
			
			fileExists = (new File(fileName)).exists();
			if (fileExists)
			{
				Toolkit.getDefaultToolkit().beep();
				System.out.println("There is already a file named "+fileName);
				if (yesNo("Would you like to overwrite?", stdin))
				{
					System.out.println("OK. Overwriting...");
					PrintStream save = new PrintStream(new File(fileName));
					save.println(record);
					saved = true;
				}
			}
			else
			{
				PrintStream save = new PrintStream(new File(fileName));
				save.println(record);
				saved = true;
			}
		}
	}
	
	
	public static boolean yesNo(String prompt, Scanner console)
	{
		int yesNo = 0;
		Scanner userChoice = new Scanner(System.in);
		String response = " ";
		do
		{
			System.out.print(prompt);
			System.out.print(" (yes/no) : ");
			response = userChoice.nextLine();
			if (response.equalsIgnoreCase("yes"))
			{
				return true;
			}
			else if (response.equalsIgnoreCase("no"))
			{
				return false;
			}
			else 
			{
				System.out.println("Invalid response! Try again...");
			}
		}
		while (!response.equalsIgnoreCase("yes") && !response.equalsIgnoreCase("no"));
		return false;
	}
		
		
		
}