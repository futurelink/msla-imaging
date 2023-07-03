package futurelink.msla.formats.utils;

import futurelink.msla.formats.MSLAFile;
import futurelink.msla.formats.MSLAFileDefaults;
import futurelink.msla.formats.MSLAFileFactory;
import futurelink.msla.formats.anycubic.PhotonWorkshopFileFactory;
import futurelink.msla.formats.creality.CXDLPFileFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FileFactory {
    private static final ArrayList<MSLAFileFactory> supportedFiles = new ArrayList<>();
    static {
        supportedFiles.add(new PhotonWorkshopFileFactory());
        supportedFiles.add(new CXDLPFileFactory());
    }

    public static MSLAFileDefaults defaults(String machineName) {
        return supportedFiles
                .stream()
                .filter((f) -> f.checkDefaults(machineName))
                .findFirst()
                .map(f -> f.defaults(machineName))
                .orElse(null);
    }

    public static MSLAFile create(String machineName) {
        return supportedFiles
                .stream()
                .filter((f) -> f.checkDefaults(machineName))
                .findFirst()
                .map(f -> {
                    try { return f.create(machineName); } catch (IOException e) { throw new RuntimeException(e); }
                })
                .orElse(null);
    }

    public static MSLAFile load(String fileName) throws IOException {
        // Detect file type and choose a factory to operate on a file
        try (var stream = new FileInputStream(fileName)) {
            return supportedFiles.stream().filter((t) -> {
                try {
                    if (t.checkType(stream)) {
                        System.out.println("Found file of type " + t.getClass());
                        return true;
                    }
                    return false;
                } catch (IOException e) { throw new RuntimeException(e); }
            }).findFirst().map(f -> {
                try { return f.load(fileName); } catch (IOException e) { throw new RuntimeException(e); }
            }).orElse(null);
        }
    }

    public static ArrayList<String> getSupportedMachines() {
        var a = new ArrayList<String>();
        supportedFiles.forEach((f) -> { if (f.getSupportedMachines() != null) a.addAll(f.getSupportedMachines()); });
        return a;
    }
}
