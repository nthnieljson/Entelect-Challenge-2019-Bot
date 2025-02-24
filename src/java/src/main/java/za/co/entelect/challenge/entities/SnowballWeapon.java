package za.co.entelect.challenge.entities;

import com.google.gson.annotations.SerializedName;

public class SnowballWeapon {
    @SerializedName("freezeDuration")
    public int freezeDuration;

    @SerializedName("range")
    public int range;

    @SerializedName("count")
    public int count;

    @SerializedName("freezeRadius")
    public int freezeRadius;
}