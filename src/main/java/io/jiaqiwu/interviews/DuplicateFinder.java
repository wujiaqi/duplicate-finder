package io.jiaqiwu.interviews;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command line utility to find duplicate files given a directory
 * 
 * @author Jiaqi Wu
 */
public class DuplicateFinder
{
    private static final Logger _logger = LoggerFactory.getLogger(DuplicateFinder.class);

    // Using MD5 for performance since we are not worried about malicious
    // collisions. If we need a more guaranteed approach we can choose SHA-256
    // instead
    private static final String ALGO = "MD5";

    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println("Usage: java -jar duplicate-finder.jar [directory]");
            return;
        }

        Path directory = Paths.get(args[0]);
        DuplicateFinder finder = new DuplicateFinder();
        finder.printDuplicates(directory.toFile());
    }

    /**
     * print duplicate files in the directory to std out
     * 
     * @param directory
     *            a directory
     */
    public void printDuplicates(File directory)
    {
        Map<String, List<File>> duplicates = null;
        try
        {
            duplicates = getDuplicateFiles(directory);
        } catch (IllegalArgumentException ee)
        {
            _logger.error(directory + " is not a directory", ee);
        }
        if (duplicates.keySet().isEmpty())
        {
            System.out.println("No duplicates found");
        } else
        {
            System.out.println("Listing duplicate files:");
        }
        for (String checksum : duplicates.keySet())
        {
            List<File> duplicateList = duplicates.get(checksum);
            System.out.println("\n-------------------------------------------");
            for (File file : duplicateList)
            {
                System.out.println(file);
            }
        }

    }

    /**
     * Returns duplicate files in a directory and all sub directories
     * 
     * @param directory
     *            a directory to scan
     * 
     * @return Returns map of checksums to list of files that share that
     *         checksum
     * @throws IllegalArgumentException
     *             if directory is not a directory
     */
    public Map<String, List<File>> getDuplicateFiles(File directory)
    {
        if (!directory.exists() || !directory.isDirectory())
        {
            throw new IllegalArgumentException(directory + " is not a directory");
        }
        Map<String, List<File>> checksumMap = new HashMap<>();
        Set<String> duplicates = new HashSet<>();
        traverseFiles(directory, checksumMap, duplicates);
        Map<String, List<File>> checksumMapPruned = new HashMap<>();
        for (String checksum : duplicates)
        {
            checksumMapPruned.put(checksum, checksumMap.get(checksum));
        }
        return checksumMapPruned;
    }

    private void traverseFiles(File directory, Map<String, List<File>> checksumMap, Set<String> duplicates)
    {
        for (File child : directory.listFiles())
        {
            if (child.isDirectory())
            {
                traverseFiles(child, checksumMap, duplicates);
                continue;
            }
            String checksum = getFileChecksum(child);
            if (checksumMap.containsKey(checksum))
            {
                checksumMap.get(checksum).add(child);
                // this branch means file already exists, add to set of
                // duplicates
                duplicates.add(checksum);
            } else
            {
                List<File> fileList = new ArrayList<File>();
                fileList.add(child);
                checksumMap.put(checksum, fileList);
            }
        }
    }

    private String getFileChecksum(File file)
    {
        try (FileInputStream fis = new FileInputStream(file);
                DigestInputStream dis = new DigestInputStream(fis, MessageDigest.getInstance(ALGO));)
        {
            int numRead = 0;
            byte[] buffer = new byte[1024]; // read 1kb at a time
            do
            {
                numRead = dis.read(buffer);
            } while (numRead != -1);
            byte[] checksum = dis.getMessageDigest().digest();
            return Hex.encodeHexString(checksum);
        } catch (FileNotFoundException e)
        {
            _logger.error("File not found: " + file, e);
        } catch (NoSuchAlgorithmException ee)
        {
            _logger.error(ALGO + " is not a valid Message Digest algorithm");
        } catch (IOException ee)
        {
            _logger.error("Error occured while reading file", ee);
        }

        return null;
    }

}
