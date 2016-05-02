package io.jiaqiwu.interviews;

import static org.hamcrest.Matchers.is;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DuplicateFinderTest
{
    private static Path testDir;

    private static int NUM_DUPLICATES = 1;
    private static int NUM_FILES_1 = 4;

    @BeforeClass
    public static void setup() throws URISyntaxException
    {
        URL testDirURL = DuplicateFinderTest.class.getClassLoader().getResource(".");
        DuplicateFinderTest.testDir = Paths.get(testDirURL.toURI());
    }

    @Test
    public void testGetDuplicatesValid()
    {
        DuplicateFinder finder = new DuplicateFinder();
        Map<String, List<File>> duplicates = finder.getDuplicateFiles(testDir.toFile());
        Assert.assertThat(duplicates.keySet().size(), is(NUM_DUPLICATES));
        Set<String> checksums = duplicates.keySet();
        for (String checksum : checksums)
        {
            Assert.assertThat(duplicates.get(checksum).size(), is(NUM_FILES_1));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDirectoryDoesNotExist()
    {
        DuplicateFinder finder = new DuplicateFinder();
        finder.getDuplicateFiles(new File("bogus"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDirectoryIsFile()
    {
        DuplicateFinder finder = new DuplicateFinder();
        File fileNotDir = null;
        for (File testFile : testDir.toFile().listFiles())
        {
            if (testFile.isFile())
            {
                fileNotDir = testFile;
                break;
            }
        }
        finder.getDuplicateFiles(fileNotDir);
    }
}
