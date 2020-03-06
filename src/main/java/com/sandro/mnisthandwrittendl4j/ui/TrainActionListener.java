package com.sandro.mnisthandwrittendl4j.ui;

import com.sandro.mnisthandwrittendl4j.LearinigLauncher;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrainActionListener implements ActionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainActionListener.class);

    private final UiLauncher uiLauncher;
    private final JFrame mainFrame;

    public TrainActionListener(UiLauncher uiLauncher, JFrame mainFrame) {
        this.uiLauncher = uiLauncher;
        this.mainFrame = mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Dialog dialog = new Dialog(mainFrame, "Traing in propgress, please wait", true);
            dialog.setLayout(new FlowLayout());
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            dialog.add(progressBar);
            dialog.setSize(300, 300);
            dialog.setLocationRelativeTo(mainFrame);
            dialog.add(new Label("Training might take 5+ minutes. Please wait..."));

            SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>() {
                @Override
                public String doInBackground() throws Exception {
                    LearinigLauncher.main(null);
                    uiLauncher.reloadNet();
                    return null;
                }

                @Override
                public void done() {
                    SwingUtilities.invokeLater(() -> {
                        dialog.setVisible(false);
                        dialog.dispose();
                    });
                }
            };

            worker.execute();
            SwingUtilities.invokeLater(() -> {
                dialog.setVisible(true);
            });

        } catch (Exception ex) {
            LOGGER.error("Train error:", ex);
        }
    }
}
