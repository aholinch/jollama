package jollama;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jollama.json.JSONArray;
import jollama.json.JSONObject;

public class OllamaClient 
{
	public static final Logger logger = Logger.getLogger(OllamaClient.class.getName());
	
    protected String baseURL = "http://localhost:11434";
    protected String userAgent = "jollama";
    
    protected String overrideSystemPrompt = null;
    protected boolean think = true;
    
    public OllamaClient()
    {
    	
    }
    
    public boolean getThink()
    {
    	return think;
    }
    
    public void setThink(boolean flag)
    {
    	think = flag;
    }
    
    public void setOverrideSystemPrompt(String prompt)
    {
    	overrideSystemPrompt = prompt;
    }
    
    public String getOverrideSystemPrompt()
    {
    	return overrideSystemPrompt;
    }
    
    public void setBaseURL(String url)
    {
    	baseURL = url;
    }
    
    public String getBaseURL()
    {
    	return baseURL;
    }
    
    public void setUserAgent(String agent)
    {
    	userAgent = agent;
    }
    
    public String getUserAgent()
    {
    	return userAgent;
    }
    
    /**
     * A non-streaming response to a generate prompt returning just the response.
     * 
     * @param model
     * @param prompt
     * @return
     */
    public String generateResponse(String model, String prompt)
    {
    	return generateResponse(model,prompt,null,null);
    }
    
    public String generateResponse(String model, String prompt, String base64Image)
    {

    	return generateResponse(model,prompt,base64Image,null);
    }
    

    public String generateResponse(String model, String prompt, String base64Image, String format)
    {
    	String output = null;
    	
    	try
    	{
    		String jsonStr = generateResponseJSON(model,prompt,base64Image,format);
    		JSONObject obj = new JSONObject(jsonStr);
    		output = obj.getString("response");
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.WARNING,"Error getting prompt response",ex);
    	}
    	
