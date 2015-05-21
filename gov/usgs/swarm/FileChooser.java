package gov.usgs.swarm;

import javax.swing.JFileChooser;

public final class FileChooser {
    private static final FileChooser INSTANCE = new FileChooser();
    private static JFileChooser fileChooser;
    
    private FileChooser() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                fileChooser = new JFileChooser();
            }
        });
        
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }
    
    public static JFileChooser getFileChooser() {
        int timeout = 10000;
        while (fileChooser == null && timeout > 0) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
            timeout -= 100;
        }
        return fileChooser;
    }
}