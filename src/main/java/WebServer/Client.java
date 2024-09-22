package WebServer;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class Client extends JFrame {
    private JTextField userInputText;
    private JTextArea chatWindow;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String message = "";
    private String serverIP;
    private Socket socket;

    // конструктор

    public Client(String host) {
        super("Клиентская часть");
        serverIP = host;
        userInputText = new JTextField();
        userInputText.setEnabled(false);
        userInputText.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        sendMessage(e.getActionCommand());
                        userInputText.setText("");
                    }
                }
        );
        add(userInputText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        chatWindow.setBackground(Color.LIGHT_GRAY);
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        setSize(300, 600);
        setVisible(true);
    }

    // запуск клиента

    public void startClient() {
        try {
            connectToServer();
            setupStreams();
            whileChatting();
        }
        catch (EOFException eofException) {
            showMessage("\nКлиент оборвал соединение");
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
        finally {
             closeConnection();
        }
    }

    // подключаемся к серверу

    private void connectToServer() throws IOException {
        showMessage("Пытаемся подключиться...\n");
        socket = new Socket(InetAddress.getByName(serverIP), 7777);
        showMessage("Теперь ты подключен к: " + socket.getInetAddress().getHostName());
    }

    // настройка потоков для отправки и получения сообщений

    private void setupStreams() throws IOException {
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(socket.getInputStream());
        showMessage("\nПотоки готовы к работе");
    }

    // Обработка данных во время общения

    private void whileChatting() throws IOException {
        readyToType(true);
        do {
            try {
                message = (String) inputStream.readObject();
                showMessage("\n" + message);
            }
            catch (ClassNotFoundException classNotFoundException) {
                showMessage("\nНепонятно!");
            }
        }
        while (!message.equals("Сервер - *"));
    }

    // закрытие потоков и сокетов

    private void closeConnection() {
        showMessage("\nЗакрываем соединение...");
        readyToType(false);
        try {
            outputStream.close();
            inputStream.close();
            socket.close();
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    // отправка сообщений на сервер

    private void sendMessage(String message) {
        try {
            outputStream.writeObject("КЛИЕНТ - " + message);
            outputStream.flush();
            showMessage("\nКЛИЕНТ - " + message);
        }
        catch (IOException ioException) {
            chatWindow.append("\nЧто-то пошло не так во время отправки сообщения...");
        }
    }

    // обновление окна чата

    private void showMessage(final String msg) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        chatWindow.append(msg);
                    }
                }
        );
    }

    // установка прав на ввод текста

    private void readyToType(final boolean tof) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        userInputText.setEnabled(tof);
                    }
                }
        );
    }
}