    	return output;
    }
    
    /**
     * A non-streaming response to a generate prompt returning the whole JSON object.
     * 
     * @param model
     * @param prompt
     * @return
     */
    public String generateResponseJSON(String model, String prompt)
    {
    	return generateResponseJSON(model,prompt,null,null);
    }
    
    public String generateResponseJSON(String model, String prompt, String base64Image)
    {
    	return generateResponseJSON(model,prompt,base64Image,null);
    }
    
    public String generateResponseJSON(String model, String prompt, String base64Image, String format)
    {

    	String url = baseURL + "/api/generate";
    	JSONObject obj = new JSONObject();
    	obj.put("model", model);
    	obj.put("prompt", prompt);
    	obj.put("stream", false);
    	obj.put("think", think);
    	
    	if(base64Image != null)
    	{
    		obj.put("images",new String[] {base64Image});
    	}
    	if(format != null)
    	{
    		obj.put("format", format);
    	}
    	if(overrideSystemPrompt != null)
    	{
    		obj.put("system", overrideSystemPrompt);
    	}
    	
    	return postJSON(url,obj.toString());
    }
 
    /**
     * A streaming response to a generate prompt returning just the response.
     * 
     * @param model
     * @param prompt
     * @param callback
     * @return
     */
    public void streamGenerateResponse(String model, String prompt, StreamTokenCallback callback)
    {  	
    	streamGenerateResponse(model,prompt,null,null,callback);
    }
    public void streamGenerateResponse(String model, String prompt, String base64Image, StreamTokenCallback callback)
    {  	
    	streamGenerateResponse(model,prompt,base64Image,null,callback);
    }
    public void streamGenerateResponse(String model, String prompt, String base64Image, String format, StreamTokenCallback callback)
    {  	

    	try
    	{
            AsyncGenerate aGen = new AsyncGenerate();
            aGen.setModel(model);
            aGen.setPrompt(prompt);
            aGen.setBase64Image(base64Image);
            aGen.setTokenCallback(callback);
            aGen.setFormat(format);
            aGen.setOverrideSystemPrompt(overrideSystemPrompt);

            Thread t = new Thread(aGen);
            t.start();
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.WARNING,"Error getting prompt response",ex);
    	}    	
    }
    
    /**
     * A streaming response to a generate prompt returning the whole JSON object.
     * 
     * @param model
     * @param prompt
     * @param callback
     * @return
     */
    public void streamGenerateResponseJSON(String model, String prompt, StreamJSONCallback callback)
    {
    	streamGenerateResponseJSON(model,prompt,null,null,callback);
    }
    
    public void streamGenerateResponseJSON(String model, String prompt, String base64Image, StreamJSONCallback callback)
    {
    	streamGenerateResponseJSON(model,prompt,base64Image,null,callback);
    }
    
    public void streamGenerateResponseJSON(String model, String prompt, String base64Image, String format, StreamJSONCallback callback)
    {

    	try
    	{
            AsyncGenerate aGen = new AsyncGenerate();
            aGen.setModel(model);
            aGen.setPrompt(prompt);
            aGen.setBase64Image(base64Image);
            aGen.setFormat(format);
            aGen.setJSONCallback(callback);
            aGen.setOverrideSystemPrompt(overrideSystemPrompt);
            
            Thread t = new Thread(aGen);
            t.start();
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.WARNING,"Error getting prompt response",ex);
    	}
    }

    /**
     * return the embedding array
     * 
     * @param model
     * @param prompt
     * @return
     */
    public double[] getEmbedding(String model, String prompt)
    {
    	double da[] = null;
    	
    	try
    	{
    		String jsonStr = getEmbeddingJSON(model,prompt);
    		JSONObject obj = new JSONObject(jsonStr);
    		JSONArray arr = obj.getJSONArray("embedding");
    		int len = arr.length();
    		da = new double[len];
    		for(int i=0; i<len; i++)
    		{
    			da[i]=arr.getDouble(i);
    		}
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.WARNING,"Error getting prompt response",ex);
    	}
    	
    	return da;
    }
    
    /**
     * A non-streaming response to a generate prompt returning the whole JSON object.
     * Uses /api/embeddings
     * 
     * @param model
     * @param prompt
     * @return
     */
    public String getEmbeddingJSON(String model, String prompt)
    {
    	String url = baseURL + "/api/embeddings";
    	JSONObject obj = new JSONObject();
    	obj.put("model", model);
    	obj.put("prompt", prompt);
    	return postJSON(url,obj.toString());
    }

    /**
     * Embed single text with /api/embed call.
     * 
     * @param model
     * @param text
     * @return
     */
    public double[] getEmbed(String model, String text)
    {
    	List<String> list = new ArrayList<String>();
    	list.add(text);
    
    	List<double[]> out = getEmbed(model,list);
    	
    	return out.get(0);
    }	

    /**
     * Embed multiple texts with a single call.
     * 
     * @param model
     * @param text
     * @return
     */
    public List<double[]> getEmbed(String model, List<String> text)
    {
    	List<double[]> out = null;
    	
    	try
    	{
    		String jsonStr = getEmbedJSON(model,text);
    		JSONObject obj = new JSONObject(jsonStr);
    		JSONArray bigArr = obj.getJSONArray("embeddings");
    		int size = bigArr.length();
    		out = new ArrayList<double[]>(size);
    		
    		int len = 0;
    		JSONArray arr = null;
        	double da[] = null;
        	for(int j=0; j<size; j++)
        	{
        		arr = bigArr.getJSONArray(j);
    		    len = arr.length();
    		    da = new double[len];
    		    for(int i=0; i<len; i++)
    		    {
    			    da[i]=arr.getDouble(i);
    		    }
    		    out.add(da);
        	}
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.WARNING,"Error getting prompt response",ex);
    	}
    	
    	return out;
    }
    
    /**
     * A non-streaming response to a generate prompt returning the whole JSON object.
     * Uses /api/embed 
     * @param model
     * @param prompt
     * @return
     */
    public String getEmbedJSON(String model, List<String> texts)
    {
    	String url = baseURL + "/api/embed";
    	JSONObject obj = new JSONObject();
    	obj.put("model", model);
    	obj.put("input", texts);
    	obj.put("think", think);

    	return postJSON(url,obj.toString());
    }
    
    /**
     * Returns just the list of local model names.
     * 
     * @return
     */
    public List<String> listModelNames()
    {
    	List<String> names = new ArrayList<String>();
    	
    	String json = listModelsJSON();
    	if(json != null)
    	{
    		try
    		{
    		    JSONObject obj = new JSONObject(json);
    		    JSONArray arr = obj.getJSONArray("models");
    		    int len = arr.length();
    		    for(int i=0; i<len; i++)
    		    {
    		    	names.add(arr.getJSONObject(i).getString("name"));
    		    }
    		}
    		catch(Exception ex)
    		{
    			logger.log(Level.WARNING,"Error getting names",ex);
    		}
    	}
    	return names;	
    }
    
    /**
     * Returns the json with local model info.
     * 
     * @return
     */
    public String listModelsJSON()
    {
    	String url = baseURL + "/api/tags";
    	return getJSON(url);
    }
    
    public String getJSON(String apiURL)
    {
    	HttpURLConnection conn = null;
    	String output = null;
    	
    	try
    	{
    		logger.info("Attempting GET on " + apiURL);
    		conn = getConnection(apiURL);
    		conn.setRequestMethod("GET");
    		
    		int code = conn.getResponseCode();
    		logger.info("Response: " + code);
    		
    		if(code == HttpURLConnection.HTTP_OK)
    		{
    			output = readConnInput(conn);
    		}
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.WARNING,"Error performing GET",ex);
    	}
    	finally
    	{
    	}
    	return output;
    }
    
    public String postJSON(String apiURL, String jsonBody)
    {
    	HttpURLConnection conn = null;
    	String output = null;
    	OutputStream os = null;
    	
    	try
    	{
    		logger.info("Attempting GET on " + apiURL);
    		conn = getConnection(apiURL);
    		conn.setRequestMethod("POST");
    	
    		// send json to server
    		conn.setDoOutput(true);
    		os = conn.getOutputStream();
    		os.write(jsonBody.getBytes());
    		os.flush();
    		os.close();
    		os = null;
    		
    		// read response
    		int code = conn.getResponseCode();
    		logger.info("Response: " + code);
    		
    		if(code == HttpURLConnection.HTTP_OK)
    		{
    			output = readConnInput(conn);
    		}
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.WARNING,"Error performing POST",ex);
    	}
    	finally
    	{
    		if(os != null)try {os.close();}catch(Exception ex) {}
    	}
    	return output;

    }
    
    protected String readConnInput(HttpURLConnection conn)
    {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuffer sb = new StringBuffer(10000);
        try
        {
        	is = conn.getInputStream();
        	isr = new InputStreamReader(is,"UTF-8");
        	br = new BufferedReader(isr);
        	
        	String line = null;
        	
        	line = br.readLine();
        	while(line != null)
        	{
        		sb.append(line);
        		sb.append("\n");
        		line = br.readLine();
        	}
        }
        catch(Exception ex)
        {
        	logger.log(Level.WARNING,"Error reading response",ex);
        }
        finally
        {
        	if(is != null)try {is.close();}catch(Exception ex) {}
        	if(isr != null)try {isr.close();}catch(Exception ex) {}
        	if(br != null)try {br.close();}catch(Exception ex) {}
        }
        
        return sb.toString();
    }
    
    protected HttpURLConnection getConnection(String url)
    {
    	HttpURLConnection conn = null;
    	
    	try
    	{
    	    URL urlObj = new URL(url);
    	    conn = (HttpURLConnection)urlObj.openConnection();
    	    conn.setRequestProperty("User-Agent", userAgent);
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.WARNING,"Error getting connection",ex);
    	}
    	return conn;
    }

    protected class AsyncGenerate implements Runnable
    {
        protected StreamTokenCallback tokenCallback;
        protected StreamJSONCallback jsonCallback;
        protected String model;
        protected String prompt;
        protected String finalLine;
        protected String base64Image;
        protected String format;
        protected String overrideSystemPrompt;
        
        public AsyncGenerate()
        {
        	
        }
        
        public void setFormat(String str)
        {
        	format = str;
        }
        
        public String getFormat()
        {
        	return format;
        }
        
        public void setOverrideSystemPrompt(String prompt)
        {
        	overrideSystemPrompt = prompt;
        }
        
        public String getOverrideSystemPrompt()
        {
        	return overrideSystemPrompt;
        }
        
        public void setTokenCallback(StreamTokenCallback callback)
        {
        	tokenCallback = callback;
        }
        
        public void setJSONCallback(StreamJSONCallback callback)
        {
        	jsonCallback = callback;
        }
        
        public void setModel(String str)
        {
        	model = str;
        }
        
        public void setPrompt(String str)
        {
        	prompt = str;
        }
        
        public void setBase64Image(String img)
        {
        	base64Image = img;
        }
        
		@Override
		public void run() 
		{
	    	String url = baseURL + "/api/generate";
	    	JSONObject obj = new JSONObject();
	    	obj.put("model", model);
	    	obj.put("prompt", prompt);
	    	obj.put("stream", true);
	    	obj.put("think", think);

	    	if(base64Image != null)
	    	{
	    		obj.put("images", new String[] {base64Image});
	    	}
	    	
	    	if(format != null)
	    	{
	    		try
	    		{
		    		obj.put("format", new JSONObject(format));
	    		}
	    		catch(Exception ex)
	    		{
	    			logger.log(Level.WARNING, "Error converting format to JSON",ex);
	    		}
	    	}
	    	
	    	if(overrideSystemPrompt != null)
	    	{
	    		obj.put("system", overrideSystemPrompt);
	    	}

	    	String output = postJSON(url,obj.toString());
	    	this.finalLine = output;
		}
		
		public String getFinalLine()
		{
			return finalLine;
		}
		
		protected boolean handleLine(String line)
		{
			boolean flag = false;
			JSONObject obj = new JSONObject(line);
			
			if(obj.getBoolean("done"))
			{
				flag = true;
			}
			
			if(jsonCallback != null)
			{
				jsonCallback.nextJSON(line);
				if(flag)jsonCallback.streamFinished();
			}
			else if(tokenCallback != null)
			{
				tokenCallback.nextTokens(obj.getString("response"));
				if(flag)tokenCallback.streamFinished();
			}
			
			return flag;
		}
		
	    protected String postJSON(String apiURL, String jsonBody)
	    {
	    	HttpURLConnection conn = null;
	    	String output = null;
	    	OutputStream os = null;
	    	InputStream is = null;
	        InputStreamReader isr = null;
	        BufferedReader br = null;
	        
	    	try
	    	{
	    		logger.info("Attempting GET on " + apiURL);
	    		conn = getConnection(apiURL);
	    		conn.setRequestMethod("POST");

	    		// send json to server
	    		conn.setDoOutput(true);
	    		os = conn.getOutputStream();
	    		os.write(jsonBody.getBytes());
	    		os.flush();
	    		os.close();
	    		os = null;
	    		
	    		// read response
	    		int code = conn.getResponseCode();
	    		logger.info("Response: " + code);
	    		
	    		if(code == HttpURLConnection.HTTP_OK)
	    		{
	    			is = conn.getInputStream();
	            	isr = new InputStreamReader(is,"UTF-8");
	            	br = new BufferedReader(isr);
	            	
	            	String line = null;
	            	
	            	line = br.readLine();
	            	while(line != null)
	            	{
	            		if(handleLine(line))
	            		{
	            			logger.info("Last response was: " + line);
	            			output = line;
	            			break;
	            		}
	            		
	            		line = br.readLine();
	            	}
	    		}
	    	}
	    	catch(Exception ex)
	    	{
	    		logger.log(Level.WARNING,"Error performing POST",ex);
	    	}
	    	finally
	    	{
	    		if(os != null)try {os.close();}catch(Exception ex) {}
	        	if(is != null)try {is.close();}catch(Exception ex) {}
	        	if(isr != null)try {isr.close();}catch(Exception ex) {}
	        	if(br != null)try {br.close();}catch(Exception ex) {}
	    	}
	    	return output;

	    }
    	
    }
    
    public static interface StreamTokenCallback
    {
    	public void nextTokens(String tokens);
    	public void streamFinished();
    }
    
    public static interface StreamJSONCallback
    {
    	public void nextJSON(String json);
    	public void streamFinished();
    }
}
