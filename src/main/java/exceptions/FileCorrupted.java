package exceptions;

import java.io.IOException;

public class FileCorrupted extends IOException {
    public FileCorrupted() {
        super("File is corrupted");
    }
}
