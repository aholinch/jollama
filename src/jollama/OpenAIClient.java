package jollama;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import jollama.json.JSONArray;
import jollama.json.JSONObject;

public class OpenAIClient extends OllamaClient 
{
	public OpenAIClient()
	{
		super();
	}
	
	/**
	 * For OpenAI this will actually be a chat completion
	 */
    public String generateResponse(String model, String prompt, String base64Image, String format)
    {
    	String output = null;
    	
    	try
    	{
    		String jsonStr = generateResponseJSON(model,prompt,base64Image,format);
    		JSONObject obj = new JSONObject(jsonStr);
    		output = obj.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.WARNING,"Error getting prompt response",ex);
    	}
    	
    	return output;
    }
    
	/**
	 * For OpenAI this will actually be a chat completion
	 */
    public String generateResponseJSON(String model, String prompt, String base64Image, String format)
    {

    	String url = baseURL + "/v1/chat/completions";
    	JSONObject obj = new JSONObject();
    	obj.put("model", model);
    	//obj.put("input", prompt);
    	//obj.put("stream", false);
    	//obj.put("think", think);
    	/*
    	 "messages": [
    	              {
    	                "role": "developer",
    	                "content": "You are a helpful assistant."
    	              },
    	              {
    	                "role": "user",
    	                "content": "Hello!"
    	              }
    	            ]
    	*/
    	JSONObject role = new JSONObject();
    	role.put("role", "user");
    	role.put("content", prompt);
    	JSONArray arr = new JSONArray();
    	arr.put(role);
    	obj.put("messages", arr);
    	/*
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
    	*/
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
    		    JSONArray arr = obj.getJSONArray("data");
    		    int len = arr.length();
    		    for(int i=0; i<len; i++)
    		    {
    		    	names.add(arr.getJSONObject(i).getString("id"));
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
    	String url = baseURL + "/v1/models";
    	return getJSON(url);
    }
}
