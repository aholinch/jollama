package jollama;

import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;

public class ImageUtil 
{
    public static String imageToBase64(String imageFile)
    {
    	File f = new File(imageFile);
    	return imageToBase64(f);
    }
    
    public static String imageToBase64(File imageFile)
    {
    	int size = (int)imageFile.length();
    	byte ba[] = new byte[(int)size];
    	
    	FileInputStream fis = null;
    	try
    	{
    		fis = new FileInputStream(imageFile);
    		byte small[] = new byte[4096];
    		int numRead = 0;
    		int tot = 0;
    		numRead = fis.read(small);
    		while(numRead > 0)
    		{
        		System.arraycopy(small, 0, ba, tot, numRead);
    			tot+=numRead;
    			
    			numRead = fis.read(small);
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	finally
    	{
    		if(fis != null)try {fis.close();}catch(Exception ex) {}
    	}
    	return bytesToBase64(ba);
    }
    
    public static String bytesToBase64(byte ba[])
    {
    	return Base64.getEncoder().encodeToString(ba);
    }
}
