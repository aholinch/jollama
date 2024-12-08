# jollama
This is a Java client for ollama REST API.
ollama already has full clients in Javascript and python.  This is for adding connectivity directly from Java applications.

# Basic Features
The client can list models and generate responses.  The responses can be returned all at once or streamed.  To stream a response just register a callback.

## Getting Started
If ollama is running locally on the default port, the following is all that is needed to make the client ready to use.
    
    import jollama.OllamaClient;
    
    OllamaClient client = new OllamaClient();


## List Models
To list models, instantiate the client and call

    List<String> modelNames = client.listModelNames();
    for(int i=0; i<modelNames.size(); i++)
    {
        System.out.println(i + "\t" + modelNames.get(i));
    }

## To generate a response from a prompt

    String model = "llama3";
    String prompt = "Why is the sky blue?";
    
    String response = client.generateResponse(model,prompt);
    
    System.out.println(response);
    
To stream the response instead of blocking while the whole thing is generated you can use the stream methods.  They require that you implement one of the callback interfaces  _StreamTokenCallback_  or  _StreamJSONCallback_ .

## To get a vector embedding
Ollama (as of 0.3.0) seems to have two api endpoints "/api/embeddings" and "/api/embed".  This client can call either one.  Be aware that they return different calculated embeddings [Github Issue](https://github.com/chroma-core/chroma/issues/2614).

    String model = "llama3";
    String text = "Why is the sky blue?";
    
    // uses /api/embed
    double vec[] = client.getEmbed(model,text);
    
    
# Building
You can easily import the src folder into a project in your IDE.  From the command line you can execute the compile.sh (Mac and Linux) or compile.bat script.  It assumes that you have javac and jar available on the command line.

It will produce a set of class files in the target/bin directory and a jar file in the target/jar directory.
 
# Dependencies
This package has no external dependencies to compile or run.  To receive a response, there needs to be an ollama server running on localhost on port 11434.

The code does use Sean Leary's [JSON-java](https://github.com/stleary/JSON-java) package.  Because that code has a 'Public Domain' license, a copy of the source is included in this package. 
 
# Test UI
There is a simple chat user interface written in Swing.  Just run the testui.sh or testui.bat script after compiling the code.  The UI has a list of available models.  Enter a prompt and click generate.  The response will stream back from the model.

# JSON Format
As of version 0.5.x Ollama supports a JSON format object to geerate structured output.  Pass the format as a String parameter generateResponse() and Ollama will enforce it.  Newer models do better with it, but even older ones tend to comply.  See their [blog post](https://ollama.com/blog/structured-outputs).