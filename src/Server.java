import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class Server extends JFrame {

    private static final int PORT = 9001;
    private static Set<ObjectOutputStream> clientWriters = new HashSet<>();

    private javax.swing.JButton startButton;
    private javax.swing.JButton stopButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private ServerSocket serverSocket;
    private boolean running = false;

    public Server() {
        initComponents();
    }

    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();
        startButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Serveur ProjetS");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setEditable(false);
        jScrollPane1.setViewportView(jTextArea1);

        jTextField1.setEditable(false);
        jTextField1.setBackground(new java.awt.Color(255, 255, 255));
        jTextField1.setFont(new java.awt.Font("Monospaced", 0, 24));
        jTextField1.setText("     Serveur ");
        jTextField1.setFocusable(false);

        startButton.setFont(new java.awt.Font("Monospaced", 0, 13));
        startButton.setText("Démarrer");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        stopButton.setFont(new java.awt.Font("Monospaced", 0, 13));
        stopButton.setText("Arrêter");
        stopButton.setEnabled(false);
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1)
                        .addComponent(jTextField1)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(startButton, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(stopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(218, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(startButton)
                                        .addComponent(stopButton))
                                .addGap(0, 12, Short.MAX_VALUE))
        );

        pack();
    }

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {
        new Thread(() -> startServer()).start();
    }

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {
        stopServer();
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            SwingUtilities.invokeLater(() -> {
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                jTextArea1.append("Le serveur a démarré sur le port " + PORT + "\n");
            });

            while (running) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopServer() {
        try {
            running = false;
            if (serverSocket != null) {
                serverSocket.close();
            }
            SwingUtilities.invokeLater(() -> {
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                jTextArea1.append("Le serveur a été arrêté.\n");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler extends Thread {
        private Socket socket;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                inputStream = new ObjectInputStream(socket.getInputStream());
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                synchronized (clientWriters) {
                    clientWriters.add(outputStream);
                }

                while (true) {
                    ChatMessage message = (ChatMessage) inputStream.readObject();
                    if (message.getType() == ChatMessage.LOGOUT) {
                        break;
                    }
                    broadcast(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    synchronized (clientWriters) {
                        clientWriters.remove(outputStream);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(ChatMessage message) {
            synchronized (clientWriters) {
                for (ObjectOutputStream writer : clientWriters) {
                    try {
                        writer.writeObject(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Server().setVisible(true);
        });
    }
}
