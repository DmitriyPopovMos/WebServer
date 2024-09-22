package WebServer;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Server extends JFrame {
    private JTextField userInputText;
    private JTextArea chatWindow;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private ServerSocket serverSocket;
    private Socket connection;

    public Server() {
        super("Серверная часть");
        userInputText = new JTextField();
        userInputText.setEditable(false);
        userInputText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(e.getActionCommand());
                userInputText.setText("");
            }
        });
        add(userInputText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow));
        setSize(300, 600);
        setVisible(true);
    }

    // Настройка и запуск серверной части

    public void startServer() {
        try {
            serverSocket = new ServerSocket(7777, 100);
            while (true) {
                try {
                    waitForConnection();
                    setupStreams();
                    whileChatting();
                }
                catch (EOFException eofException) {
                    showMessage("\nСервер оборвал соединение!");
                }
                finally {
                    closeConnection();
                }
            }
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    // Ожидание соединения и отображение информации о подключении

    private void waitForConnection() throws IOException {
        showMessage("Ожидание подключения клиентов...\n");
        connection = serverSocket.accept();
        showMessage("Соединён с \n" + connection.getInetAddress().getHostName());
    }

    // Настройка потоков для отправки и получения данных

    private void setupStreams() throws IOException {
        outputStream = new ObjectOutputStream(connection.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(connection.getInputStream());
        showMessage("\nПоток установлен!\n");
    }

    // Обработка данных во время общения

    private void whileChatting() throws IOException {
        String message = "Вы подключены!";
        sendMessage(message);
        readyToType(true);
        do{
            try {
                message = (String) inputStream.readObject();
                showMessage("\n" + message);
            }
            catch (ClassNotFoundException classNotFoundException) {
                showMessage("\nНе пойму, что отправил пользователь!");
            }
        }
        while (!message.equals("КЛИЕНТ - *"));
    }

    // закрываем сокеты и потоки, когда пользователь начатился

    private void closeConnection() {
        showMessage("\nЗакрытие соединения...");
        readyToType(false);
        try {
            outputStream.close();
            inputStream.close();
            connection.close();
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    // Отправка сообщений клиенту

    private void sendMessage(String message) {
        try {
            outputStream.writeObject("СЕРВЕР - " + message);
            outputStream.flush();
            showMessage("\nСЕРВЕР - " + message);
        }
        catch (IOException ioException) {
            chatWindow.append("\nОШИБКА: не могу отправить");
        }
    }

    // Обновление окна чата

    private void showMessage(final String text) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        chatWindow.append(text);
                    }
                }
        );
    }

    // Установка прав на ввод данных

    private void readyToType(final boolean tof) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        userInputText.setEditable(tof);
                    }
                }
        );
    }
}