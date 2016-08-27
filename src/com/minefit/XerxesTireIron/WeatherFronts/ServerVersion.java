package com.minefit.XerxesTireIron.WeatherFronts;

public class ServerVersion {

    private WeatherFronts plugin;
    public final String nmsVersion;
    public final String major;
    public final String minor;
    public final String revision;

    public ServerVersion(WeatherFronts instance) {
        this.plugin = instance;
        String name = plugin.getServer().getClass().getPackage().getName();
        this.nmsVersion = name.substring(name.lastIndexOf(".") + 1);
        String[] vn = this.nmsVersion.split("_");
        this.major = vn[0];
        this.minor = vn[1];
        this.revision = vn[2];
    }
}
