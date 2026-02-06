package org.informatics.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for object serialization and deserialization
 */
public class SerializationUtil {

    /**
     * Serializes an object to file
     */
    public static void serialize(Object obj, String filePath) throws IOException {
        // Create directory if it doesn't exist
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(obj);
        }
    }

    /**
     * Deserializes an object from file
     */
    public static Object deserialize(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return ois.readObject();
        }
    }

    /**
     * Serializes an object to byte array
     */
    public static byte[] serializeToBytes(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        }
    }

    /**
     * Deserializes an object from byte array
     */
    public static Object deserializeFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ois.readObject();
        }
    }
}