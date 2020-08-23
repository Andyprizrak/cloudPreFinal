import java.io.*;
import java.net.Socket;

public class ConnectionHandler implements Runnable {

    private DataInputStream is;
    private DataOutputStream os;

    public ConnectionHandler(Socket socket) throws IOException, InterruptedException {
        System.out.println("Connection accepted");
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        Thread.sleep(2000);
    }

    @Override
    public void run() {
        byte [] buffer = new byte[1024];
        while (true) {
            try {
                String command = is.readUTF();
                System.out.println(command);
                if (command.equals("./upload")) {
                    String fileName = is.readUTF();
                    System.out.println("fileName: " + fileName);
                    long fileLength = is.readLong();
                    System.out.println("fileLength: " + fileLength);
                    File file = new File(ServerS.serverPath + "/" + fileName);
                    System.out.println(ServerS.serverPath+'/'+fileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    try(FileOutputStream fos = new FileOutputStream(file)) {
                        for (long i = 0; i < (fileLength / 1024 == 0 ? 1 : fileLength / 1024); i++) {
                            int bytesRead = is.read(buffer);
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                    os.writeUTF("File transmit successful");
                }

                if (command.equals("./download")) {
                    System.out.println(command);
                    String fileName = is.readUTF();
                    File file = new File(ServerS.serverPath + "/" + fileName);
                    os.writeLong(file.length());


//                    FileInputStream fis = new FileInputStream(currentFile);
//                    while (fis.available() > 0) {
//                        int bytesRead = fis.read(buffer);
//                        os.write(buffer, 0, bytesRead);
//                    }
//                    System.out.println("end send");
//                    os.flush();
//                    fis.close();
               }
             } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
