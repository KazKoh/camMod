package com.starrycammod;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.minecraft.world.phys.Vec3;

public class camWriter{ 
    @JsonProperty("camListName")
    private String camListName;

    @JsonProperty("CamPos")
    private Vec3 camPos;

    @JsonProperty("CamLookingX")
    private float camLookingX;
    @JsonProperty("CamLookingY")
    private float camLookingY;



    @JsonCreator
    public camWriter()
    {

    }
    public camWriter(String camListName, Vec3 camPos, float camLookingX, float camLookingY) {
        this.camListName = camListName;
        this.camPos = camPos;
        this.camLookingX = camLookingX;
        this.camLookingY = camLookingY;
    }
    public String getCamListName() {
        return camListName;
    }
    public Vec3 getCamPos()
    {
        return camPos;
    }
    public float getCamLookingX()
    {
        return camLookingX;
    }
    public float getCamLookingY()
    {
        return camLookingY;
    }

    public String toString()
    {
        return "Camera at " + camPos.x + ", " + camPos.y + ", " + camPos.z;
    }
}