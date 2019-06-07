/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sandro.mnisthandwrittendl4j.ui;

import com.sandro.mnisthandwrittendl4j.Constants;
import com.sandro.mnisthandwrittendl4j.model.ImageModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Stroke;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author Aleksandr_Thchelkuno
 */
public class UiLauncher {

    /**
     * Image used to make changes.
     */
    private BufferedImage canvasImage;
    /**
     * The main GUI that might be added to a frame or applet.
     */
    private JPanel gui;
    /**
     * The color to use when calling clear, text or other drawing functionality.
     */
    private Color color = Color.BLACK;
    /**
     * General user messages.
     */
    private final JLabel output = new JLabel("Welcome, human meat!");
    private final JLabel nnOutput = new JLabel("Welcome, human meat!");

    private final BufferedImage colorSample = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
    private JLabel imageLabel;

    private boolean dirty = false;
    private Stroke stroke = new BasicStroke(
            8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.7f);
    private RenderingHints renderingHints;
    private MultiLayerNetwork net;
    private JFrame mainFrame;

    private JComponent getGui() {
        if (gui == null) {
            Map<Key, Object> hintsMap = new HashMap<>();
            hintsMap.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            hintsMap.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            renderingHints = new RenderingHints(hintsMap);

            setImage(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB));
            gui = new JPanel(new BorderLayout(4, 4));
            gui.setBorder(new EmptyBorder(5, 3, 5, 3));

            JPanel imageView = new JPanel(new GridBagLayout());
            imageView.setPreferredSize(new Dimension(480, 320));
            imageLabel = new JLabel(new ImageIcon(canvasImage));
            JScrollPane imageScroll = new JScrollPane(imageView);
            imageView.add(imageLabel);
            imageLabel.addMouseMotionListener(new ImageMouseMotionListener());
            imageLabel.addMouseListener(new ImageMouseListener());
            gui.add(imageScroll, BorderLayout.CENTER);

            JToolBar tb = new JToolBar();
            tb.setFloatable(false);
            setColor(color);

            final SpinnerNumberModel strokeModel
                    = new SpinnerNumberModel(8, 1, 16, 1);
            JSpinner strokeSize = new JSpinner(strokeModel);
            ChangeListener strokeListener = (ChangeEvent arg0) -> {
                Object o = strokeModel.getValue();
                Integer i = (Integer) o;
                stroke = new BasicStroke(
                        i,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND,
                        1.7f);
            };
            strokeSize.addChangeListener(strokeListener);
            strokeSize.setMaximumSize(strokeSize.getPreferredSize());
            JLabel strokeLabel = new JLabel("Stroke");
            strokeLabel.setLabelFor(strokeSize);
            strokeLabel.setDisplayedMnemonic('t');
            tb.add(strokeLabel);
            tb.add(strokeSize);

            tb.addSeparator();

            ActionListener clearListener = (ActionEvent arg0) -> {
                int result = JOptionPane.OK_OPTION;
                if (dirty) {
                    result = JOptionPane.showConfirmDialog(
                            gui, "Erase the current painting?");
                }
                if (result == JOptionPane.OK_OPTION) {
                    clear(canvasImage);
                }
            };
            JButton clearButton = new JButton("Clear");
            tb.add(clearButton);
            clearButton.addActionListener(clearListener);

            createRecognizeAction(tb);
            createTrainAction(tb);

            gui.add(tb, BorderLayout.PAGE_START);

            JToolBar tools = new JToolBar(JToolBar.VERTICAL);
            tools.setFloatable(false);
            gui.add(tools, BorderLayout.LINE_END);

            gui.add(output, BorderLayout.PAGE_END);
            gui.add(nnOutput, BorderLayout.PAGE_END);
            clear(colorSample);
            clear(canvasImage);
        }

