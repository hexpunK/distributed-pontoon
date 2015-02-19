package distributedpontoon.shared;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Handles the logging of system events to both files and consoles.
 * 
 * @author 6266215
 * @version 1.1
 * @since 2015-02-19
 */
public class PontoonLogger 
{
    /** The {@link FileHandler} to write the log to file through. */
    private static FileHandler file;
    /** The {@link ConsoleHandler} to write to the console with. */
    private static ConsoleHandler con;
    /** Basic log formatter for the file output. */
    private static Formatter fileFmt;
    /** A {@link Formatter} for the console output. */
    private static Formatter conFmt;
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
        
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Calendar cal = Calendar.getInstance();
        String fileName = String.format("%s-%s.log", 
                prefix, fmt.format(cal.getTime()));
        
        logger.setUseParentHandlers(false); // Disable built in formatters.
        logger.setLevel(Level.FINE); // Logging granularity.
        
        fileFmt = new SimpleFormatter();
        conFmt = new ConsoleFormatter();
        
        file = new FileHandler(fileName, true);
        file.setLevel(Level.INFO);
        file.setFormatter(fileFmt);
        logger.addHandler(file);
        
        con = new ConsoleHandler();
        con.setLevel(Level.FINE);
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
        
        file.close();
        con.close();
        
        LogManager.getLogManager().reset();
    }
    
    /**
     * Formats messages to print them out to a single line for console output.
     * 
     * @since 1.1
     */
    private static final class ConsoleFormatter extends Formatter
    {
        @Override
        public String format(LogRecord record) 
        {
            StringBuilder sb = new StringBuilder();
            
            sb.append(formatMessage(record));
            sb.append(System.getProperty("line.separator"));
            
            return sb.toString();
        }
    }
}
