package jollama;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ollama uses sha256 to verify blobs
 */
public class FileUtil 
{
	private static final Logger logger = Logger.getLogger(FileUtil.class.getName());
	
	/**
	 * Some possible locations for the blob directory:
	 * 
	 * <user home>/.ollama/models/blobs
	 * /usr/share/ollama/.ollama/models/blobs
	 * 
	 */
	
	/**
	 * Assumes the files are named sha256-[hash] the way ollama names model-related files.
	 * "good" contains the list of files that match.
	 * "bad" contains non-matching.
	 * "notchecked" contains files not checked, such as partial.
	 * 
	 * @param dir
	 * @return
	 */
	public static Map<String,List<String>> verifyBlobsInDir(String dir)
	{
		Map<String,List<String>> m = new HashMap<String,List<String>>();
		List<String> good = new ArrayList<String>();
		List<String> bad = new ArrayList<String>();
		List<String> notchecked = new ArrayList<String>();
		m.put("good", good);
		m.put("bad", bad);
		m.put("notchecked", notchecked);
		
		File fd = new File(dir);
		File files[] = fd.listFiles();
		if(files == null || files.length == 0)
		{
			logger.warning("No files in dir '"+dir+"'");
			return m;
		}
		int nf = files.length;
		File f = null;
		String name = null;
		String hash = null;
		
		for(int i=0; i<nf; i++)
		{
			f = files[i];
			name = f.getName();
			if(!name.startsWith("sha256-") || name.contains("partial"))
			{
				notchecked.add(name);
			}
			else
			{
				hash = sha256(f.getAbsolutePath());
				hash = "sha256-"+hash;
				
				if(hash.equals(name))
				{
					logger.info("good\t"+name);
					good.add(name);
				}
				else
				{
					logger.info("bad\t"+name);
					bad.add(name);
				}
			}
		}
		return m;
	}
	
    public static String sha256(String file)
    {
    	String digest = null;
    	FileInputStream fis = null;
    	try
    	{
    		fis = new FileInputStream(file);
    		digest = sha256(fis);
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.WARNING,"Error computing sha256",ex);
    	}
    	finally
    	{
    		if(fis != null)try {fis.close();}catch(Exception ex) {};// ignore errors
    	}
    	return digest;
    }
    
    /**
     * Calculates SHA256 digest of stream.  Caller needs to close stream.
     * 
     * @param is
     */
    public static String sha256(InputStream is)
    {
    	String digest = null;
    	try
    	{
    		MessageDigest md = MessageDigest.getInstance("SHA-256");
    		
    		//byte ba[] = new byte[32*4096];
    		byte ba[] = new byte[1024*1024];  // chunk size might be hardware dependent
    		int numRead = 0;
    		
    		numRead = is.read(ba);
    		while(numRead > 0)
    		{
    			md.update(ba, 0, numRead);
        		numRead = is.read(ba);    			
    		}
    		
    		ba = md.digest();
    		
    		digest = bytesToHex(ba);
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.WARNING,"Error computing sha256",ex);
    	}
    	
    	return digest;
    }
    
    //https://stackoverflow.com/questions/9655181/java-convert-a-byte-array-to-a-hex-string
    //private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] HEX_ARRAY = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
    
    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
    
    public static void main(String args[])
    {
    	String dir = null;
    	File fd = null;
    	
    	if(args != null && args.length>0)
    	{
    		dir = args[0];
    		fd = new File(dir);
    		if(!(fd.exists() && fd.isDirectory()))
    		{
    			logger.severe("Directory does not exist '"+dir+"'");
    			dir = null;
    		}
    	}

    	if(dir == null)
    	{
    		dir = "/usr/share/ollama/.ollama/models/blobs/";
    		fd = new File(dir);
    		if(!(fd.exists() && fd.isDirectory()))
    		{
    			logger.warning("Directory does not exist '"+dir+"'");
    			dir = null;
    		}    		
    	}

    	if(dir == null)
    	{
    		String home = System.getProperty("user.home");
    		if(home == null) home = "~/";
    		home = home.replace('\\', '/');
    		if(!home.endsWith("/"))home+="/";
    		
    		dir = home+".ollama/models/blobs/";
    		fd = new File(dir);
    		if(!(fd.exists() && fd.isDirectory()))
    		{
    			logger.warning("Directory does not exist '"+dir+"'");
    			dir = null;
    		}    		
    	}
    	
    	if(dir == null)
    	{
    		logger.severe("No blobs dir specified");
    		System.exit(0);
    	}
    	
    	logger.info("Verifying blobs in dir '"+dir+"'");
    	
    	Map<String,List<String>> m = FileUtil.verifyBlobsInDir(dir);
    	
    	List<String> keys = new ArrayList<String>(m.keySet());
    	List<String> names = null;
    	
    	for(int i=0; i<keys.size(); i++)
    	{
    		String key = keys.get(i);
    		names = m.get(key);
    		System.out.println("\n\n\n"+key);
    		if(names != null)
    		{
	    		for(int j=0; j<names.size(); j++)
	    		{
	    			System.out.println(j + "\t" + names.get(j));
	    		}
    		}
    	}

    	System.exit(0);
    }
}
