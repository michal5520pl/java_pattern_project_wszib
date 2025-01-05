package pl.michal5520pl.wszib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

class JSONParser {
    private final File filename;
    private static final Logger logger = LogManager.getLogger();

    JSONParser(String filename){
        this.filename = new File(filename);
    }

    List<User> getUsers(){
        try {
            logger.debug("Starting JSON parsing...");
            JsonElement rootElement = JsonParser.parseReader(new JsonReader(new FileReader(this.filename)));
            ArrayList<User> array = new ArrayList<>();
            Gson gson = new Gson();
            Consumer<JsonElement> arrayAppender = (element) -> {
                if(array.add(gson.fromJson(element.getAsJsonObject(), User.class))){
                    logger.debug("Added element to array");
                }
                else {
                    logger.debug("Failed to add element to array!");
                }
            };

            if(rootElement.isJsonArray()){
                logger.debug("JSON's root element appears to be a list");

                rootElement.getAsJsonArray().forEach(arrayAppender);
            }
            else if(rootElement.isJsonObject()){
                logger.debug("JSON's root element appears to be an object");

                arrayAppender.accept(rootElement.getAsJsonObject());
            }
            else {
                logger.error("JSON seems to be invalid!");
                throw new IllegalArgumentException("File is not a proper JSON!");
            }

            return array;
        }
        catch(FileNotFoundException e){
            logger.error("No file found!");
            System.exit(1);
            return null;
        }
    }
}
