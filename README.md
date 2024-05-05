# jollama
This is a Java client for ollama REST API.
ollama already has full clients in Javascript and python.  This is for adding connectivity directly from Java applications.

# Basic Features
The client can list models and generate responses.  The responses can be returned all at once or streamed.  To stream a response just register a callback.

# Dependencies
This package has no external dependencies to compile or run.  To receive a response, there needs to be an ollama server running on localhost on port 11434.

The code does use Sean Leary's [JSON-java](https://github.com/stleary/JSON-java) package.  Because that code has a 'Public Domain' license, a copy of the source is included in this package. 
 