        return gui;
    }

    private void loadNet() throws RuntimeException {
        if (net == null) {
            File modelFile = new File("./lenetmnist.zip");
            if (modelFile.exists()) {
                try {
                    net = ModelSerializer.restoreMultiLayerNetwork(modelFile);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                throw new RuntimeException("NN is not trained.");
            }
        }
    }

    /**
     * Clears the entire image area by painting it with the current color.
     *
     * @param bi
     */
    private void clear(BufferedImage bi) {
        Graphics2D g = bi.createGraphics();
        g.setRenderingHints(renderingHints);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, bi.getWidth(), bi.getHeight());

        g.dispose();
        imageLabel.repaint();
    }

    private void setImage(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        canvasImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = this.canvasImage.createGraphics();
        g.setRenderingHints(renderingHints);
        g.setColor(Color.WHITE);
        g.drawImage(image, 0, 0, gui);
        g.dispose();

        if (this.imageLabel != null) {
            imageLabel.setIcon(new ImageIcon(canvasImage));
            this.imageLabel.repaint();
        }
        if (gui != null) {
            gui.invalidate();
        }
    }

    /**
     * Set the current painting color and refresh any elements needed.
     *
     * @param color
     */
    private void setColor(Color color) {
        this.color = color;
        clear(colorSample);
    }

    private JMenu getFileMenu(boolean webstart) {
        JMenu file = new JMenu("File");
        file.setMnemonic('f');

        JMenuItem newImageItem = new JMenuItem("New");
        newImageItem.setMnemonic('n');
        ActionListener newImage = (ActionEvent arg0) -> {
            BufferedImage bi = new BufferedImage(
                    360, 300, BufferedImage.TYPE_INT_ARGB);
            clear(bi);
            setImage(bi);
        };
        newImageItem.addActionListener(newImage);
        file.add(newImageItem);

        if (webstart) {
            //TODO Add open/save functionality using JNLP API
        } else {
            //TODO Add save functionality using J2SE API
            file.addSeparator();
            ActionListener openListener = (ActionEvent arg0) -> {
                if (!dirty) {
                    JFileChooser ch = getFileChooser();
                    int result = ch.showOpenDialog(gui);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        try {
                            BufferedImage bi = ImageIO.read(ch.getSelectedFile());
                            setImage(bi);
                        } catch (IOException e) {
                            showError(e);
                            e.printStackTrace();
                        }
                    }
                } else {
                    // TODO
                    JOptionPane.showMessageDialog(
                            gui, "TODO - prompt save image..");
                }
            };
            JMenuItem openItem = new JMenuItem("Open");
            openItem.setMnemonic('o');
            openItem.addActionListener(openListener);
            file.add(openItem);

            ActionListener saveListener = (ActionEvent e) -> {
                JFileChooser ch = getFileChooser();
                int result = ch.showSaveDialog(gui);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        File f = ch.getSelectedFile();
                        ImageIO.write(UiLauncher.this.canvasImage, "png", f);
                        dirty = false;
                    } catch (IOException ioe) {
                        showError(ioe);
                        ioe.printStackTrace();
                    }
                }
            };
            JMenuItem saveItem = new JMenuItem("Save");
            saveItem.addActionListener(saveListener);
            saveItem.setMnemonic('s');
            file.add(saveItem);
        }

        if (canExit()) {
            ActionListener exit = (ActionEvent arg0) -> {
                // TODO Auto-generated method stub
                System.exit(0);
            };
            JMenuItem exitItem = new JMenuItem("Exit");
            exitItem.setMnemonic('x');
            file.addSeparator();
            exitItem.addActionListener(exit);
            file.add(exitItem);
        }

        return file;
    }

    private void showError(Throwable t) {
        JOptionPane.showMessageDialog(
                gui,
                t.getMessage(),
                t.toString(),
                JOptionPane.ERROR_MESSAGE);
    }

    JFileChooser chooser = null;

    private JFileChooser getFileChooser() {
        if (chooser == null) {
            chooser = new JFileChooser();
            FileFilter ff = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
            chooser.setFileFilter(ff);
        }
        return chooser;

    }

    private boolean canExit() {
        boolean canExit = false;
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            canExit = true;
        } else {
            try {
                sm.checkExit(0);
                canExit = true;
            } catch (Exception stayFalse) {
            }
        }

        return canExit;
    }

    private JMenuBar getMenuBar(boolean webstart) {
        JMenuBar mb = new JMenuBar();
        mb.add(this.getFileMenu(webstart));
        return mb;
    }

    public static void main(String[] args) {
        Runnable r = () -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // use default
            }
            UiLauncher bp = new UiLauncher();

            JFrame f = new JFrame("DooDoodle!");
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setLocationByPlatform(true);

            f.setContentPane(bp.getGui());
            f.setJMenuBar(bp.getMenuBar(false));

            f.pack();
            f.setMinimumSize(f.getSize());
            f.setVisible(true);
        };
        SwingUtilities.invokeLater(r);
    }

    private void draw(Point point) {
        Graphics2D g = this.canvasImage.createGraphics();
        g.setRenderingHints(renderingHints);
        g.setColor(this.color);
        g.setStroke(stroke);
        int n = 0;
        g.drawLine(point.x, point.y, point.x + n, point.y + n);
        g.dispose();
        this.imageLabel.repaint();
    }

    class ImageMouseListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent arg0) {
            draw(arg0.getPoint());
        }
    }

    class ImageMouseMotionListener implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent arg0) {
            reportPositionAndColor(arg0);
            draw(arg0.getPoint());
        }

        @Override
        public void mouseMoved(MouseEvent arg0) {
            reportPositionAndColor(arg0);
        }

    }

    private void reportPositionAndColor(MouseEvent me) {
        String text = "X,Y: " + (me.getPoint().x + 1) + "," + (me.getPoint().y + 1);
        output.setText(text);
    }

    private void createTrainAction(JToolBar tb) {
        ActionListener actionListener = (ActionEvent arg0) -> {
            loadNet();
            Dialog d = new Dialog(mainFrame, "Dialog Example", true);
            d.setLayout(new FlowLayout());
            Button okButton = new Button("OK");
            TextField input = new TextField();
            okButton.addActionListener((ActionEvent e) -> {
                try {
                    int digit = Integer.parseInt(input.getText());
                    float[] normalizedPixels = getNetworkInput();
                    loadNet();
                    net.fit(Nd4j.create(normalizedPixels), createExpectedOutput(digit));
                    d.setVisible(false);
                } catch (IOException ex) {
                    Logger.getLogger(UiLauncher.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            d.add(new Label("What was that?"));
            d.add(input);
            d.add(okButton);
            d.setSize(300, 300);
            d.setLocationRelativeTo(mainFrame);
            d.setVisible(true);
        };

        JButton recognizeButton = new JButton("Train");
        tb.add(recognizeButton);
        recognizeButton.addActionListener(actionListener);
    }

    private INDArray createExpectedOutput(int digit) {
        INDArray expectedOutput = Nd4j.zeros(1, Constants.AMOUNT_OF_DIGITS);
        expectedOutput.putScalar(new int[]{0, digit}, Constants.NUMERIC_TRUE);
        return expectedOutput;
    }

    private void createRecognizeAction(JToolBar tb) {
        ActionListener recognizeListener = (ActionEvent arg0) -> {
            try {
                float[] normalizedPixels = getNetworkInput();

//                System.out.println("normalizedPixels.");
//                System.out.println(Arrays.toString(normalizedPixels));
                loadNet();
                INDArray result = net.output(Nd4j.create(normalizedPixels));
                float[] floatVector = result.toFloatVector();
//                System.out.println(Arrays.toString(floatVector));
                int maxIndex = 0;
                float maxProbablity = 0;
                for (int i = 0; i < floatVector.length; i++) {
                    System.out.printf("It is " + i + ": %.2f%n", floatVector[i]);
                    if (floatVector[i] > maxProbablity) {
                        maxProbablity = floatVector[i];
                        maxIndex = i;
                    }
                }
//                double maxProbablity = IntStream.range(0, floatVector.length).mapToDouble(i -> floatVector[i]).max().getAsDouble();                
                nnOutput.setText(String.format("This is %s (%.2f%% probability)", maxIndex, maxProbablity * 100));
//                float[] arr = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5058824f, 0.99607843f, 0.99607843f, 1.0f, 0.91764706f, 0.1764706f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.37254903f, 0.87058824f, 0.99215686f, 0.99215686f, 0.99215686f, 0.99215686f, 0.35686275f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.7921569f, 0.99215686f, 0.99215686f, 0.9019608f, 0.46666667f, 0.3137255f, 0.019607844f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.49803922f, 0.98039216f, 0.99215686f, 0.8980392f, 0.2f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.3019608f, 0.9764706f, 0.99215686f, 0.89411765f, 0.20784314f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.78039217f, 0.99215686f, 0.99215686f, 0.7372549f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.30588236f, 0.972549f, 0.99215686f, 0.99215686f, 0.5647059f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5137255f, 0.99215686f, 0.99215686f, 0.90588236f, 0.15686275f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.87058824f, 0.99215686f, 0.99215686f, 0.30588236f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.87058824f, 0.99215686f, 0.99215686f, 0.2f, 0.0f, 0.0f, 0.05490196f, 0.6117647f, 0.6862745f, 0.09411765f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.87058824f, 0.99215686f, 0.99215686f, 0.2f, 0.3137255f, 0.7411765f, 0.7921569f, 0.99215686f, 0.99215686f, 0.4745098f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.2784314f, 0.95686275f, 0.99215686f, 0.99215686f, 0.25882354f, 0.8156863f, 0.99215686f, 0.7372549f, 0.2784314f, 0.75686276f, 0.95686275f, 0.27450982f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.31764707f, 0.972549f, 0.99215686f, 0.99215686f, 0.48235294f, 0.99215686f, 0.9529412f, 0.22745098f, 0.0f, 0.3137255f, 0.9254902f, 0.18431373f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.87058824f, 0.99215686f, 0.7882353f, 0.30588236f, 0.95686275f, 0.68235296f, 0.0f, 0.0f, 0.15686275f, 0.99215686f, 0.38039216f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.87058824f, 0.99215686f, 0.6862745f, 0.0f, 0.5372549f, 0.09019608f, 0.0f, 0.0f, 0.22352941f, 0.99215686f, 0.38039216f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.87058824f, 0.99215686f, 0.8745098f, 0.1254902f, 0.0f, 0.0f, 0.0f, 0.11372549f, 0.8666667f, 0.98039216f, 0.34117648f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.87058824f, 0.99215686f, 0.99215686f, 0.5254902f, 0.078431375f, 0.019607844f, 0.019607844f, 0.24313726f, 0.99215686f, 0.8666667f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.56078434f, 0.9843137f, 0.99215686f, 0.99215686f, 0.99215686f, 0.67058825f, 0.6509804f, 0.99215686f, 0.99215686f, 0.57254905f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.5568628f, 0.7019608f, 0.9764706f, 0.99215686f, 0.99215686f, 0.99215686f, 0.99215686f, 0.8980392f, 0.16470589f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.13725491f, 0.6117647f, 0.99215686f, 0.99215686f, 0.63529414f, 0.16078432f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
//                INDArray result1 = net.output(Nd4j.create(arr));
//                System.out.println(Arrays.toString(result1.toFloatVector()));
            } catch (IOException ex) {
                Logger.getLogger(UiLauncher.class.getName()).log(Level.SEVERE, null, ex);
            }
        };

        JButton recognizeButton = new JButton("Recognize");
        tb.add(recognizeButton);
        recognizeButton.addActionListener(recognizeListener);
    }

    private float[] getNetworkInput() throws IOException {
        BufferedImage outputImage = new BufferedImage(Constants.IMAGE_WIDTH, Constants.IMAGE_HEIGTH, BufferedImage.TYPE_USHORT_GRAY);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(canvasImage, 0, 0, Constants.IMAGE_WIDTH, Constants.IMAGE_HEIGTH, null);
        g2d.dispose();
        File outputfile = new File("image.jpg");
        ImageIO.write(outputImage, "jpg", outputfile);
        short[] pixels = ((DataBufferUShort) outputImage.getRaster().getDataBuffer()).getData();
        //                System.out.println("Pixels before.");
//                System.out.println(Arrays.toString(pixels));
        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] == -1) {
                pixels[i] = 0;
            } else if (pixels[i] == 0) {
                pixels[i] = 255;
            }
        }
//                System.out.println("Pixels after.");
//                System.out.println(Arrays.toString(pixels));
        float[] normalizedPixels = new float[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            normalizedPixels[i] = (float) pixels[i] / Constants.MAX_COLOR_CODE;
        }
        return normalizedPixels;
    }
}