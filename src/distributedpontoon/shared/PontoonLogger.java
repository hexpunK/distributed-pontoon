package distributedpontoon.shared;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Handles the logging of system events to both files and consoles.
 * 
 * @author 6266215
 * @version 1.3
 * @since 2015-02-21
 */
public class PontoonLogger 
{
    /** The {@link Handler} to write the log to file through. */
    private static Handler file;
    /** The {@link Handler} to write to the console with. */
    private static Handler con;
    /** Basic log formatter for the file output. */
    private static Formatter fileFmt;
    /** A {@link Formatter} for the console output. */
    private static Formatter conFmt;
    /** The prefix of the output fie as a String. */
    private static String prefix;
    /** Set this to true to log to file, false to not log to file. */
    public static boolean fileLog = true;
    /** Set to true to get more verbose output to the console. */
    public static boolean verbose = false;
    /** Prevents double configuration per-process. */
    private static boolean configured = false;
    
    /**
     * Set up this {@link PontoonLogger} to make the 
     * {@link Logger#GLOBAL_LOGGER_NAME} log to the correct files.
     * 
     * @param prefix The prefix for the log file name.
     * @throws IOException Thrown if there are any issues setting up the 
     * outputs for this {@link PontoonLogger}.
     * @since 1.0
     */
    public static final void setup(String prefix) throws IOException
    {
        if (configured) return; // Prevent double configuration.
        
        PontoonLogger.prefix = prefix;
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Calendar cal = Calendar.getInstance();
        String fileName = String.format("%s-%s.log", 
                prefix, fmt.format(cal.getTime()));
        
        logger.setUseParentHandlers(false); // Disable built in formatters.
        logger.setLevel(Level.FINEST); // Logging granularity.
        
        fileFmt = new FileFormatter();
        conFmt = new ConsoleFormatter();
        
        if (fileLog) {
            file = new FileHandler(fileName, true);
            if (!verbose)
                file.setLevel(Level.INFO);
            else 
                file.setLevel(Level.FINER);
            file.setFormatter(fileFmt);
            logger.addHandler(file);
        }
        
        con = new ConsoleHandler();
        if (!verbose)
            con.setLevel(Level.INFO);
        else 
            con.setLevel(Level.FINER);
        con.setFormatter(conFmt);
        logger.addHandler(con);
        
        configured = true;
    }
    
    /**
     * Shuts down the file and console streams to release them for other 
     * applications to use.
     * 
     * @throws IOException Thrown if there are any issues closing the file or 
     * console outputs.
     * @since 1.1
     */
    public static final void close() throws IOException
    {
        if (!configured) return;
        
        if (fileLog)
            file.close();
        con.close();
        
        LogManager.getLogManager().reset();
    }
    
    /**
     * Formats the logged messages to be place in a file.
     * 
     * @version 1.0
     * @since 1.2
     */
    private static final class FileFormatter extends Formatter
    {
        @Override
        public String getHead(Handler h) 
        {
            return String.format("Pontoon Log (%s)%nLogging start: %s%n%n", 
                    PontoonLogger.prefix, getDateString());
        }
        
        
        @Override
        public String format(LogRecord record) 
        {            
            return String.format("%s : %s - %s%n", 
                    record.getLevel(), getDateString(), formatMessage(record));
        }

        @Override
        public String getTail(Handler h) 
        {
            return String.format("%nLogging end: %s%n", getDateString());
        }
        
        /**
         * Generate a ISO formatted date string.
         * 
         * @return A String containing the current date and time in ISO format.
         * @since 1.0
         */
        private String getDateString()
        {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            return f.format(cal.getTime());
        }
    }
    
    /**
     * Formats messages to print them out to a single line for console output.
     * 
     * @version 1.0
     * @since 1.1
     */
    private static final class ConsoleFormatter extends Formatter
    {
        @Override
        public String format(LogRecord record) 
        {   
            return String.format("%s%n", formatMessage(record));
        }
    }
}
