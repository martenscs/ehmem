package com.googlecode.ehmem.performance.profile;

import java.io.File;
import java.io.FileNotFoundException;

import org.ho.yaml.Yaml;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class ProfileLoader {
    private static final String DEFAULT_FILE = "profile.yaml";

    public static ProfileConfiguration loadProfile(String folder) {
        try {
            return (ProfileConfiguration) Yaml.load(new File(folder, DEFAULT_FILE));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(
                    "Unable to find file [" + DEFAULT_FILE + "] in folder [" + folder + "]"
            );
        }
    }
}
