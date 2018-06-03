/**
 * @author Suhas Dara
 * @version 5.0
 * 			Supports plotting of output data using JavaPlot.
 */

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.NamedPlotColor;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.ImageTerminal;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class FilterMacs {
	
	public static int NUMBER_PHRASES = 0;
	public static String PLOT_OPTION = "";
	public static ArrayList<String> PATTERNS = new ArrayList<String>();
	public static ArrayList<String> DIFFS = new ArrayList<String>();
	public static ArrayList<String> COLUMN_NAMES = new ArrayList<String>();
	public static String FILE_NAME = "";
	public static Scanner LOGS;
	public static int LINES;
	public static ArrayList<String> MACS = new ArrayList<String>();
	public static ArrayList<ArrayList<String>> TIMESTAMPS = new ArrayList<ArrayList<String>>();
	public static ArrayList<ArrayList<Long>> EPOCHTIMES = new ArrayList<ArrayList<Long>>();
	public static ArrayList<Boolean> SEAL = new ArrayList<Boolean>();
	public static Scanner PLOT_LOGS;
	public final static String VERSION = "5.0";
	public final static int YEAR = Calendar.getInstance().get(Calendar.YEAR);
	public final static String MAC_regex = "([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}";
	public final static String COMMENT = "#";
	public final static String SEPARATOR = ":::";
	public final static String NOT_FOUND = "";
	public final static String FILE_OUT = "MAC_LifeCycle.csv";
	public final static String P_NO = "noplot";
	public final static String P_FILE = "plotfile";
	public final static String P_WIN = "plotwindow";
	public final static String SEP = System.getProperty("os.name").contains("Windows") ? "\\" : "/";

	public static void main(String[] args) throws Exception{
		File logFile = new File("");
		File patternFile = new File("");
		File diffFile = new File("");
		if(args.length == 0) {
			System.out.println("Try 'java -cp PATH" + SEP + "JavaPlot.jar:. FilterMacs --help' for more information");
			return;
		} else if(args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"))) {
			System.out.printf("\t%-5s\t%-15s\tAuthor and version\n", "-a", "--about");
			System.out.printf("\t%-5s\t%-15s\tUse: java -cp PATH" + SEP + "JavaPlot.jar:. FilterMacs -a\n", "", "");
			System.out.printf("\t%-5s\t%-15s\tHelp\n", "-h", "--help");
			System.out.printf("\t%-5s\t%-15s\tUse: java -cp PATH" + SEP + "JavaPlot.jar:. FilterMacs -h\n", "", "");
			System.out.printf("\t%-5s\t%-15s\topens Interactive mode\n", "-i", "--interactive");
			System.out.printf("\t%-5s\t%-15s\tUse: java -cp PATH" + SEP + "JavaPlot.jar:. FilterMacs -i\n", " ", " ");
			System.out.printf("\n\n\tjava -cp PATH" + SEP + "JavaPlot.jar:. FilterMacs <logFile> <patternFile> <columnDiffFile>\n");
			System.out.printf("\t  Only creates a csv file. Does not plot the data\n\n");
			System.out.printf("\tjava -cp PATH" + SEP + "JavaPlot.jar:. FilterMacs <logFile> <patternFile> <columnDiffFile> <plotOption>\n");
			System.out.printf("\t  plotOptions:\n");
			System.out.printf("\t  %-15s:\tDoes not plot the data (default)\n", P_NO);
			System.out.printf("\t  %-15s:\tPlots data into png files\n", P_FILE);
			System.out.printf("\t  %-15s:\tPlots data on interactive windows\n", P_WIN);
			return;
		} else if(args.length == 3) {
			logFile = new File(args[0]);
			FILE_NAME = args[0];
			patternFile = new File(args[1]);
			diffFile = new File(args[2]);
			if(!logFile.canRead() || !logFile.isFile() || !patternFile.canRead() || !patternFile.isFile() || !diffFile.canRead() || !diffFile.isFile()) {
				System.out.println("One or more of the files could not be read or are directories.");
				return;
			}
			PLOT_OPTION = P_NO;
		} else if(args.length == 4) {
			logFile = new File(args[0]);
			FILE_NAME = args[0];
			patternFile = new File(args[1]);
			diffFile = new File(args[2]);
			PLOT_OPTION = args[3];
			if(!logFile.canRead() || !logFile.isFile() || !patternFile.canRead() || !patternFile.isFile() || !diffFile.canRead() || !diffFile.isFile()) {
				System.out.println("One or more of the files could not be read or are directories.");
				return;
			}
			if(!PLOT_OPTION.equals(P_WIN) && !PLOT_OPTION.equals(P_FILE) && !PLOT_OPTION.equals(P_NO)) {
				System.out.println("Plot option not found");
				return;
			}
		} else if(args.length == 1 && (args[0].equals("-a") || args[0].equals("--about"))) {
			System.out.printf("%-20sSuhas Dara\n", "Author: ");
			System.out.printf("%-20s" + VERSION + "\n", "Current version: ");
			return;
		} else if(args.length == 1 && (args[0].equals("-i") || args[0].equals("--interactive"))) {
			Scanner input = new Scanner(System.in);
			System.out.print("Enter file name for parsing: ");
			logFile = new File(input.nextLine());
			while(!logFile.canRead() || !logFile.isFile()) {
				System.out.print("File can't be read. Enter file name for parsing: ");
				logFile = new File(input.nextLine());
			}
			FILE_NAME = logFile.getName();
			
			System.out.println("\nLine format in file for patterns:\nSearchPhrase ::: ColumnName\nExample:\nNew authentication request ::: NewAuth\nLines starting with '#' will be ignored\nIf a line is unformatted, output will have columns named UNFORMATTED");
			System.out.print("Enter file name containing search patterns: ");
			patternFile = new File(input.nextLine());
			while(!patternFile.canRead() || !patternFile.isFile()) {
				System.out.print("File can't be read. Enter a file name containing search patterns: ");
				patternFile = new File(input.nextLine());
			}
			
			System.out.println("\nLine format in file for column differences:\nFirstColumn SecondColumn\nExample:\n5 2\nThis will compute Line5 - Line2");
			System.out.print("Enter file name containing required column differences: ");
			diffFile = new File(input.nextLine());
			while(!diffFile.canRead() || !diffFile.isFile()) {
				System.out.print("File can't be read. Enter file name containing required column differences: ");
				diffFile = new File(input.nextLine());
			}
			
			System.out.print("\nEnter Plot Option ('noplot' for None, 'plotwindow' for Window-mode, 'plotfile' for File-mode): ");
			PLOT_OPTION = input.nextLine();
			while(!PLOT_OPTION.equals(P_WIN) && !PLOT_OPTION.equals(P_FILE) && !PLOT_OPTION.equals(P_NO)) {
				System.out.print("Invalid Plot Option. Enter valid Plot Option: ");
				PLOT_OPTION = input.nextLine();
			}
			
			System.out.println();
			
			input.close();
		} else {
			System.out.print("java FilterMacs ");
			for(int i = 0; i < args.length - 1; i++) {
				System.out.print(args[i] + " ");
			}
			System.out.println(args[args.length - 1] + ": Command not found");
			return;
		}
		
		File theDir = new File(FILE_NAME + "_analysis");
		if (!theDir.exists()) {
			try {
				theDir.mkdir();
			} catch (SecurityException error) {
				System.err.println(error);
			}
		}
		
		File output = new File(FILE_NAME + "_analysis", FILE_OUT);
		PrintStream csv = new PrintStream(output);
		LOGS = new Scanner(logFile);
		Scanner patterns = new Scanner(patternFile);
		patternsList(patterns);
		patterns.close();
		Scanner diffs = new Scanner(diffFile);
		diffsList(diffs);
		diffs.close();
		
		parse(csv);
		LOGS.close();
		csv.close();
	}
	
	//Parses the data using four parallel lists.
	public static void parse(PrintStream csv) throws Exception{
		System.out.println("Parsing data...");
		while(LOGS.hasNextLine()) {
			String log = LOGS.nextLine();
		    Pattern pattern = Pattern.compile(MAC_regex);
		    Matcher matcher = pattern.matcher(log);
			if(matcher.find()) {
				//First search pattern implemented for adding unique MAC addresses.
				if(log.contains(PATTERNS.get(0))) {
					open(log);
				}
				//Remaining search patterns are updated as and when they are encountered.
				for(int i = 1; i < NUMBER_PHRASES - 1; i++) {
					if(log.contains(PATTERNS.get(i))) {
						addTimestamps(log, i);
					}
				}
				//Last search pattern implemented for 'sealing' the modification of a particular MAC address.
				if(log.contains(PATTERNS.get(NUMBER_PHRASES - 1))) {
					close(log);
				}
			}
		}
		
//		deleteBadData(); //debug line
		System.out.println("Writing data into " + FILE_OUT + "...");
		writeData(csv);
//		printForTest(MACS); //debug line
//		printForTest(seal); //debug line
//		printForTest(timestamps); //debug line
//		printForTest(epochtimes); //debug line
		
		if(PLOT_OPTION.equals(P_WIN) || PLOT_OPTION.equals(P_FILE)) {
			System.out.println("Plotting data...");
			for(int i = 0; i < DIFFS.size(); i++) {
				plot(i);
			}
		}
		System.out.println("Done! Output files are saved under directory " + FILE_NAME + "_analysis");
	}
	
	public static void plot(int column) throws FileNotFoundException {
		PLOT_LOGS = new Scanner(new File(FILE_NAME + "_analysis", FILE_OUT));
		LINES = 0;
		while(PLOT_LOGS.hasNextLine()) {
			PLOT_LOGS.nextLine();
			LINES++;
		}
		PLOT_LOGS.close();
		
		int[][] plotData = new int[LINES][2];
		int stop = PATTERNS.size() * 2 + 1;
		int line_count = 0;
		int min = -1;
		int max = -1;

		PLOT_LOGS = new Scanner(new File(FILE_NAME + "_analysis", FILE_OUT));
		PLOT_LOGS.nextLine();
		PLOT_LOGS.nextLine();
		while (PLOT_LOGS.hasNextLine()) {
			Scanner line = new Scanner(PLOT_LOGS.nextLine());
			line.useDelimiter(",");
			ArrayList<Integer> tokens = new ArrayList<Integer>();
			for (int i = 0; i < stop; i++) {
				line.next();
			}
			while (line.hasNext()) {
				if (!line.hasNextInt()) {
					tokens.add(-1);
					line.next();
				} else {
					tokens.add(line.nextInt());
				}
			}
			line.close();

			plotData[line_count][0] = line_count + 1;
			plotData[line_count][1] = tokens.get(column);
			min = plotData[line_count][1] < min ? plotData[line_count][1] : min;
			max = plotData[line_count][1] > max ? plotData[line_count][1] : max;
			line_count++;
			// System.out.print(tokens.size() + " ");
		}
		// printArray(plotData);
		if(PLOT_OPTION.equals(P_FILE)) {
			plotgraphF(column, max, min, plotData);
		} else {
			plotgraphW(column, max, min, plotData);
		}
	}

	private static void plotgraphF(int column, int maxY, int minY, int[][] data) {
		String[] diff = DIFFS.get(column).split(" ");
		String column1 = COLUMN_NAMES.get(Integer.parseInt(diff[0]) - 1);
		String column2 = COLUMN_NAMES.get(Integer.parseInt(diff[1]) - 1);
		String title = column1 + "-" + column2;

		PlotStyle style = new PlotStyle();
		style.setStyle(Style.POINTS);
		style.setLineType(NamedPlotColor.BLUE);

		DataSetPlot dataSet = new DataSetPlot(data);
		dataSet.setPlotStyle(style);
		dataSet.setTitle(title);

		ImageTerminal png = new ImageTerminal();
		File file = new File(FILE_NAME + "_analysis", "Plot_" + title + ".png");
		try {
			file.createNewFile();
			png.processOutput(new FileInputStream(file));
		} catch (FileNotFoundException error) {
			System.err.print(error);
		} catch (IOException error) {
			System.err.print(error);
		}

		JavaPlot p = new JavaPlot();
		p.setTerminal(png);

		p.getAxis("x").setLabel("Row number in logs.csv");
		p.getAxis("y").setLabel(title + " (in seconds)");
		p.getAxis("x").setBoundaries(0, LINES + 5);
		p.getAxis("y").setBoundaries(minY - 1, maxY + 1);
		p.addPlot(dataSet);
		p.setTitle(title);
		p.plot();

		try {
			ImageIO.write(png.getImage(), "png", file);
		} catch (IOException error) {
			System.err.print(error);
		}
	}
	
	private static void plotgraphW(int column, int maxY, int minY, int[][] data) {
		String[] diff = DIFFS.get(column).split(" ");
		String column1 = COLUMN_NAMES.get(Integer.parseInt(diff[0]) - 1);
		String column2 = COLUMN_NAMES.get(Integer.parseInt(diff[1]) - 1);
		String title = column1 + "-" + column2;

		PlotStyle style = new PlotStyle();
		style.setStyle(Style.POINTS);
		style.setLineType(NamedPlotColor.BLUE);

		DataSetPlot dataSet = new DataSetPlot(data);
		dataSet.setPlotStyle(style);
		dataSet.setTitle(title);
		
		JavaPlot p = new JavaPlot();

		p.getAxis("x").setLabel("Row number in logs.csv");
		p.getAxis("y").setLabel(title + " (in seconds)");
		p.getAxis("x").setBoundaries(0, LINES + 5);
		p.getAxis("y").setBoundaries(minY - 1, maxY + 1);
		p.addPlot(dataSet);
		p.setTitle(title);
		p.plot();
	}
	
	//Creates a list of all required patterns.
	public static void patternsList(Scanner patterns) {
		int count = 0;
		while(patterns.hasNextLine()) {
			String line = patterns.nextLine();
			int index = line.indexOf(SEPARATOR) - 1;
			if(line.length() == 0 || line.startsWith(COMMENT)) {
				//ignore line;
			} else if(index != -2 && index != -1 && !line.endsWith(SEPARATOR)) {
				PATTERNS.add(line.substring(0, index));
				COLUMN_NAMES.add(line.substring(index + 5));
				count++;
			} else {
				PATTERNS.add("UNFORMATTED LINE WILL NOT BE PROCESSED");
				COLUMN_NAMES.add("UNFORMATTED");
				count++;
			}
		}
		NUMBER_PHRASES = count;
	}
	
	//Creates a list of all required column differences.
	public static void diffsList(Scanner diffs) {
		while(diffs.hasNextLine()) {
			String line = diffs.nextLine();
			Scanner diff = new Scanner(line);
			if(diff.hasNextInt()) {
				int first = diff.nextInt();
				if(first >= 1 && first <= NUMBER_PHRASES) {
					if(diff.hasNextInt()) {
						int second = diff.nextInt();
						if(second >= 1 && second <= NUMBER_PHRASES && !diff.hasNext()) {
							DIFFS.add(line);
						}
					}
				}
			}
			diff.close();
		}
	}
	
	//If open search phrase is found.
	public static void open(String log) throws Exception{
		String MAC = getMAC(log);
		MACS.add(0, MAC);
		SEAL.add(0, false);
		ArrayList<String> stamps = new ArrayList<String>();
		stamps.add(getTimestamp(log));
		createEmptyArrayList(stamps, NOT_FOUND);
		TIMESTAMPS.add(0, stamps);
		ArrayList<Long> epochs = new ArrayList<Long>();
		epochs.add(getTimeEpoch(getTimestamp(log)));
		createEmptyArrayList(epochs, 0);
		EPOCHTIMES.add(0, epochs);
	}
	
	//Adds timestamps when search phrase is found.
	public static void addTimestamps(String log, int value) throws Exception{
		String MAC = getMAC(log);
		int index = MACS.indexOf(MAC);
		if(index != -1 && !SEAL.get(index)) {
			TIMESTAMPS.get(index).set(value, getTimestamp(log));
			EPOCHTIMES.get(index).set(value, getTimeEpoch(getTimestamp(log)));
		}
	}
	
	//If closing search phrase is found.
	public static void close(String log) throws Exception{
		String MAC = getMAC(log);
		int index = MACS.indexOf(MAC);
		if(index != -1 && !SEAL.get(index)) {
			TIMESTAMPS.get(index).set(NUMBER_PHRASES - 1, getTimestamp(log));
			EPOCHTIMES.get(index).set(NUMBER_PHRASES - 1, getTimeEpoch(getTimestamp(log)));
			SEAL.set(index, true);
		}
	}
	
	//Writes all the data to a file.
	public static void writeData(PrintStream csv) throws Exception{
		int index = MACS.size() - 1;
		csv.print("TS means TimeStamps");
		for(int i = 0; i < COLUMN_NAMES.size(); i++) {
			csv.print(",Line " + (i + 1) + " TS");
			csv.print(",Line " + (i + 1) + " Epoch");
		}
		for(int i = 0; i < DIFFS.size(); i++) {
			String diff[] = DIFFS.get(i).split(" ");
			csv.print(",Line" + diff[0] + " - Line" + diff[1]);
		}
		csv.print("\nMAC");
		for(int i = 0; i < COLUMN_NAMES.size(); i++) {
			csv.print("," + COLUMN_NAMES.get(i) + " TS");
			csv.print("," + COLUMN_NAMES.get(i) + " Epoch");
		}
		for(int i = 0; i < DIFFS.size(); i++) {
			String diff[] = DIFFS.get(i).split(" ");
			csv.print("," + COLUMN_NAMES.get(Integer.parseInt(diff[0]) - 1) + " - " + COLUMN_NAMES.get(Integer.parseInt(diff[1]) - 1));
		}
		csv.println();
		while(index >= 0) {
			csv.print(MACS.get(index));
			for(int i = 0; i < NUMBER_PHRASES; i++) {
				csv.print("," + TIMESTAMPS.get(index).get(i));
				csv.print("," + EPOCHTIMES.get(index).get(i));
			}
			for(int i = 0; i < DIFFS.size(); i++) {
				Scanner diff = new Scanner(DIFFS.get(i));
				int first = diff.nextInt() - 1;
				int second = diff.nextInt() - 1;
				if(EPOCHTIMES.get(index).get(first) != 0 && EPOCHTIMES.get(index).get(second) != 0) {
					csv.print("," + (EPOCHTIMES.get(index).get(first) - EPOCHTIMES.get(index).get(second)));
				} else {
					csv.print(", NAN");
				}
				diff.close();
			}
			csv.println();
			index--;
		}
	}
	
	//fills empty arraylist with given phrase.
	public static void createEmptyArrayList(ArrayList<String> list, String toAdd) {
		for(int i = 1; i < NUMBER_PHRASES; i++) {
			list.add(toAdd);
		}
	}
	
	//fills empty arraylist with given long value.
	public static void createEmptyArrayList(ArrayList<Long> list, long toAdd) {
		for(int i = 1; i < NUMBER_PHRASES; i++) {
			list.add(toAdd);
		}
	}
	
	//returns the MAC address found in the string.
	public static String getMAC(String log) {
	    Pattern pattern = Pattern.compile(MAC_regex);
	    Matcher matcher = pattern.matcher(log);
	    String MAC = NOT_FOUND;
	    if(matcher.find()){
	    	int index = matcher.start();
	    	MAC = log.substring(index, index + 17);
	    }
		
		return MAC;
	}
	
	//returns the timestamp found in the string.
	public static String getTimestamp(String log) throws Exception{
		Date date = new SimpleDateFormat("MMM").parse(log.substring(0, 3));
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int month = cal.get(Calendar.MONTH) + 1;
		String time = log.substring(0, 15);
		String stamp = month + "/" + time.substring(4, 6) + "/" + YEAR + time.substring(6);
		
		return stamp;
	}
	
	//returns the time in epoch format (in seconds) found in the string.
	public static long getTimeEpoch(String str) throws Exception{
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = sdf.parse(str);
		long epoch = date.getTime() / 1000;
		
		return epoch;
	}
	
	
	//FOR DEBUG:
	
	private static void deleteBadData() throws Exception{
		int i = 0;
		while(i < MACS.size()) {
			boolean remove = TIMESTAMPS.get(i).size() > NUMBER_PHRASES + 1;
			if(remove) {
				MACS.remove(i);
				TIMESTAMPS.remove(i);
				EPOCHTIMES.remove(i);
			} else {
				i++;
			}
		}
	}
	
	private static void printForTest(ArrayList list) {
		int limit = list.size();
		for(int i = 0; i < limit; i++) {
			System.out.println(i + " " + list.get(i));
		}
	}
}