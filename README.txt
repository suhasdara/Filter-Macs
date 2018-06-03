
Version 5.0 Updated: 08/01/2018
FilterMacs
----------

Analyze life-cycle for each MAC address.
Generate CSV for timing data.
Also generate Plot image files from timing data if chosen.

NOTE: For all paths, use '\' instead of '/' if OS is Windows.

Environment Required
--------------------

*Download the JavaPlot library from http://javaplot.panayotis.com/
*Extract the JavaPlot-x.x.x folder.
*Note down the path of extracted dist/JavaPlot.jar file 
*(optional) For ease of use of FilterMacs, copy the JavaPlot-x.x.x/dist/JavaPlot.jar
 file into folder containing FilterMacs.java or FilterMacs.class file.

Compiling
---------

*Class path needs to be specified so that JavaPlot library is accessible.
*javac -cp PATH_TO_JAVAPLOT/JavaPlot.jar FilterMacs.java
*JavaPlot.jar file can be found under JavaPlot-x.x.x/dist/

Executing
---------

*Class path needs to be specified so that JavaPlot library is accessible.
*java -cp PATH_TO_JAVAPLOT/JavaPlot.jar:. FilterMacs <args>
*JavaPlot.jar file can be found under JavaPlot-x.x.x/dist/

Arguments
---------

-a --about	 (Displays author and current version of FilterMacs)
-h --help	 (Displays help menu)
-i --interactive (Starts interactive mode to enter file names)

<logFile> <patternFile> <columnDiffFile>		(Runs as <logFile> <patternFile> <columnDiffFile> noplot)
<logFile> <patternFile> <columnDiffFile> <plotOption>   (Give file names and plot option as arguments)
 plotOptions:
 noplot     (Does not plot the data)
 plotfile   (Plots the data into png files)
 plotwindow (Plots the data on interactive windows)


Pattern file guidelines
-----------------------

*Line format in file for patterns
 SearchPhrase ::: ColumnName   (Note: SearchPhrase does not support any regex)
 Example: New authentication request ::: NewAuth
*<space> required before and after the separator ':::' for the program to understand
 the search phrase and column name correctly.
*Lines starting with '#' will be ignored, and line is not counted.
 '#' is supported in search phrase
*Empty lines will be ignored, and line is not counted.
*If a line is unformatted, output will have corresponding columns named UNFORMATTED.
 However, line is counted.

Diff file guidelines
--------------------

*Line format in file for generating time difference between two columns:
 Column1 Column2
 This will compute (Column1 epoch time) - (Column2 epoch time)
*Anything not following correct line format are automatically ignored.
*Empty lines are ignored.


Thank you for trying this tool.
Feedback: suhasdara01@gmail.com
