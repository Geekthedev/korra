package korra.registry;

import java.util.Objects;

/**
 * Version of a KORRA agent
 */
public class AgentVersion implements Comparable<AgentVersion> {
    private final int major;
    private final int minor;
    private final int patch;
    
    /**
     * Create a new agent version
     * 
     * @param major Major version number
     * @param minor Minor version number
     * @param patch Patch version number
     */
    public AgentVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }
    
    /**
     * Parse a version string in the format "major.minor.patch"
     * 
     * @param versionString Version string
     * @return Agent version
     * @throws IllegalArgumentException If the version string is invalid
     */
    public static AgentVersion parse(String versionString) {
        String[] parts = versionString.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid version string: " + versionString);
        }
        
        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = Integer.parseInt(parts[2]);
            return new AgentVersion(major, minor, patch);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid version string: " + versionString, e);
        }
    }
    
    /**
     * Get the major version number
     * 
     * @return Major version number
     */
    public int getMajor() {
        return major;
    }
    
    /**
     * Get the minor version number
     * 
     * @return Minor version number
     */
    public int getMinor() {
        return minor;
    }
    
    /**
     * Get the patch version number
     * 
     * @return Patch version number
     */
    public int getPatch() {
        return patch;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentVersion that = (AgentVersion) o;
        return major == that.major && minor == that.minor && patch == that.patch;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }
    
    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }
    
    @Override
    public int compareTo(AgentVersion other) {
        if (major != other.major) {
            return Integer.compare(major, other.major);
        }
        if (minor != other.minor) {
            return Integer.compare(minor, other.minor);
        }
        return Integer.compare(patch, other.patch);
    }
}