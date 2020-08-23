import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler {
    private static final int WIDTH = 700;
    private static final int HEIGHT = 600;

    public static Socket socket;
    private DataInputStream is;
    private DataOutputStream os;

    private final JTextArea log = new JTextArea();
    private final JPanel panelTop = new JPanel(new GridLayout(2, 3));
    private final JTextField tfIPAddress = new JTextField("localhost");
    private final JTextField tfPort = new JTextField("8189");

    private final JTextField tfLogin = new JTextField("Andy");
    private final JPasswordField tfPassword = new JPasswordField("123");
    private final JButton btnLogin = new JButton("Login");

          private final JFileChooser directionServer = new JFileChooser();
          private final JFileChooser directionClient = new JFileChooser();



    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JButton btnDisconnect = new JButton("<html><b>Disconnect</b></html>");
    private boolean shownIoErrors = false;
    String clientPath = "/users/pilot/Desktop/java/level5_cloud/PreFinal_version/clientclgit /client_users/";
    String serverPath ="/users/pilot/Desktop/java/level5_cloud/PreFinal_version/seervercl/users";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI();
            }
        });
    }

    ClientGUI() {

        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(WIDTH, HEIGHT);
        JScrollPane scrLog = new JScrollPane(log);

          directionServer.setCurrentDirectory(new File (clientPath));
          directionServer.setPreferredSize(new Dimension(250,0));
          directionServer.setApproveButtonText("Send to Server");
          directionServer.addActionListener(this);

          directionClient.setPreferredSize(new Dimension(250,0));
          directionClient.setApproveButtonText("Send to Client");
          directionClient.addActionListener(this);
          directionClient.setVisible(false);

        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setEditable(false);
        btnLogin.addActionListener(this);

        panelTop.add(tfIPAddress);
        panelTop.add(tfPort);
        panelTop.add(tfLogin);
        panelTop.add(tfPassword);
        panelTop.add(btnLogin);
        panelBottom.add(btnDisconnect, BorderLayout.WEST);

        add(scrLog, BorderLayout.CENTER);
        add(directionServer,BorderLayout.WEST);
        add(directionClient, BorderLayout.EAST);
        add(panelTop, BorderLayout.NORTH);
        add(panelBottom, BorderLayout.SOUTH);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Object src = e.getSource();
        if (src == btnLogin) {
            connect();
        } else if (e.getActionCommand().equals("CancelSelection")) {
            System.out.println("Cancel button pressed");
        } else if (src == btnDisconnect) {
                Diconnect();
        } else if (e.getActionCommand().equals("ApproveSelection")) {
            if (e.getSource().equals(directionServer)) {
                System.out.println("directinServer event");
                sendFile(directionServer.getSelectedFile().getName());
            }
            if (e.getSource().equals(directionClient)) {
                System.out.println("directinClient event");
                resiveFile(directionClient.getSelectedFile().getName());
            }

        } else {
            throw new RuntimeException("Unknown source:" + src);
        }
    }

    private void Diconnect() {
        try {
            is.close();
            os.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    private void connect() {

        try {
            socket = new Socket(tfIPAddress.getText(), Integer.parseInt(tfPort.getText()));
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread.sleep(1000);

            directionClient.setCurrentDirectory(new File (serverPath));
            directionClient.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFile(String fileName) {
        putLog(fileName);
        File currentFile = new File(clientPath+fileName);

        if (currentFile != null) {
            try {
                os.writeUTF("./upload");
                os.writeUTF(fileName);
                os.writeLong(currentFile.length());
                System.out.println("begin send");
                FileInputStream fis = new FileInputStream(currentFile);
                byte [] buffer = new byte[1024];
                while (fis.available() > 0) {
                    int bytesRead = fis.read(buffer);
                    os.write(buffer, 0, bytesRead);
                }
                System.out.println("end send");
                os.flush();
                fis.close();
                directionClient.rescanCurrentDirectory();
                directionServer.rescanCurrentDirectory();

                putLog(is.readUTF());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void resiveFile(String fileName) {
        putLog(fileName);

            try {
                os.writeUTF("./download");
                os.writeUTF(fileName);
//                long fileLength = is.readLong();
                File file = new File(clientPath + fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                System.out.println("begin resive " + fileName + " length " + file.length());

//                try(FileOutputStream fos = new FileOutputStream(file)) {
//                   for (long i = 0; i < (fileLength / 1024 == 0 ? 1 : fileLength / 1024); i++) {
//                        int bytesRead = is.read(buffer);
//                        fos.write(buffer, 0, bytesRead);
//                    }
//                }
                System.out.println("end resive " + fileName);

                directionClient.rescanCurrentDirectory();
                directionServer.rescanCurrentDirectory();

                putLog("File transmitted");
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private void putLog(String msg) {
        if ("".equals(msg)) return;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + System.lineSeparator());
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    private void showException(Thread t, Throwable e) {
        String msg;
        StackTraceElement[] ste = e.getStackTrace();
        if (ste.length == 0)
            msg = "Empty Stacktrace";
        else {
            msg = String.format("Exception in \"%s\" %s: %s\n\tat %s",
                    t.getName(), e.getClass().getCanonicalName(), e.getMessage(), ste[0]);
            JOptionPane.showMessageDialog(this, msg, "Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        showException(t, e);
        System.exit(1);
    }
}
