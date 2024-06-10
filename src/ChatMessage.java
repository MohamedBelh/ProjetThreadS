
import java.io.Serializable;

public class ChatMessage implements Serializable {

    public static final int MESSAGE = 1, LOGOUT = 2, FILE = 3, VOICE = 4;
    private final int type;
    private final String message;
    private final String sender;
    private final byte[] fileContent; // Pour les fichiers et les messages vocaux
    private final String fileName;    // Pour stocker le nom du fichier
    private final long fileSize;      // Pour stocker la taille du fichier (en octets) ou la durée du message vocal (en secondes)
    private final String fileFormat;  // Pour stocker le format du fichier ou du message vocal
    public static final int REQUEST_FILE = 5;

    // Constructeur pour les messages texte et logout
    public ChatMessage(int type, String message, String sender) {
        this.type = type;
        this.message = message;
        this.sender = sender;
        this.fileContent = null;
        this.fileName = null;
        this.fileSize = 0;
        this.fileFormat = null;
    }

    public ChatMessage(int type, byte[] fileContent, String fileName, String sender) {
        this.type = type;
        this.fileContent = fileContent;
        this.fileName = fileName;
        this.sender = sender;
        this.message = null;
        this.fileSize = 0; // Vous n'avez pas la taille du fichier dans ce constructeur
        this.fileFormat = null; // Vous n'avez pas le format du fichier dans ce constructeur
    }
    // Getters
    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileFormat() {
        return fileFormat;
    }
}
