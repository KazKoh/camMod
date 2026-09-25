package com.starrycammod;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class camjsonWriter
{
    @JsonProperty("Cameras")
    private List<camWriter> cameras;

    private File jsonFile;

    ObjectMapper jsonMap;

    public camjsonWriter()
    {
        //lets us read and write our cameras from camList.json
        this.cameras = new ArrayList<>();
        this.jsonMap = new ObjectMapper();
        jsonMap.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMap.addMixIn(Vec3.class, vec3Mixin.class);
        this.jsonFile = new File("jsonFiles/camList.json");
    }
    public List<camWriter> getCameras() {
        return cameras;
    }

    public int addCamera(camWriter camera) {
        this.cameras.add(camera);
        return 1;
    }
    public int removeCamera() {
        this.cameras.removeLast();
        return 1;
    }

    //updates the current in code list via json file.
    public int readCamFile()
    {

        try {
            Path inputPath = Paths.get("jsonFiles/camList.json");
            if(Files.size(inputPath) == 0)
            {
                return 0;
            }
            this.cameras = jsonMap.readValue(jsonFile, new TypeReference<List<camWriter>>(){});
             } catch(IOException e)
             {
                throw new RuntimeException(e);
             }
        return 1;
    }

    //updates json file via in code list.
    public int writeToCamFile()
    {
        String sendToJson = "";
        Path outputPath = Paths.get("jsonFiles/camList.json");
        try {
            sendToJson = jsonMap.writerWithDefaultPrettyPrinter().writeValueAsString(this.cameras);
        }
        catch(JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
        try {
            Files.write(outputPath, sendToJson.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 1;
    }
}