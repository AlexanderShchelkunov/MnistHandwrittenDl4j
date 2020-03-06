package com.sandro.mnisthandwrittendl4j.ui;

import com.sandro.mnisthandwrittendl4j.Constants;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveActionListener implements ActionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(UiLauncher.class);

    private final JFrame mainFrame;
    private final BufferedImage canvasImage;

    public SaveActionListener(JFrame mainFrame, BufferedImage canvasImage) {
        this.mainFrame = mainFrame;
        this.canvasImage = canvasImage;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Dialog d = new Dialog(mainFrame, "Add image to training set", true);
        d.setLayout(new FlowLayout());
        Button okButton = new Button("OK");
        TextField input = new TextField();
        okButton.addActionListener((ActionEvent ae) -> {
            int inputNum;
            try {
                inputNum = Integer.parseInt(input.getText());
                if (inputNum < 0 || inputNum > 9) {
                    JOptionPane.showMessageDialog(d, "Please enter number from 0 to 9.");
                    return;
                }
            } catch (Exception ex) {
                LOGGER.error("Error parsing number:", ex);
                JOptionPane.showMessageDialog(d, "Please enter number from 0 to 9.");
                return;
            }

            try {
                String fileName = "images/" + findFreeFileName() + inputNum + "." + Constants.IMAGES_EXTENSION;
                File f = new File(fileName);
                BufferedImage outputImage = new BufferedImage(Constants.IMAGE_WIDTH, Constants.IMAGE_HEIGTH, BufferedImage.TYPE_INT_BGR);
                Graphics2D g2d = outputImage.createGraphics();
                g2d.drawImage(canvasImage, 0, 0, Constants.IMAGE_WIDTH, Constants.IMAGE_HEIGTH, null);
                g2d.dispose();
                boolean write = ImageIO.write(outputImage, "jpg", f);
                if (!write) {
                    JOptionPane.showMessageDialog(d, "Cannot save image =(");
                } else {
                    d.dispose();
                }
            } catch (Exception ex) {
                LOGGER.error("Saving error:", ex);
            }
        });
        d.add(new Label("What was that?"));
        d.add(input);
        d.add(okButton);

        Button cancelButton = new Button("Cancel");
        cancelButton.addActionListener((ActionEvent ae) -> d.dispose());
        d.add(cancelButton);

        d.setSize(300, 300);
        d.setLocationRelativeTo(mainFrame);
        d.setVisible(true);
    }

    private String findFreeFileName() {
        File imageDir = new File(Constants.IMAGES_PATH);
        String separator = "-";
        int maxFileName = Arrays.stream(imageDir.listFiles())
                .filter(file -> file.getName().endsWith(Constants.IMAGES_EXTENSION) && file.getName().split(separator, 2).length == 2)
                .map(file -> Integer.parseInt(file.getName().split(separator, 2)[0])).max(Comparator.naturalOrder()).orElse(0);
        return ++maxFileName + separator;
    }
}
