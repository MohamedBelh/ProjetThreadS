import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;


public class Client extends JFrame {

    private JTextField messageField;
    private JTextArea chatArea;
    private JButton sendButton, sendFileButton;
    private JButton downloadButton; // Nouveau bouton pour le téléchargement
    private String username;
    private ObjectOutputStream outputStream;
    public static final int REQUEST_FILE = 5;

    public Client(String serverAddress, int port) {
        // Configuration de l'interface utilisateur
        setupUI();

        try {
            Socket socket = new Socket(serverAddress, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            new Thread(new MessageReceiver(socket)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupUI() {
        setTitle("Client de Chat");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        messageField = new JTextField(20);
        chatArea = new JTextArea(10, 30);
        chatArea.setEditable(false);

        sendButton = new JButton("Envoyer");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        sendFileButton = new JButton("Envoyer Fichier");
        sendFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendFile();
            }
        });

        downloadButton = new JButton("Télécharger");
        downloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                downloadFile();
            }
        });

        JPanel panel = new JPanel();
        panel.add(new JLabel("Message:"));
        panel.add(messageField);
        panel.add(sendButton);
        panel.add(sendFileButton);
        panel.add(downloadButton); // Ajout du bouton de téléchargement

        add(new JScrollPane(chatArea), "Center");
        add(panel, "South");
    }

    private void sendMessage() {
        try {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                outputStream.writeObject(new ChatMessage(ChatMessage.MESSAGE, message, username));
                messageField.setText("");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] fileContent = new byte[(int) file.length()];
                fis.read(fileContent);
                outputStream.writeObject(new ChatMessage(ChatMessage.FILE, fileContent, file.getName(), username));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void downloadFile() {
        // Sélectionner le fichier à télécharger
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();
            // Envoyer une demande au serveur pour le fichier sélectionné
            try {
                // Créer un tableau de bytes vide car nous n'envoyons pas de contenu de fichier
                byte[] emptyContent = new byte[0];
                // Créer un objet ChatMessage avec le type REQUEST_FILE et les informations nécessaires
                outputStream.writeObject(new ChatMessage(ChatMessage.REQUEST_FILE, emptyContent, saveFile.getName(), username));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    private class MessageReceiver implements Runnable {
        private ObjectInputStream inputStream;

        public MessageReceiver(Socket socket) {
            try {
                inputStream = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                while (true) {
                    ChatMessage message = (ChatMessage) inputStream.readObject();
                    if (message.getType() == ChatMessage.MESSAGE) {
                        chatArea.append(message.getSender() + ": " + message.getMessage() + "\n");
                    } else if (message.getType() == ChatMessage.FILE) {
                        File file = new File("received_" + message.getFileName());
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(message.getFileContent());
                        }
                        chatArea.append(message.getSender() + " a envoyé un fichier: " + message.getFileName() + "\n");
                    } else if (message.getType() == ChatMessage.REQUEST_FILE) {
                        // Demander le téléchargement du fichier
                        int response = JOptionPane.showConfirmDialog(null, "Voulez-vous télécharger le fichier: " + message.getFileName() + "?");
                        if (response == JOptionPane.YES_OPTION) {
                            // Télécharger le fichier
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setSelectedFile(new File(message.getFileName()));
                            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                                File saveFile = fileChooser.getSelectedFile();
                                try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                                    fos.write(message.getFileContent());
                                    chatArea.append("Fichier " + message.getFileName() + " téléchargé avec succès.\n");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost"; // Adresse du serveur
        int port = 9001; // Port du serveur
        String username = JOptionPane.showInputDialog("Entrez votre nom:");
        if (username != null && !username.isEmpty()) {
            Client client = new Client(serverAddress, port);
            client.username = username;
            client.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, "Le nom ne peut pas être vide.");
        }
    }
}
